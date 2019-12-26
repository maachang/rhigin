package rhigin.lib.jdbc.pooling;

import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.sql.DataSource;

import rhigin.http.HttpConstants;
import rhigin.lib.jdbc.runner.JDBCDriverManager;
import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.lib.jdbc.runner.JDBCKind;
import rhigin.util.Flag;

/**
 * １データベースのAtomicなプーリング管理.
 */
public class AtomicPooling implements DataSource {

	/**
	 * プーリング最大管理数. 最大数は、スレッド数＊４.
	 */
	private static final int MAX_POOL = java.lang.Runtime.getRuntime().availableProcessors()
			* HttpConstants.WORKER_CPU_COEFFICIENT;

	/**
	 * デフォルトプーリング数. スレッド数.
	 */
	public static final int DEF_POOL = java.lang.Runtime.getRuntime().availableProcessors();

	/** 最大タイムアウト値(ミリ秒). **/
	/** 30分. **/
	private static final long MAX_TIMEOUT = 1800000L;

	/** デフォルトタイムアウト値(ミリ秒). **/
	/** 1分. **/
	public static final long DEF_TIMEOUT = 60000L;

	/** プーリングデータ格納用. **/
	protected final Queue<SoftReference<AtomicPoolConnection>> pooling = new ConcurrentLinkedQueue<SoftReference<AtomicPoolConnection>>();

	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();

	/** kind. **/
	private JDBCKind kind;

	/** 最大プーリング数. **/
	private int maxPool;

	/** タイムアウト値. **/
	protected long timeout;
	
	/** プーリング管理. **/
	protected AtomicPoolingMonitor monitor = null;

	/**
	 * コンストラクタ.
	 */
	protected AtomicPooling() {
	}
	
	/**
	 * コンストラクタ.
	 * 
	 * @param kind    対象のDbKindを設定します.
	 * @param mon     プーリング管理オブジェクトを設定します.
	 */
	public AtomicPooling(JDBCKind kind, AtomicPoolingMonitor mon) {
		this(kind, mon,
			kind.getPoolingSize() == null ? -1 : kind.getPoolingSize(),
			kind.getPoolingTimeout() == null ? -1 : kind.getPoolingTimeout());
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param kind    対象のDbKindを設定します.
	 * @param mon     プーリング管理オブジェクトを設定します.
	 * @param maxPool プーリング最大管理数を設定します.
	 * @param timeout コネクションタイムアウト値を設定します.
	 */
	public AtomicPooling(JDBCKind kind, AtomicPoolingMonitor mon, int maxPool, long timeout) {
		if (maxPool <= 0) {
			maxPool = MAX_POOL;
		} else if (maxPool <= DEF_POOL) {
			maxPool = DEF_POOL;
		}
		if (timeout > MAX_TIMEOUT) {
			timeout = MAX_TIMEOUT;
		} else if (timeout <= 0) {
			timeout = DEF_TIMEOUT;
		}

		this.kind = kind;
		this.maxPool = maxPool;
		this.timeout = timeout;

		this.destroyFlag.set(false);
		
		this.monitor = mon;

		// プーリング監視オブジェクトに登録.
		monitor.setPooling(this);
	}

	/**
	 * オブジェクト破棄.
	 */
	public void destroy() {
		if (!destroyFlag.setToGetBefore(true)) {

			// プーリング監視オブジェクトに登録されている条件を破棄.
			monitor.clearPooling(this);

			// 保持しているコネクションを全て破棄.
			if (pooling.size() > 0) {
				SoftReference<AtomicPoolConnection> n;
				Iterator<SoftReference<AtomicPoolConnection>> it = pooling.iterator();
				while (it.hasNext()) {
					try {
						n = it.next();
						if (n.get() != null) {
							n.get().destroy();
						}
					} catch (Exception e) {
					}
				}
				pooling.clear();
			}

		}
	}

	/**
	 * オブジェクトが既に破棄されているかチェック.
	 * 
	 * @return boolean [true]の場合、既に破棄されています.
	 */
	public boolean isDestroy() {
		return destroyFlag.get();
	}

	/** チェック処理. **/
	private void check() {
		if (isDestroy()) {
			throw new JDBCException("オブジェクトは既に破棄されています");
		}
	}

	/** プーリングコネクションクラス. **/
	@SuppressWarnings("rawtypes")
	private static final Class[] POOL_CONN_CLASS = new Class[] { AtomicPoolConnection.class };

	/** ConnectionProxyクラス. **/
	private static class AtomicPoolConnectionImpl implements InvocationHandler {
		private final boolean notPool;
		private final Flag poolCloseFlag = new Flag(); // 論理Open.
		private final Queue<SoftReference<AtomicPoolConnection>> resource;
		private final Flag destroyFlag;
		private final int max;
		private final Connection src;
		private final JDBCKind kind;
		private long lastTime;

		/** コンストラクタ. **/
		private AtomicPoolConnectionImpl(final boolean np, final JDBCKind k, final Connection conn, final int mx,
				final Queue<SoftReference<AtomicPoolConnection>> pl, final Flag df) {
			notPool = np;
			resource = pl;
			destroyFlag = df;
			max = mx;
			kind = k;
			src = conn;
			lastTime = -1L;
			poolCloseFlag.set(false);
			
			// AutoCommitは基本OFF.
			try {
				src.setAutoCommit(false);
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		/**
		 * メソッド呼び出し.
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				String name = method.getName();
				if ("destroy".equals(name)) {
					poolCloseFlag.set(true); // 論理close.
					try {
						src.close();
					} catch (Throwable e) {
					}
					return null;
				} else if ("recreate".equals(name)) {
					if (notPool) {
						// プーリングしない場合.
						throw new SQLException("not pooling Connection");
					} else if (src.isClosed()) {
						throw new SQLException("Connection is closed");
					}
					poolCloseFlag.set(false); // 論理open.
					return this;
				} else if ("lastTime".equals(name)) {
					return lastTime;
				} else if ("close".equals(name)) {
					poolCloseFlag.set(true); // 論理close.
					if (notPool) {
						// プーリングしない場合.
						if (!src.getAutoCommit()) {
							try {
								src.rollback();
							} catch (Throwable e) {
							}
						}
						try {
							src.close();
						} catch (Throwable e) {
						}
						return null;
					} else if (src.isClosed()) {
						return null;
					} else if (!src.getAutoCommit()) {
						src.rollback();
					}
					if (destroyFlag.get() || max < resource.size()) {
						// プーリングしない場合は削除.
						// 最大コネクション管理数を越える場合は削除.
						// オブジェクトが破棄されている場合も同様.
						try {
							src.close();
						} catch (Throwable e) {
						}
					} else {
						// 基本条件を再設定.
						src.setAutoCommit(false);
						// プーリング可能な場合は、セット.
						lastTime = System.currentTimeMillis();
						resource.offer(new SoftReference<AtomicPoolConnection>((AtomicPoolConnection) proxy));
					}
					return null;
				} else if ("isClosed".equals(name)) {
					if (notPool) {
						return src.isClosed();
					} else if (!poolCloseFlag.get()) { // 論理open状態の場合.
						// 実際のConnection状態を反映.
						poolCloseFlag.set(src.isClosed());
					}
					return poolCloseFlag.get();
				} else if ("getKind".equals(name)) {
					// getKind.
					return kind;
				}
				
				// クローズの場合.
				if ((notPool && src.isClosed()) || poolCloseFlag.get()) {
					throw new SQLException("Connection is closed");
				}

				// statement系の処理.
				if ("prepareStatement".equals(name) || "prepareCall".equals(name) || "createStatement".equals(name)) {
					// statement系.
					Statement ret = (Statement) method.invoke(src, args);
					// busyTimeout と fetchSizeを設定.
					kind.setBusyTimeout(ret);
					kind.setFetchSize(ret);
					return ret;
				}
				// 通常コネクション命令はリフレクション呼び出し.
				return method.invoke(src, args);
			} catch (JDBCException je) {
				throw je;
			} catch (SQLException e) {
				throw e;
			} catch (InvocationTargetException ex) {
				throw ex.getCause();
			}
		}
	}

	/** 対象コネクションオブジェクトを取得. **/
	private static final AtomicPoolConnection createPoolConnection(final boolean np, final JDBCKind k,
			final Connection conn, final int mx, final Queue<SoftReference<AtomicPoolConnection>> pl, final Flag df)
			throws SQLException {

		// proxy返却.
		return (AtomicPoolConnection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				POOL_CONN_CLASS, new AtomicPoolConnectionImpl(np, k, conn, mx, pl, df));
	}

	/**
	 * コネクションオブジェクトを取得.
	 * 
	 * @return Connection コネクションオブジェクトが返却されます.
	 * @exception Exception 例外.
	 */
	@Override
	public Connection getConnection() {
		check();
		try {

			// プーリング領域からコネクションオブジェクトを取得.
			SoftReference<AtomicPoolConnection> conn;
			Connection ret = null;

			// Pooling先から取得.
			while ((conn = pooling.poll()) != null) {
				if ((ret = conn.get()) != null) {
					return ((AtomicPoolConnection) ret).recreate();
				}
			}

			// 存在しない場合は、新規コネクションを生成.
			try {
				ret = JDBCDriverManager.readWrite(kind);
			} catch (Exception e) {
				// エラーの場合は、ドライバー登録して、再取得.
				JDBCDriverManager.regDriver(kind.getDriver());
				ret = JDBCDriverManager.readWrite(kind);
			}

			// プーリングコネクションオブジェクトに変換.
			return AtomicPooling.createPoolConnection(false, kind, ret, maxPool, pooling, destroyFlag);
		} catch (JDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new JDBCException(e);
		}
	}

	/**
	 * 現在のプーリングコネクション数を取得.
	 * 
	 * @return int 現在のプーリングコネクション数が返却されます.
	 */
	public int size() {
		return pooling.size();
	}

	/**
	 * JDBCKindを取得.
	 * 
	 * @return JDBCKind JDBCKindが返却されます.
	 */
	public JDBCKind getKind() {
		return kind;
	}

	/**
	 * 最大プーリング数を取得.
	 * 
	 * @return int 最大プーリング数が返却されます.
	 */
	public int getMaxPool() {
		return maxPool;
	}

	/**
	 * コネクション待機タイムアウト値を取得.
	 * 
	 * @return long コネクション待機タイムアウト値が返却されます.
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 文字変換.
	 * 
	 * @return String 登録されている情報内容が文字で返却されます.
	 */
	public String toString() {
		return new StringBuilder().append("name:").append(kind.getName()).append(" ").append("url:")
				.append(kind.getUrl()).append(" ").append("user:").append(kind.getUser()).append(" ")
				.append("password:").append(kind.getPassword()).append(" ").append("maxPool:").append(maxPool)
				.append(" ").append("timeout:").append(timeout).toString();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> c) throws SQLException {
		return AtomicPooling.class.equals(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> c) throws SQLException {
		if (isWrapperFor(c)) {
			return (T) this;
		}
		throw new SQLException("対象オブジェクトはありません: " + c);
	}

	@Override
	public Connection getConnection(String user, String password) throws SQLException {
		Connection ret;
		// 新規コネクションを生成.
		try {
			ret = JDBCDriverManager.readWrite(kind, kind.getUrl(), user, password);
		} catch (Exception e) {
			// エラーの場合は、ドライバー登録して、再取得.
			try {
				JDBCDriverManager.regDriver(kind.getDriver());
			} catch (Exception ee) {
				throw new SQLException(ee);
			}
			ret = JDBCDriverManager.readWrite(kind, kind.getUrl(), user, password);
		}
		return AtomicPooling.createPoolConnection(true, kind, ret, maxPool, pooling, destroyFlag);
	}
	
	/**
	 * プーリングなしのコネクション生成.
	 * 
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public static final Connection getConnetion(String driver, String url, String user, String password)
		throws SQLException {
		Connection ret;
		JDBCKind kind = JDBCKind.create(driver, url);
		// 新規コネクションを生成.
		try {
			ret = JDBCDriverManager.readWrite(kind, kind.getUrl(), user, password);
		} catch (Exception e) {
			// エラーの場合は、ドライバー登録して、再取得.
			try {
				JDBCDriverManager.regDriver(kind.getDriver());
			} catch (Exception ee) {
				throw new SQLException(ee);
			}
			ret = JDBCDriverManager.readWrite(kind, kind.getUrl(), user, password);
		}
		return AtomicPooling.createPoolConnection(true, kind, ret, -1, null, new Flag(false));

	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter p) throws SQLException {
		DriverManager.setLogWriter(p);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	@Override
	public void setLoginTimeout(int timeout) throws SQLException {
		DriverManager.setLoginTimeout(timeout);
	}
}

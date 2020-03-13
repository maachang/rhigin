package rhigin.lib.jdbc;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import rhigin.RhiginConfig;
import rhigin.RhiginStartup;
import rhigin.lib.jdbc.pooling.AtomicPooling;
import rhigin.lib.jdbc.pooling.AtomicPoolingManager;
import rhigin.lib.jdbc.pooling.AtomicPoolingMonitor;
import rhigin.lib.jdbc.runner.JDBCCloseable;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCConnect.Time12;
import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.lib.jdbc.runner.JDBCKind;
import rhigin.lib.jdbc.runner.JDBCSystemCloseable;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.util.Flag;
import rhigin.util.Time12SequenceId;

/**
 * JDBCコアオブジェクト.
 */
public class JDBCCore {
	
	/** デフォルトのJDBCコンフィグ名. **/
	public static final String DEF_JDBC_JSON_CONFIG_NAME = "jdbc";
	
	/** TIME12. **/
	public static final String TIME12 = "TIME12";
	
	protected final Flag startup = new Flag(false);
	protected final Flag end = new Flag(false);
	protected final AtomicPoolingManager man = new AtomicPoolingManager();
	protected final AtomicPoolingMonitor mon = new AtomicPoolingMonitor();
	protected final JDBCCloseable closeable = new JDBCCloseable();
	protected final Map<String, Time12SequenceId> sequenceKind = new ConcurrentHashMap<String, Time12SequenceId>();
	
	/**
	 * コンストラクタ.
	 */
	public JDBCCore() {
	}
	
	/**
	 * オブジェクト破棄.
	 */
	public void destroy() {
		try {
			close();
		} catch(Exception e) {}
		if(!end.setToGetBefore(true)) {
			try {
				man.destroy();
			} catch(Exception e) {}
			try {
				mon.stopThread();
			} catch(Exception e) {}
		}
	}
	
	// オブジェクト破棄チェック.
	protected void checkDestroy() {
		if(end.get()) {
			throw new JDBCException("The object has been destroyed.");
		}
	}
	
	// オブジェクト破棄チェック.
	// スタートアップしていない場合もチェック.
	protected void check() {
		if(!startup.get() || end.get()) {
			if(!startup.get()) {
				throw new JDBCException("startup has not been performed.");
			}
			checkDestroy();
		}
	}
	
	/**
	 * オブジェクト破棄チェック.
	 * @return
	 */
	public boolean isDestroy() {
		return end.get();
	}
	
	/**
	 * 初期化処理.
	 * @param configName
	 * @param args
	 * @return
	 */
	public RhiginEndScriptCall[] startup(String configName, String[] args) {
		checkDestroy();
		if(!startup.get()) {
			final RhiginConfig conf = RhiginStartup.getConfig();
			return startup(conf, configName);
		}
		return new RhiginEndScriptCall[] { closeable, new JDBCSystemCloseable(this) };
	}
	
	/**
	 * 初期化処理.
	 * @param conf
	 * @param configName
	 * @return
	 */
	public RhiginEndScriptCall[] startup(RhiginConfig conf, String configName) {
		checkDestroy();
		if(!startup.get()) {
			String jdbcJsonConfigName = DEF_JDBC_JSON_CONFIG_NAME;
			if(configName != null && !configName.isEmpty()) {
				jdbcJsonConfigName = configName;
			}
			startup(conf.get(jdbcJsonConfigName));
		}
		return new RhiginEndScriptCall[] { closeable, new JDBCSystemCloseable(this) };
	}
	
	/**
	 * 初期化処理.
	 * @param conf
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RhiginEndScriptCall[] startup(Map<String, Object> conf) {
		checkDestroy();
		if(conf == null || conf.size() == 0) {
			throw new JDBCException("jdbc connection definition config object is not set");
		}
		if(!startup.get()) {
			String name;
			Iterator<String> itr = conf.keySet().iterator();
			AtomicPooling p;
			while(itr.hasNext()) {
				name = itr.next();
				p = new AtomicPooling(JDBCKind.create(name, (Map)conf.get(name)), mon);
				man.register(p); p = null;
			}
			mon.startThread();
			ExecuteScript.addOriginals(TIME12, new Time12());
			startup.set(true);
		}
		return new RhiginEndScriptCall[] { closeable, new JDBCSystemCloseable(this) };
	}
	
	/**
	 * 初期化処理が行われているかチェック.
	 * @return
	 */
	public boolean isStartup() {
		checkDestroy();
		return startup.get();
	}
	
	/**
	 * 新しいコネクションオブジェクトを取得.
	 * @param name
	 * @return
	 */
	public JDBCConnect getNewConnect(String name) {
		check();
		final AtomicPooling p = man.get(name);
		if(p == null) {
			throw new JDBCException(
				"Connection information with the specified name does not exist:" + name);
		}
		return JDBCConnect.create(closeable, _getTime12SequenceId(name), p.getConnection());
	}
	
	/**
	 * 非プーリングのJDBCコネクションを取得.
	 * @param args [0] drivre, [1] url, [2] user, [3] password を入れます.
	 * @return
	 */
	public JDBCConnect getNoPoolingConnect(Object... args) {
		final String driver = args.length > 0 ? "" + args[0] : null;
		final String url = args.length > 1 ? "" + args[1] : null;
		final String user = args.length > 2 ? "" + args[2] : null;
		final String password = args.length > 3 ? "" + args[3] : null;
		return getNoPoolingConnect(driver, url, user, password);
	}
	
	/**
	 * 非プーリングのJDBCコネクションを取得.
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 */
	public JDBCConnect getNoPoolingConnect(String driver, String url, String user, String password) {
		check();
		try {
			final Connection c = AtomicPooling.getConnetion(driver, url, user, password);
			return JDBCConnect.create(closeable, null, c);
		} catch(JDBCException je) {
			throw je;
		} catch(Exception e) {
			throw new JDBCException(e);
		}
	}
	
	/**
	 * 接続定義を取得.
	 * @param name
	 * @return
	 */
	public JDBCKind getKind(String name) {
		check();
		final JDBCKind k = man.getKind(name);
		if(k == null) {
			throw new JDBCException(
				"Connection information with the specified name does not exist:" + name);
		}
		return k;
	}
	
	/**
	 * 指定名が登録されているかチェック.
	 * @param name
	 * @return
	 */
	public boolean isRegister(String name) {
		check();
		return man.contains(name);
	}
	
	/**
	 * 登録接続条件を取得.
	 * @return
	 */
	public int size() {
		check();
		return man.size();
	}
	
	/**
	 * 接続定義名一覧を取得.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> names() {
		check();
		return new JavaScriptable.ReadArray(man.getNames());
	}
	
	/**
	 * 利用中のコネクションをすべてクローズ.
	 */
	public void close() {
		check();
		closeable.call(null);
	}
	
	/**
	 * コミット処理.
	 */
	public void commit() {
		check();
		JDBCConnect c;
		final Iterator<Entry<String, JDBCConnect>> it =
			closeable.useConnect().entrySet().iterator();
		while(it.hasNext()) {
			c = it.next().getValue();
			if(c != null && !c.isClose()) {
				c.commit();
			}
		}
	}
	
	/**
	 * ロールバック処理.
	 */
	public void rollback() {
		check();
		JDBCConnect c;
		final Iterator<Entry<String, JDBCConnect>> it =
			closeable.useConnect().entrySet().iterator();
		while(it.hasNext()) {
			try {
				c = it.next().getValue();
				if(c != null && !c.isClose()) {
					c.rollback();
				}
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * コネクションオブジェクトを取得.
	 * 一度利用したコネクションオブジェクトは、再利用します.
	 * @param args args[0] だけをセットし jdbc接続名を設定することで、プーリング情報でアクセスします.
	 *             プーリング接続を利用しない場合は [0] drivre, [1] url, [2] user, [3] password を入れます.
	 * @return
	 */
	public JDBCConnect getConnect(Object... args) {
		check();
		if(args.length == 1) {
			String name = "" + args[0];
			final Map<String, JDBCConnect> c = closeable.useConnect();
			JDBCConnect ret = c.get(name);
			if(ret == null || ret.isClose()) {
				ret = getNewConnect(name);
				c.put(name, ret);
			}
			return ret;
		}
		StringBuilder buf = new StringBuilder();
		int len = args.length;
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append("\'").append(args[i]).append("\'");
		}
		String name = buf.toString(); buf = null;
		final Map<String, JDBCConnect> c = closeable.useConnect();
		JDBCConnect ret = c.get(name);
		if(ret == null || ret.isClose()) {
			ret = getNoPoolingConnect(args);
			c.put(name, ret);
		}
		return ret;
	}
	
	/**
	 * 登録順で接続名を取得.
	 * @param no
	 * @return
	 */
	public String getName(int no) {
		check();
		return man.getName(no);
	}
	
	/**
	 * シーケンスID発行オブジェクトを取得.
	 * @param name
	 * @return
	 */
	public Time12SequenceId getTime12SequenceId(String name) {
		check();
		return _getTime12SequenceId(name);
	}
	
	private Time12SequenceId _getTime12SequenceId(String name) {
		Time12SequenceId ret = sequenceKind.get(name);
		if(ret == null) {
			JDBCKind kind = getKind(name);
			ret = new Time12SequenceId(kind.getMachineId());
			sequenceKind.put(name, ret);
		}
		return ret;
	}
	
}
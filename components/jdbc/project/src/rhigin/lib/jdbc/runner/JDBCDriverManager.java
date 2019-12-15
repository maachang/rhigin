package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * JDBCドライバーマネージャ.
 */
public final class JDBCDriverManager {
	private JDBCDriverManager() {
	}

	/** ブロックサイズ. **/
	private static final String BLOCK_SIZE = "512";

	/**
	 * ドライバーマネージャにドライバ登録.
	 * 
	 * @param driver 対象のドライバ名を設定します.
	 * @exception Exception 例外.
	 */
	public static final void regDriver(String driver) throws Exception {
		Class.forName(driver);
	}

	/**
	 * 読み込み専用コネクションの取得.
	 * 
	 * @param kind JDBCKindを設定します.
	 * @return Connection コネクション情報が返却されます.
	 */
	public static final Connection readOnly(JDBCKind kind) {
		return readOnly(kind, kind.getUrl(), kind.getUser(), kind.getPassword());
	}

	/**
	 * 読み込み専用コネクションの取得.
	 * 
	 * @param kind   JDBCKindを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 */
	public static final Connection readOnly(JDBCKind kind, String url, String user, String passwd) {
		try {
			Connection ret;
			Properties p = new java.util.Properties();
			kind.setProperty(p);
			p.put("block size", BLOCK_SIZE);

			if (user == null || user.length() <= 0) {
				p.put("user", "");
				p.put("password", "");
				ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
			} else {
				p.put("user", user);
				p.put("password", passwd);
				ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
			}

			ret.setReadOnly(true);
			ret.setAutoCommit(false);
			kind.setTransactionLevel(ret);

			return ret;
		} catch (Exception e) {
			throw new JDBCException(e);
		}
	}

	/**
	 * 読み書きコネクションの取得.
	 * 
	 * @param kind   DbKindを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 */
	public static final Connection readWrite(JDBCKind kind) {
		return readWrite(kind, kind.getUrl(), kind.getUser(), kind.getPassword());
	}

	/**
	 * 読み書きコネクションの取得.
	 * 
	 * @param kind   DbKindを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 */
	public static final Connection readWrite(JDBCKind kind, String url, String user, String passwd) {
		try {
			Connection ret;
			Properties p = new java.util.Properties();
			kind.setProperty(p);
			p.put("block size", BLOCK_SIZE);

			if (user == null || user.length() <= 0) {
				p.put("user", "");
				p.put("password", "");
				ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
			} else {
				p.put("user", user);
				p.put("password", passwd);
				ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
			}

			ret.setReadOnly(false);
			ret.setAutoCommit(false);
			kind.setTransactionLevel(ret);

			return ret;
		} catch (Exception e) {
			throw new JDBCException(e);
		}
	}

}

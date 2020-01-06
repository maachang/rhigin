package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import rhigin.scripts.JsMap;
import rhigin.scripts.JsonOut;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.OList;

/**
 * JDBC-Kind.
 */
public class JDBCKind {
	private String name = null;
	private String driver = null;
	private String url = null;
	private String user = null;
	private String password = null;
	private boolean readOnly = false;
	private Integer busyTimeout = null;
	private Integer transactionLevel = null;
	private Integer fetchSize = null;
	private ArrayMap params = null;
	
	private boolean urlType = true;
	private String urlParams = "";
	
	private Integer poolingSize = null;
	private Integer poolingTimeout = null;
	
	// oracle の jdbc接続など、末尾に；を付けるとエラーになるものは[true].
	// oracleやderbyなど.
	private boolean notSemicolon = false;
	
	// マシンID.
	private int machineId = 0;
	
	/**
	 * コンストラクタ.
	 */
	protected JDBCKind() {
		
	}
	
	/**
	 * コンフィグ情報を読み込む.
	 * @param conf
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final JDBCKind create(String name, Map<String,Object> conf) {
		JDBCKind ret = new JDBCKind();
		// name定義がされている場合は、こちらを優先する.
		ret.name = Converter.convertString(conf.get("name"));
		if(ret.name == null || ret.name.isEmpty()) {
			ret.name = name;
		}
		ret.driver = Converter.convertString(conf.get("driver"));
		ret.url = Converter.convertString(conf.get("url"));
		ret.user = Converter.convertString(conf.get("user"));
		ret.password = Converter.convertString(conf.get("password"));
		if(ret.password == null || ret.password.isEmpty()) {
			ret.password = Converter.convertString(conf.get("passwd"));
		}
		ret.readOnly = Converter.convertBool(conf.get("readOnly"));
		ret.busyTimeout = Converter.convertInt(conf.get("busyTimeout"));
		if(ret.busyTimeout == null) {
			ret.busyTimeout = Converter.convertInt(conf.get("timeout"));
		}
		ret.transactionLevel = transactionLevel(conf.get("transactionLevel"));
		if(ret.transactionLevel == null) {
			ret.transactionLevel = transactionLevel(conf.get("transaction"));
		}
		ret.fetchSize = Converter.convertInt(conf.get("fetchSize"));
		if(ret.fetchSize == null) {
			ret.fetchSize = Converter.convertInt(conf.get("fetch"));
		}
		ret.poolingSize = Converter.convertInt(conf.get("poolingSize"));
		if(ret.poolingSize == null) {
			ret.poolingSize = Converter.convertInt(conf.get("poolSize"));
		}
		ret.poolingTimeout = Converter.convertInt(conf.get("poolingTimeout"));
		if(ret.poolingTimeout == null) {
			ret.poolingTimeout = Converter.convertInt(conf.get("poolTimeout"));
		}
		if(conf.get("params") instanceof Map) {
			ret.params = new ArrayMap((Map)conf.get("params"));
		}
		ret.urlType = Converter.convertBool(conf.get("urlType"));
		{
			Object o = conf.get("urlParams");
			if(o == null) {
				ret.urlParams = null;
			} else if(o instanceof Map) {
				ret.urlParams = convertUrlParams((Map)o, ret.urlType);
			} else if(o instanceof String) {
				ret.urlParams = (String)o;
				if(ret.urlParams.isEmpty()) {
					ret.urlParams = null;
				}
			}
		}
		ret.notSemicolon = checkNotSemicolon(ret.url);
		if(conf.containsKey("machineId") && Converter.isNumeric(conf.get("machineId"))) {
			ret.machineId = Converter.convertInt(conf.get("machineId"));
			if(ret.machineId >= 511) {
				ret.machineId = 511;
			} else if(ret.machineId <= 0) {
				ret.machineId = 0;
			}
		}
		ret.check();
		return ret;
	}
	
	/**
	 * 直接設定.
	 * @param name
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @param readOnly
	 * @param busyTimeout
	 * @param transactionLevel
	 * @param fetchSize
	 * @param poolingSize
	 * @param poolingTimeout
	 * @param machineId
	 * @param urlType
	 * @param urlParams
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final JDBCKind create(String name, String driver, String url,
		String user, String password, boolean readOnly, Integer busyTimeout,
		Object transactionLevel, Integer fetchSize, Integer poolingSize, Integer poolingTimeout,
		int machineId, boolean urlType, Object urlParams, Map<String, Object> params) {
		JDBCKind ret = new JDBCKind();
		ret.name = name;
		ret.driver = driver;
		ret.url = url;
		ret.user = user;
		ret.password = password;
		ret.readOnly = readOnly;
		ret.busyTimeout = busyTimeout;
		ret.transactionLevel = transactionLevel(transactionLevel);
		ret.fetchSize = fetchSize;
		ret.poolingSize = poolingSize;
		ret.poolingTimeout = poolingTimeout;
		ret.params = new ArrayMap(params);
		ret.urlType = urlType;
		{
			Object o = urlParams;
			if(o == null) {
				ret.urlParams = null;
			} else if(o instanceof Map) {
				ret.urlParams = convertUrlParams((Map)o, ret.urlType);
			} else if(o instanceof String) {
				ret.urlParams = (String)o;
				if(ret.urlParams.isEmpty()) {
					ret.urlParams = null;
				}
			}
		}
		ret.notSemicolon = checkNotSemicolon(ret.url);
		ret.machineId = machineId;
		if(ret.machineId >= 511) {
			ret.machineId = 511;
		} else if(ret.machineId <= 0) {
			ret.machineId = 0;
		}
		ret.check();
		return ret;
	}
	
	/**
	 * プーリングなしのコネクション生成用のkindオブジェクトを生成.
	 * @param driver
	 * @param url
	 * @return
	 */
	public static final JDBCKind create(String driver, String url) {
		JDBCKind ret = new JDBCKind();
		ret.name = "notPooling-Connection";
		ret.driver = driver;
		ret.url = url;
		ret.params = new ArrayMap();
		ret.notSemicolon = checkNotSemicolon(ret.url);

		ret.check();
		return ret;
	}
	
	// Map指定したURLパラメータを文字列変換.
	@SuppressWarnings("rawtypes")
	private static final String convertUrlParams(Map m, boolean urlType) {
		if(m == null || m.size() == 0) {
			return null;
		}
		Object k;
		final StringBuilder ret = urlType ? new StringBuilder("?") : new StringBuilder(";");
		final String bcode = urlType ? "&" : ";";
		final Iterator itr = m.keySet().iterator();
		while(itr.hasNext()) {
			k = itr.next();
			ret.append(k).append("=").append(m.get(k)).append(bcode);
		}
		return ret.toString();
	}
	
	// トランザクションレベルを取得.
	private static final Integer transactionLevel(Object o) {
		// static int   TRANSACTION_NONE
		//              トランザクションがサポートされていないことを示す定数です。
		// static int   TRANSACTION_READ_COMMITTED
		//              ダーティ読込みは抑制され、繰返し不可の読み込みおよびファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_READ_UNCOMMITTED
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_REPEATABLE_READ
		//              ダーティ読み込みおよび繰返し不可の読込みは抑制され、ファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_SERIALIZABLE
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが抑制されることを示す定数です。
		
		if(Converter.isNumeric(o)) {
			Integer i = (Integer)Converter.convertInt(o);
			if(i.equals(Connection.TRANSACTION_NONE) ||
					i.equals(Connection.TRANSACTION_READ_COMMITTED) ||
					i.equals(Connection.TRANSACTION_READ_UNCOMMITTED) ||
					i.equals(Connection.TRANSACTION_REPEATABLE_READ) ||
					i.equals(Connection.TRANSACTION_SERIALIZABLE)) {
				return i;
			}
		} else {
			String s = ("" + o).toLowerCase();
			if("none".equals(s) || "transaction_none".equals(s)) {
				return Connection.TRANSACTION_NONE;
			}
			if("read_committed".equals(s) || "transaction_read_committed".equals(s)) {
				return Connection.TRANSACTION_READ_COMMITTED;
			}
			if("read_uncommitted".equals(s) || "transaction_read_uncommitted".equals(s)) {
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			}
			if("repeatable_read".equals(s) || "transaction_repeatable_read".equals(s)) {
				return Connection.TRANSACTION_REPEATABLE_READ;
			}
			if("serializable".equals(s) || "transaction_serializable".equals(s)) {
				return Connection.TRANSACTION_SERIALIZABLE;
			}
		}
		return null;
	}
	
	// SQLの末端に「；」セミコロンを付けるとNGかチェック.
	private static final boolean checkNotSemicolon(String url) {
		final String u = url.toLowerCase();
		if(u.startsWith("jdbc:oracle:") ||
			u.startsWith("jdbc:derby:")) {
			return true;
		}
		return false;
	}
	
	// 設定チェック.
	private final void check() {
		if(this.name == null || this.name.isEmpty()) {
			throw new JDBCException("kind name is not set.");
		}
		if(this.driver == null || this.driver.isEmpty()) {
			throw new JDBCException("jdbc driver package name is not set.");
		}
		if(this.url == null || this.url.isEmpty()) {
			throw new JDBCException("jdbc url is not set.");
		}
	}

	/**
	 * kind名を取得.
	 * 
	 * @return String kind名が返されます.
	 */
	public String getName() {
		return name;
	}

	/**
	 * ドライバー名を取得.
	 * 
	 * @return String ドライバー名が返却されます.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * URLを取得.
	 * 
	 * @return String URLが返却されます.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * ユーザ名を取得.
	 * 
	 * @return String ユーザ名が返却されます.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * パスワードを取得.
	 * 
	 * @return String パスワードが返却されます.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 読み込み専用データベース.
	 * 
	 * @return boolean [true]の場合は、読み込み専用データベースです.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * URLパラメータを取得.
	 * 
	 * @return String URLパラメータが返却されます.
	 */
	public String getUrlParams() {
		if (urlParams == null || urlParams.isEmpty()) {
			return "";
		}
		return urlParams;
	}

	/**
	 * BusyTimeoutを設定.
	 * 
	 * @param stmt 対象のStatemnetを設定します.
	 */
	public void setBusyTimeout(Statement stmt) {
		if (busyTimeout != null && busyTimeout > 0) {
			try {
				// 秒単位で設定.
				stmt.setQueryTimeout(busyTimeout);
			} catch (Exception e) {
				throw new JDBCException(e);
			}
		}
	}

	/**
	 * フェッチサイズを設定.
	 * 
	 * @param stmt 対象のStatemnetを設定します.
	 */
	public void setFetchSize(Statement stmt) {
		if (fetchSize != null && fetchSize > 0) {
			try {
				stmt.setFetchSize(fetchSize);
			} catch (Exception e) {
				throw new JDBCException(e);
			}
		}
	}

	/**
	 * 基本トランザクションレベルを設定.
	 * 
	 * @param connection 対象のコネクションオブジェクトを設定します.
	 */
	public void setTransactionLevel(Connection connection) {
		if (transactionLevel != null) {
			try {
				connection.setTransactionIsolation(transactionLevel);
			} catch (Exception e) {
				throw new JDBCException(e);
			}
		}
	}

	/**
	 * Property定義.
	 * 
	 * @param prop 対象のプロパティを設定します.
	 */
	public void setProperty(Properties prop) {
		if (params == null || params.size() == 0) {
			return;
		}
		Object[] n;
		OList<Object[]> list = params.getListMap().rawData();
		int len = list.size();
		for(int i = 0; i < len; i ++) {
			n = list.get(i);
			if(n[0] != null) {
				prop.put("" + n[0], n[1] == null ? null : "" + n[1]);
			}
		}
	}
	
	/**
	 * プーリングサイズを取得.
	 * @return
	 */
	public Integer getPoolingSize() {
		return poolingSize;
	}
	
	/**
	 * プーリングタイムアウトを取得.
	 * @return
	 */
	public Integer getPoolingTimeout() {
		return poolingTimeout;
	}
	
	/**
	 * SQLの末端にセミコロンを付与させない場合は「true」が返却されます.
	 * @return
	 */
	public boolean isNotSemicolon() {
		return notSemicolon;
	}
	
	/**
	 * マシンIDを取得.
	 * @return
	 */
	public int getMachineId() {
		return machineId;
	}
	
	/**
	 * Kind設定内容をMapで取得.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getMap() {
		return new JsMap(
			"name", name, "driver", driver, "url", url, "user", user,
			"password", password, "readOnly", readOnly, "urlParams", urlParams,
			"busyTimeout", busyTimeout, "transactionLevel", transactionLevel, "fetchSize", fetchSize,
			"params", new JsMap(params), "poolingSize", poolingSize, "poolingTimeout", poolingTimeout,
			"notSemicolon", notSemicolon, "machineId", machineId);
	}
	
	/**
	 * 文字列変換.
	 * @return
	 */
	@Override
	public String toString() {
		return JsonOut.toString(getMap());
	}
}

package rhigin;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.Json;
import rhigin.scripts.Read;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhinoScriptable;
import rhigin.util.ArrayMap;
import rhigin.util.ConvertGet;
import rhigin.util.FileUtil;
import rhigin.util.OList;

/**
 * Rhiginコンフィグ.
 */
public class RhiginConfig implements RhinoScriptable {
	private static final String CONF_CHARSET = "UTF8";
	
	private String _confDir = null;
	private String _rhiginEnv = null;
	private ReloadFunction _reloadFunc = null;
	private Map<String, Map<String, Object>> _config = null;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	// メイン情報.
	private static RhiginConfig mainConfig = null;
	
	/**
	 * 管理対象のRhiginConfigを設定.
	 * 
	 * @param conf
	 */
	public static final void setMainConfig(RhiginConfig conf) {
		mainConfig = conf;
	}
	
	/**
	 * 管理対象のRhiginConfigを取得.
	 * 
	 * @return RhiginConfig
	 */
	public static final RhiginConfig getMainConfig() {
		return mainConfig;
	}
	
	protected RhiginConfig() {
		throw new RhiginException("Unsupported constructor.");
	}
	
	/**
	 * コンストラクタ.
	 * @param rhiginEnv
	 * @param dir
	 */
	public RhiginConfig(String rhiginEnv, String dir) {
		load(rhiginEnv, dir);
	}
	
	/**
	 * コンフィグ情報のロード.
	 * @param rhiginEnv
	 * @param dir
	 * @return [true]の場合、rhiginEnvの条件でロード出来ました.
	 */
	public boolean load(String rhiginEnv, String dir) {
		String[] confDir = _getToCheckConfDir(rhiginEnv, dir);
		try {
			rwLock.writeLock().lock();
			try {
				this._config = loadJSONByDir(confDir[1]);
				this._rhiginEnv = confDir[0];
				this._confDir = dir;
			} finally {
				rwLock.writeLock().unlock();
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
		return confDir[0] != null;
	}
	
	/**
	 * 指定ディレクトリ以下のJSONファイルを読み込み.
	 * @param dir
	 * @return
	 */
	public static final Map<String, Map<String, Object>> loadJSONByDir(String dir) {
		try {
			Map<String, Map<String, Object>> ret = new ArrayMap<String, Map<String, Object>>();
			if (!FileUtil.isDir(dir)) {
				return ret;
			}
			dir = FileUtil.getFullPath(dir);
			if (!dir.endsWith("/")) {
				dir += "/";
			}
			String[] list = new File(dir).list();
			if (list != null && list.length > 0) {
				Map<String, Object> v;
				int len = list.length;
				for (int i = 0; i < len; i++) {
					v = loadJSONByFile(dir + list[i]);
					if(v != null) {
						ret.put(cutExtention(list[i]), v);
					}
				}
			}
			return ret;
		} catch (RhiginException re) {
			throw re;
		} catch (Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * 指定ファイルのJSONファイルを読み込み
	 * @param name
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Map<String, Object> loadJSONByFile(String name) {
		try {
			if (FileUtil.isFile(name)) {
				Object o = Json.decodeByComment(
					FileUtil.getFileString(name, CONF_CHARSET));
				if(o instanceof Map) {
					return new Read.Maps((Map) o);
				}
			}
			return null;
		} catch (RhiginException re) {
			throw re;
		} catch (Exception e) {
			throw new RhiginException(e);
		}
	}

	// 拡張子を除外.
	private static final String cutExtention(String name) {
		int p = name.lastIndexOf(".");
		if (p == -1) {
			return name;
		}
		return name.substring(0, p);
	}
	
	/**
	 * リロード処理.
 	 * @return [true]の場合、rhiginEnvの条件でロード出来ました.
	 */
	public boolean reload() {
		String rhiginEnv, confDir;
		rwLock.readLock().lock();
		try {
			rhiginEnv = _rhiginEnv;
			confDir = _confDir;
		} finally {
			rwLock.readLock().unlock();
		}
		return load(rhiginEnv, confDir);
	}

	/**
	 * リロード処理.
	 * @param rhiginEnv
 	 * @return [true]の場合、rhiginEnvの条件でロード出来ました.
	 */
	public boolean reload(String rhiginEnv) {
		return load(rhiginEnv, _getConfDir());
	}
	
	/**
	 * コンフィグフォルダを取得.
	 * @return String コンフィグフォルダが返却されます.
	 */
	public String getConfigDir() {
		String rhiginEnv, confDir;
		rwLock.readLock().lock();
		try {
			rhiginEnv = _rhiginEnv;
			confDir = _confDir;
		} finally {
			rwLock.readLock().unlock();
		}
		return _getToCheckConfDir(rhiginEnv, confDir)[1];
	}
	
	// configフォルダをRhiginEnv名が存在するかチェックして取得.
	private static final String[] _getToCheckConfDir(String rhiginEnv, String confDir) {
		if(rhiginEnv == null || rhiginEnv.isEmpty()) {
			rhiginEnv = null;
		}
		try {
			String dir = FileUtil.getFullPath(confDir);
			confDir = dir + "/";
			if (rhiginEnv != null) {
				confDir += rhiginEnv + "/";
				// 対象フォルダが存在しない、対象フォルダ以下のコンフィグ情報が０件の場合は
				// confフォルダ配下を読み込む.
				File confStat = new File(confDir);
				if (!confStat.isDirectory() || confStat.list() == null || confStat.list().length == 0) {
					confDir = dir + "/";
					rhiginEnv = null;
				}
				confStat = null;
			}
			return new String[] {rhiginEnv, confDir};
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	// コンフィグ情報の取得.
	private Map<String, Map<String, Object>> _getConfig() {
		rwLock.readLock().lock();
		try {
			return _config;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	// reload用のメソッドオブジェクトを取得.
	private ReloadFunction _getReloadFunction() {
		rwLock.readLock().lock();
		try {
			if(_reloadFunc == null) {
				_reloadFunc = new ReloadFunction(this);
			}
			return _reloadFunc;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	// configフォルダ名を取得.
	private String _getConfDir() {
		rwLock.readLock().lock();
		try {
			return _confDir;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	// RhiginEnvを取得.
	private String _getRhiginEnv() {
		rwLock.readLock().lock();
		try {
			return _rhiginEnv;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/**
	 * 指定コンフィグ情報を取得.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @param s
	 *            Scriptable が設定されます.
	 * @return Object
	 */
	@Override
	public Object _get(String name, Scriptable s) {
		if("reload".equals(name)) {
			return _getReloadFunction();
		} else if("dir".equals(name)) {
			return _getConfDir();
		} else if("env".equals(name)) {
			String rhiginEnv = _getRhiginEnv();
			if(rhiginEnv == null) {
				return "";
			}
			return rhiginEnv;
		} else if (has(name)) {
			return get(name);
		}
		return Undefined.instance;
	}
	
	/**
	 * 指定コンフィグ情報を取得.
	 * 
	 * @param no
	 *            番号を設定します.
	 * @param s
	 *            Scriptable が設定されます.
	 * @return Object
	 */
	@Override
	public Object _get(int no, Scriptable parent) {
		return null;
	}

	/**
	 * 指定コンフィグ情報が存在するかチェック.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @param s
	 *            Scriptable が設定されます.
	 * @return boolean
	 */
	@Override
	public boolean has(String name, Scriptable s) {
		if("reload".equals(name) || "dir".equals(name) || "env".equals(name)) {
			return true;
		}
		return has(name);
	}

	/**
	 * コンフィグ名一覧を取得.
	 * 
	 * @return Object[]
	 */
	@Override
	public Object[] getIds() {
		OList<Object> list = new OList<Object>();
		Iterator<String> it = _getConfig().keySet().iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		list.add("reload");
		list.add("dir");
		list.add("env");
		return list.getArray();
	}

	/**
	 * オブジェクト名を取得.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return "[config]";
	}

	/**
	 * 指定コンフィグ情報を取得.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @return Map<String,Object>
	 */
	public Map<String, Object> get(String name) {
		return _getConfig().get(name);
	}

	/**
	 * 指定コンフィグ情報が存在するかチェック.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @return boolean
	 */
	public boolean has(String name) {
		return _getConfig().containsKey(name);
	}

	/**
	 * 読み込みコンフィグ数を取得.
	 * 
	 * @return int
	 */
	public int size() {
		return _getConfig().size() + 3;
	}

	/**
	 * 読み込みディレクトリを取得.
	 * 
	 * @return
	 */
	public String getDir() {
		return _getConfDir();
	}
	
	/**
	 * Rhigin環境変数を取得.
	 * @return
	 */
	public String getRhiginEnv() {
		return _getRhiginEnv();
	}
	
	/**
	 * ReadWriteLockを取得.
	 * @return
	 */
	public ReadWriteLock getRwLock() {
		return rwLock;
	}

	// ConvertGetに変換して取得.
	@SuppressWarnings("unchecked")
	private ConvertGet<String> _get(String name) {
		Map<String, Object> ret = _getConfig().get(name);
		if (ret != null) {
			return (ConvertGet<String>) ret;
		}
		return null;
	}

	/**
	 * 指定コンフィグ情報のデータが定義されているかチェック.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return boolean
	 */
	public boolean has(String name, String key) {
		if (has(name)) {
			return _getConfig().get(name).containsKey(key);
		}
		return false;
	}

	/**
	 * Object情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Object 情報が返却されます.
	 */
	public Object get(String name, String key) {
		if (has(name)) {
			return _getConfig().get(name).get(key);
		}
		return null;
	}

	/**
	 * boolean情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	public Boolean getBoolean(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getBoolean(key);
		}
		return null;
	}

	/**
	 * int情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Integer 情報が返却されます.
	 */
	public Integer getInt(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getInt(key);
		}
		return null;
	}

	/**
	 * long情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Long 情報が返却されます.
	 */
	public Long getLong(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getLong(key);
		}
		return null;
	}

	/**
	 * float情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Float 情報が返却されます.
	 */
	public Float getFloat(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getFloat(key);
		}
		return null;
	}

	/**
	 * double情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Double 情報が返却されます.
	 */
	public Double getDouble(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getDouble(key);
		}
		return null;
	}

	/**
	 * String情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return String 情報が返却されます.
	 */
	public String getString(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getString(key);
		}
		return null;
	}

	/**
	 * Date情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Date 情報が返却されます.
	 */
	public java.sql.Date getDate(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getDate(key);
		}
		return null;
	}

	/**
	 * Time情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Time 情報が返却されます.
	 */
	public java.sql.Time getTime(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getTime(key);
		}
		return null;
	}

	/**
	 * Timestamp情報を取得.
	 * 
	 * @parma name コンフィグ名を設定します.
	 * @param key
	 *            nameのコンフィグの要素名を設定します.
	 * @return Timestamp 情報が返却されます.
	 */
	public java.sql.Timestamp getTimestamp(String name, String key) {
		ConvertGet<String> m = _get(name);
		if (m != null) {
			return m.getTimestamp(key);
		}
		return null;
	}
	
	/**
	 * リロード用処理.
	 */
	private static final class ReloadFunction extends RhiginFunction {
		RhiginConfig conf;
		protected ReloadFunction(RhiginConfig conf) {
			this.conf = conf;
		}
		
		@Override
		public String getName() {
			return "reload";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1 && args[0] != null) {
				return conf.reload("" + args[0]);
			} else {
				return conf.reload();
			}
		}
	}

	@Override
	public void _put(String name, Scriptable obj, Object value) {
	}

	@Override
	public void _put(int no, Scriptable obj, Object value) {
	}
}

package rhigin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.Json;
import rhigin.scripts.Read;
import rhigin.scripts.function.ToStringFunction;
import rhigin.util.AndroidMap;
import rhigin.util.BlankScriptable;
import rhigin.util.ConvertGet;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.OList;

/**
 * Rhiginコンフィグ.
 */
public class RhiginConfig implements BlankScriptable {
	private static final String CONF_CHARSET = "UTF8";
	private final ToStringFunction.Execute toStringFunction = new ToStringFunction.Execute(this);
	private String confDir = null;
	private Map<String, Map<String, Object>> config = null;

	public RhiginConfig() throws IOException {
		this(RhiginConstants.DIR_CONFIG);
	}

	public RhiginConfig(String dir) throws IOException {
		config = load(dir);
		confDir = dir;
	}

	// 指定ディレクトリ以下のJSONファイルを読み込み.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Map<String, Map<String, Object>> load(String dir) throws IOException {
		try {
			Map<String, Map<String, Object>> ret = new AndroidMap<String, Map<String, Object>>();
			if (!FileUtil.isDir(dir)) {
				return ret;
			}
			dir = FileUtil.getFullPath(dir);
			if (!dir.endsWith("/")) {
				dir += "/";
			}
			String[] list = new File(dir).list();
			if (list != null && list.length > 0) {
				String name;
				Map<String, Object> v;
				int len = list.length;
				for (int i = 0; i < len; i++) {
					name = dir + list[i];
					if (FileUtil.isFile(name)) {
						v = (Map) Json.decode(Converter.cutComment(
							FileUtil.getFileString(name, CONF_CHARSET)));
						ret.put(cutExtention(list[i]), new Read.Maps(v));
						v = null;
					}
				}
			}
			return ret;
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new IOException(e);
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
	 * 指定コンフィグ情報を取得.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @param s
	 *            Scriptable が設定されます.
	 * @return Object
	 */
	@Override
	public Object get(String name, Scriptable s) {
		if (has(name)) {
			return get(name);
		}
		return Undefined.instance;
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
		Iterator<String> it = config.keySet().iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
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

	@Override
	public Object getDefaultValue(Class<?> clazz) {
		return toStringFunction.getDefaultValue(clazz);
	}

	/**
	 * 指定コンフィグ情報を取得.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @return Map<String,Object>
	 */
	public Map<String, Object> get(String name) {
		return config.get(name);
	}

	/**
	 * 指定コンフィグ情報が存在するかチェック.
	 * 
	 * @param name
	 *            コンフィグ名を設定します.
	 * @return boolean
	 */
	public boolean has(String name) {
		return config.containsKey(name);
	}

	/**
	 * 読み込みコンフィグ数を取得.
	 * 
	 * @return int
	 */
	public int size() {
		return config.size();
	}

	/**
	 * 読み込みディレクトリを取得.
	 * 
	 * @return
	 */
	public String getDir() {
		return confDir;
	}

	// ConvertGetに変換して取得.
	@SuppressWarnings("unchecked")
	private ConvertGet<String> _get(String name) {
		Map<String, Object> ret = config.get(name);
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
			return config.get(name).containsKey(key);
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
			return config.get(name).get(key);
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
}

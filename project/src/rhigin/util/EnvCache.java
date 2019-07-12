package rhigin.util;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 環境変数キャッシュ.
 */
public class EnvCache {
	private static final ThreadLocal<Map<String,String>> cache = new ThreadLocal<Map<String,String>>();
	private static final Map<String,String> getCache() {
		Map<String,String> ret = cache.get();
		if(ret == null) {
			ret = new WeakHashMap<String,String>();
			cache.set(ret);
		}
		return ret;
	}
	
	/**
	 * 指定名の環境変数内容を取得.
	 * @param name 取得対象の環境変数名を設定します.
	 * @return String
	 */
	public static final String get(String name) {
		Map<String,String> c = getCache();
		String ret = c.get(name);
		if(ret == null) {
			ret = System.getenv(name);
			c.put(name, ret);
		}
		return ret;
	}
}

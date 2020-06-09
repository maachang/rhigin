package rhigin.scripts.compile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Script;

import rhigin.RhiginException;
import rhigin.http.Http;
import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.util.FileUtil;
import rhigin.util.LruCache;
import rhigin.util.WatchPath;

/**
 * Rhiginコンパイルキャッシュ コンパイルされた、キャッシュ情報は、全体で管理するのではなく、スレッド単位で作成します.
 */
public class CompileCache {
	private static final ThreadLocal<LruCache<String, ScriptElement>> cache = new ThreadLocal<LruCache<String, ScriptElement>>();

	// threadlocal: キャッシュオブジェクト(LRU)を取得.
	private static final LruCache<String, ScriptElement> getCache(int size) {
		LruCache<String, ScriptElement> ret = cache.get();
		if (ret == null) {
			ret = new LruCache<String, ScriptElement>(size);
			cache.set(ret);
		}
		return ret;
	}

	// jsファイル文字コード.
	private static final String CHARSET = "UTF8";

	// キャッシュサイズ定義.
	private static final int NOT_CACHE_SIZE = 8192;
	private static final int MIN_CACHE_SIZE = 64;
	private static final int DEF_CACHE_SIZE = 256;
	private static final int MAX_CACHE_SIZE = 4096;
	private int maxCacheSize;

	// ベースディレクトリ.
	private static final String DEF_BASE_DIR = ".";
	private String baseDir = "";

	/**
	 * 初期化.
	 */
	public CompileCache() {
		this(DEF_CACHE_SIZE, DEF_BASE_DIR);
	}

	/**
	 * キャッシュサイズを設定して初期化.
	 * 
	 * @param s
	 *            キャッシュサイズを設定します.
	 */
	public CompileCache(int s) {
		this(s, DEF_BASE_DIR);
	}

	/**
	 * キャッシュサイズを設定して初期化.
	 * 
	 * @param b
	 *            ベースパスを設定します.
	 */
	public CompileCache(String b) {
		this(DEF_CACHE_SIZE, b);
	}

	/**
	 * キャッシュサイズを設定して初期化.
	 * 
	 * @param s
	 *            キャッシュサイズを設定します.
	 * @param b
	 *            ベースパスを設定します.
	 */
	public CompileCache(int s, String b) {
		if (s <= 0) {
			s = NOT_CACHE_SIZE;
		} else if (s <= MIN_CACHE_SIZE) {
			s = MIN_CACHE_SIZE;
		} else if (s >= MAX_CACHE_SIZE) {
			s = MAX_CACHE_SIZE;
		}
		maxCacheSize = s;
		try {
			baseDir = FileUtil.getFullPath(b) + "/";
		} catch (Exception e) {
		}
	}

	// キャッシュオブジェクトを取得.
	private LruCache<String, ScriptElement> cache() {
		return getCache(maxCacheSize);
	}

	/**
	 * キャッシュクリア.
	 */
	public void clear() {
		cache().clear();
	}

	// キャッシュ情報が古いかキャッシュに存在しない場合は、ファイルからコンパイル結果を作成してロードする.
	private static final ScriptElement load(LruCache<String, ScriptElement> c, String key, String jsName, long time,
			String headerScript, String footerScript, int lineNo) throws Exception {
		BufferedReader r = null;
		try {
			// ファイルが存在しない場合.
			if (time == -1) {
				// キャッシュに情報が存在する場合.
				if (c.contains(key)) {
					c.remove(key);
				}
				// ファイルが存在しないことを示すエラー返却.
				throw new CompileException(404);
			}
			// ファイルを読み込んでキャッシュセット.
			r = new BufferedReader(new InputStreamReader(new FileInputStream(jsName), CHARSET));
			Script sc = ExecuteScript.compile(r, key, headerScript, footerScript, lineNo);
			r.close();
			r = null;
			ScriptElement em = new ScriptElement(sc, jsName, time);
			c.put(key, em);
			return em;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * キャッシュ情報をロード＆取得.
	 * 
	 * @param jsName
	 *            ロードするファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @return ScriptElement スクリプト要素が返却されます.
	 * @exception CompileException
	 *                コンパイル例外.
	 */
	public ScriptElement get(String jsName, String headerScript, String footerScript) {
		try {
			// 対象ファイルパスをフルパスで取得.
			jsName = FileUtil.getFullPath(jsName);
			// 拡張子がjsでない場合は、jsを付与.
			if (!jsName.toLowerCase().endsWith(".js")) {
				jsName += ".js";
			}
			// 現在のファイル時間を取得.
			long time = WatchPath.getInstance().getMtime(jsName);
			if(time == -1) {
				time = FileUtil.getFileLength(jsName);
			}
			// ベースパス内の場合は、ベースパス名を除外.
			final String key = jsName.startsWith(baseDir) ?
				jsName.substring(baseDir.length()) : jsName;
			// 現在のキャッシュ情報を取得.
			final LruCache<String, ScriptElement> c = cache();
			ScriptElement ret = c.get(key);
			// キャッシュ情報が無いか、キャッシュ情報が更新された場合.
			if (ret == null || time != ret.getTime()) {
				// データロード.
				ret = load(c, key, jsName, time, headerScript, footerScript, 1 - ExecuteScript.getEnterCount(headerScript));
			}
			return ret;
		} catch (CompileException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CompileException(500, e);
		}
	}
	
	/**
	 * コンパイルキャッシュオブジェクトがNULLだった場合のスクリプト実行.
	 * @param jsName
	 *            ロードするファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @return Object eval処理結果が返却されます.
	 * @exception CompileException
	 *                コンパイル例外.
	 */
	public static final Object noCacheByEval(String jsName, String headerScript, String footerScript) {
		Reader r = null;
		try {
			// 対象ファイルパスをフルパスで取得.
			jsName = FileUtil.getFullPath(jsName);
			// 拡張子がjsでない場合は、jsを付与.
			if (!jsName.toLowerCase().endsWith(".js")) {
				jsName += ".js";
			}
			r = new BufferedReader(new InputStreamReader(new FileInputStream(jsName), CHARSET));
			Object ret = ExecuteScript.eval(r, jsName, headerScript, footerScript,
				1 - ExecuteScript.getEnterCount(headerScript));
			r.close();
			r = null;
			return ret;
		} catch (Exception e) {
			throw new CompileException(500, e);
		} finally {
			if(r != null) {
				try {
					r.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// threadローカルでCompileCacaheを管理.
	private static final ThreadLocal<CompileCache> threadCache = new ThreadLocal<CompileCache>();
	private static int baseCacheSize = -1;
	private static String baseCacheDir = null;
	
	/**
	 * コンパイルキャッシュ定義をセット.
	 * @param cacheSize
	 * @param cacheDir
	 */
	public static final void setBaseCache(Integer cacheSize, String cacheDir) {
		if(cacheSize == null || cacheSize <= 0) {
			baseCacheSize = -1;
		} else {
			baseCacheSize = cacheSize;
		}
		if(cacheDir == null || cacheDir.isEmpty()) {
			baseCacheDir = null;
		} else {
			baseCacheDir = cacheDir;
		}
	}

	/**
	 * コンパイルキャッシュを取得.
	 * @return
	 */
	public static final CompileCache getCache() {
		// 別スレッドを作成した場合の拡張対応.
		CompileCache ret = threadCache.get();
		if (ret == null) {
			if(baseCacheSize == -1 || baseCacheDir == null) {
				HttpInfo info = Http.getHttpInfo();
				if (info != null) {
					ret = new CompileCache(
						baseCacheSize == -1 ? info.getCompileCacheSize() : baseCacheSize,
						baseCacheDir == null ? info.getCompileCacheRootDir() : baseCacheDir);
					threadCache.set(ret);
				}
			} else {
				ret = new CompileCache(baseCacheSize, baseCacheDir);
			}
		}
		return ret;
	}
	
	/**
	 * スクリプト実行.
	 * @param jsName
	 *            ロードするファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @return Object eval処理結果が返却されます.
	 * @exception CompileException
	 *                コンパイル例外.
	 */
	public static final Object eval(String jsName, String headerScript, String footerScript) {
		try {
			final CompileCache c = getCache();
			if (c == null) {
				return CompileCache.noCacheByEval(jsName, headerScript, footerScript);
			}
			final ScriptElement se = c.get(jsName, headerScript, footerScript);
			return ExecuteScript.eval(se.getScript());
		} catch (RhiginException re) {
			throw re;
		} catch (Exception e) {
			throw new RhiginException(500, e);
		}
	}

}

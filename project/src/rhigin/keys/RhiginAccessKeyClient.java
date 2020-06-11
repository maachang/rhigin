package rhigin.keys;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.scripts.Json;
import rhigin.scripts.JsonOut;
import rhigin.util.ArrayMap;
import rhigin.util.FileUtil;
import rhigin.util.Flag;
import rhigin.util.ObjectList;

/**
 * RhiginAccessKeyのクライアント管理.
 */
public class RhiginAccessKeyClient {
	// シングルトン.
	private static final RhiginAccessKeyClient SNGL = new RhiginAccessKeyClient();
	
	// デフォルトのコンフィグファイル名.
	private static final String DEFAULT_CONFIG_FILE = new StringBuilder(System.getProperty("user.home"))
			.append(System.getProperty("file.separator"))
			.append(".rhiginAccessKey.json")
			.toString();
	
	// conf/accessKey.jsonのデータ管理.
	private Map<String, Map<String, String>> configMap = null;
	
	// 外部ファイルのjsonデータ管理.
	private Map<String, Map<String, String>> configHome = null;
	
	// Homeファイル名.
	private String homeFileName = null;
	
	// ロードフラグ.
	private Flag loadFlag = new Flag(false);
	
	// readWriteロック.
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	/**
	 * オブジェクトを取得.
	 * @return
	 */
	public static final RhiginAccessKeyClient getInstance() {
		return SNGL;
	}
	
	// コンフィグファイルのロード.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object[] _readConfig(String file) {
		
		// conf/accessKey.jsonを読み込む.
		Map<String, Map<String, String>> confMap = new ArrayMap<String, Map<String, String>>();
		RhiginConfig rconf = RhiginConfig.getMainConfig();
		if(rconf != null) {
			Map<String, Map<String, String>> conf = (Map)rconf.get("accessKey");
			if(conf != null) {
				Entry<String, Map<String, String>> entry;
				Iterator<Entry<String, Map<String, String>>> it = conf.entrySet().iterator();
				while(it.hasNext()) {
					entry = it.next();
					confMap.put(entry.getKey(),entry.getValue());
				}
				conf = null;
			}
		}
		
		// HomeファイルのaccessKey定義を読み込む.
		Map<String, Map<String, String>> configHome = new ArrayMap<String, Map<String, String>>();
		try {
			Map<String, Map<String, String>> conf = (Map)RhiginConfig.loadJSONByFile(file);
			if(conf != null) {
				Entry<String, Map<String, String>> entry;
				Iterator<Entry<String, Map<String, String>>> it = conf.entrySet().iterator();
				while(it.hasNext()) {
					entry = it.next();
					configHome.put(entry.getKey(),entry.getValue());
				}
				conf = null;
			}
		} catch(Exception e) {
			// 例外が出た場合は無視する.
		}
		return new Object[] { confMap, configHome };
	}
	
	// mapの指定した複数のキーの条件を取得.
	private static final String _getValue(Map<String, String> m, String... keys) {
		int len = keys.length;
		for(int i = 0; i < len; i ++) {
			if(m.containsKey(keys[i])) {
				return m.get(keys[i]);
			}
		}
		return null;
	}
	
	/**
	 * 初期化処理.
	 * @return
	 */
	public boolean init() {
		return init(DEFAULT_CONFIG_FILE);
	}
	
	/**
	 * Homeファイルを指定して初期化処理.
	 * @param file
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean init(String file) {
		if(!loadFlag.get()) {
			rwLock.writeLock().lock();
			try {
				if(!loadFlag.get()) {
					Object[] o = _readConfig(file);
					configMap = (Map)o[0];
					configHome = (Map)o[1];
					homeFileName = file;
					loadFlag.setToGetBefore(true);
					return true;
				}
			} finally {
				rwLock.writeLock().lock();
			}
		}
		return false;
	}
	
	/**
	 * リロード処理.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reload() {
		rwLock.writeLock().lock();
		try {
			String file = homeFileName;
			Object[] o = _readConfig(file);
			configMap = (Map)o[0];
			configHome = (Map)o[1];
			homeFileName = file;
			loadFlag.setToGetBefore(true);
		} finally {
			rwLock.writeLock().lock();
		}
	}
	
	/**
	 * 初期化済みかチェック.
	 * @return
	 */
	public boolean isInit() {
		return loadFlag.get();
	}
	
	// 初期化チェック.
	protected void check() {
		//if(!loadFlag.get()) {
		//	throw new RhiginException("RhiginAccessKeyConfig is not initialized.");
		//}
		
		// 初期化されていない場合はデフォルトのHomeファイルで初期化処理.
		init();
	}
	
	/**
	 * Homeファイル名を取得.
	 * @return String
	 */
	public String getHomeName() {
		check();
		rwLock.readLock().lock();
		try {
			return homeFileName;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/**
	 * アクセスキーを取得.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @return String[] [0] アクセスキー [1]認証コード.
	 */
	public String[] get(String url) {
		check();
		if(url == null || url.isEmpty()) {
			return null;
		}
		Map<String, String> m = null;
		url = RhiginAccessKeyUtil.getDomain(false, url);
		rwLock.readLock().lock();
		try {
			Map<String, Map<String, String>> c = configMap;
			if(!c.containsKey(url)) {
				c = configHome;
				if(!c.containsKey(url)) {
					return null;
				}
			}
			m = c.get(url);
			if(m == null) {
				return null;
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return new String[] {
			_getValue(m, "accessKey", "akey"),
			_getValue(m, "authCode", "acode")
		};
	}
	
	/**
	 * conf/accessKey.json に指定条件を追加.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @param key accessKeyを設定します.
	 * @param code authCodeを設定します.
	 */
	public final void setByConfig(String url, String key, String code) {
		_set(false, url, key, code);
	}
	
	/**
	 * conf/accessKey.json から アクセスキーを取得.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @return String[] [0] アクセスキー [1]認証コード.
	 */
	public String[] getByConfig(String url) {
		return _get(false, url);
	}
	
	/**
	 * Homeファイル に指定条件を追加.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @param key accessKeyを設定します.
	 * @param code authCodeを設定します.
	 */
	public final void setByHome(String url, String key, String code) {
		_set(true, url, key, code);
	}
	
	/**
	 * Homeファイル から アクセスキーを取得.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @return String[] [0] アクセスキー [1]認証コード.
	 */
	public String[] getByHome(String url) {
		return _get(true, url);
	}
	
	/**
	 * conf/accessKey.json に指定条件を削除.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @return [true]の場合、削除に成功しました。
	 */
	public final boolean removeByConfig(String url) {
		return _remove(false, url);
	}
	
	/**
	 * Homeファイル に指定条件を削除.
	 * @param url URL(http://domain:port)までの条件を設定します.
	 * @return [true]の場合、削除に成功しました。
	 */
	public final boolean removeByHome(String url) {
		return _remove(true, url);
	}
	
	/**
	 * 登録URL一覧を取得.
	 * @return List<String> 登録されているURL一覧が返却されます.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final List<String> getUrls() {
		check();
		int len;
		Integer no;
		String name;
		List<String> ret = null;
		ArrayMap<String, Integer> map = new ArrayMap<String, Integer>();
		rwLock.readLock().lock();
		try {
			Map[] maps = new Map[] { configMap, configHome };
			len = maps.length;
			for(int i = 0; i < len; i ++) {
				Iterator<String> itr = maps[i].keySet().iterator();
				while(itr.hasNext()) {
					name = itr.next();
					no = map.get(name);
					map.put(name, no == null ? 1 : no + 1);
				}
			}
			len = map.size();
			ret = new ObjectList(len);
			for(int i = 0; i < len; i ++) {
				ret.add(map.getKey(i));
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return ret;
	}
	
	/**
	 * 登録URL一覧を取得.
	 * @param url 対象のURLを設定します.
	 * @return List<String> 指定URLに対するアクセスキー一覧が返却されます.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final List<String> getKeys(String url) {
		check();
		if(url == null || url.isEmpty()) {
			return null;
		}
		List<String> ret = new ObjectList<String>();
		rwLock.readLock().lock();
		try {
			Map[] maps = new Map[] { configMap, configHome };
			int len = maps.length;
			for(int i = 0; i < len; i ++) {
				Map<String, String> m = (Map)maps[i].get(url);
				if(m == null) {
					continue;
				}
				ret.add(_getValue(m, "accessKey", "akey"));
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return ret;
	}
	
	// RhiginConfigが有効かチェック.
	private final void _checkRhiginConfig(boolean home) {
		if(!home) {
			RhiginConfig rc = RhiginConfig.getMainConfig();
			if(rc == null) {
				throw new RhiginException("RhiginConfig is not valid.");
			}
		}
	}
	
	// fileかconfigかで情報取得.
	private final String[] _get(boolean home, String url) {
		check();
		if(url == null || url.isEmpty()) {
			return null;
		}
		url = RhiginAccessKeyUtil.getDomain(false, url);
		Map<String, String> m = null;
		rwLock.readLock().lock();
		try {
			Map<String, Map<String, String>> c = home ? configHome : configMap;
			if(!c.containsKey(url)) {
				return null;
			}
			m = c.get(url);
			if(m == null) {
				return null;
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return new String[] {
			_getValue(m, "accessKey", "akey"),
			_getValue(m, "authCode", "acode")
		};
	}
	
	// コンフィグ情報の更新.
	private final void _set(boolean home, String url, String key, String code) {
		check();
		_checkRhiginConfig(home);
		// key, codeの内容が不正な場合.
		if(key == null || key.length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			throw new RhiginException("An invalid access key is set.");
		} else if(code == null || code.length() != RhiginAccessKeyConstants.AUTH_CODE_LENGTH_64) {
			throw new RhiginException("An invalid auth code is set.");
		}
		Map<String, Map<String, String>> c = null;
		rwLock.writeLock().lock();
		try {
			c = home ? configHome : configMap;
			url = RhiginAccessKeyUtil.getDomain(false, url);
			c.put(url, new ArrayMap<String, String>(
					"accessKey", key.trim(), "authCode", code.trim()));
		} finally {
			rwLock.writeLock().unlock();
		}
		_save(home, c);
	}
	
	// コンフィグ情報の削除.
	private final boolean _remove(boolean home, String url) {
		check();
		_checkRhiginConfig(home);
		Map<String, Map<String, String>> c = null;
		rwLock.writeLock().lock();
		try {
			c = home ? configHome : configMap;
			url = RhiginAccessKeyUtil.getDomain(false, url);
			if(!c.containsKey(url)) {
				return false;
			}
			c.remove(url);
		} finally {
			rwLock.writeLock().unlock();
		}
		_save(home, c);
		return true;
	}
	
	// コンフィグ情報の保存.
	private final void _save(boolean home, Object c) {
		boolean errFlg = false;
		rwLock.writeLock().lock();
		try {
			if(!home) {
				RhiginConfig rc = RhiginConfig.getMainConfig();
				String name = rc.getConfigDir() + "/accessKey.json";
				rc.getRwLock().writeLock().lock();
				try {
					FileUtil.setFileString(true, name, JsonOut.toString(c), "UTF8");
				} catch(Exception e) {
					throw new RhiginException(e);
				} finally {
					rc.getRwLock().writeLock().unlock();
				}
				rc.reload();
			} else {
				try {
					FileUtil.setFileString(true, homeFileName, JsonOut.toString(c), "UTF8");
				} catch(Exception e) {
					throw new RhiginException(e);
				}
			}
		} catch(RhiginException re) {
			errFlg = true;
			throw re;
		} finally {
			rwLock.writeLock().unlock();
			// エラーが発生した場合はロールバック.
			if(errFlg) {
				try {
					reload();
				} catch(Exception e) {
				}
			}
		}
	}
}

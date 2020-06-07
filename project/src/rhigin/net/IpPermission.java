package rhigin.net;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.scripts.JsonOut;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.ObjectList;

/**
 * Ipアドレスのパーミッション定義.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class IpPermission {
	private static final int ATHER_HEAD = -1;
	private Map<String, Map<Integer, List<IpRange>>> permissions = null;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	// メイン情報.
	private static IpPermission mainPermission = null;
	
	/**
	 * 管理対象のIpPermissionを設定.
	 * 
	 * @param p
	 */
	public static final void setMainIpPermission(IpPermission p) {
		mainPermission = p;
	}
	
	/**
	 * 管理対象のIpPermissionを取得.
	 * 
	 * @return IpPermission
	 */
	public static final IpPermission getMainIpPermission() {
		return mainPermission;
	}
	
	/**
	 * コンストラクタ.
	 */
	public IpPermission() {
		permissions = new ArrayMap<String, Map<Integer, List<IpRange>>>();
	}
	
	/**
	 * コンストラクタ.
	 * @param conf コンフィグ情報を設定します.
	 */
	public IpPermission(Map<String, Object> conf) {
		load(conf);
	}
	
	/**
	 * データをロード.
	 */
	public void load() {
		load(RhiginConfig.getMainConfig().get(NetConstants.IP_PERMISSION_JSON));
	}
	
	/**
	 * ipPermission.jsonのコンフィグ情報を設定してデータをロード.
	 * @param conf コンフィグ情報を設定します.
	 */
	public void load(Map<String, Object> conf) {
		Map<String, Map<Integer, List<IpRange>>> pm;
		try {
			pm = new ArrayMap<String, Map<Integer, List<IpRange>>>();
			Iterator<Entry<String, Object>> it = conf.entrySet().iterator();
			int i, len;
			Integer headNo;
			IpRange range;
			Entry<String, Object> entry;
			List<String> list;
			Map<Integer, List<IpRange>> headList = null;
			List<IpRange> rangeList = null;
			while(it.hasNext()) {
				entry = it.next();
				if(!(entry.getValue() instanceof List)) {
					continue;
				}
				list = (List)entry.getValue();
				len = list.size();
				for(i = 0; i < len; i ++) {
					range = new IpRange(list.get(i));
					// mask付きの条件[192.168.0.0/24]のような形式でない場合.
					if((headNo = range.getHead()) == null) {
						// その他の番号で処理.
						headNo = ATHER_HEAD;
					}
					// 定義名からIpRangeのHeadリストを取得.
					headList = pm.get(entry.getKey());
					if(headList == null) {
						// 定義名が存在しない場合は新規で追加.
						headList = new ArrayMap<Integer, List<IpRange>>();
						pm.put(entry.getKey(), headList);
					} else {
						// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
						rangeList = headList.get(headNo);
					}
					// ipRangeのリストが存在しない場合は新規で追加.
					if(rangeList == null) {
						rangeList = new ObjectList<IpRange>();
						headList.put(headNo, rangeList);
					}
					// ipRangeのリストに今回のipRangeオブジェクトを追加.
					rangeList.add(range);
					headList = null;
					rangeList = null;
					range = null;
				}
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
		rwLock.writeLock().lock();
		try {
			permissions = pm;
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	// permissionsを保存.
	private final void _save() {
		// permissionsを conf/ipPermission.json 用に再変換.
		Map<String, List<String>> conf = new ArrayMap<String, List<String>>();
		List<String> confList = null;
		
		int i, len;
		IpRange range;
		List<IpRange> list;
		Entry<Integer, List<IpRange>> he;
		Iterator<Entry<Integer, List<IpRange>>> hit;
		Map<Integer, List<IpRange>> headList;
		Entry<String, Map<Integer, List<IpRange>>> e;
		
		rwLock.readLock().lock();
		try {
			Iterator<Entry<String, Map<Integer, List<IpRange>>>> it = permissions.entrySet().iterator();
			while(it.hasNext()) {
				e = it.next();
				confList = conf.get(e.getKey());
				if(confList == null) {
					confList = new ObjectList<String>();
				}
				headList = e.getValue();
				hit = headList.entrySet().iterator();
				while(hit.hasNext()) {
					he = hit.next();
					list = he.getValue();
					len = list.size();
					for(i = 0; i < len; i ++) {
						range = list.get(i);
						confList.add(range.toString());
					}
				}
				if(confList.size() > 0) {
					conf.put(e.getKey(), confList);
				}
				confList = null;
			}
		} finally {
			rwLock.readLock().unlock();
		}
		
		try {
			// データ保存.
			RhiginConfig rc = RhiginConfig.getMainConfig();
			String name = rc.getConfigDir() + "/" + NetConstants.IP_PERMISSION_JSON + ".json";
			rwLock.writeLock().lock();
			try {
				FileUtil.setFileString(true, name, JsonOut.toString(conf), "UTF8");
			} finally {
				rwLock.writeLock().unlock();
			}
			// RhiginConfigの再読込.
			rc.reload();
		} catch(RhiginException re) {
			throw re;
		} catch(Exception ex) {
			throw new RhiginException(ex);
		}
	}
	
	/**
	 * 情報を追加.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 */
	public void add(String addr) {
		add(null, addr);
	}
	
	/**
	 * 情報を追加.
	 * @param name 定義名を設定します.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 */
	public void add(String name, String addr) {
		if(addr == null || addr.isEmpty()) {
			throw new RhiginException("Not enough parameters.");
		}
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		try {
			rwLock.writeLock().lock();
			try {
				List<IpRange> rangeList = null;
				IpRange range = new IpRange(addr);
				Integer headNo = range.getHead();
				// mask付きの条件[192.168.0.0/24]でない場合.
				if((headNo = range.getHead()) == null) {
					// その他の番号で処理.
					headNo = ATHER_HEAD;
				}
				// 定義名から既に定義されているIpRangeのHeadリストを取得.
				Map<Integer, List<IpRange>> headList = permissions.get(name);
				if(headList == null) {
					// 定義名の条件が存在しない場合.
					headList = new ArrayMap<Integer, List<IpRange>>();
					permissions.put(name, headList);
				} else {
					// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
					rangeList = headList.get(headNo);
				}
				// ipRangeのリストが存在しない場合は新規で追加.
				if(rangeList == null) {
					rangeList = new ObjectList<IpRange>();
					headList.put(headNo, rangeList);
				}
				// ipRangeの追加.
				rangeList.add(range);
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(e);
			} finally {
				rwLock.writeLock().unlock();
			}
		} catch(RhiginException re) {
			// エラーが出た場合は、現在のconfを再ロードする.
			load();
			throw re;
		}
		// データ保存.
		_save();
	}
	
	/**
	 * 情報を削除.
	 */
	public boolean removeName() {
		return removeName(null);
	}
	
	/**
	 * 情報を削除.
	 * @param name 定義名を設定します.
	 */
	public boolean removeName(String name) {
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		boolean ret;
		try {
			rwLock.writeLock().lock();
			try {
				ret = permissions.containsKey(name);
				permissions.remove(name);
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(e);
			} finally {
				rwLock.writeLock().unlock();
			}
		} catch(RhiginException re) {
			// エラーが出た場合は、現在のconfを再ロードする.
			load();
			throw re;
		}
		// データ保存.
		_save();
		return ret;
	}
	
	/**
	 * 情報を削除.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 * @return boolean [true]の場合、削除できました.
	 */
	public boolean remove(String addr) {
		return remove(null, addr);
	}
	
	/**
	 * 情報を削除.
	 * @param name 定義名を設定します.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 * @return boolean [true]の場合、削除できました.
	 */
	public boolean remove(String name, String addr) {
		if(addr == null || addr.isEmpty()) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		boolean ret = false;
		try {
			rwLock.writeLock().lock();
			try {
				Integer headNo;
				IpRange range = new IpRange(addr);
				// 定義名から既に定義されているIpRangeのHeadリストを取得.
				Map<Integer, List<IpRange>> headList = permissions.get(name);
				if(headList == null) {
					return false;
				}
				// mask付きの条件[192.168.0.0/24]でない場合.
				if((headNo = range.getHead()) == null) {
					// その他の番号で処理.
					headNo = ATHER_HEAD;
				}
				// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
				List<IpRange> rangeList = headList.get(headNo);
				if(rangeList == null) {
					return false;
				}
				// 削除処理.
				int len = rangeList.size();
				for(int i = len - 1; i >= 0; i --) {
					if(rangeList.get(i).equals(range)) {
						ret = true;
						rangeList.remove(i);
					}
				}
				// ipRangeのリストが空の場合は削除.
				if(rangeList.size() == 0) {
					headList.remove(headNo);
					// IpRangeのHeadリストが空の場合は削除.
					if(headList.size() == 0) {
						permissions.remove(name);
					}
				}
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(e);
			} finally {
				rwLock.writeLock().unlock();
			}
		} catch(RhiginException re) {
			// エラーが出た場合は、現在のconfを再ロードする.
			load();
			throw re;
		}
		// データ保存.
		_save();
		return ret;
	}
	
	// headNoを取得.
	private static final Integer getHead(String addr) {
		int p = addr.indexOf(".");
		if(p == -1) {
			return null;
		}
		String n = addr.substring(0, p);
		if(Converter.isNumeric(n)) {
			int ret = Converter.parseInt(n);
			if((ret & 0xffffff00) == 0) {
				return ret;
			}
		}
		return null;
	}
	
	// headNoを取得.
	private static final Integer getHead(InetAddress addr) {
		return addr.getAddress()[0] & 0x000000ff;
	}
	
	/**
	 * 指定アドレス or ドメインが許可されているかチェック.
	 * @param addr IP(V4)アドレス or ドメイン名を設定します.
	 * @return boolean [true]の場合、一致しています.
	 */
	public boolean isPermission(String addr) {
		return isPermission(null, addr);
	}
	
	/**
	 * 指定アドレス or ドメインが許可されているかチェック.
	 * @param name 定義名を設定します.
	 * @param addr IP(V4)アドレス or ドメイン名を設定します.
	 * @return boolean [true]の場合、許可されています.
	 */
	public boolean isPermission(String name, String addr) {
		if(addr == null || addr.isEmpty()) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		rwLock.readLock().lock();
		try {
			Integer headNo;
			// 定義名から既に定義されているIpRangeのHeadリストを取得.
			Map<Integer, List<IpRange>> headList = permissions.get(name);
			if(headList == null) {
				return false;
			}
			// IPV4の文字列で設定されている場合.
			if((headNo = getHead(addr)) == null) {
				// その他の番号で処理.
				headNo = ATHER_HEAD;
			}
			while(true) {
				// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
				List<IpRange> rangeList = headList.get(headNo);
				if(rangeList != null) {
					int len = rangeList.size();
					for(int i = 0; i < len; i ++) {
						if(rangeList.get(i).isRange(addr)) {
							return true;
						}
					}
				}
				// その他の番号でない場合は、その他の番号で再検索.
				if(headNo != ATHER_HEAD) {
					headNo = ATHER_HEAD;
				} else {
					return false;
				}
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/**
	 * 指定アドレス or ドメインが許可されているかチェック.
	 * @param addr InetAddressを設定します.
	 * @return boolean [true]の場合、一致しています.
	 */
	public boolean isPermission(InetAddress addr) {
		return isPermission(null, addr);
	}
	
	/**
	 * 指定アドレス or ドメインが許可されているかチェック.
	 * @param name 定義名を設定します.
	 * @param addr IP(V4)アドレス or ドメイン名を設定します.
	 * @return boolean [true]の場合、許可されています.
	 */
	public boolean isPermission(String name, InetAddress addr) {
		if(addr == null) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		rwLock.readLock().lock();
		try {
			Integer headNo;
			// 定義名から既に定義されているIpRangeのHeadリストを取得.
			Map<Integer, List<IpRange>> headList = permissions.get(name);
			if(headList == null) {
				return false;
			}
			// IPV4のInetAddressで設定されている場合.
			if((headNo = getHead(addr)) == null) {
				// その他の番号で処理.
				headNo = ATHER_HEAD;
			}
			while(true) {
				// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
				List<IpRange> rangeList = headList.get(headNo);
				if(rangeList != null) {
					// 削除処理.
					int len = rangeList.size();
					for(int i = 0; i < len; i ++) {
						if(rangeList.get(i).isRange(addr)) {
							return true;
						}
					}
				}
				// その他の番号でない場合は、その他の番号で再検索.
				if(headNo != ATHER_HEAD) {
					headNo = ATHER_HEAD;
				} else {
					return false;
				}
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/**
	 * 定義名が存在するかチェック.
	 * @return [true]の場合、存在します.
	 */
	public boolean isName() {
		return isName(null);
	}
	
	/**
	 * 定義名が存在するかチェック.
	 * @param name 定義名を設定します.
	 * @return [true]の場合、存在します.
	 */
	public boolean isName(String name) {
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		boolean ret = false;
		rwLock.readLock().lock();
		try {
			if(permissions.get(name) != null) {
				ret = true;
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return ret;
	}
	
	/**
	 * IpRangeが登録されているかチェック.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 * @return boolean [true]の場合、存在します.
	 */
	public boolean isIpRange(String addr) {
		return isIpRange(null, addr);
	}
	/**
	 * IpRangeが登録されているかチェック.
	 * @param name 定義名を設定します.
	 * @param addr ipRangeで設定可能な条件を設定します.
	 * @return boolean [true]の場合、存在します.
	 */
	public boolean isIpRange(String name, String addr) {
		if(addr == null || addr.isEmpty()) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		}
		rwLock.readLock().lock();
		try {
			Integer headNo;
			IpRange range = new IpRange(addr);
			// 定義名から既に定義されているIpRangeのHeadリストを取得.
			Map<Integer, List<IpRange>> headList = permissions.get(name);
			if(headList == null) {
				return false;
			}
			// mask付きの条件[192.168.0.0/24]でない場合.
			if((headNo = range.getHead()) == null) {
				// その他の番号で処理.
				headNo = ATHER_HEAD;
			}
			// 存在する場合はそのheadNoに対するIpRangeのリストを取得.
			List<IpRange> rangeList = headList.get(headNo);
			if(rangeList == null) {
				return false;
			}
			// 存在チェック.
			int len = rangeList.size();
			for(int i = len - 1; i >= 0; i --) {
				if(rangeList.get(i).equals(range)) {
					return true;
				}
			}
			return false;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/**
	 * 定義名一覧を取得.
	 * @return List<String> 定義名一覧が返却されます.
	 */
	public List<String> getNames() {
		List<String> ret = new ObjectList<String>();
		rwLock.readLock().lock();
		try {
			Iterator<String> it = permissions.keySet().iterator();
			while(it.hasNext()) {
				ret.add(it.next());
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return ret;
	}
	
	/**
	 * IpRange情報一覧を取得します.
	 * @param name 定義名を設定します.
	 * @return List<String> IpRange情報が返却されます.
	 */
	public List<String> getIpRanges(String name) {
		List<String> ret = new ObjectList<String>();
		rwLock.readLock().lock();
		try {
//			if(name == null || name.isEmpty()) {
//				Iterator<String> it = permissions.keySet().iterator();
//				while(it.hasNext()) {
//					_getIpRange(ret, it.next());
//				}
//			} else {
//				_getIpRange(ret, name);
//			}
			_getIpRange(ret, name);
		} finally {
			rwLock.readLock().unlock();
		}
		return ret;
		
	}
	
	// 指定名のIpRange情報一覧を取得.
	private final void _getIpRange(List<String> out, String name) {
		Map<Integer, List<IpRange>> headList = permissions.get(name);
		if(headList == null) {
			return;
		}
		int i, len;
		List<IpRange> list;
		Iterator<Entry<Integer, List<IpRange>>> it = headList.entrySet().iterator();
		while(it.hasNext()) {
			list = it.next().getValue();
			len = list == null ? 0 : list.size();
			for(i = 0; i < len; i ++) {
				out.add(list.get(i).toString());
			}
		}
	}

}

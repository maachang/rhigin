package rhigin.keys;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rhigin.RhiginException;
import rhigin.util.ArrayMap;
import rhigin.util.FileUtil;
import rhigin.util.Flag;

/**
 * 標準のRhiginAccessKey管理.
 */
public class DefaultRhiginAccessKey implements RhiginAccessKey {
	private static final String DEF_FILE_NAME = "./.rhiginAccessKey";
	
	private static final int BODY_LENGTH = RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64
		+ RhiginAccessKeyConstants.SAVE_CODE_LENGTH;
	
	private static final byte CREATE_CODE = (byte)1;
	private static final byte DELETE_CODE = (byte)2;
	
	private String name = DEF_FILE_NAME;
	private final Flag loadFlag = new Flag(false);
	private Map<String, String> memoryMan;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public DefaultRhiginAccessKey() {
	}
	
	/**
	 * コンストラクタ.
	 * @param name ファイル名を設定します.
	 */
	public DefaultRhiginAccessKey(String name) {
		this.name = name;
	}
	
	@Override
	public String[] create() {
		load();
		rwLock.writeLock().lock();
		try {
			while(true) {
				String[] ret = RhiginAccessKeyUtil.create();
				if(memoryMan.containsKey(ret[0])) {
					continue;
				}
				String saveCode = RhiginAccessKeyByFCipher.convertSaveCode(ret[1]);
				byte[] body = new byte[BODY_LENGTH + 1];
				body[0] = CREATE_CODE;
				byte[] n = ret[0].getBytes("UTF8");
				System.arraycopy(n, 0, body, 1, RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64);
				n = saveCode.getBytes("UTF8");
				System.arraycopy(n, 0, body, 1 + RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64,
						RhiginAccessKeyConstants.SAVE_CODE_LENGTH);
				add(body);
				memoryMan.put(ret[0], saveCode);
				return ret;
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	@Override
	public boolean contains(String key) {
		if(key == null || key.length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			return false;
		}
		load();
		rwLock.readLock().lock();
		try {
			return memoryMan.get(key) != null;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	@Override
	public boolean delete(String key) {
		if(key == null || key.length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			return false;
		}
		load();
		rwLock.writeLock().lock();
		try {
			if(memoryMan.get(key) == null) {
				return false;
			}
			byte[] body = new byte[RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64 + 1];
			body[0] = DELETE_CODE;
			byte[] n = key.getBytes("UTF8");
			System.arraycopy(n, 0, body, 1, RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64);
			add(body);
			memoryMan.put(key, null);
			return true;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	@Override
	public byte[] createFCipher(String key) {
		if(key == null || key.length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			return null;
		}
		load();
		rwLock.readLock().lock();
		try {
			String saveCode = memoryMan.get(key);
			if(saveCode == null) {
				return null;
			}
			return RhiginAccessKeyByFCipher.createBySaveCode(key, saveCode);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	// データロード.
	private void load() {
		if(!loadFlag.setToGetBefore(true)) {
			synchronized(this) {
				memoryMan = load(name);
			}
		}
	}
	
	// データ追加.
	private void add(byte[] b) {
		synchronized(this) {
			add(name, b);
		}
	}
	
	// データロード.
	private static final Map<String, String> load(String name) {
		if(!FileUtil.isFile(name)) {
			return new ArrayMap<String, String>();
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(name);
			Map<String, String> ret = load(in);
			in.close();
			in = null;
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// データロード.
	private static final Map<String, String> load(InputStream inputStream) {
		InputStream in = null;
		try {
			Map<String, String> ret = new ArrayMap<String, String>();
			in = new BufferedInputStream(inputStream);
			String a, b;
			byte[] head = new byte[1];
			byte[] del = new byte[RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64];
			byte[] body = new byte[BODY_LENGTH];
			while(in.read(head) != -1) {
				switch(head[0] & 0x000000ff) {
				case CREATE_CODE:
					if(in.read(body) != BODY_LENGTH) {
						break;
					}
					a = new String(body, 0, RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64, "UTF8");
					b = new String(body, RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64,
							RhiginAccessKeyConstants.SAVE_CODE_LENGTH, "UTF8");
					ret.put(a, b);
					continue;
				case DELETE_CODE:
					if(in.read(del) != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
						break;
					}
					a = new String(del, 0, RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64, "UTF8");
					ret.put(a, null);
					continue;
				}
				throw new RhiginException("Failed to load data.");
			}
			in.close();
			in = null;
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 情報の追加.
	private static final void add(String name, byte[] b) {
		OutputStream out = null;
		try {
			out = (FileUtil.isFile(name)) ?
				new FileOutputStream(name, true) :
				new FileOutputStream(name);
			add(out, b);
			out.close();
			out = null;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 情報の追加.
	private static final void add(OutputStream out, byte[] b) {
		try {
			out.write(b);
			out.close();
			out = null;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
		}
	}
}

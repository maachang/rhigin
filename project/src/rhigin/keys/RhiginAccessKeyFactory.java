package rhigin.keys;

import rhigin.util.Flag;

/**
 * RhiginAccessKeyFactory.
 */
public class RhiginAccessKeyFactory {
	private RhiginAccessKeyFactory() {}
	
	private static final RhiginAccessKeyFactory SNGL = new RhiginAccessKeyFactory();
	
	/**
	 * オブジェクトの取得.
	 * @return
	 */
	public static final RhiginAccessKeyFactory getInstance() {
		return SNGL;
	}
	
	private final Flag newFlag = new Flag(false);
	private RhiginAccessKey accessKey = null;
	
	/**
	 * アクセスキーを取得.
	 * @return RhiginAccessKey アクセスキー管理オブジェクトが返却されます.
	 */
	public RhiginAccessKey get() {
		if(newFlag.get() == false) {
			synchronized(this) {
				if(newFlag.get() == false) {
					RhiginAccessKey ak = new DefaultRhiginAccessKey();
					accessKey = ak;
					newFlag.set(true);
				}
			}
		}
		return accessKey;
	}
}

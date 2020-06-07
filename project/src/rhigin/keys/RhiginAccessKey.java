package rhigin.keys;

/**
 * RhiginAccess用のアクセスキー、認証コードを生成、管理.
 */
public interface RhiginAccessKey {
	/**
	 * アクセスキーを生成.
	 * @return String[] [0]アクセスキー [1]認証コードが生成されます.
	 */
	public String[] create();
	
	/**
	 * アクセスキーを削除.
	 * @param key アクセスキーを設定します.
	 * @return boolean [true]の場合、削除されました。
	 */
	public boolean delete(String key);
	
	/**
	 * アクセスキーが存在するかチェック.
	 * @param key アクセスキーを設定します.
	 * @return boolean [true]の場合、存在します.
	 */
	public boolean contains(String key);
	
	/**
	 * アクセスキーに対する FCipher の publicKey を取得.
	 * @param key アクセスキーを設定します.
	 * @return byte[] FCipher の publicKey が返却されます.
	 */
	public byte[] createFCipher(String key);
}

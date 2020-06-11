package rhigin.keys;

public class RhiginAccessKeyConstants {
	private RhiginAccessKeyConstants() {}
	
	/** ACCESS-KEY用のHTTPヘッダ名. **/
	public static final String RHIGIN_ACCESSKEY_HTTP_HEADER = "X-Rhigin-Accesskey";
	
	/** アクセスキーの生成データ長. **/
	public static final int ACCESS_KEY_LENGTH = 81;
	
	/** 認証コードの生成データ長. **/
	public static final int AUTH_CODE_LENGTH = 95;
	
	/** アクセスキーの６４進数変換後の文字数. **/
	public static final int ACCESS_KEY_LENGTH_64 = 45;
	
	/** 認証コードの６４進数変換後の文字数 **/
	public static final int AUTH_CODE_LENGTH_64 = 53;
	
	/** 保管コードの文字数. **/
	public static final int SAVE_CODE_LENGTH = 28;
}

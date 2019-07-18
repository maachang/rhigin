package rhigin.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import rhigin.RhiginException;
import rhigin.scripts.Json;

/**
 * 単純なJwtオブジェクト.
 * (HS256のみ対応)
 */
public class Jwt {
	protected Jwt() {}
	
	// アルゴリズム.
	protected static final String ALG = "HmacSHA256";
	
	// ヘッダ.
	protected static final String JWT_HEADER;
	static {
		String s = null;
		try {
			s = Base64.encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes("UTF8"));
		} catch(Exception e) {}
		JWT_HEADER = s;
	}
	
	// ＝を削除.
	protected static final String cutEq(String n) {
		final int p = n.indexOf("=");
		return (p != -1) ? n.substring(0, p) : n;
	}
	
	// シグニチャを生成.
	protected static final String signature(byte[] key, byte[] payload) {
		try {
			final SecretKeySpec sk = new SecretKeySpec(key, ALG);
			final Mac mac = Mac.getInstance(ALG);
			mac.init(sk);
			return cutEq(Base64.encode(mac.doFinal(payload)));
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
	
	/**
	 * JWTを生成.
	 * @param key キー情報を設定します.
	 * @param payload payloadを設定します.
	 * @return　
	 */
	public static final String create(String key, Object payload) {
		try {
			String value = new StringBuilder(JWT_HEADER).append(".")
				.append(Base64.encode((payload instanceof String ? (String)payload : Json.encode(payload)).getBytes("UTF8")))
				.toString();
			return new StringBuilder(value)
				.append(".").append(signature(key.getBytes("UTF8"), value.getBytes("UTF8")))
				.toString();
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
	
	/**
	 * ペイロードを取得.
	 * @param jwt jwt文字列を設定します.
	 * @return Object
	 */
	public static final Object payload(String jwt) {
		int p = jwt.indexOf(".");
		if(p == -1) {
			return null;
		}
		int pp = jwt.indexOf(".", p + 1);
		if(pp == -1) {
			return null;
		}
		try {
			return Json.decode(new String(Base64.decode(jwt.substring(p+1, pp)), "UTF8"));
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
	
	/**
	 * jwtの内容が正しいかチェック.
	 * @param key キー情報を設定します.
	 * @param jwt jwt文字列を設定します.
	 * @return boolean
	 */
	public static final boolean validate(String key, String jwt) {
		int p = jwt.lastIndexOf(".");
		if(p == -1) {
			return false;
		}
		try {
			return jwt.substring(p + 1).equals(signature(key.getBytes("UTF8"), jwt.substring(0, p).getBytes("UTF8")));
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
}

package rhigin.keys;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import rhigin.RhiginException;
import rhigin.scripts.function.RandomFunction;
import rhigin.util.ByteArrayIO;
import rhigin.util.FCipher;

/**
 * RhiginAccessKeyでのFCipher処理.
 */
public final class RhiginAccessKeyByFCipher {
	private RhiginAccessKeyByFCipher() {}
	
	// AuthCodeをSHA-1で変換.
	private static final byte[] _convertAuthCode(String authCode) {
		if(authCode == null || authCode.length() != RhiginAccessKeyConstants.AUTH_CODE_LENGTH_64) {
			throw new RhiginException(401, "No valid auth code exists.");
		}
		try {
			return MessageDigest.getInstance("SHA-1").digest(authCode.getBytes("UTF8"));
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * 認証コードを管理コード変換.
	 * @param authCode 認証コードを設定します.
	 * @return String 管理コードが返却されます.
	 */
	public static final String convertSaveCode(String authCode) {
		return FCipher.cb64_enc(_convertAuthCode(authCode));
	}
	
	/**
	 * publicKeyを生成.
	 * @param accessKey アクセスキーを設定します.
	 * @param authCode 認証コードを設定します.
	 * @return byte[] FCipher用のPublicKeyが返却されます.
	 */
	public static final byte[] createByAuthCode(String accessKey, String authCode) {
		return create(accessKey, _convertAuthCode(authCode));
	}
	
	/**
	 * publicKeyを生成.
	 * @param accessKey アクセスキーを設定します.
	 * @param saveCode 管理コードを設定します.
	 * @return byte[] FCipher用のPublicKeyが返却されます.
	 */
	public static final byte[] createBySaveCode(String accessKey, String saveCode) {
		return create(accessKey, FCipher.cb64_dec(saveCode));
	}
	
	/**
	 * publicKeyを生成.
	 * @param accessKey アクセスキーを設定します.
	 * @param code 認証コードをSHA1で変換されたバイナリを設定します.
	 * @return byte[] FCipher用のPublicKeyが返却されます.
	 */
	public static final byte[] create(String accessKey, byte[] code) {
		byte[] a = FCipher.hash(code);
		byte[] b = FCipher.hash(accessKey);
		int cnt = 0;
		byte[] ret = new byte[32];
		for(int i = 0; i < 16; i ++) {
			ret[cnt ++] = a[i];
			ret[cnt ++] = b[i];
		}
		return ret;
	}
	
	/**
	 * AccessKeyとAuthCodeでエンコード.
	 * @param accessKey 対象のAccessKeyを設定します.
	 * @param authCode 対象のAuthCodeを設定します.
	 * @param body エンコード対象のBodyを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] encode(String accessKey, String authCode, byte[] body) {
		if(accessKey == null ||
			!(accessKey instanceof String) ||
			((String)accessKey).length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		byte[] pkey = createByAuthCode(accessKey, authCode);
		if(pkey == null) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// 暗号を行う.
		FCipher fc = new FCipher(RandomFunction.get());
		// GZIP圧縮して暗号化.
		return fc.benc_b(gzip(body), pkey);
	}
	
	/**
	 * AccessKeyとAuthCodeでデコード.
	 * @param accessKey 対象のAccessKeyを設定します.
	 * @param authCode 対象のAuthCodeを設定します.
	 * @param body デコード対象のBodyを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] decode(String accessKey, String authCode, byte[] body) {
		if(accessKey == null ||
			!(accessKey instanceof String) ||
			((String)accessKey).length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		byte[] pkey = createByAuthCode(accessKey, authCode);
		if(pkey == null) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// 暗号の解析を行う.
		FCipher fc = new FCipher(RandomFunction.get());
		// 暗号解析して、GZIP解凍して変薬.
		try {
			body = fc.bdec_b(body, pkey);
		} catch(Exception e) {
			// 暗号の解析に失敗した場合は「エラー」は不正アクセスとして扱う.
			throw new RhiginException(401, "invalid access.");
		}
		return unzip(body);
	}
	/**
	 * AccessKeyでBodyをエンコード.
	 * @param accessKey RhiginAccessKeyFactoryで管理されているAccessKeyを設定します.
	 * @param body エンコード対象のBodyを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] encode(String accessKey, byte[] body) {
		if(accessKey == null ||
			!(accessKey instanceof String) ||
			((String)accessKey).length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// RhiginAccessKey管理オブジェクトを取得.
		RhiginAccessKey ak = RhiginAccessKeyFactory.getInstance().get();
		byte[] pkey = ak.createFCipher((String)accessKey);
		if(pkey == null) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// 暗号を行う.
		FCipher fc = new FCipher(RandomFunction.get());
		// GZIP圧縮して暗号化.
		return fc.benc_b(gzip(body), pkey);
	}
	
	/**
	 * AccessKeyでBodyをデコード.
	 * @param accessKey RhiginAccessKeyFactoryで管理されているAccessKeyを設定します.
	 * @param body デコード対象のBodyを設定します.
	 * @return byte[] 変換されたバイナリが返却されます.
	 */
	public static final byte[] decode(String accessKey, byte[] body) {
		if(accessKey == null ||
			!(accessKey instanceof String) ||
			((String)accessKey).length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// RhiginAccessKey管理オブジェクトを取得.
		RhiginAccessKey ak = RhiginAccessKeyFactory.getInstance().get();
		byte[] pkey = ak.createFCipher((String)accessKey);
		if(pkey == null) {
			throw new RhiginException(401, "No valid access key exists.");
		}
		// 暗号の解析を行う.
		FCipher fc = new FCipher(RandomFunction.get());
		try {
			body = fc.bdec_b(body, pkey);
		} catch(Exception e) {
			// 暗号の解析に失敗した場合は「エラー」は不正アクセスとして扱う.
			throw new RhiginException(401, "invalid access.");
		}
		// 暗号解析して、GZIP解凍して変薬.
		return unzip(body);
	}
	
	// gzip圧縮.
	private static final byte[] gzip(byte[] o) {
		GZIPOutputStream out = null;
		try {
			ByteArrayIO bo = new ByteArrayIO();
			out = new GZIPOutputStream(bo);
			out.write(o);
			out.flush();
			out.finish();
			out.close();
			out = null;
			return bo.toByteArray();
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
	
	// unzip.
	private static final byte[] unzip(byte[] o) {
		GZIPInputStream in = null;
		try {
			byte[] b = new byte[1024];
			in = new GZIPInputStream(new ByteArrayInputStream(o));
			ByteArrayIO out = new ByteArrayIO(1024);
			int len;
			while((len = in.read(b)) != -1) {
				out.write(b, 0, len);
			}
			in.close();
			in = null;
			b = null;
			b = out.toByteArray();
			out.close();
			return b;
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
}

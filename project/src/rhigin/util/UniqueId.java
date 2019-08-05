package rhigin.util;

import java.math.BigInteger;

/**
 * ユニークID生成処理.
 */
public class UniqueId {
	private static final String _CODE64 = "abcdefghijklmnopqrstuvwxyz+ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789";
	private static final BigInteger _BI64 = new BigInteger("64");
	private static final BigInteger[] _L64;
	static {
		BigInteger[] bl = new BigInteger[64];
		for(int i = 0; i < 64; i ++) {
			bl[i] = new BigInteger("" + i);
		}
		_L64 = bl;
	}
	
	private RandomUUID uuid;
	
	protected UniqueId() {
	}
	
	/**
	 * コンストラクタ.
	 * @param uuid
	 */
	public UniqueId(RandomUUID uuid) {
		this.uuid = uuid;
	}
	
	// code64変換.
	private static final String _code64(String noCode) {
		BigInteger no = new BigInteger(noCode);
		BigInteger[] m;
		String n = _CODE64;
		StringBuilder ret = new StringBuilder();
		while(no.compareTo(_BI64) >= 0 ) {
			m = no.divideAndRemainder(_BI64);
			ret.append(n.charAt(m[1].intValue()));
			no = m[0];
		}
		ret.append(n.charAt(no.intValue()));
		return ret.toString();
	}
	
	// code64を元に戻す.
	private static final String _decode64(String code) {
		BigInteger ret = new BigInteger("0");
		int len = code.length();
		String n = _CODE64;
		for(int i = len-1 ; i >= 0; i --) {
			ret = ret.multiply(_BI64);
			ret = ret.add(_L64[n.indexOf(code.charAt(i))]);
		}
		return ret.toString(10);
	}
	
	// id生成.
	private final void _nextNumber(StringBuilder buf) {
		final int[] code = uuid.getId(1).getInt4();
		final int len = code.length;
		for(int i = 0; i < len; i ++) {
			buf.append(code[i]);
		}
	}
	
	// 指定文字数のIDを生成します.
	private final String _getNumberId(int size) {
		if(size <= 0) {
			size = 10;
		}
		StringBuilder buf = new StringBuilder(size + 50);
		while(buf.length() < size) {
			_nextNumber(buf);
		}
		return buf.substring(0, size);
	}
	
	/**
	 * UUIDを取得.
	 * @return
	 */
	public String getUUID() {
		return uuid.getId(1).getUUID();
	}
	
	/**
	 * 10進数のIDを生成.
	 * @param size
	 * @return
	 */
	public String get(int size) {
		return _getNumberId(size);
	}
	
	/**
	 * 10進数のIDを64進数変換して取得.
	 * @param size
	 * @return
	 */
	public String get64(int size) {
		return _code64(_getNumberId(size));
	}
	
	/**
	 * 10進数のIDを64進数に変換.
	 * @param id
	 * @return
	 */
	public String code64(String id) {
		return _code64(id);
	}
	
	/**
	 * 64進数を10進数のIDに変換.
	 * @param code
	 * @return
	 */
	public String decode64(String code) {
		return _decode64(code);
	}
}

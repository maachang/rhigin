package rhigin.util;

/**
 * FCompBuffer.
 */
public class FCompBuffer {
	private byte[] data;
	private int length;

	/**
	 * コンストラクタ.
	 */
	public FCompBuffer() {
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param capacity
	 */
	public FCompBuffer(int capacity) {
		data = new byte[capacity];
	}

	/**
	 * オブジェクトリセット.
	 * 
	 * @param length
	 *            リセットするバイナリ長を設定します.
	 */
	public void reset(int length) {
		if (data == null || data.length != length) {
			data = new byte[length];
		}
	}

	/**
	 * オブジェクト再利用.
	 * 
	 * @param length
	 *            再利用時のバイナリ長を設定します.
	 */
	public void clear(int length) {
		if (data == null || data.length < length) {
			data = new byte[length];
		}
	}

	/**
	 * データ取得.
	 * 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * データ長取得.
	 * 
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/**
	 * データ長設定.
	 * 
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * 現在のバイナリデータを取得.
	 * 
	 * @return
	 */
	public byte[] toByteArray() {
		byte[] res = new byte[length];
		System.arraycopy(data, 0, res, 0, length);
		return res;
	}
}

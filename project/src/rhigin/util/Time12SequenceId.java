package rhigin.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Time 12 byte(96bit) シーケンスID発行処理.
 */
public class Time12SequenceId {
	public static final int ID_LENGTH = 12;
	private final AtomicInteger nowId = new AtomicInteger(0);
	private final AtomicLong nowTime = new AtomicLong(-1L);
	private int machineId = 0;

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します.
	 */
	public Time12SequenceId(int id) {
		machineId = id;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します.
	 * @param lastTime
	 *            設定した最終時間を設定します.
	 * @param lastId
	 *            設定した最終IDを設定します.
	 */
	public Time12SequenceId(int id, long lastTime, int lastId) {
		nowTime.set(lastTime);
		machineId = id;
		nowId.set(lastId);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param binary
	 *            対象のバイナリを設定します.
	 */
	public Time12SequenceId(byte[] binary) {
		set(binary);
	}

	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @param buf
	 *            対象のバッファを設定します.
	 */
	public final void get(byte[] buf) {
		createId(buf, machineId, nowTime.get(), nowId.get());
	}

	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @return byte[] シーケンスIDが発行されます.
	 */
	public final byte[] get() {
		final byte[] ret = new byte[12];
		get(ret);
		return ret;
	}

	/**
	 * シーケンスIDを設定.
	 * 
	 * @param binary
	 *            対象のバイナリを設定します.
	 */
	public final void set(byte[] binary) {
		setBinary(binary);
	}

	/**
	 * マシンIDを取得.
	 * 
	 * @return int 設定されているマシンIDが返却されます.
	 */
	public final int getMachineId() {
		return (int) machineId;
	}

	/**
	 * シーケンスIDを発行.
	 * 
	 * @return byte[] シーケンスIDが発行されます.
	 */
	public final byte[] next() {
		final byte[] ret = new byte[12];
		next(ret);
		return ret;
	}

	/**
	 * シーケンスIDを発行.
	 * 
	 * @param buf
	 *            対象のバッファを設定します.
	 */
	public final void next(byte[] out) {
		int id;
		long beforeTime, time;
		while (true) {
			id = nowId.get();
			beforeTime = nowTime.get();
			time = System.currentTimeMillis();

			// システム時間が変更された場合.
			if (time != beforeTime) {
				if (nowTime.compareAndSet(beforeTime, time) && nowId.compareAndSet(id, 0)) {
					createId(out, machineId, time, 0);
					break;
				}
				// シーケンスIDが一定を超える場合.
			} else if (id + 1 > 0x0000ffff) {
				if (nowTime.compareAndSet(beforeTime, beforeTime + 1L) && nowId.compareAndSet(id, 0)) {
					createId(out, machineId, beforeTime + 1L, 0);
					break;
				}
				// シーケンスIDをセット.
			} else if (nowId.compareAndSet(id, id + 1)) {
				createId(out, machineId, beforeTime, id + 1);
				break;
			}
		}
	}

	/**
	 * バイナリ変換.
	 * 
	 * @param machineId
	 * @param time
	 * @param seqId
	 * @return
	 */
	public static final byte[] createId(final int machineId, final long time, final int seqId) {
		byte[] ret = new byte[12];
		createId(ret, machineId, time, seqId);
		return ret;
	}

	/**
	 * バイナリ変換.
	 * 
	 * @param out
	 * @param machineId
	 * @param time
	 * @param seqId
	 */
	public static final void createId(final byte[] out, final int machineId, final long time, final int seqId) {
		out[0] = (byte) ((time & 0xff00000000000000L) >> 56L);
		out[1] = (byte) ((time & 0x00ff000000000000L) >> 48L);
		out[2] = (byte) ((time & 0x0000ff0000000000L) >> 40L);
		out[3] = (byte) ((time & 0x000000ff00000000L) >> 32L);
		out[4] = (byte) ((time & 0x00000000ff000000L) >> 24L);
		out[5] = (byte) ((time & 0x0000000000ff0000L) >> 16L);
		out[6] = (byte) ((time & 0x000000000000ff00L) >> 8L);
		out[7] = (byte) ((time & 0x00000000000000ffL) >> 0L);
		out[8] = (byte) ((seqId & 0x0000ff00) >> 8);
		out[9] = (byte) ((seqId & 0x000000ff) >> 0);
		out[10] = (byte) ((machineId & 0x0000ff00) >> 8);
		out[11] = (byte) ((machineId & 0x000000ff) >> 0);
	}

	// バイナリから、データ変換.
	private final void setBinary(final byte[] value) {
		nowId.set(getSequenceId(value));
		nowTime.set(getTime(value));
		machineId = getMachineId(value);
	}

	public final String toString() {
		return toString(get());
	}

	/**
	 * binaryから、時間を取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final long getTime(final byte[] value) {
		return (((long) value[0] & 0x00000000000000ffL) << 56L) | (((long) value[1] & 0x00000000000000ffL) << 48L)
				| (((long) value[2] & 0x00000000000000ffL) << 40L) | (((long) value[3] & 0x00000000000000ffL) << 32L)
				| (((long) value[4] & 0x00000000000000ffL) << 24L) | (((long) value[5] & 0x00000000000000ffL) << 16L)
				| (((long) value[6] & 0x00000000000000ffL) << 8L) | (((long) value[7] & 0x00000000000000ffL) << 0L);
	}

	/**
	 * binaryからシーケンスIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getSequenceId(final byte[] value) {
		return (((int) value[8] & 0x000000ff) << 8) | (((int) value[9] & 0x000000ff) << 0);
	}

	/**
	 * binaryからマシンIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getMachineId(final byte[] value) {
		return (((int) value[10] & 0x000000ff) << 8) | (((int) value[11] & 0x000000ff) << 0);
	}

	/**
	 * マシンIDとシーケンスIDを０に設定.
	 * 
	 * @param value
	 */
	public static final void first(final byte[] value) {
		value[8] = 0;
		value[9] = 0;
		value[10] = 0;
		value[11] = 0;
	}

	/**
	 * バイナリを16進数文字列に変換.
	 * 
	 * @param b
	 * @return String 16文字のBase64変換された内容が返却されます.
	 */
	public static final String toString(byte[] b) {
		// base64で処理する.
		return Base64.encode(b);
	}

	/**
	 * 16進数文字列をバイナリに変換.
	 * 
	 * @param s
	 * @return
	 */
	public static final byte[] toBinary(String s) {
		byte[] ret = new byte[12];
		toBinary(ret, s);
		return ret;
	}

	/**
	 * 16進数文字列をバイナリに変換.
	 * 
	 * @param o
	 * @param s
	 */
	public static final void toBinary(byte[] o, String s) {
		// base64で処理する.
		Base64.decode(o, 0, s);
	}
}

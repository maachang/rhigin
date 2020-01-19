package rhigin.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Time 20 byte(160bit) シーケンスID発行処理.
 */
public class Time20SequenceId {
	public static final int ID_LENGTH = 20;
	private final AtomicInteger nowId = new AtomicInteger(0);
	private final AtomicLong nowTime = new AtomicLong(-1L);
	private final AtomicLong nowNano = new AtomicLong(-1L);
	private int machineId = 0;

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します.
	 */
	public Time20SequenceId(int id) {
		machineId = id;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します.
	 * @param lastTime
	 *            設定した最終時間を設定します.
	 * @param lastNano
	 *            設定した最終ナノタイムを設定します.
	 * @param lastId
	 *            設定した最終IDを設定します.
	 */
	public Time20SequenceId(int id, long lastTime, long lastNano, int lastId) {
		nowTime.set(lastTime);
		nowNano.set(lastNano);
		machineId = id;
		nowId.set(lastId);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param binary
	 *            対象のバイナリを設定します.
	 */
	public Time20SequenceId(byte[] binary) {
		set(binary);
	}

	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @return byte[] シーケンスIDが発行されます.
	 */
	public final byte[] get() {
		return get();
	}
	
	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @param fooder 追加するバイナリを設定します.
	 * @return byte[] シーケンスIDが発行されます.
	 */
	public final byte[] get(byte[] fooder) {
		final byte[] ret = new byte[20 + (fooder == null ? 0 : fooder.length)];
		_get(ret, fooder);
		return ret;
	}
	
	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @param out シーケンスIDを受け取るバイナリを設定します.
	 */
	protected final void _get(byte[] out, byte[] fooder) {
		_createId(out, machineId, nowTime.get(), nowNano.get(), nowId.get(), fooder);
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
		return next(null);
	}

	/**
	 * シーケンスIDを発行.
	 * 
	 * @param fooder 追加するバイナリを設定します.
	 * @return byte[] シーケンスIDが発行されます.
	 */
	public final byte[] next(byte[] fooder) {
		final byte[] ret = new byte[20 + (fooder == null ? 0 : fooder.length)];
		_next(ret, fooder);
		return ret;
	}

	/**
	 * シーケンスIDを発行.
	 * 
	 * @param out シーケンスIDを受け取るバイナリを設定します.
	 */
	protected final void _next(byte[] out, byte[] fooder) {
		int id;
		long beforeTime, time;
		long beforeNano, nano;
		while (true) {
			id = nowId.get();
			beforeTime = nowTime.get();
			time = System.currentTimeMillis();
			beforeNano = nowNano.get();
			nano = System.nanoTime();
			// システム時間が変更された場合.
			if (time != beforeTime) {
				if (nowTime.compareAndSet(beforeTime, time) && nowNano.compareAndSet(beforeNano, nano) && nowId.compareAndSet(id, 0)) {
					_createId(out, machineId, time, nano, 0, fooder);
					break;
				}
			// ナノタイムが変更された場合.
			} else if (nano != beforeNano) {
				if (nowTime.compareAndSet(beforeTime, time) && nowNano.compareAndSet(beforeNano, nano) && nowId.compareAndSet(id, 0)) {
					_createId(out, machineId, time, nano, 0, fooder);
					break;
				}
			// シーケンスIDが一定を超える場合.
			} else if (id + 1 > 0x0000ffff) {
				if (nowNano.compareAndSet(beforeNano, beforeNano + 1L) && nowId.compareAndSet(id, 0)) {
					_createId(out, machineId, beforeTime, nano + 1L, 0, fooder);
					break;
				}
			// シーケンスIDをセット.
			} else if (nowId.compareAndSet(id, id + 1)) {
				_createId(out, machineId, beforeTime, nano, id + 1, fooder);
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
	public static final byte[] createId(final int machineId, final long time,
		final long nano, final int seqId, final byte[] fooder) {
		final byte[] ret = new byte[20 + (fooder == null ? 0 : fooder.length)];
		_createId(ret, machineId, time, nano, seqId, fooder);
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
	protected static final void _createId(final byte[] out, final int machineId, final long time,
		final long nano, final int seqId, final byte[] fooder) {
		out[0] = (byte) ((time & 0xff00000000000000L) >> 56L);
		out[1] = (byte) ((time & 0x00ff000000000000L) >> 48L);
		out[2] = (byte) ((time & 0x0000ff0000000000L) >> 40L);
		out[3] = (byte) ((time & 0x000000ff00000000L) >> 32L);
		out[4] = (byte) ((time & 0x00000000ff000000L) >> 24L);
		out[5] = (byte) ((time & 0x0000000000ff0000L) >> 16L);
		out[6] = (byte) ((time & 0x000000000000ff00L) >> 8L);
		out[7] = (byte) ((time & 0x00000000000000ffL) >> 0L);
		out[8] = (byte) ((nano & 0xff00000000000000L) >> 56L);
		out[9] = (byte) ((nano & 0x00ff000000000000L) >> 48L);
		out[10] = (byte) ((nano & 0x0000ff0000000000L) >> 40L);
		out[11] = (byte) ((nano & 0x000000ff00000000L) >> 32L);
		out[12] = (byte) ((nano & 0x00000000ff000000L) >> 24L);
		out[13] = (byte) ((nano & 0x0000000000ff0000L) >> 16L);
		out[14] = (byte) ((nano & 0x000000000000ff00L) >> 8L);
		out[15] = (byte) ((nano & 0x00000000000000ffL) >> 0L);
		out[16] = (byte) ((seqId & 0x0000ff00) >> 8);
		out[17] = (byte) ((seqId & 0x000000ff) >> 0);
		out[18] = (byte) ((machineId & 0x0000ff00) >> 8);
		out[19] = (byte) ((machineId & 0x000000ff) >> 0);
		if(out.length != 20) {
			System.arraycopy(fooder, 0, out, 20, fooder.length);
		}
	}

	// バイナリから、データ変換.
	private final void setBinary(final byte[] value) {
		nowId.set(getSequenceId(value));
		nowTime.set(getTime(value));
		nowNano.set(getNano(value));
		machineId = getMachineId(value);
	}

	public final String toString() {
		return toString(next());
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
	 * binaryから、ナノ時間を取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final long getNano(final byte[] value) {
		return (((long) value[8] & 0x00000000000000ffL) << 56L) | (((long) value[9] & 0x00000000000000ffL) << 48L)
				| (((long) value[10] & 0x00000000000000ffL) << 40L) | (((long) value[11] & 0x00000000000000ffL) << 32L)
				| (((long) value[12] & 0x00000000000000ffL) << 24L) | (((long) value[13] & 0x00000000000000ffL) << 16L)
				| (((long) value[14] & 0x00000000000000ffL) << 8L) | (((long) value[15] & 0x00000000000000ffL) << 0L);
	}

	/**
	 * binaryからシーケンスIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getSequenceId(final byte[] value) {
		return (((int) value[16] & 0x000000ff) << 8) | (((int) value[17] & 0x000000ff) << 0);
	}

	/**
	 * binaryからマシンIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getMachineId(final byte[] value) {
		return (((int) value[18] & 0x000000ff) << 8) | (((int) value[19] & 0x000000ff) << 0);
	}

	/**
	 * マシンIDとシーケンスIDを０に設定.
	 * 
	 * @param value
	 */
	public static final void first(final byte[] value) {
		value[16] = 0;
		value[17] = 0;
		value[18] = 0;
		value[19] = 0;
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
		byte[] ret = new byte[Base64.decodeOutSize(s)];
		_toBinary(ret, s);
		return ret;
	}

	/**
	 * 16進数文字列をバイナリに変換.
	 * 
	 * @param o
	 * @param s
	 */
	protected static final void _toBinary(byte[] o, String s) {
		// base64で処理する.
		Base64.decode(o, 0, s);
	}
}

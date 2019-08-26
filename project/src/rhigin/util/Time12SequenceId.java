package rhigin.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Time 12 byte(96bit) シーケンスID発行処理.
 */
public class Time12SequenceId {
	private final AtomicInteger nowId = new AtomicInteger(0);
	private final AtomicLong nowTime = new AtomicLong(-1L);
	private int machineId = 0;

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *			対象のマシンIDを設定します.
	 */
	public Time12SequenceId(int id) {
		machineId = id;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *			対象のマシンIDを設定します.
	 * @param lastTime
	 *			設定した最終時間を設定します.
	 * @param lastId
	 *			設定した最終IDを設定します.
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
	 *			対象のバイナリを設定します.
	 */
	public Time12SequenceId(byte[] binary) {
		set(binary);
	}

	/**
	 * 現在発行したシーケンスIDを再取得.
	 * 
	 * @param buf
	 *			対象のバッファを設定します.
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
		final byte[] ret = new byte[16];
		get(ret);
		return ret;
	}

	/**
	 * シーケンスIDを設定.
	 * 
	 * @param binary
	 *			対象のバイナリを設定します.
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
	 *			対象のバッファを設定します.
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
			} else if(id + 1 > 0x0000ffff) {
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

	// バイナリ変換.
	private static final void createId(final byte[] out, final int machineId, final long time, final int seqId) {
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

	/**
	 * binaryから、時間を取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final long getTime(final byte[] value) {
		return (((long) value[0] & 0x00000000000000ffL) << 56L)
			| (((long) value[1] & 0x00000000000000ffL) << 48L)
			| (((long) value[2] & 0x00000000000000ffL) << 40L)
			| (((long) value[3] & 0x00000000000000ffL) << 32L)
			| (((long) value[4] & 0x00000000000000ffL) << 24L)
			| (((long) value[5] & 0x00000000000000ffL) << 16L)
			| (((long) value[6] & 0x00000000000000ffL) << 8L)
			| (((long) value[7] & 0x00000000000000ffL) << 0L);
	}

	/**
	 * binaryからシーケンスIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getSequenceId(final byte[] value) {
		return (((int) value[8] & 0x000000ff) << 8)
			| (((int) value[9] & 0x000000ff) << 0);
	}

	/**
	 * binaryからマシンIDを取得.
	 * 
	 * @param value
	 * @return
	 */
	public static final int getMachineId(final byte[] value) {
		return (((int) value[10] & 0x000000ff) << 8)
			| (((int) value[11] & 0x000000ff) << 0);
	}
	
	/**
	 * バイナリを16進数文字列に変換.
	 * @param b
	 * @return
	 */
	public static final String toString(byte[] b) {
		int i, j;
		int len = b.length;
		StringBuilder buf = new StringBuilder(len << 1);
		for(i = 0; i < len; i ++) {
			for(j = 4; j >= 0; j -= 4) {
				switch(((b[i] & (0x0f << j)) >> j) & 0x0f) {
				case 0: buf.append("0"); break;
				case 1: buf.append("1"); break;
				case 2: buf.append("2"); break;
				case 3: buf.append("3"); break;
				case 4: buf.append("4"); break;
				case 5: buf.append("5"); break;
				case 6: buf.append("6"); break;
				case 7: buf.append("7"); break;
				case 8: buf.append("8"); break;
				case 9: buf.append("9"); break;
				case 10: buf.append("A"); break;
				case 11: buf.append("B"); break;
				case 12: buf.append("C"); break;
				case 13: buf.append("D"); break;
				case 14: buf.append("E"); break;
				case 15: buf.append("F"); break;
				}
			}
		}
		return buf.toString();
	}
	
	/**
	 * 16進数文字列をバイナリに変換.
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
	 * @param o
	 * @param s
	 */
	public static final void toBinary(byte[] o, String s) {
		if(s.length() != 24) {
			return;
		}
		int i, j, c;
		for(i = 0, c = 0; i < 24; i += 2, c ++) {
			o[c] = 0;
			for(j = 0; j < 2; j ++) {
				switch(s.charAt(i+j)) {
				case '0' : break;
				case '1' : o[c] |= (1 << (4 * (1-j))); break;
				case '2' : o[c] |= (2 << (4 * (1-j))); break;
				case '3' : o[c] |= (3 << (4 * (1-j))); break;
				case '4' : o[c] |= (4 << (4 * (1-j))); break;
				case '5' : o[c] |= (5 << (4 * (1-j))); break;
				case '6' : o[c] |= (6 << (4 * (1-j))); break;
				case '7' : o[c] |= (7 << (4 * (1-j))); break;
				case '8' : o[c] |= (8 << (4 * (1-j))); break;
				case '9' : o[c] |= (9 << (4 * (1-j))); break;
				case 'a' : o[c] |= (10 << (4 * (1-j))); break;
				case 'A' : o[c] |= (10 << (4 * (1-j))); break;
				case 'b' : o[c] |= (11 << (4 * (1-j))); break;
				case 'B' : o[c] |= (11 << (4 * (1-j))); break;
				case 'c' : o[c] |= (12 << (4 * (1-j))); break;
				case 'C' : o[c] |= (12 << (4 * (1-j))); break;
				case 'd' : o[c] |= (13 << (4 * (1-j))); break;
				case 'D' : o[c] |= (13 << (4 * (1-j))); break;
				case 'e' : o[c] |= (14 << (4 * (1-j))); break;
				case 'E' : o[c] |= (14 << (4 * (1-j))); break;
				case 'f' : o[c] |= (15 << (4 * (1-j))); break;
				case 'F' : o[c] |= (15 << (4 * (1-j))); break;
				}
			}
		}
	}
}

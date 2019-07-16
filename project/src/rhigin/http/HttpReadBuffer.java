package rhigin.http;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import rhigin.util.AtomicNumber;

/**
 * Atomicにデータ受信を行うためのHttp受信用バッファ.
 */
public class HttpReadBuffer {
	private final Queue<byte[]> buffer = new ConcurrentLinkedQueue<byte[]>();
	private final AtomicNumber bufferLength = new AtomicNumber(0);
	private byte[] topBuffer = null;
	
	/**
	 * データクリア.
	 */
	public void clear() {
		topBuffer = null;
		buffer.clear();
		bufferLength.set(0);
	}
	
	/**
	 * 現在の格納バイナリ長を取得.
	 * @return
	 */
	public int size() {
		return bufferLength.get();
	}
	
	/**
	 * 書き込み処理.
	 * @param buf
	 */
	public void write(ByteBuffer buf) {
		final int bufLen = buf.remaining();
		if(bufLen <= 0) {
			return;
		}
		final byte[] bin = new byte[bufLen];
		buf.get(bin);
		buffer.offer(bin);
		bufferLength.add(bin.length);
	}
	
	/**
	 * 書き込み処理.
	 * @param b
	 */
	public void write(byte[] b) {
		write(b, 0, b.length);
	}
	
	/**
	 * 書き込み処理.
	 * @param b
	 * @param len
	 */
	public void write(byte[] b, int len) {
		write(b, 0, len);
	}
	
	/**
	 * 書き込み処理.
	 * @param b
	 * @param off
	 * @param len
	 */
	public void write(byte[] b, int off, int len) {
		final byte[] bin = new byte[len];
		System.arraycopy(b, off, bin, 0, len);
		buffer.offer(bin);
		bufferLength.add(bin.length);
	}
	
	/**
	 * 読み込み処理.
	 * @param b
	 * @return
	 */
	public int read(byte[] b) {
		return read(b, 0, b.length);
	}
	
	/**
	 * 読み込み処理.
	 * @param b
	 * @param len
	 * @return
	 */
	public int read(byte[] b, int len) {
		return read(b, 0, len);
	}
	
	/**
	 * 読み込み処理.
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public int read(byte[] b, int off, int len) {
		int bufLen, etcLen;
		int ret = 0;
		byte[] etcBuf;
		byte[] buf = null;
		
		// 前回読み込み中のTOPデータが存在する場合.
		buf = topBuffer; topBuffer = null;
		if(buf != null) {
			bufLen = buf.length;
			if(len <= ret + bufLen) {
				etcLen = len - ret;
				System.arraycopy(buf, 0, b, off, etcLen);
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				bufferLength.remove(ret);
				return ret;
			}
			System.arraycopy(buf, 0, b, off, bufLen);
			off += bufLen;
			ret += bufLen;
			if(len <= ret) {
				bufferLength.remove(ret);
				return ret;
			}
		}
		// 断片化されたバイナリを結合.
		while((buf = buffer.poll()) != null) {
			bufLen = buf.length;
			if(len <= ret + bufLen) {
				// １つの塊のデータの読み込みに対して、残りが発生する場合.
				// 次の処理で読み込む.
				etcLen = len - ret;
				System.arraycopy(buf, 0, b, off, etcLen);
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				break;
			}
			System.arraycopy(buf, 0, b, off, bufLen);
			off += bufLen;
			ret += bufLen;
			if(len <= ret) {
				break;
			}
		}
		bufferLength.remove(ret);
		return ret;
	}
	
	/**
	 * スキップ.
	 * @param len
	 * @return
	 */
	public int skip(int len) {
		int bufLen, etcLen;
		int ret = 0;
		byte[] etcBuf;
		byte[] buf = null;
		
		// 前回読み込み中のTOPデータが存在する場合.
		buf = topBuffer; topBuffer = null;
		if(buf != null) {
			bufLen = buf.length;
			if(len <= ret + bufLen) {
				etcLen = len - ret;
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				bufferLength.remove(ret);
				return ret;
			}
			ret += bufLen;
			if(len <= ret) {
				bufferLength.remove(ret);
				return ret;
			}
		}
		// 断片化されたバイナリを結合.
		while((buf = buffer.poll()) != null) {
			bufLen = buf.length;
			if(len <= ret + bufLen) {
				// １つの塊のデータの読み込みに対して、残りが発生する場合.
				// 次の処理で読み込む.
				etcLen = len - ret;
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				break;
			}
			ret += bufLen;
			if(len <= ret) {
				break;
			}
		}
		bufferLength.remove(ret);
		return ret;
	}
	
	/**
	 * バイナリ検索.
	 * @param index
	 * @return
	 */
	public int indexOf(byte[] index) {
		return indexOf(index, 0);
	}
	
	/**
	 * バイナリ検索.
	 * @param index
	 * @param pos
	 * @return
	 */
	public int indexOf(byte[] index, int pos) {
		int i, j, bufLen, startPos, off ;
		int bp, np;
		int cnt = 0;
		int eqCnt = 0;
		byte[] buf = null;
		Object[] array = buffer.toArray();
		final int len = array.length;
		final byte top = index[0];
		final int indexLen = index.length;
		bp = 0; off = 0;
		// posの位置を算出.
		if(pos > 0) {
			// データサイズを超えている場合.
			if(pos >= size()) {
				return -1;
			}
			// topBufferが存在する場合.
			if(topBuffer != null) {
				bp = -1;
				bufLen = topBuffer.length;
				if(bufLen > pos) {
					off = pos;
					cnt = pos;
				} else {
					cnt += bufLen;
					bp = 0;
				}
			}
			// topBufferが存在しないか、topBuffer内の範囲でoffが確定できなかった場合.
			if(off == 0) {
				// posの位置まで移動.
				for(int p = 0; p < len; p ++) {
					bufLen = ((byte[])array[bp]).length;
					if(cnt + bufLen > pos) {
						off = pos - cnt;
						break;
					}
					cnt += bufLen;
					bp ++;
				}
				cnt = pos;
			}
		} else if(topBuffer != null) {
			bp = -1;
		}
		// toArrayで配列化された内容を元に、検索.
		for(; bp < len; bp ++) {
			buf = (bp == -1) ? topBuffer : (byte[])array[bp];
			bufLen = buf.length;
			for(i = off; i < bufLen; i++) {
				// 先頭のindex条件が一致.
				if(top == buf[i]) {
					// indexデータ数が１つの場合.
					if(indexLen == 1) {
						return cnt;
					}
					eqCnt = 0;
					startPos = i;
					
					// 跨ったデータのチェック用.
					for(np = bp; np < len; np ++) {
						buf = (np == -1) ? topBuffer : (byte[])array[np];
						bufLen = buf.length;
						// 一致するかチェックする.
						for(j = startPos; j < bufLen; j ++) {
							if(index[eqCnt++] != buf[j]) {
								eqCnt = -1;
								break;
							} else if(eqCnt >= indexLen) {
								break;
							}
						}
						// 不一致.
						if(eqCnt == -1) {
							// 元に戻す.
							buf = (np == -1) ? topBuffer : (byte[])array[np];
							bufLen = buf.length;
							break;
						}
						// 一致.
						else if(eqCnt == indexLen) {
							// 一致条件を返却.
							return cnt;
						}
						// 次のまたがった情報を取得.
						startPos = 0;
					}
				}
				cnt ++;
			}
			off = 0;
		}
		return -1;
	}
}

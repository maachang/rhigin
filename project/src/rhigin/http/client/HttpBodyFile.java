package rhigin.http.client;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import rhigin.util.FileUtil;

/**
 * body受信用ファイル管理オブジェクト.
 */
final class HttpBodyFile {
	private static final String NAME_HEAD = "body_data";
	private static final char[] NAME_CODE = new char[] { (char) 'A', (char) 'B', (char) 'C', (char) 'D', (char) 'E',
			(char) 'F', (char) 'G', (char) 'H', (char) 'I', (char) 'J', (char) 'K', (char) 'L', (char) 'M', (char) 'N',
			(char) 'O', (char) 'P', (char) 'Q', (char) 'R', (char) 'S', (char) 'T', (char) 'U', (char) 'V', (char) 'W',
			(char) 'X', (char) 'Y', (char) 'Z', (char) 'a', (char) 'b', (char) 'c', (char) 'd', (char) 'e', (char) 'f',
			(char) 'g', (char) 'h', (char) 'i', (char) 'j', (char) 'k', (char) 'l', (char) 'm', (char) 'n', (char) 'o',
			(char) 'p', (char) 'q', (char) 'r', (char) 's', (char) 't', (char) 'u', (char) 'v', (char) 'w', (char) 'x',
			(char) 'y', (char) 'z', (char) '0', (char) '1', (char) '2', (char) '3', (char) '4', (char) '5', (char) '6',
			(char) '7', (char) '8', (char) '9', (char) '-', (char) '_' };

	private String fileName = null;
	private long fileSize = 0L;

	private FileInputStream in = null;
	private OutputStream out = null;

	private Rand rand = new Rand();

	/**
	 * コンストラクタ.
	 * 
	 * @param baseDir
	 */
	public HttpBodyFile() {
		this("./");
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param baseDir
	 */
	public HttpBodyFile(String baseDir) {
		rand.seet(System.nanoTime());
		if (!baseDir.endsWith("/")) {
			baseDir += "/";
		}
		// bodyのデータを一時格納するための一意のファイルを作成.
		String name;
		OutputStream o = null;
		try {
			while (true) {
				if (FileUtil.isFile(name = getFileName(baseDir, rand))) {
					continue;
				}
				o = new BufferedOutputStream(new FileOutputStream(name));
				break;
			}
			out = o;
			o = null;
			fileName = name;
		} catch (Exception e) {
			throw new HttpClientException(500, e);
		} finally {
			if (o != null) {
				try {
					o.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// ファイル名を取得.
	private static final String getFileName(String baseDir, Rand rand) {
		StringBuilder buf = new StringBuilder(baseDir).append(NAME_HEAD).append("_");
		int code = rand.nextInt();
		int cnt = 0;
		while (true) {
			buf.append(NAME_CODE[code & 0x003f]);
			code = code >> 6;
			if (code == 0) {
				cnt++;
				if (cnt >= 5) {
					break;
				}
				code = rand.nextInt();
			}
		}
		return buf.append(".body").toString();
	}

	@Override
	protected void finalize() throws Exception {
		close();
	}

	/**
	 * 情報クリア.
	 */
	public void close() {
		if (in != null) {
			FileInputStream n = in;
			in = null;
			try {
				n.close();
			} catch (Exception e) {
			}
			n = null;
		}
		if (out != null) {
			OutputStream n = out;
			out = null;
			try {
				n.close();
			} catch (Exception e) {
			}
			n = null;
		}
		if (fileName != null) {
			String fn = fileName;
			fileName = null;
			try {
				FileUtil.removeFile(fn);
			} catch (Exception e) {
			}
		}
		fileSize = -1L;
	}

	/**
	 * データセット.
	 * 
	 * @param b
	 * @param len
	 */
	public void write(byte[] b, int len) {
		try {
			out.write(b, 0, len);
			fileSize += len;
		} catch (Exception e) {
			close();
			throw new HttpClientException(500, e);
		}
	}

	/**
	 * データセット終了.
	 */
	public void endWrite() {
		try {
			out.flush();
			out.close();
			out = null;
			fileSize = -1L;
		} catch (Exception e) {
			close();
			throw new HttpClientException(500, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
				out = null;
			}
		}
	}

	/**
	 * データ取得.
	 * 
	 * @return FileInputStream
	 */
	public FileInputStream getInputStream() {
		if (in != null) {
			return in;
		}
		try {
			in = new FileInputStream(fileName);
		} catch (Exception e) {
			close();
			throw new HttpClientException(500, e);
		}
		return in;
	}

	/**
	 * ファイルサイズを取得.
	 * 
	 * @return long
	 */
	public long getFileLength() {
		return fileSize;
	}

	/**
	 * ファイル名を取得. return String
	 */
	public String getFileName() {
		return fileName;
	}

	/** ランダムファイル. **/
	private static final class Rand {
		private int a = 123456789;
		private int b = 362436069;
		private int c = 521288629;
		private int d = 88675123;

		public void seet(long t) {
			int s = (int) (t & 0x00000000ffffffffL);
			a = s = 1812433253 * (s ^ (s >> 30)) + 1;
			b = s = 1812433253 * (s ^ (s >> 30)) + 2;
			c = s = 1812433253 * (s ^ (s >> 30)) + 3;
			d = s = 1812433253 * (s ^ (s >> 30)) + 4;
		}

		public final int nextInt() {
			int t, r;
			t = a;
			r = t;
			t <<= 11;
			t ^= r;
			r = t;
			r >>= 8;
			t ^= r;
			r = b;
			a = r;
			r = c;
			b = r;
			r = d;
			c = r;
			t ^= r;
			r >>= 19;
			r ^= t;
			d = r;
			return r;
		}
	}
}

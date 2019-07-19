package rhigin.http;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import rhigin.RhiginException;
import rhigin.util.FileUtil;
import rhigin.util.Xor128;

/**
 * 大容量のPOST受付処理でのBody情報.
 */
public class HttpPostBodyFile {
	private static final String NAME_HEAD = "post_data_";
	private static final int DELETE_RETRY_COUNT = 3;
	private static final char[] NAME_CODE = new char[]{(char) 'A', (char) 'B', (char) 'C',
        (char) 'D', (char) 'E', (char) 'F', (char) 'G', (char) 'H',
        (char) 'I', (char) 'J', (char) 'K', (char) 'L', (char) 'M',
        (char) 'N', (char) 'O', (char) 'P', (char) 'Q', (char) 'R',
        (char) 'S', (char) 'T', (char) 'U', (char) 'V', (char) 'W',
        (char) 'X', (char) 'Y', (char) 'Z', (char) 'a', (char) 'b',
        (char) 'c', (char) 'd', (char) 'e', (char) 'f', (char) 'g',
        (char) 'h', (char) 'i', (char) 'j', (char) 'k', (char) 'l',
        (char) 'm', (char) 'n', (char) 'o', (char) 'p', (char) 'q',
        (char) 'r', (char) 's', (char) 't', (char) 'u', (char) 'v',
        (char) 'w', (char) 'x', (char) 'y', (char) 'z', (char) '0',
        (char) '1', (char) '2', (char) '3', (char) '4', (char) '5',
        (char) '6', (char) '7', (char) '8', (char) '9', (char) '-',
        (char) '_' };
	
	private String fileName = null;
	private long fileSize = 0L;
	
	private FileInputStream in = null;
	private OutputStream out = null;
	
	/**
	 * コンストラクタ.
	 * @param workerId
	 * @param baseDir
	 * @param rand
	 */
	public HttpPostBodyFile(int workerId, String baseDir, Xor128 rand) {
		if(!baseDir.endsWith("/")) {
			baseDir += "/";
		}
		// bodyのデータを一時格納するための一意のファイルを作成.
		String name;
		OutputStream o = null;
		try {
			while(true) {
				if(FileUtil.isFile(name = getFileName(workerId, baseDir, rand))) {
					continue;
				}
				o = new BufferedOutputStream(new FileOutputStream(name));
				break;
			}
			out = o; o = null;
			fileName = name;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		} finally {
			if(o != null) {
				try { o.close(); } catch(Exception e) {}
			}
		}
	}
	
	// ファイル名を取得.
	private static final String getFileName(int workerId, String baseDir, Xor128 rand) {
		StringBuilder buf = new StringBuilder(baseDir)
			.append(NAME_HEAD).append(workerId).append("_");
		int code = rand.nextInt();
		int cnt = 0;
		while(true) {
			buf.append(NAME_CODE[code & 0x003f]);
			code = code >> 6;
			if(code == 0) {
				cnt ++;
				if(cnt >= 5) {
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
		if(in != null) {
			FileInputStream n = in; in = null;
			try {
				n.close();
			} catch(Exception e) {}
			n = null;
		}
		if(out != null) {
			OutputStream n = out; out = null;
			try {
				n.close();
			} catch(Exception e) {}
			n = null;
		}
		if(fileName != null) {
			String fn = fileName; fileName = null;
			try {
				// ファイル削除のリトライを行って、削除する.
				for(int i = 0; i < DELETE_RETRY_COUNT; i ++) {
					FileUtil.removeFile(fn);
				}
			} catch(Exception e) {}
		}
		fileSize = -1L;
	}
	
	/**
	 * データセット.
	 * @param b
	 * @param len
	 */
	public void write(byte[] b, int len) {
		try {
			out.write(b, 0, len);
			fileSize += len;
		} catch(Exception e) {
			close();
			throw new RhiginException(500, e);
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
		} catch(Exception e) {
			close();
			throw new RhiginException(500, e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
				out = null;
			}
		}
	}
	
	/**
	 * データ取得.
	 * @return FileInputStream 
	 */
	public FileInputStream getInputStream() {
		if(in != null) {
			return in;
		}
		try {
			in = new FileInputStream(fileName);
		} catch(Exception e) {
			close();
			throw new RhiginException(500, e);
		}
		return in;
	}
	
	/**
	 * ファイルサイズを取得.
	 * @return long
	 */
	public long getFileLength() {
		return fileSize;
	}
	
	/**
	 * ファイル名を取得.
	 * return String
	 */
	public String getFileName() {
		return fileName;
	}
}

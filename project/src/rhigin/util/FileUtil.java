package rhigin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import rhigin.net.ByteArrayIO;

/**
 * ファイルユーティリティ.
 */
public final class FileUtil {
	protected FileUtil() {}
  
	/**
	 * ファイル名の存在チェック.
	 * 
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return boolean [true]の場合、ファイルは存在します.
	 */
	public static final boolean isFile(String name) {
	  File file = new File(name);
	  return (file.exists() && !file.isDirectory());
  }

  /**
   * ディレクトリ名の存在チェック.
   * 
   * @param name
   *            対象のディレクトリ名を設定します.
   * @return boolean [true]の場合、ディレクトリは存在します.
   */
  public static final boolean isDir(String name) {
	  File file = new File(name);
	  return (file.exists() && file.isDirectory());
  }

  /**
   * 指定情報が読み込み可能かチェック.
   * 
   * @param name
   *            対象のファイル／ディレクトリ名を設定します.
   * @return boolean [true]の場合、読み込み可能です.
   */
  public static final boolean isRead(String name) {
	  File file = new File(name);
	  return (file.exists() && file.canRead());
  }

  /**
   * 指定情報が書き込み可能かチェック.
   * 
   * @param name
   *            対象のファイル／ディレクトリ名を設定します.
   * @return boolean [true]の場合、書き込み可能です.
   */
  public static final boolean isWrite(String name) {
	  File file = new File(name);
	  return (file.exists() && file.canWrite());
  }

  /**
   * 指定情報が読み書き込み可能かチェック.
   * 
   * @param name
   *            対象のファイル／ディレクトリ名を設定します.
   * @return boolean [true]の場合、読み書き込み可能です.
   */
  public static final boolean isIO(String name) {
	  File file = new File(name);
	  return (file.exists() && file.canRead() && file.canWrite());
  }

  /**
   * 対象のディレクトリを生成.
   * 
   * @param dirName
   *            生成対象のディレクトリ名を設定します.
   * @exception Exception
   *                例外.
   */
  public static final void mkdirs(String dir) throws Exception {
	  File fp = new File(dir);
	  if (!fp.mkdirs()) {
		  throw new IOException("Failed to create directory ("+ dir +").");
	  }
  }

  /**
   * ファイルの長さを取得.
   * 
   * @param name
   *            対象のファイル名を設定します.
   * @return long ファイルの長さが返却されます. [-1L]が返却された場合、ファイルは存在しません.
   * @exception Exception
   *                例外.
   */
  public static final long getFileLength(String name) throws Exception {
	  File fp = new File(name);
	  return (fp.exists()) ? fp.length() : -1L;
  }

  /**
   * ファイルタイムを取得.
   * 
   * @param name
   *            対象のファイル名を設定します.
   * @return long ファイルタイムが返却されます. [-1L]が返却された場合、ファイルは存在しません.
   * @exception Exception
   *                例外.
   */
  public static final long getFileTime(String name) throws Exception {
	  File fp = new File(name);
	  return (fp.exists()) ? fp.lastModified() : -1L;
  }

  /**
   * ファイル名のフルパスを取得.
   * 
   * @param name
   *            対象のファイル名を設定します.
   * @return String フルパス名が返却されます.
   * @exception Exception
   *                例外.
   */
  public static final String getFullPath(String name) throws Exception {
	  File f = new File(name);
	  String s = f.getCanonicalPath();
	  if (s.indexOf("\\") != -1) {
		  s = Converter.changeString(s, "\\", "/");
	  }
	  if (!s.startsWith("/")) {
		  s = "/" + s;
	  }
	  // if( f.isDirectory() ) {
	  // s = s + "/" ;
	  // }
	  return s;
  }

  /**
   * 対象パスのファイル名のみ取得.
   * @param path 対象のパスを設定します.
   * @return String ファイル名が返却されます.
   */
  public static final String getFileName(String path) {
	  int p = path.lastIndexOf("/");
	  if (p == -1) {
		  p = path.lastIndexOf("\\");
	  }
	  if (p == -1) {
		  return path;
	  }
	  return path.substring(p + 1);
  }

  /**
   * ファイル内容を取得.
   * @param name 対象のファイル名を設定します.
   * @return byte[] バイナリ情報が返却されます.
   * @exception Exception 例外.
   */
  public static final byte[] getFile(String name)
    throws Exception {
	  int len;
	  InputStream buf = null;
	  ByteArrayIO bo = null;
	  byte[] b = new byte[1024];
	  try {
		  bo = new ByteArrayIO();
		  buf = new BufferedInputStream(new FileInputStream(name));
		  while (true) {
			  if ((len = buf.read(b)) <= 0) {
				  if (len <= -1) {
					  break;
				  }
				  continue;
			  }
			  bo.write(b, 0, len);
		  }
		  buf.close();
		  buf = null;
		  byte[] ret = bo.toByteArray();

		  bo.close();
		  bo = null;

		  return ret;
	  } finally {
		  if (buf != null) {
			  try {
				  buf.close();
			  } catch (Exception t) {
			  }
		  }
		  buf = null;
		  if (bo != null) {
			  bo.close();
		  }
	  }
  }

  /**
   * ファイル内容を取得.
   * @param name 対象のファイル名を設定します.
   * @param charset 対象のキャラクタセットを設定します.
   * @return String 文字列情報が返却されます.
   * @exception Exception 例外.
   */
  public static final String getFileString(String name, String charset)
		throws Exception {
	  int len;
	  char[] tmp = new char[1024];
	  CharArrayWriter ca = null;
	  Reader buf = null;
	  String ret = null;
	  try {
		  ca = new CharArrayWriter();
		  buf = new BufferedReader(new InputStreamReader(new FileInputStream(
				  name), charset));
		  while ((len = buf.read(tmp, 0, 512)) > 0) {
			  ca.write(tmp, 0, len);
		  }
		  ret = ca.toString();
		  ca.close();
		  ca = null;
		  buf.close();
		  buf = null;
	  } finally {
		  if (buf != null) {
			  try {
				  buf.close();
			  } catch (Exception t) {
			  }
		  }
		  if (ca != null) {
			  try {
				  ca.close();
			  } catch (Exception t) {
			  }
		  }
		  buf = null;
		  ca = null;
		  tmp = null;
	  }
	  return ret;
  }

  /**
   * バイナリをファイル出力.
   * @param newFile [true]の場合、新規でファイル出力します.
   * @param name ファイル名を設定します.
   * @param binary 出力対象のバイナリを設定します.
   * @exception Exception 例外.
   */
  public static final void setFile(boolean newFile, String name, byte[] binary)
		throws Exception {
	  if (binary == null) {
		  throw new IOException("There is no binary to output.");
	  }
	  BufferedOutputStream buf = new BufferedOutputStream(
			  new FileOutputStream(name, !newFile));
	  try {
		  buf.write(binary);
		  buf.flush();
		  buf.close();
		  buf = null;
	  } finally {
		  if (buf != null) {
			  try {
				  buf.close();
			  } catch (Exception e) {
			  }
		  }
	  }
  }

  /**
   * 文字情報をファイル出力.
   * @param newFile [true]の場合、新規でファイル出力します.
   * @param name ファイル名を設定します.
   * @param value 出力対象の文字列を設定します.
   * @param charset 対象のキャラクタセットを設定します. nullの場合は、UTF8が設定されます.
   * @exception Exception 例外.
   */
  public static final void setFileString(boolean newFile, String name, String value, String charset)
    throws Exception {
	  if (value == null) {
		  throw new IOException("There is no target string information for output.");
	  }
	  BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(
			  new FileOutputStream(name, !newFile),
			  (charset == null) ? "UTF8" : charset));
	  try {
		  buf.write(value, 0, value.length());
		  buf.flush();
		  buf.close();
		  buf = null;
	  } finally {
		  if (buf != null) {
			  try {
				  buf.close();
			  } catch (Exception e) {
			  }
		  }
	  }
  }

  /**
   * 指定ファイルorフォルダを削除.
   * @param name 対象のファイルorフォルダ名を設定します.
   * @return boolean 削除結果が返されます.
   * @exception 例外.
   */
  public static final boolean removeFile(String name) throws Exception {
	  return new File(name).delete();
  }

  /**
   * ファイル、フォルダの移動.
   * @param src 移動元のファイル名を設定します.
   * @param dest 移動先のファイル名を設定します.
   * @return boolean [true]が返却された場合、移動は成功しました.
   */
  public static final boolean move(String src, String dest) {
	  return new File(src).renameTo(new File(dest));
  }
}


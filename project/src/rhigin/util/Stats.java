package rhigin.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Iterator;
import java.util.Set;

/**
 * ファイル属性.
 */
public class Stats {
	
	/** 対象ファイルパス. **/
	protected final Path path;
	
	/**
	 * コンストラクタ.
	 * @param name ファイル名 or ディレクトリ名を設定します.
	 */
	public Stats(String name) {
		path = Paths.get(name);
	}
	
	/**
	 * ディレクトリ存在チェック.
	 * @return
	 */
	public boolean isDirectory() {
		return Files.isDirectory(path);
	}
	
	/**
	 * ファイル存在チェック.
	 * @return
	 */
	public boolean isFile() {
		return Files.isRegularFile(path);
	}
	
	/**
	 * シンボリック存在チェック.
	 * @return
	 */
	public boolean isSymbolicLink() {
		return Files.isSymbolicLink(path);
	}
	
	/**
	 * ファイルパーミッションを取得.
	 * ８進数で 777 とセットされるので、この内容は「linuxのファイルパーミッションと同じ並び」
	 * 自分(読み込み(4), 書き込み(2), 実行(1)), グループ(読み込み(4), 書き込み(2), 実行(1)), その他(読み込み(4), 書き込み(2), 実行(1))
	 * 
	 * @return
	 * @throws IOException
	 */
	public long mode() throws IOException {
		Set<PosixFilePermission> list = Files.getPosixFilePermissions(path);
		int ret = 0;
		// 各パーミッションを８進数を出割り当てる.
		PosixFilePermission p;
		Iterator<PosixFilePermission> it = list.iterator();
		while(it.hasNext()) {
			p = it.next();
			if(p.equals(PosixFilePermission.OWNER_READ)) {
				ret |= 4 << 6;
			} else if(p.equals(PosixFilePermission.OWNER_WRITE)) {
				ret |= 2 << 6;
			} else if(p.equals(PosixFilePermission.OWNER_EXECUTE)) {
				ret |= 1 << 6;
			} else if(p.equals(PosixFilePermission.GROUP_READ)) {
				ret |= 4 << 3;
			} else if(p.equals(PosixFilePermission.GROUP_WRITE)) {
				ret |= 2 << 3;
			} else if(p.equals(PosixFilePermission.GROUP_EXECUTE)) {
				ret |= 1 << 3;
			} else if(p.equals(PosixFilePermission.OTHERS_READ)) {
				ret |= 4;
			} else if(p.equals(PosixFilePermission.OTHERS_WRITE)) {
				ret |= 2;
			} else if(p.equals(PosixFilePermission.OTHERS_EXECUTE)) {
				ret |= 1;
			}
		}
		return ret;
	}
	
	/**
	 * 最終アクセス時間を取得.
	 * @return
	 * @throws IOException
	 */
	public long atime() throws IOException {
		return Files.readAttributes(path, BasicFileAttributes.class).lastAccessTime().toMillis();
	}
	
	/**
	 * 最終更新時間を取得.
	 * @return
	 * @throws IOException
	 */
	public long mtime() throws IOException {
		return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toMillis();
	}
	
	/**
	 * 生成日付を取得.
	 * @return
	 * @throws IOException
	 */
	public long birthtime() throws IOException {
		return Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis();
	}
	
	/**
	 * パスを取得.
	 * @return
	 */
	public Path getPath() {
		return path;
	}
}

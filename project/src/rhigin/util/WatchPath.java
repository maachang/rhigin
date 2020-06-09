package rhigin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rhigin.RhiginException;

/**
 * 指定フォルダ内のStat情報管理.
 * 
 * 指定されたフォルダ群を監視してStat情報を保持します.
 * このオブジェクトの存在としては、FileSystemに都度アクセスすると、ディスクにその都度I/Oするので
 * アクセスが増えれば増えるほど、負荷がかかります.
 * 
 * 指定フォルダを監視して、Stat情報をキャッシュ化することで、これらを軽減します.
 */
public class WatchPath {
	
	/** シングルトン. **/
	private static WatchPath SNGL = null;
	
	/**
	 * setStaticWatchPathで登録されているパスを取得.
	 * @return
	 */
	public static final WatchPath getInstance() {
		return SNGL;
	}
	
	/**
	 * シングルトンなWatchPathを登録.
	 * @param p
	 */
	public static final void setStaticWatchPath(WatchPath p) {
		SNGL = p;
	}

	/** １つのファイル or ディレクトリの管理. **/
	public static final class StatObject {
		String path;
		long atime;
		long mtime;
		long barthTime;
		long length;
		boolean dirFlg;
		int topDirNo;
		long updateTime;
		List<String> topDirs;
		
		StatObject(List<String> t, String name) throws IOException {
			this(t, name, Paths.get(name));
		}
		
		StatObject(List<String> t, String name, Path p) throws IOException {
			path = name;
			BasicFileAttributes ba = Files.readAttributes(p, BasicFileAttributes.class);
			atime = ba.lastAccessTime().toMillis();
			mtime = ba.lastModifiedTime().toMillis();
			barthTime = ba.creationTime().toMillis();
			length = ba.size();
			dirFlg = ba.isDirectory();
			updateTime = System.currentTimeMillis();
			topDirNo = -1;
			topDirs = t;
			int len = t.size();
			for(int i = 0; i < len; i ++) {
				if(name.startsWith(t.get(i))) {
					if(name.equals(t.get(i))) {
						path = "/";
					} else {
						path = path.substring(t.get(i).length());
					}
					topDirNo = i;
					break;
				}
			}
		}
		
		/**
		 * 現在の相対パスを取得.
		 * @return
		 */
		public String getPath() {
			return path;
		}
		
		/**
		 * トップディレクトリを取得.
		 * @return
		 */
		public String getTopDir() {
			return topDirNo == -1 ? null : topDirs.get(topDirNo);
		}
		
		/**
		 * 現在のフルパスを取得.
		 * @return
		 */
		public String getFullPath() {
			if(topDirNo != -1) {
				return topDirs.get(topDirNo) + path;
			}
			return path;
		}

		/**
		 * atimeを取得.
		 * @return
		 */
		public long getAtime() {
			return atime;
		}

		/**
		 * mtimeを取得.
		 * @return
		 */
		public long getMtime() {
			return mtime;
		}

		/**
		 * barthTimeを取得.
		 * @return
		 */
		public long getBarthTime() {
			return barthTime;
		}
		
		/**
		 * ファイル長を取得.
		 * @return
		 */
		public long getLength() {
			return length;
		}

		/**
		 * ディレクトリか取得.
		 * @return
		 */
		public boolean isDirectory() {
			return dirFlg;
		}
		
		/**
		 * ファイルか取得.
		 * @return
		 */
		public boolean isFile() {
			return !dirFlg;
		}
		
		/**
		 * このStatObjectの最終取得時間を取得.
		 * @return
		 */
		public long getUpdateTime() {
			return updateTime;
		}

		@Override
		public String toString() {
			return new StringBuilder("{path: \"").append(path).append("\"")
					.append(", topDir: \"").append(topDirs.get(topDirNo)).append("\"")
					.append(", atime: ").append(atime)
					.append(", mtime: ").append(mtime)
					.append(", barthTime: ").append(barthTime)
					.append(", length: ").append(length)
					.append(", directoryFlag: ").append(dirFlg)
					.append(", updateTime: ").append(updateTime)
					.append("}")
					.toString();
		}
	}
	
	// スレッド監視.
	protected static final class WatchThread extends Thread {
		private WatchService sv = null;
		private Map<WatchKey, String> wman = null;
		private Map<String, WatchKey> kman = null;
		private Map<String, StatObject> man = null;
		private List<String> tdir = null;
		private Flag cf = null;
		private volatile boolean stopFlag = true;
		private volatile boolean exitFlag = false;
		WatchThread(WatchService s, Map<WatchKey, String> wm, Map<String, WatchKey> km,
			Map<String, StatObject> m, List<String> t, Flag c) {
			sv = s;
			wman = wm;
			kman = km;
			man = m;
			tdir = t;
			cf = c;
			//outMan(man);
		}
		
		public void startThread() {
			stopFlag = false;
			setDaemon(true);
			start();
		}

		public void stopThread() {
			stopFlag = true;
		}

		public boolean isStopThread() {
			return stopFlag;
		}

		public boolean isExitThread() {
			return exitFlag;
		}

		public void run() {
			ThreadDeath d = null;
			try {
				int len;
				boolean dirFlg;
				String name;
				String dir;
				Path path;
				Object kind;
				WatchEvent<?> event;
				List<WatchEvent<?>> list;
				boolean endFlag = false;
				WatchKey watchKey = null;
				while (!cf.get() && !endFlag && !stopFlag) {
					try {
						// 100ミリ秒で監視.
						watchKey = sv.poll(100, TimeUnit.MILLISECONDS);
						if(watchKey == null) {
							//System.out.println("poll");
							continue;
						}
						// watchManagerに登録されているディレクトリ名を取得.
						dir = wman.get(watchKey);
						if(dir == null) {
							continue;
						}
						// イベント一覧を取得.
						list = watchKey.pollEvents();
						len = list.size();
						for(int i = 0; i < len; i ++) {
							if(cf.get() || endFlag || stopFlag) {
								break;
							}
							try {
								event = list.get(i);
								if(!(event.context() instanceof Path)) {
									//if(StandardWatchEventKinds.OVERFLOW.equals(event.kind())) {
									//	System.out.println(">Overflow context: " + event.context());
									//}
									continue;
								}
								path = (Path)event.context();
								kind = event.kind();
								// フルパス名を取得.
								name = dir + "/" + path.getFileName();
								// pathを作成.
								path = Paths.get(name);
								//System.out.println("kind: " + kind + " name:" + name + " contains:" + man.containsKey(name));
								
								// 新しく情報が生成された場合.
								if(StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
									// フォルダかチェック.
									dirFlg = Files.isDirectory(path);
									//System.out.println("kind: " + kind + " name:" + name + " dir:" + dirFlg);
									if(dirFlg) {
										// 新しいフォルダが生成された場合は新しい監視対象として追加.
										WatchPath.createWatch(wman, kman, sv, path);
										// フォルダのStat情報を登録.
										man.put(name, new StatObject(tdir, name));
										// フォルダ内の情報をStat内容を登録.
										List<String> watchList = new ObjectList<String>();
										WatchPath.statByDir(watchList, man, tdir, name);
										// WatchListに情報がある場合、内部ディレクトリが存在するので
										// そこもWatch対象にする.
										//System.out.println("watchList: " + watchList);
										if(watchList.size() > 0) {
											WatchPath.createWatch(wman, kman, sv, watchList);
										}
									} else {
										// ファイルのStat情報を登録.
										man.put(name, new StatObject(tdir, name));
									}
									
								// 情報が更新された場合.
								} else if(StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
									if(man.containsKey(name)) {
										// フォルダかチェック.
										dirFlg = man.get(name).dirFlg;
										//System.out.println("kind:" + kind + " name:" + name + " dir:" + dirFlg);
										// 情報の更新.
										man.put(name, new StatObject(tdir, name));
									}
									
								// 情報が削除された場合.
								} else if(StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
									if(man.containsKey(name)) {
										// フォルダかチェック.
										dirFlg = man.get(name).dirFlg;
										//System.out.println("kind: " + kind + " name:" + name + " dir:" + dirFlg);
										if(dirFlg) {
											// watchManagerから除外.
											WatchKey wk = kman.remove(name);
											if(wk != null) {
												// フォルダが削除された場合はこのWatchKeyの監視を停止.
												wman.remove(wk);
												wk.cancel();
												wk = null;
												//System.out.println("cancel watchKey:" + name);
											}
											// フォルダのStat情報を削除.
											man.remove(name);
											
											String p;
											Entry<String, StatObject> e;
											// manで管理しているフォルダ以下の情報を削除.
											Iterator<Entry<String, StatObject>> it = man.entrySet().iterator();
											while(it.hasNext()) {
												e = it.next();
												p = e.getKey();
												// 対象フォルダ以下の情報の場合.
												if(p.startsWith(name)) {
													// フォルダ管理の場合.
													if(e.getValue().dirFlg) {
														// 管理しているWatchKeyを削除.
														wk = kman.remove(p);
														if(wk != null) {
															wman.remove(wk);
															wk.cancel();
															wk = null;
															//System.out.println("cancel watchKey:" + p);
														}
													}
													// 管理マネージャから削除.
													man.remove(p);
												}
											}
										} else {
											// ファイルのStat情報を削除.
											man.remove(name);
										}
									}
								} else if(StandardWatchEventKinds.OVERFLOW.equals(kind)) {
									// overflow.
									//System.out.println("kind: " + kind + " name:" + name + " contains:" + man.containsKey(name));
								}
							} catch(Throwable t) {
								t.printStackTrace();
								if (t instanceof InterruptedException) {
									endFlag = true;
									break;
								} else if(t instanceof ThreadDeath) {
									throw t;
								}
							}
						}
						if(wman.containsKey(watchKey)) {
							try {
								watchKey.reset();
								//System.out.println("watchKey.reset: " + wman.get(watchKey));
							} catch(Throwable t) {
								//t.printStackTrace();
							}
						}
						watchKey = null;
						//outMan(man);
						//outWatchKey(kman);
						//outWatchKey2(wman);
					} catch(Throwable t) {
						t.printStackTrace();
						if (t instanceof InterruptedException) {
							endFlag = true;
						} else if(t instanceof ThreadDeath) {
							throw t;
						}
					}
				}
			} catch(ThreadDeath td) {
				d = td;
			} catch(Throwable t) {
			} finally {
				try {
					sv.close();
				} catch(Exception e) {}
				exitFlag = true;
			}
			//System.out.println("exit");
			if (d != null) {
				throw d;
			}
		}
		/*
		private static final void outMan(Map<String, StatObject> o) {
			int n = 1;
			Iterator<String> it = o.keySet().iterator();
			while(it.hasNext()) {
				System.out.println("(" + (n++) + ") " + o.get(it.next()));
			}
		}
		
		private static final void outWatchKey(Map<String, WatchKey> o) {
			int n = 1;
			String s;
			WatchKey w;
			Iterator<String> it = o.keySet().iterator();
			while(it.hasNext()) {
				s = it.next();
				w = o.get(s);
				if(w == null) {
					System.out.println("(" + (n++) + ") warkKey: " + s + " null");
				} else {
					System.out.println("(" + (n++) + ") warkKey: " + s + " " + w.isValid());
				}
			}
		}
		
		private static final void outWatchKey2(Map<WatchKey, String> o) {
			int n = 1;
			String s;
			WatchKey w;
			Iterator<WatchKey> it = o.keySet().iterator();
			while(it.hasNext()) {
				w = it.next();
				if(w != null) {
					s = o.get(w);
					System.out.println("(" + (n++) + ") $warkKey: " + s + " " + w.isValid());
				}
			}
		}
		*/

	}
	/** 監視スレッド. **/
	private WatchThread wthread = null;
	
	/** ファイル状態管理. +*/
	private Map<String, StatObject> statMan = null;
	
	/** 監視Topディレクトリ名群. **/
	private List<String> topDirs = null;
	
	/** クローズフラグ. **/
	private final Flag closeFlag = new Flag();
	
	/**
	 * コンストラクタ.
	 * @param pathList 監視対象のパス名群を設定します.
	 */
	public WatchPath(String... pathList) {
		if(pathList == null || pathList.length == 0) {
			throw new RhiginException("No monitored path has been set.");
		}
		this.closeFlag.set(false);
		try {
			List<String> topDirs = new ObjectList<String>();
			int len = pathList.length;
			// topDirsを作成.
			// 指定した監視パス名がディレクトリで無い場合はエラー.
			for(int i = 0; i < len; i ++) {
				pathList[i] = pathList[i].trim();
				if(FileUtil.isDir(pathList[i])) {
					topDirs.add(FileUtil.getFullPath(pathList[i]));
				} else {
					throw new RhiginException("The specified directory \""
						+ pathList[i] + "\" does not exist.");
				}
			}
			Map<String, StatObject> man = new ConcurrentHashMap<String, StatObject>();
			Map<WatchKey, String> wm = new ConcurrentHashMap<WatchKey, String>();
			Map<String, WatchKey> km = new ConcurrentHashMap<String, WatchKey>();
			List<String> targetWatchList = new ObjectList<String>();
			for(int i = 0; i < len; i ++) {
				// このフォルダのStat情報を登録.
				pathList[i] = FileUtil.getFullPath(pathList[i]);
				man.put(pathList[i], new StatObject(topDirs, pathList[i]));
				// Watch対象のディレクトリとして登録.
				targetWatchList.add(pathList[i]);
				// 監視指定したTOPディレクトリを保存.
				statByDir(targetWatchList, man, topDirs, pathList[i]);
			}
			WatchService watcher = FileSystems.getDefault().newWatchService();
			createWatch(wm, km, watcher, targetWatchList);
			WatchThread wt = new WatchThread(watcher, wm, km, man, topDirs, closeFlag);
			wt.startThread();
			this.statMan = man;
			this.topDirs = topDirs;
			this.wthread = wt;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	// 監視対象フォルダー内の初期Stat作成処理.
	protected static final void statByDir(List<String> targetWatchList, Map<String, StatObject> man, List<String> tdir, String dir)
		throws Exception {
		Path n;
		String name;
		dir = FileUtil.getFullPath(dir) + "/" ;
		File d = new File(dir);
		String[] list = d.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len ; i ++) {
			// 最初に.が付くファイルは対象外.
			if(list[i].startsWith(".")) {
				continue;
			}
			n = Paths.get(name = dir + list[i]);
			if(Files.isDirectory(n)) {
				targetWatchList.add(name);
				man.put(name, new StatObject(tdir, name, n));
				statByDir(targetWatchList, man, tdir, name);
			} else if(Files.isRegularFile(n)) {
				man.put(name, new StatObject(tdir, name, n));
			}
		}
	}
	
	// Watch情報を作成.
	protected static final void createWatch(Map<WatchKey, String> watchMap,  Map<String, WatchKey> keyMap, WatchService service, List<String> dirs)
		throws IOException {
		int len = dirs.size();
		for(int i = 0; i < len; i ++) {
			// 監視するディレクトリを設定.
			createWatch(watchMap, keyMap, service, Paths.get(dirs.get(i)));
		}
	}
	
	// Watch情報を作成.
	protected static final void createWatch(Map<WatchKey, String> watchMap, Map<String, WatchKey> keyMap, WatchService service, Path path)
		throws IOException {
		WatchEvent.Modifier[] extModifiers = new WatchEvent.Modifier[] {};
		
		// 監視するイベントを登録
		WatchKey k = path.register(service, new Kind[]{
				StandardWatchEventKinds.ENTRY_CREATE // 作成
				,StandardWatchEventKinds.ENTRY_MODIFY // 変更
				,StandardWatchEventKinds.ENTRY_DELETE // 削除
				//,StandardWatchEventKinds.OVERFLOW // 特定不能時
			},
			extModifiers); // オプションの修飾子、不要ならば空配列
		
		// WatchManagerに監視イベントで作成されたWatchKeyをキーとして
		// 監視フォルダ名として記録する.
		watchMap.put(k, path.toString());
		keyMap.put(path.toString(), k);
	}
	
	/**
	 * クローズ処理.
	 */
	public void close() {
		if(!closeFlag.setToGetBefore(true)) {
			if(wthread != null) {
				wthread.stopThread();
			}
			wthread = null;
			statMan = null;
			topDirs = null;
		}
	}
	
	// クローズチェック.
	private void checkClose() {
		if(closeFlag.get()) {
			throw new RhiginException("Already closed.");
		}
	}
	
	/**
	 * クローズされているかチェック.
	 * @return
	 */
	public boolean isClose() {
		return closeFlag.get();
	}
	
	/**
	 * 監視登録パス一覧を取得.
	 * @return
	 */
	public List<String> getTopPaths() {
		checkClose();
		return topDirs;
	}
	
	/**
	 * このパスが、WatchPath対象であるかチェック.
	 * @param name
	 * @return
	 */
	public boolean isTopPath(String name) {
		checkClose();
		try {
			if(!name.startsWith("/")) {
				name = FileUtil.getFullPath(name);
			}
			int len = topDirs.size();
			for(int i = 0; i < len; i ++) {
				if(name.startsWith(topDirs.get(i))) {
					return true;
				}
			}
			return false;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
		
	}
	
	/**
	 * 指定パスの情報が存在するかチェック.
	 * @param name
	 * @return
	 */
	public boolean contains(String name) {
		checkClose();
		try {
			if(!name.startsWith("/")) {
				return statMan.containsKey(FileUtil.getFullPath(name));
			}
			return statMan.containsKey(name);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * 指定パスのStatObjectを取得.
	 * @param name
	 * @return
	 */
	public StatObject get(String name) {
		checkClose();
		try {
			if(!name.startsWith("/")) {
				return statMan.get(FileUtil.getFullPath(name));
			}
			return statMan.get(name);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * 指定パスの相対パスを取得.
	 * @param name
	 * @return
	 */
	public String getPath(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getPath();
		}
		return null;
	}
	
	/**
	 * トップディレクトリを取得.
	 * @param name
	 * @return
	 */
	public String getTopDir(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getTopDir();
		}
		return null;
	}
	
	/**
	 * トップディレクトリを取得.
	 * @param name
	 * @return
	 */
	public String getFullPath(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getFullPath();
		}
		return null;
	}
	
	/**
	 * 対象パスがファイル化チェック.
	 * @param name
	 * @return
	 */
	public boolean isFile(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.isFile();
		}
		return false;
	}
	
	/**
	 * 対象パスがディレクトリかチェック.
	 * @param name
	 * @return
	 */
	public boolean isDir(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.isDirectory();
		}
		return false;
	}
	
	/**
	 * atimeを取得.
	 * @param name
	 * @return
	 */
	public long getAtime(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getAtime();
		}
		return -1L;
	}

	/**
	 * mtimeを取得.
	 * @param name
	 * @return
	 */
	public long getMtime(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getMtime();
		}
		return -1L;
	}

	/**
	 * barthTimeを取得.
	 * @param name
	 * @return
	 */
	public long getBarthTime(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getBarthTime();
		}
		return -1L;
	}
	
	/**
	 * ファイル長を取得.
	 * @param name
	 * @return
	 */
	public long getLength(String name) {
		StatObject stat = get(name);
		if(stat != null) {
			return stat.getLength();
		}
		return -1L;
	}
}

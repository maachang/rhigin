package rhigin.scripts.objects;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.FixedKeyValues;
import rhigin.util.FixedSearchArray;
import rhigin.util.Stats;

/**
 * ファイル関連のオブジェクト.
 */
public class FileObject {
	public static final String OBJECT_NAME = "File";
	public static final String STATS_OBJECT_NAME = "Stats";
	
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				if (args.length >= 1) {
					switch (type) {
					case 0: // isDir
						return FileUtil.isDir("" + args[0]);
					case 1: // isFile
						return FileUtil.isFile("" + args[0]);
					case 2: // readByString
						if (args.length >= 2) {
							return FileUtil.getFileString("" + args[0], "" + args[1]);
						} else {
							return FileUtil.getFileString("" + args[0], "UTF8");
						}
					case 3: // writeByString
						if (args.length >= 4) {
							FileUtil.setFileString(Converter.convertBool(args[0]), "" + args[1], "" + args[2],
									"" + args[3]);
						} else if (args.length >= 3) {
							FileUtil.setFileString(true, "" + args[0], "" + args[1], "" + args[2]);
						} else if (args.length >= 2) {
							FileUtil.setFileString(true, "" + args[0], "" + args[1], "UTF8");
						}
						break;
					case 4: // removeFile
						FileUtil.removeFile("" + args[0]);
						return Undefined.instance;
					case 5: // removeDir
						FileUtil.removeFile("" + args[0]);
						return Undefined.instance;
					case 6: // delete
						FileUtil.delete("" + args[0]);
						return Undefined.instance;
					case 7: // list
					{
						String[] ret = FileUtil.list("" + args[0]);
						if(ret == null || ret.length == 0) {
							return new JavaScriptable.ReadArray(new String[] {});
						}
						return new JavaScriptable.ReadArray(ret);
					}
					case 8: // length
						return FileUtil.getFileLength("" + args[0]);
					case 9: // birthtime
						return FileUtil.birthtime("" + args[0]);
					case 10: // atime
						return FileUtil.atime("" + args[0]);
					case 11: // mtime
						return FileUtil.mtime("" + args[0]);
					case 12: // rename
						if (args.length >= 2) {
							FileUtil.move("" + args[0], "" + args[1]);
						}
						break;
					case 13: // copy
						if (args.length >= 2) {
							FileUtil.copy("" + args[0], "" + args[1]);
						}
						break;
					case 14: // readFile
						return FileUtil.getFile("" + args[0]);
					case 15: // writeFile
						if (args.length >= 3) {
							if (args[2] instanceof InputStream) {
								setFileByInputStream(Converter.convertBool(args[0]), "" + args[1],
										(InputStream) args[2]);
							} else {
								FileUtil.setFile(Converter.convertBool(args[0]), "" + args[1], toBinary(args[2]));
							}
						} else if (args.length >= 2) {
							if (args[1] instanceof InputStream) {
								setFileByInputStream(true, "" + args[0], (InputStream) args[1]);
							} else {
								FileUtil.setFile(true, "" + args[0], toBinary(args[1]));
							}
						}
						break;
					case 16: // fullPath
						return FileUtil.getFullPath("" + args[0]);
					case 17: // fileName
						return FileUtil.getFileName("" + args[0]);
					case 18: // stat
						return new RhiginInstanceObject(STATS_OBJECT_NAME, FUNCTIONS, new Stats("" + args[0]));
					case 19: // inputStream
						return new FileInputStream("" + args[0]);
					case 20: // outputStream
						return new FileOutputStream("" + args[0]);
					}
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return argsError(args);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "isDir";
			case 1:
				return "isFile";
			case 2:
				return "readByString";
			case 3:
				return "writeByString";
			case 4:
				return "removeFile";
			case 5:
				return "removeDir";
			case 6:
				return "delete";
			case 7:
				return "list";
			case 8:
				return "length";
			case 9:
				return "birthtime";
			case 10:
				return "atime";
			case 11:
				return "mtime";
			case 12:
				return "rename";
			case 13:
				return "copy";
			case 14:
				return "readFile";
			case 15:
				return "writeFile";
			case 16:
				return "fullPath";
			case 17:
				return "fileName";
			case 18:
				return "stat";
			case 19:
				return "inputStream";
			case 20:
				return "outputStream";
			}
			return "unknown";
		}

		// 引数チェック.
		private final Object argsError(Object[] args) {
			if (!(args.length >= 1)) {
				argsException(OBJECT_NAME);
			}
			switch (type) {
			case 3:
				if (!(args.length >= 2)) {
					argsException(OBJECT_NAME);
				}
				break;
			case 12:
				if (!(args.length >= 2)) {
					argsException(OBJECT_NAME);
				}
				break;
			case 13:
				if (!(args.length >= 2)) {
					argsException(OBJECT_NAME);
				}
				break;
			case 15:
				if (!(args.length >= 2)) {
					argsException(OBJECT_NAME);
				}
				break;
			}
			return Undefined.instance;
		}

		private static final byte[] toBinary(Object o) throws Exception {
			if (o == null) {
				throw new IOException("There is no binary to output.");
			} else if (o instanceof String) {
				return ((String) o).getBytes("UTF8");
			} else if (o instanceof byte[]) {
				return (byte[]) o;
			} else if (o instanceof InputStream) {
				int len;
				byte[] b = new byte[1024];
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				InputStream in = (InputStream) o;
				while ((len = in.read(b)) != -1) {
					bo.write(b, 0, len);
				}
				return bo.toByteArray();
			}
			throw new RhiginException(500, "Argument is not binary.");
		}

		// inputStreamでファイル出力.
		private static final void setFileByInputStream(boolean newFile, String name, InputStream in) throws Exception {
			if (in == null) {
				throw new IOException("There is no inputStream to output.");
			}
			BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(name, !newFile));
			try {
				int len;
				byte[] b = new byte[1024];
				while ((len = in.read(b)) != -1) {
					bo.write(b, 0, len);
				}
				bo.flush();
				bo.close();
				bo = null;
			} finally {
				if (bo != null) {
					try {
						bo.close();
					} catch (Exception e) {
					}
				}
			}
		}
	};
	
	// メソッド名群.
	private static final String[] FUNCTION_NAMES = new String[] {
		"isDirectory"
		,"isFile"
		,"isSymbolicLink"
		,"mode"
		,"atimeMS"
		,"mtimeMS"
		,"birthtimeMS"
		,"atime"
		,"mtime"
		,"birthtime"
		,"object"
	};

	// メソッド生成処理.
	private static final ObjectFunction FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(FUNCTION_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new StatsExecute(no, (Stats)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};

	// stats.
	private static final class StatsExecute extends RhiginFunction {
		final int type;
		final Stats stats;

		StatsExecute(int t, Stats stats) {
			this.type = t;
			this.stats = stats;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0:
					return stats.isDirectory();
				case 1:
					return stats.isFile();
				case 2:
					return stats.isSymbolicLink();
				case 3:
					return stats.mode();
				case 4:
					return stats.atime();
				case 5:
					return stats.mtime();
				case 6:
					return stats.birthtime();
				case 7:
					return ctx.newObject(scope, "Date", new Object[] { stats.atime() });
				case 8:
					return ctx.newObject(scope, "Date", new Object[] { stats.mtime() });
				case 9:
					return ctx.newObject(scope, "Date", new Object[] { stats.birthtime() });
				case 10:
					return stats;
				}
				return Undefined.instance;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
		}

		@Override
		public final String getName() {
			return FUNCTION_NAMES[type];
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME, new RhiginFunction[] {
		new Execute(0), new Execute(1), new Execute(2), new Execute(3), new Execute(4),
		new Execute(5), new Execute(6), new Execute(7), new Execute(8), new Execute(9),
		new Execute(10), new Execute(11), new Execute(12), new Execute(13), new Execute(14),
		new Execute(15), new Execute(16), new Execute(17), new Execute(18), new Execute(19),
		new Execute(20) });

	public static final RhiginObject getInstance() {
		return THIS;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, FileObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, FileObject.getInstance());
	}
}

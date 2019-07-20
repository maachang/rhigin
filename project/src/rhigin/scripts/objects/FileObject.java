package rhigin.scripts.objects;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.Stats;

/**
 * ファイル関連のオブジェクト.
 */
public class FileObject{
	private static final class Execute extends RhiginFunction {
		final int type;
		Execute(int t) {
			this.type = t;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				if(args.length >= 1) {
					switch(type) {
					case 0: return FileUtil.isDir(""+args[0]);
					case 1: return FileUtil.isFile(""+args[0]);
					case 2: if(args.length >= 2) {
							return FileUtil.getFileString(""+args[0], ""+args[1]);
						} else {
							return FileUtil.getFileString(""+args[0], "UTF8");
						}
					case 3: if(args.length >= 4) {
							FileUtil.setFileString(Converter.convertBool(args[0]), ""+args[1], ""+args[2], ""+args[3]);
						} else if(args.length >= 3) {
							FileUtil.setFileString(true, ""+args[0], ""+args[1], ""+args[2]);
						} else if(args.length >= 2) {
							FileUtil.setFileString(true, ""+args[0], ""+args[1], "UTF8");
						}
						break;
					case 4: FileUtil.removeFile(""+args[0]);
					case 5: FileUtil.removeFile(""+args[0]);
					case 6: FileUtil.delete(""+args[0]);
					case 7: return FileUtil.list(""+args[0]);
					case 8: return FileUtil.getFileLength(""+args[0]);
					case 9: return FileUtil.birthtime(""+args[0]);
					case 10: return FileUtil.atime(""+args[0]);
					case 11: return FileUtil.mtime(""+args[0]);
					case 12: if(args.length >= 2) {
							FileUtil.move(""+args[0], ""+args[1]);
						}
						break;
					case 13: if(args.length >= 2) {
							FileUtil.copy(""+args[0], ""+args[1]);
						}
						break;
					case 14: return FileUtil.getFile(""+args[0]);
					case 15: if(args.length >= 3) {
							FileUtil.setFile(Converter.convertBool(args[0]), ""+args[1], toBinary(args[2]));
						} else if(args.length >= 2) {
							FileUtil.setFile(true, ""+args[0], toBinary(args[1]));
						}
						break;
					case 16: return FileUtil.getFullPath(""+args[0]);
					case 17: return FileUtil.getFileName(""+args[0]);
					case 18:
						Stats stats = new Stats("" + args[0]);
						return new RhiginObject("Stats", new RhiginFunction[] {
							new StatsExecite(0, stats), new StatsExecite(1, stats), new StatsExecite(2, stats),
							new StatsExecite(3, stats), new StatsExecite(4, stats), new StatsExecite(5, stats),
							new StatsExecite(6, stats), new StatsExecite(7, stats), new StatsExecite(8, stats),
							new StatsExecite(9, stats)
						});
					case 19: return new FileInputStream("" + args[0]);
					case 20: return new FileOutputStream("" + args[0]);
					}
				}
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "isDir";
			case 1: return "isFile";
			case 2: return "readByString";
			case 3: return "writeByString";
			case 4: return "removeFile";
			case 5: return "removeDir";
			case 6: return "delete";
			case 7: return "birthtime";
			case 8: return "atime";
			case 9: return "mtime";
			case 10: return "size";
			case 11: return "time";
			case 12: return "rename";
			case 13: return "copy";
			case 14: return "readFile";
			case 15: return "writeFile";
			case 16: return "fullPath";
			case 17: return "fileName";
			case 18: return "stat";
			case 19: return "inputStream";
			case 20: return "outputStream";
			}
			return "unknown";
		}
		
		private static final byte[] toBinary(Object o)
			throws Exception {
			if(o instanceof String) {
				return ((String)o).getBytes("UTF8");
			} else if(o instanceof byte[]) {
				return (byte[])o;
			} else if(o instanceof InputStream) {
				int len;
				byte[] b = new byte[1024];
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				InputStream in = (InputStream)o;
				while((len = in.read(b)) != -1) {
					bo.write(b, 0, len);
				}
				return bo.toByteArray();
			}
			throw new RhiginException(500, "引数がバイナリではありません");
		}
	};
	
	// オブジェクトリスト.
	private static final RhiginFunction[] list = {
		new Execute(0), new Execute(1), new Execute(2), new Execute(3),
		new Execute(4), new Execute(5), new Execute(6), new Execute(7),
		new Execute(8), new Execute(9), new Execute(10), new Execute(11),
		new Execute(12), new Execute(13), new Execute(14), new Execute(15),
		new Execute(16), new Execute(17), new Execute(18), new Execute(19),
		new Execute(20)
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("File", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
	
	// stats.
	private static final class StatsExecite extends RhiginFunction {
		final int type;
		final Stats stats;
		StatsExecite(int t, Stats stats) {
			this.type = t;
			this.stats = stats;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch(type) {
				case 0: return stats.isDirectory();
				case 1: return stats.isFile();
				case 2: return stats.isSymbolicLink();
				case 3: return stats.mode();
				case 4: return stats.atime();
				case 5: return stats.mtime();
				case 6: return stats.birthtime();
				case 7: return ctx.newObject(scope, "Date", new Object[] {stats.atime()});
				case 8: return ctx.newObject(scope, "Date", new Object[] {stats.mtime()});
				case 9: return ctx.newObject(scope, "Date", new Object[] {stats.birthtime()});
				}
				return Undefined.instance;
			} catch(Exception e) {
				throw new RhiginException(500, e);
			}
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "isDirectory";
			case 1: return "isFile";
			case 2: return "isSymbolicLink";
			case 3: return "mode";
			case 4: return "atimeMS";
			case 5: return "mtimeMS";
			case 6: return "birthtimeMS";
			case 7: return "atime";
			case 8: return "mtime";
			case 9: return "birthtime";
			}
			return "unknown";
		}
	};
}

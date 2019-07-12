package rhigin.logs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import rhigin.RhiginConstants;
import rhigin.util.Converter;

/**
 * RhiginLogファクトリ.
 */
public class LogFactory {
	
	// 出力先ログ.
	private static String outDir = RhiginConstants.DIR_LOG;
	
	// ログ出力レベル.
	// 0: trace, 1: debug, 2: info, 3: warn, 4: error, 5: fatal.
	private static int outLogLevel = 1;

	// １ファイルの最大サイズ(1MByte).
	private static long outFileSize = 0x00100000;
	
	// 個別のログ設定用のコンフィグ.
	private static Map<String,Object> config = null;
	
	// 日付情報を取得.
	@SuppressWarnings("deprecation")
	private static final String dateString(Date d) {
		String n;
		return new StringBuilder().append((d.getYear() + 1900)).append("-").
			append("00".substring((n = ""+(d.getMonth()+1)).length())).append(n).append("-").
			append("00".substring((n = ""+d.getDate()).length())).append(n).
			toString();
	}
	
	// ログフォーマット情報を作成.
	@SuppressWarnings("deprecation")
	private static final String format(String type, Object[] args) {
		String n;
		Date d = new Date();
		StringBuilder buf = new StringBuilder();
		buf.append("[")
		.append(d.getYear() + 1900).append("/")
		.append("00".substring((n = ""+(d.getMonth()+1)).length())).append(n).append("/")
		.append("00".substring((n = ""+d.getDate()).length())).append(n).append(" ")
		.append("00".substring((n = ""+d.getHours()).length())).append(n).append(":")
		.append("00".substring((n = ""+d.getMinutes()).length())).append(n).append(":")
		.append("00".substring((n = ""+d.getSeconds()).length())).append(n).append(".")
		.append((n = ""+d.getTime()).substring(n.length()-3))
		.append("] [").append(type).append("] ");
		
		Object o;
		String nx = "";
		int len = (args == null) ? 0 : args.length;
		for(int i = 0; i < len; i ++) {
			if((o = args[i]) instanceof Throwable) {
				buf.append("\n").append(getStackTrace((Throwable)o));
				nx = "\n";
			} else {
				buf.append(nx).append(o).append(" ");
				nx = "";
			}
		}
		return buf.append("\n").toString();
	}
	
	// stackTraceを文字出力.
	private static final String getStackTrace(Throwable t) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			return sw.toString();
	}
	
	// 文字指定のログレベルを、数値に変換.
	private static final int strLogLevelByNumber(String level) {
		level = level.toLowerCase();
		switch(level) {
			case "trace": return 0;
			case "debug": return 1;
			case "dev": return 1;
			case "info": return 2;
			case "normal": return 2;
			case "warn": return 3;
			case "warning": return 3;
			case "error": return 4;
			case "fatal": return 5;
		}
		return 1;
	}
	
	// 対象のログレベルの数値を、文字変換.
	private static final String numberLogLevelByStr(int level) {
		switch(level) {
		case 0: return "TRACE";
		case 1: return "DEBUG";
		case 2: return "INFO";
		case 3: return "WARN";
		case 4: return "ERROR";
		case 5: return "FATAL";
		}
		return "DEBUG";
	}
	
	// ファイル追加書き込み.
	private static final void appendFile(String name, String out) {
		FileOutputStream o = null;
		try {
			byte[] b = out.getBytes("UTF8");
			o = new FileOutputStream(name, true);
			o.write(b);
			o.close();
			o = null;
		} catch(Exception e) {
			if(o != null) {
				try {
					o.close();
				} catch(Exception ee) {}
			}
		}
	}
		
	// ログ出力処理.
	@SuppressWarnings("deprecation")
	public static final void write(final String name,
		final int logLevel, final long fileSize, final int typeNo, final String logDir,
		final Object... args) {
		// 指定されたログレベル以下はログ出力させない場合.
		if(typeNo < logLevel) {
			return;
		}
		// ログ出力先がない場合は作成.
		final File dir = new File(logDir);
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
		
		final String format = format(numberLogLevelByStr(logLevel), args);
		final String fileName = name + ".log";
		final File stat = new File(logDir + fileName);
		final Date date = new Date(stat.lastModified());
		final Date now = new Date();
	
		// ファイルサイズの最大値が設定されていて、その最大値が増える場合.
		// また、現在のログファイルの日付が、現在の日付と一致しない場合.
		if(stat.isFile() && (
				(fileSize > 0 && stat.length() + format.length() > fileSize) ||
				((date.getYear() & 31) | ((date.getMonth() & 31) << 9) | ((date.getDate() & 31) << 18))
					!=
				((now.getYear() & 31) | ((now.getMonth() & 31) << 9) | ((now.getDate() & 31) << 18))
			)
		) {
			// 現在のログファイルをリネームして、新しいログファイルに移行する.
			int p, v;
			String n;
			int cnt = -1;
			File renameToStat = null;
			final String targetName = fileName + "." + dateString(date) + ".";
			
			// nameでjava内同期.
			final String sync = name.intern();
			synchronized(sync) {
				// 指定フォルダ内から、targetNameの条件とマッチするものを検索.
				String[] list = dir.list(new FilenameFilter() {
					public boolean accept(final File file, final String str) {
						return str.indexOf(targetName) == 0;
					}
				});
				// そこの一番高いカウント値＋１の値を取得.
				int len = (list == null) ? 0 : list.length;
				for(int i = 0; i < len; i ++) {
					n = list[i];
					p = n.lastIndexOf(".");
					v = Converter.parseInt(n.substring(p+1));
					if(cnt < v) {
						cnt = v;
					}
				}
				// 今回のファイルをリネーム.
				stat.renameTo(renameToStat = new File(logDir + targetName + (cnt+1)));
			}
			
			// リネーム先ファイル名.
			final String tname = logDir + targetName + (cnt+1);
			final File tstat = renameToStat;
			
			//gzip圧縮(スレッド実行).
			Thread t = new Thread() {
				public void run() {
					try {
						int len;
						byte[] b = new byte[1024];
						InputStream in = new BufferedInputStream(new FileInputStream(tname));
						OutputStream out = null;
						try {
							out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tname + ".gz")));
							while((len = in.read(b)) != -1) {
								out.write(b, 0, len);
							}
							out.flush();
							out.close();
							out = null;
							in.close();
							in = null;
							tstat.delete();
						} catch(Exception e) {}
						finally {
							if(out != null) {
								try { out.close(); } catch(Exception e) {}
							}
							if(in != null) {
								try { in.close(); } catch(Exception e) {}
							}
						}
					} catch(Exception e) {}
				}
			};
			t.setDaemon(true);
			t.start();
			t = null;
		}
		// ログ出力.
		appendFile(logDir + fileName, format);
		System.out.print(format);
	}
	
	/**
	 * ログ基本設定.
	 * @param level 出力させない基本ログレベルを設定します.
	 * @param fileSize ログ分割をする基本ファイルサイズを設定します.
	 * @param dir ログ出力先のディレクトリを設定します.
	 */
	public static final void setting(Object level, Long fileSize, String dir) {
		if(level != null) {
			if(level instanceof String) {
				level = strLogLevelByNumber((String)level);
			}
			outLogLevel = Converter.convertInt(level);
		}
		if(fileSize != null) {
			outFileSize = fileSize;
			outFileSize = outFileSize < 0 ? -1 : outFileSize;
		}
		if(dir != null && dir.length() != 0) {
			outDir = dir;
			if(outDir.lastIndexOf("/") != outDir.length() - 1) {
				outDir = outDir + "/";
			}
		}
	}

	/**
	 * Jsonでログ基本設定.
	 * @param json
	 */
	public static final void setting(Map<String, Object> json) {
		Object level = json.get("level");
		Long fileSize = null;
		String dir = null;
		
		if(json.containsKey("maxFileSize")) {
			if(Converter.isNumeric(json.get("maxFileSize"))) {
				fileSize = Converter.convertLong(json.get("maxFileSize"));
			}
		}
		if(json.containsKey("logDir")) {
			dir = "" + json.get("logDir");
		}
		setting(level, fileSize, dir);
		
		// コンフィグ情報として保持.
		config = json;
	}
	
	/**
	 * デフォルトの設定ログレベルを取得.
	 * @return
	 */
	public static final int logLevel() {
		return outLogLevel;
	}

	/**
	 * デフォルトのログ出力先フォルダを取得.
	 * @return
	 */
	public static final String logDir() {
		return outDir;
	}

	/**
	 * デフォルトの１つのログファイルサイズを設定します.
	 * @return
	 */
	public static final long maxFileSize() {
		return outFileSize;
	}
	
	// ログ情報生成.
	public static final Log create() {
		return create("system", null, null);
	}
	
	// ログ情報生成.
	public static final Log create(String name) {
		return create(name, null, null);
	}

	// ログ情報生成.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Log create(String name, Object logLevel, Long fileSize) {
		if(name == null) {
			throw new NullPointerException();
		} else if(name.length() == 0) {
			throw new IllegalArgumentException();
		}
		// コンフィグ情報が存在する場合は、コンフィグ情報の内容を元に設定.
		if(config != null && (logLevel == null || fileSize == null)) {
			Object o = config.get(name);
			if(o instanceof Map) {
				Map<String, Object> logConf = (Map)o;
				if(logLevel == null) {
					logLevel = logConf.get("logLevel");
					if(logLevel != null && Converter.isNumeric(logLevel)) {
						logLevel = Converter.convertInt(logLevel);
					}
				}
				if(fileSize == null) {
					o = logConf.get("fileSize");
					if(o != null && Converter.isNumeric(o)) {
						fileSize = Converter.convertLong(o);
					}
				}
			}
		}
		int lv = outLogLevel;
		long fs = outFileSize;
		if(logLevel != null) {
			if(logLevel instanceof String) {
				logLevel = strLogLevelByNumber((String)logLevel);
			}
			lv = Converter.convertInt(logLevel);
		}
		if(fileSize != null) {
			fs = fileSize < 0 ? -1 : fileSize;
		}
		return new BaseLog(name, lv, fs, outDir);
	}
	
	/**
	 * ログ実装.
	 */
	private static final class BaseLog implements Log {
		private String name;
		private String logDir;
		private int logLevel;
		private long maxFileSize;
		BaseLog(String n, int lv, long fs, String ld) {
			name = n;
			logLevel = lv;
			maxFileSize = fs;
			logDir = ld;
		}

		@Override
		public void trace(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 0, logDir, args);
		}

		@Override
		public void debug(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 1, logDir, args);
		}

		@Override
		public void info(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 2, logDir, args);
		}

		@Override
		public void warn(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 3, logDir, args);
		}

		@Override
		public void error(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 4, logDir, args);
		}

		@Override
		public void fatal(Object... args) {
			LogFactory.write(name, logLevel, maxFileSize, 5, logDir, args);
		}

		@Override
		public boolean isTraceEnabled() {
			return logLevel <= 0;
		}

		@Override
		public boolean isDebugEnabled() {
			return logLevel <= 1;
		}

		@Override
		public boolean isInfoEnabled() {
			return logLevel <= 2;
		}

		@Override
		public boolean isWarnEnabled() {
			return logLevel <= 3;
		}

		@Override
		public boolean isErrorEnabled() {
			return logLevel <= 4;
		}

		@Override
		public boolean isFatalEnabled() {
			return logLevel <= 5;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return new StringBuilder("name:").append(name)
				.append(", logLevel:").append(numberLogLevelByStr(logLevel))
				.append(", fileSize:").append(maxFileSize)
				.append(", logDir:").append(logDir)
				.toString();
		}
	}
}

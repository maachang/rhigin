package rhigin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rhigin.RhiginException;

/**
 * 外部コマンドを実行.
 */
public class ExecCmd {
	private long timeout = 0;
	private String path;
	private String[] env;
	
	/**
	 * コンストラクタ.
	 */
	public ExecCmd() {
		this("./");
	}
	
	/**
	 * コンストラクタ.
	 * @param path コマンド実行時のパスを設定します.
	 */
	public ExecCmd(String path) {
		setPath(path);
	}
	
	/**
	 * コマンド実行時パスを設定.
	 * @param path コマンド実行時のパスを設定します.
	 */
	public ExecCmd setPath(String path) {
		this.path = path;
		return this;
	}
	
	/**
	 * コマンド実行時パスを取得.
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * コマンド実行時の環境変数を設定.
	 * @param args ２つの方法で設定出来ます.
	 *             java.util.Mapオブジェクトで設定します.
	 *             key, value ....  で複数設定可能です.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ExecCmd setEnv(Object... args) {
		String[] env = null;
		if(args.length == 1 && args[0] instanceof Map) {
			Object k;
			int n = 0;
			Map m = (Map)args[0];
			env = new String[m.size() << 1];
			Iterator it = m.keySet().iterator();
			while(it.hasNext()) {
				k = it.next();
				env[n++] = "" + k;
				env[n++] = "" + m.get(k);
			}
		} else if(args.length >= 2) {
			int len = args.length;
			int n = 0;
			env = new String[len];
			for(int i = 0; i < len; i += 2) {
				env[n++] = "" + args[i];
				env[n++] = "" + args[i+1];
			}
		}
		this.env = env;
		return this;
	}
	
	/**
	 * コマンド実行時の環境変数を取得.
	 * @return
	 */
	public String[] getEnv() {
		return env;
	}
	
	/**
	 * コマンド実行時のタイムアウト値を設定します.
	 * @param timeout ミリ秒で設定します.
	 *                0Lを設定した場合は、タイムアウトは設定しません.
	 * @return
	 */
	public ExecCmd setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}
	
	/**
	 * コマンド実行時のタイムアウト値を取得.
	 * @return
	 */
	public long getTimeout() {
		return this.timeout;
	}
	
	// コマンド内容を文字列変換します.
	private static final String outExecCmd(String[] s) {
		int len = s.length;
		StringBuilder buf = new StringBuilder("[");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append("\"").append(s[i]).append("\"");
		}
		return buf.append("]").toString();
	}
	
	// 処理結果のコンソールを取得.
	private static final List<String> getAllLine(Process p, String[] cmd, long timeout) {
		BufferedReader reader = null;
		try {
			String line;
			List<String> ret = new ObjectList<String>();
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			if(timeout <= 0L) {
				// タイムアウトなし.
				while((line = reader.readLine()) != null) {
					ret.add(line);
				}
			} else {
				// タイムアウトあり.
				long time = System.currentTimeMillis();
				while(true) {
					if(!p.isAlive()) {
						// プロセス終了の場合は、残りのコンソール出力を取得.
						while((line = reader.readLine()) != null) {
							ret.add(line);
						}
						break;
					} else if(reader.ready()) {
						// データが存在する場合は、情報を取得.
						if((line = reader.readLine()) != null) {
							ret.add(line);
						} else {
							// データの終端が見つかった場合は、終了.
							break;
						}
					} else if(System.currentTimeMillis() - time > timeout) {
						// タイムアウトを検知.
						throw new RhiginException("Process execution timeout detected: " + outExecCmd(cmd));
					} else {
						// タイムアウトじゃない場合は、少しスリープ.
						Thread.sleep(5L);
					}
				}
			}
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(Exception e) {}
			}
		}
	}
	
	/**
	 * コマンド実行.
	 * @param cmd コマンドを設定します.
	 *            "ls -la" と設定することができます.
	 *            "ls", "-la" と設定することも出来ます.
	 * @return
	 */
	public ResultCmd exec(String... cmd) {
		if(cmd == null || cmd.length == 0) {
			throw new RhiginException("Command information is not set.");
		}
		if(path == null || path.isEmpty()) {
			path = "./";
		}
		if(cmd.length == 1) {
			List<String> list = new ObjectList<String>();
			Converter.cutString(list, true, false, cmd[0], " \t\r\n");
			int len = list.size();
			String[] lst = new String[len];
			for(int i = 0; i < len ; i ++) {
				lst[i] = list.get(i);
			}
			cmd = lst;
		}
		Process p = null;
		try {
			List<String> out = null;
			ProcessBuilder pb = new ProcessBuilder(cmd);
			if(env != null && env.length > 0) {
				Map<String, String> emap = pb.environment();
				emap.clear();
				int len = env.length;
				for(int i = 0; i < len; i += 2) {
					emap.put(env[i], env[i+1]);
				}
			}
			pb.directory(new File(path));
			pb.redirectErrorStream(true);
			p = pb.start();
			pb = null;
			out = getAllLine(p, cmd, timeout);
			return new ResultCmd(p.exitValue(), out);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(p != null) {
				try {
					if(p.isAlive()) {
						p.destroyForcibly();
					}
				} catch(Exception e) {}
			}
		}
	}
	
	/**
	 * コマンド実行結果.
	 */
	public static final class ResultCmd {
		private int resultCode;
		private List<String> out;
		protected ResultCmd(int resultCode, List<String> out) {
			this.resultCode = resultCode;
			this.out = out;
		}
		
		/**
		 * コマンド結果のコードを取得.
		 * @return
		 */
		public int result() {
			return resultCode;
		}
		
		/**
		 * コンソールに出力された文字列を取得.
		 * @return
		 */
		public List<String> getOut() {
			return out;
		}
		
		/**
		 * コンソールに文字が出力されたかチェック.
		 * @return
		 */
		public boolean isOut() {
			return out.size() > 0;
		}
	}
}

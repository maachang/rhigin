package rhigin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
			env = new String[m.size()];
			Iterator it = m.keySet().iterator();
			while(it.hasNext()) {
				k = it.next();
				env[n++] = "" + k + "=" + m.get(k);
			}
		} else if(args.length >= 2) {
			int len = args.length;
			int n = 0;
			env = new String[len >> 1];
			for(int i = 0; i < len; i += 2) {
				env[n++] = "" + args[i] + "=" + args[i+1];
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
	
	// 処理結果のコンソールを取得.
	private static final List<String> getLine(InputStream in)
		throws IOException {
		try {
			String line;
			List<String> ret = new ObjectList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while((line = reader.readLine()) != null) {
				ret.add(line);
			}
			return ret;
		} finally {
			try {
				in.close();
			} catch(Exception e) {}
		}
	}
	
	// コマンド内容を文字列変換します.
	private static final String outString(String[] s) {
		int len = s.length;
		StringBuilder buf = new StringBuilder("[");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append(s[i]);
		}
		return buf.append("]").toString();
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
			p = Runtime.getRuntime().exec(cmd, env, new File(path));
			if(timeout > 0) {
				if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
					if(p.isAlive()) {
						p.destroyForcibly();
					}
					throw new RhiginException("Process execution timeout detected: " + outString(cmd));
				}
			}
			ResultCmd ret = new ResultCmd(p.waitFor(), getLine(p.getInputStream()), getLine(p.getErrorStream()));
			if(p.isAlive()) {
				p.destroyForcibly();
			}
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * コマンド実行結果.
	 */
	public static final class ResultCmd {
		private int resultCode;
		private List<String> out;
		private List<String> err;
		protected ResultCmd(int resultCode, List<String> out, List<String> err) {
			this.resultCode = resultCode;
			this.out = out;
			this.err = err;
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
		
		/**
		 * コンソールに出力されるエラー文字列を取得.
		 * @return
		 */
		public List<String> getErr() {
			return err;
		}
		
		/**
		 * コンソールにエラー文字が出力されたかチェック.
		 * @return
		 */
		public boolean isErr() {
			return err.size() > 0;
		}
	}
}

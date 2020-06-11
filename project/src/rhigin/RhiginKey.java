package rhigin;

import java.util.Map;

import rhigin.http.execute.RhiginExecuteClientByAccessKey;
import rhigin.keys.RhiginAccessKeyClient;
import rhigin.scripts.JsonOut;
import rhigin.util.Alphabet;
import rhigin.util.Args;
import rhigin.util.Converter;

/**
 * RhiginAccessKeyClientのコマンド実行.
 */
public class RhiginKey {
	public static final void main(String[] args) throws Exception {
		Args params = Args.set(args);
		if(viewArgs(params)) {
			System.exit(0);
			return;
		}
		RhiginKey r = new RhiginKey();
		int ret = r.execute(params);
		System.exit(ret);
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs(Args params) {
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("-h", "--help")) {
			help();
			return true;
		}
		return false;
	}
	
	// ヘルプ情報を表示.
	private static final void help() {
		System.out.println("rkey [-c] [-u] [-k]");
		System.out.println(" Configure key management for rhigin access key client.");
		System.out.println("  [-c] [--cmd] {name}");
		System.out.println("    In {name}, specify the execution command with the following command.");
		System.out.println("     home or create");
		System.out.println("      Generate a new access key for the specified URL.");
		System.out.println("       rkeys -c create -u 192.168.0.100:3120");
		System.out.println("       rkeys -c home -u 192.168.0.100:3120");
		System.out.println("     config");
		System.out.println("      Generate a new access key for the specified URL.");
		System.out.println("      Generate it under '/conf/accessKey.json'.");
		System.out.println("       rkeys -c config -u 192.168.0.100:3120");
		System.out.println("     delete");
		System.out.println("      Delete the access key of the specified URL.");
		System.out.println("       rkeys -c delete -u 192.168.0.100:3120 -k xxxxxx....");
		System.out.println("     use");
		System.out.println("      Check if the access key of the specified URL matches.");
		System.out.println("       rkeys -c use -u 192.168.0.100:3120 -k xxxxxx....");
		System.out.println("     urls");
		System.out.println("      Display a list of registered URLs.");
		System.out.println("       rkeys -c urls");
		System.out.println("     keys");
		System.out.println("      Get the AccessKey list of the specified URL.");
		System.out.println("       rkeys -c keys -u 192.168.0.100:3120");
		System.out.println("  [-u] [--url] {url}");
		System.out.println("    Set Rhigin server URL of AccessKey management.");
		System.out.println("  [-k] [--key] {accessKey}");
		System.out.println("    Set the target AccessKey.");
	}
	
	// 処理結果のJSONを取得.
	@SuppressWarnings({"rawtypes" })
	private static final Object getResult(Object r, String key) {
		if(r instanceof Map) {
			Object ret = ((Map)r).get(key);
			if(ret != null) {
				return ret;
			}
		}
		return null;
	}
	
	// 実行処理.
	private final int execute(Args params) {
		String cmd = null;
		try {
			cmd = params.get("-c", "--cmd");
			if(cmd == null || cmd.isEmpty()) {
				System.out.println("command is required.");
				System.out.println();
				help();
				return 1;
			}
			// 初期化処理.
			RhiginStartup.init(false, false);
			String url = params.get("-u", "--url");
			String key = params.get("-k", "--key");
			RhiginExecuteClientByAccessKey cl = RhiginExecuteClientByAccessKey.getInstance();
			if(Alphabet.eq("create", cmd) || Alphabet.eq("home", cmd)) {
				// Homeファイルに生成アクセスキー情報の登録.
				Object o = cl.createByHome(url);
				System.out.println(JsonOut.toString(o));
				return 0;
			} else if(Alphabet.eq("config", cmd)) {
				// ConfigファイルのAccessKey.jsonに生成アクセスキー情報の登録.
				Object o = cl.createByConf(url);
				System.out.println(JsonOut.toString(o));
				return 0;
			} else if(Alphabet.eq("delete", cmd)) {
				// 
				Object o;
				if(key == null || key.isEmpty()) {
					o = cl.delete(url);
				} else {
					o = cl.delete(url, key);
				}
				System.out.println(JsonOut.toString(o));
				if(Converter.convertBool(getResult(o, "result"))) {
					return 0;
				} else {
					return 1;
				}
			} else if(Alphabet.eq("use", cmd)) {
				Object o;
				if(key == null || key.isEmpty()) {
					o = cl.isAccessKey(url);
				} else {
					o = cl.isAccessKey(url, key);
				}
				System.out.println(JsonOut.toString(o));
				return 0;
			} else if(Alphabet.eq("urls", cmd)) {
				Object o = RhiginAccessKeyClient.getInstance().getUrls();
				System.out.println(JsonOut.toString(o));
				return 0;
			} else if(Alphabet.eq("keys", cmd)) {
				if(url != null && !url.isEmpty()) {
					Object o = RhiginAccessKeyClient.getInstance().getKeys(url);
					System.out.println(JsonOut.toString(o));
					return 0;
				}
				System.out.println("null");
				return 1;
			}
			System.out.println("Unsupported command \"" + cmd + "\".");
			return 1;
		} catch(Throwable t) {
			if(cmd != null) {
				System.out.println("error command \"" + cmd + "\".");
				System.out.println();
			}
			t.printStackTrace();
			return 1;
		}
	}

}

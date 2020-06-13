package rhigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rhigin.http.HttpConstants;
import rhigin.http.execute.RhiginExecuteClientByJs;
import rhigin.keys.RhiginAccessKeyClient;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.JsonOut;
import rhigin.util.Alphabet;
import rhigin.util.Args;
import rhigin.util.ArrayMap;
import rhigin.util.ConsoleInKey;
import rhigin.util.Converter;
import rhigin.util.FileUtil;

/**
 * Remote先のRhiginサーバでJavascript実行を行うコマンド.
 */
public class RhiginRjs {
	public static final void main(String[] args) throws Exception {
		Args params = Args.set(args);
		if(viewArgs(params)) {
			System.exit(0);
			return;
		}
		int ret = 0;
		RhiginRjs r = new RhiginRjs();
		if(!r.init(params)) {
			System.exit(1);
			return;
		}
		String f = params.get("-j", "--js", "--javascript");
		if(f != null && !f.isEmpty()) {
			ret = r.executeFile(params, f);
		} else {
			ret = r.executeConsole(params);
		}
		System.exit(ret);
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs(Args params) {
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("?", "--help")) {
			help();
			return true;
		}
		return false;
	}
	
	// ヘルプ情報を表示.
	private static final void help() {
		System.out.println("rjs [-j] [-u] [-k] [-a] [-h]");
		System.out.println(" Send Javascript to Rhigin server and execute it.");
		System.out.println("  [-j] [--js] {javascriptFileName}");
		System.out.println("    Specify the Javascript file to be sent.");
		System.out.println("     $ rjs -j ./xxxx.js");
		System.out.println("    If you do not specify the execution Javascript file,");
		System.out.println("    it will be console input.");
		System.out.println("  [-u] [--url] {url}");
		System.out.println("    Set Rhigin server URL of AccessKey management.");
		System.out.println("     $ rjs -u 127.0.0.1:3120");
		System.out.println("  [-k] [--key] {accessKey}");
		System.out.println("    If you want to set a specific AccessKey, set as follows.");
		System.out.println("     $ rjs -k @u8UO2RQmiBC5SaddhLzqGnIrab7tLjZ_TgMUD38Jyg_e");
		System.out.println("  [-a] [--auth] {authCode}");
		System.out.println("    If you want to set a specific AuthCode, set as follows.");
		System.out.println("     $ rjs -a iRrJdxnfuB@i6QUXN2RcPxYYxjGJ@32xqaK9MBYjmfF2feru@n7yb");
		System.out.println("  [-h] [--home] {homeFile}");
		System.out.println("    Set the target home file.");
		System.out.println("     $ rjs -h ~/.rhiginAccessKey2.json");
	}
	
	// 初期化関連処理.
	private final boolean init(Args params) {
		// 初期化処理.
		RhiginStartup.init(false, false);
		// AccessKeyClientの初期化.
		String homeFile = params.get("-h", "--home");
		if(homeFile == null || homeFile.isEmpty()) {
			RhiginAccessKeyClient.getInstance().init();
		} else {
			if(FileUtil.isDir(homeFile)) {
				System.out.println("The specified home file \"" + homeFile + "\" is a directory.");
				return false;
			}
			RhiginAccessKeyClient.getInstance().init(homeFile);
		}
		return true;
	}
	
	// jsスクリプトを実行.
	private final int executeJs(String url, String akey, String acode, String js) {
		int ret = 0;
		try {
			Map<String, Object> opt = new ArrayMap<String, Object>();
			opt.put("accessKey", akey);
			opt.put("authCode", acode);
			Object res = RhiginExecuteClientByJs.getInstance().send(url, js, opt);
			System.out.println(JsonOut.toString(res));
		} catch(Throwable e) {
			ret = 1;
			e.printStackTrace();
		}
		return ret;
	}
	
	// accessKey, authCode を取得.
	private final String[] getKeys(Args params) {
		String[] ret = null;
		String akey = params.get("-k", "--key", "--accessKey");
		String acode = params.get("-a", "--auth", "--authCode");
		if(akey == null || akey.isEmpty() || acode == null || acode.isEmpty()) {
			ret = RhiginAccessKeyClient.getInstance().get(
				RhiginAccessKeyUtil.getDomain(true, params.get("-u", "--url")));
		} else {
			ret = new String[] {akey, acode};
		}
		if(ret != null) {
			RhiginAccessKeyUtil.checkAccessKey(ret[0]);
			RhiginAccessKeyUtil.checkAuthCode(ret[1]);
		}
		return ret;
	}
	
	// [file]実行処理.
	private final int executeFile(Args params, String file) {
		int ret = 0;
		try {
			// ファイルを取得.
			String js = FileUtil.getFileString(file, "UTF8");
			// パラメータを取得.
			String url = RhiginAccessKeyUtil.getDomain(true, params.get("-u", "--url"));
			// AccessKey, AuthCodeを取得.
			String[] keys = getKeys(params);
			// Jsスクリプトの実行.
			return executeJs(url, keys[0], keys[1], js);
		} catch(Throwable e) {
			ret = 1;
			e.printStackTrace();
		}
		return ret;
	}
	
	// ゼロサプレスを取得.
	private static final String zero(int len) {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < len; i ++) {
			buf.append("0");
		}
		return buf.toString();
	}
	
	// cmd文字での key = value の valueを取得.
	private static final String getValue(String cmd, String key) {
		int p = cmd.indexOf("=", key.length());
		if(p == -1) {
			return null;
		}
		return cmd.substring(p + 1).trim();
	}
	
	// [console]実行処理.
	private final int executeConsole(Args params) {
		int ret = 0;
		ConsoleInKey console = null;
		try {
			// ゼロサプレス.
			String zero = zero(6); // max=99999.
			// パラメータを取得.
			String url = RhiginAccessKeyUtil.getDomain(true, params.get("-u", "--url"));
			// AccessKey, AuthCodeを取得.
			String[] keys = getKeys(params);
			
			System.out.println("" + RhiginConstants.NAME + " exectejs console version (" + RhiginConstants.VERSION + ")");
			System.out.println("");
			
			String cmd;
			String lineView;
			List<String> cmds = new ArrayList<String>();
			int tempNo = -1;
			
			// コンソールオブジェクト.
			console = new ConsoleInKey(".rcons");
			while(true) {
				if(tempNo == -1) {
					lineView = " " + zero.substring((""+(cmds.size() + 1)).length()) + (cmds.size() + 1);
				} else {
					lineView = ">" + zero.substring((""+(tempNo + 1)).length()) + (tempNo + 1);
				}
				// コマンド入力.
				cmd = console.readLine(lineView + "> ");
				if (cmd == null) {
					// null返却の場合は、ctrl+cの可能性があるので、
					// 終了処理をおこなってコンソール処理終了.
					System.out.println("");
					return 0;
				} else if ((cmd = cmd.trim()).length() == 0) {
					continue;
				} else if(cmd.startsWith(":")) {
					if (":?".equals(cmd) || Alphabet.eq(":help", cmd)) {
						System.out.println(":send [:run] Send the registered Javascript.");
						System.out.println();
						System.out.println(":end  [:close] Terminate javascript processing.");
						System.out.println();
						System.out.println(":url Set the URL of the connection destination.");
						System.out.println(" <set>");
						System.out.println( "  000001>:url = http://192.168.0.110:3120");
						System.out.println(" <get>");
						System.out.println( "  000001>:url");
						System.out.println( "  http://192.168.0.110:3120");
						System.out.println();
						System.out.println(":akey [:accessKey] Set the AccessKey of the connection destination.");
						System.out.println(" <set>");
						System.out.println( "  000001>:akey = qT5W8R5JK9JjKSObA1gNqrO1TVHic1vCsLpOijR4rROSE");
						System.out.println(" <get>");
						System.out.println( "  000001>:akey");
						System.out.println( "  qT5W8R5JK9JjKSObA1gNqrO1TVHic1vCsLpOijR4rROSE");
						System.out.println();
						System.out.println(":acode [:authCode] Set the AuthCode of the connection destination.");
						System.out.println(" <set>");
						System.out.println( "  000001>:acode = S99t@BGZ1L6VglQUUOiSDb7RH5CpQ8iVzdh3_M5mrltUgy0TMQZEd");
						System.out.println(" <get>");
						System.out.println( "  000001>:acode");
						System.out.println( "  S99t@BGZ1L6VglQUUOiSDb7RH5CpQ8iVzdh3_M5mrltUgy0TMQZEd");
						System.out.println();
						System.out.println(":list Display the script being registered.");
						System.out.println();
						System.out.println(":add Add a script to the specified line.");
						System.out.println(" <set>");
						System.out.println( "  000003>:add = 1");
						System.out.println( " >000001>var a = 100;");
						System.out.println( "  000004>");
						System.out.println();
						System.out.println(":update [:set] Replaces the script on the specified line.");
						System.out.println(" <set>");
						System.out.println( "  000003>:set = 1");
						System.out.println( " >000001>var a = 100;");
						System.out.println( "  000003>");
						System.out.println();
						System.out.println(":remove [:delete] Delete the script at the specified line.");
						System.out.println(" <set>");
						System.out.println( "  000003>:remove = 1");
						System.out.println( "  000002>");
						System.out.println();
						System.out.println(":clear Clear the script being registered.");
						System.out.println(" <set>");
						System.out.println( "  000003>:clear");
						System.out.println( "  000001>");
						System.out.println("");
					} else if (Alphabet.eq(":exit", cmd) || Alphabet.eq(":quit", cmd)) {
						System.out.println("");
						return 0;
					} else if (Alphabet.indexOf(cmd, ":url") == 0) {
						String s = getValue(cmd, ":url");
						if(s == null) {
							if(Alphabet.eq(cmd, ":url")) {
								System.out.println(url == null ? "127.0.0.1:" + HttpConstants.BIND_PORT : url);
							} else {
								System.out.println("error url");
							}
							continue;
						}
						url = RhiginAccessKeyUtil.getDomain(true, s);
					} else if (Alphabet.indexOf(cmd, ":akey") == 0 || Alphabet.indexOf(cmd, ":accesskey") == 0) {
						String s = Alphabet.indexOf(cmd, ":akey") == 0 ? getValue(cmd, ":akey") : getValue(cmd, ":accesskey");
						if(s == null) {
							if(Alphabet.eq(cmd, ":akey") || Alphabet.eq(cmd, ":accesskey")) {
								System.out.println(keys[0]);
							} else {
								System.out.println("error accessKey");
							}
							continue;
						}
						keys[0] = s;
					} else if (Alphabet.indexOf(cmd, ":acode") == 0 || Alphabet.indexOf(cmd, ":authcode") == 0) {
						String s = Alphabet.indexOf(cmd, ":acode") == 0 ? getValue(cmd, ":acode") : getValue(cmd, ":authcode");
						if(s == null) {
							if(Alphabet.eq(cmd, ":acode") || Alphabet.eq(cmd, ":authcode")) {
								System.out.println(keys[1]);
							} else {
								System.out.println("error authCode");
							}
						}
						keys[1] = s;
					} else if (Alphabet.indexOf(cmd, ":list") == 0) {
						int startNo = -1;
						if(Alphabet.eq(cmd, ":list")) {
							startNo = 1;
						} else {
							String s = getValue(cmd, ":list");
							if(!Converter.isNumeric(s)) {
								System.out.println("error line startNo: " + s);
								continue;
							}
							startNo = Converter.convertInt(s);
						}
						System.out.println();
						int len = cmds.size();
						for(int i = startNo - 1; i < len; i ++) {
							System.out.println(
									zero.substring((""+(i+1)).length()) + (i+1) + " " +
									cmds.get(i));
						}
					} else if (Alphabet.indexOf(cmd, ":add") == 0) {
						String s = getValue(cmd, ":add");
						if(Converter.isNumeric(s)) {
							tempNo = Converter.convertInt(s) - 1;
							if(tempNo < 0 || tempNo >= cmds.size()) {
								tempNo = -1;
								System.out.println("The additional line number is out of range: " + s);
								continue;
							}
							cmds.add(tempNo, "");
						} else {
							System.out.println("No additional line number.");
						}
					} else if (Alphabet.indexOf(cmd, ":update") == 0 || Alphabet.indexOf(cmd, ":set") == 0) {
						String s = Alphabet.indexOf(cmd, ":update") == 0 ? getValue(cmd, ":update") : getValue(cmd, ":set");
						if(Converter.isNumeric(s)) {
							tempNo = Converter.convertInt(s) - 1;
							if(tempNo < 0 || tempNo >= cmds.size()) {
								tempNo = -1;
								System.out.println("The update line number is out of range: " + s);
								continue;
							}
						} else {
							System.out.println("No update line number.");
						}
					} else if (Alphabet.indexOf(cmd, ":delete") == 0 || Alphabet.indexOf(cmd, ":remove") == 0) {
						String s = Alphabet.indexOf(cmd, ":delete") == 0 ? getValue(cmd, ":delete") : getValue(cmd, ":remove");
						if(Converter.isNumeric(s)) {
							int delNo = Converter.convertInt(s) - 1;
							if(delNo < 0 || delNo >= cmds.size()) {
								System.out.println("The delete line number is out of range: " + s);
								continue;
							}
							cmds.remove(delNo);
							tempNo = -1;
						} else {
							System.out.println("No delete line number.");
						}
					} else if (Alphabet.eq(":clear", cmd)) {
						cmds.clear();
					} else if (Alphabet.eq(":send", cmd) || Alphabet.eq(":run", cmd)) {
						StringBuilder buf = new StringBuilder();
						int len = cmds.size();
						for(int i = 0; i < len; i ++) {
							buf.append(cmds.get(i)).append("\n");
						}
						String js = buf.toString();
						cmds.clear();
						
						// 送信URLなどを出力.
						System.out.println("send url: " + url);
						System.out.println("accessKey: " + keys[0]);
						System.out.println("authCode:" + keys[1]);
						
						// Jsスクリプトの実行.
						return executeJs(url, keys[0], keys[1], js);
					} else {
						System.out.println("This command is not supported: " + cmd.substring(1).trim());
					}
					continue;
				} else if(tempNo != -1) {
					cmds.set(tempNo, cmd);
					tempNo = -1;
				} else {
					cmds.add(cmd);
				}
			}
			
		} catch(Throwable e) {
			ret = 1;
			e.printStackTrace();
		} finally {
			if(console != null) {
				try {
					console.close();
				} catch(Exception e) {}
			}
		}
		return ret;
	}
}

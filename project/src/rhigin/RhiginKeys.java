package rhigin;

import rhigin.util.Args;

/**
 * RhiginAccessKeyClientのコマンド実行.
 */
public class RhiginKeys {
	public static final void main(String[] args) throws Exception {
		Args params = Args.set(args);
		if(viewArgs()) {
			System.exit(0);
			return;
		}
		
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs() {
		Args params = Args.getInstance();
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("-h", "--help")) {
			System.out.println("rkeys [-c] [-u] [-k]");
			System.out.println(" Configure key management for rhigin access key client.");
			System.out.println("  [-c] [--cmd] {name}");
			System.out.println("    In {name}, specify the execution command with the following command.");
			System.out.println("     home");
			System.out.println("      Generate a new access key for the specified URL.");
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
			return true;
		}
		return false;
	}

}

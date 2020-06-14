package rhigin;

import rhigin.util.FileUtil;
import rhigin.util.RandomUUID;

/**
 * サーバIDを生成・取得. サーバIDは、rhiginが起動時(consoleやbatchも同じ)に存在しない場合は作成されて、永続化されます.
 */
public class RhiginServerId {
	private static final String SERVER_ID_FILE = "./.serverId";
	private static final int CREATE_SERVER_ID_COUNT = 1000000;
	private final RandomUUID uuid = new RandomUUID();

	public RhiginServerId() {
	}

	private static final RhiginServerId THIS = new RhiginServerId();

	public static final RhiginServerId getInstance() {
		return THIS;
	}

	public String nowId = null;

	/**
	 * サーバIDを生成. 既に存在する場合は上書き.
	 * 
	 * @return
	 */
	public synchronized String createId() {
		try {
			String id = uuid.getId(CREATE_SERVER_ID_COUNT).getUUID();
			FileUtil.setFileString(true, SERVER_ID_FILE, id, "UTF8");
			nowId = id;
			return id;
		} catch (Exception e) {
			throw new RhiginException(500, e);
		}
	}

	/**
	 * サーバIDが存在するかチェック.
	 * 
	 * @return boolean [true]の場合、存在します.
	 */
	public synchronized boolean isId() {
		return FileUtil.isFile(SERVER_ID_FILE);
	}

	/**
	 * サーバIDを取得. 存在しない場合は生成して取得.
	 * 
	 * @return
	 */
	public synchronized String getId() {
		if (nowId != null) {
			return nowId;
		}
		if (isId()) {
			try {
				nowId = FileUtil.getFileString(SERVER_ID_FILE, "UTF8");
				return nowId;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
		}
		return createId();
	}

	private static final void help() {
		System.out.println("$ rid [cmd]");
		System.out.println(" [cmd] The following commands are available.");
		System.out.println("   create : Generate and update with new ID.");
		System.out.println("   use    : Check if the ID already exists.");
		System.out.println("   get    : Get the ID, if it does not exist, create a new one.");
		System.out.println("   help   : Display help information.");
		System.out.println();
	}

	public static final void main(String[] args) throws Exception {
		RhiginServerId serverId = RhiginServerId.getInstance();
		if (args == null || args.length == 0) {
			System.out.println(serverId.getId());
			System.exit(0);
		} else if (args.length >= 1) {
			String cmd = ("" + args[0]).toLowerCase();
			if ("create".equals(cmd)) {
				System.out.println(serverId.createId());
				System.exit(0);
			} else if ("use".equals(cmd)) {
				System.out.println(serverId.isId());
				System.exit(0);
			} else if ("get".equals(cmd)) {
				System.out.println(serverId.getId());
				System.exit(0);
			} else if ("help".equals(cmd)) {
				help();
				System.exit(0);
			} else {
				System.out.println("Specified command does not exist:" + cmd);
				System.out.println();
				help();
				System.exit(1);
			}
		}
	}
}

package rhigin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import rhigin.http.HttpConstants;
import rhigin.util.Args;
import rhigin.util.Converter;
import rhigin.util.FileUtil;

/**
 * rhigin新規プロジェクト環境生成.
 */
public class RhiginProject {
	protected RhiginProject() {
	}

	public static final void main(String[] args) throws Exception {
		Args.set(args);
		RhiginProject o = new RhiginProject();
		try {
			if (o.execute(args)) {
				System.exit(0);
			} else {
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// プロジェクト実行.
	private final boolean execute(String[] args) throws Exception {
		Args params = Args.getInstance();
		// プロジェクト名を取得.
		String name = params.get("-n", "--name");

		// プロジェクト名が存在しないじゃ、ヘルプを表示.
		if (name == null || params.isValue("-h", "--help")) {
			if (name == null) {
				System.out.println("> Project name is required.");
				System.out.println("");
			}
			help();
			return false;
		}

		// バージョン情報を取得.
		String version = params.get("-v", "--version");
		// バージョン情報が存在しない場合.
		if (version == null) {
			version = "NONE";
		}

		// 必要なフォルダを生成.
		FileUtil.mkdirs(HttpConstants.ACCESS_PATH);
		FileUtil.mkdirs(RhiginConstants.DIR_CONFIG);
		FileUtil.mkdirs(RhiginConstants.DIR_LOG);
		FileUtil.mkdirs(RhiginConstants.DIR_LIB);
		FileUtil.mkdirs(RhiginConstants.DIR_JAR);

		// 必要なファイルを転送.
		FileUtil.rcpy("res/rhigin/projects/index.js", "./index.js");
		FileUtil.rcpy("res/rhigin/projects/conf/accessKey.json", RhiginConstants.DIR_CONFIG + "accessKey.json");
		FileUtil.rcpy("res/rhigin/projects/conf/ipPermission.json", RhiginConstants.DIR_CONFIG + "ipPermission.json");
		FileUtil.rcpy("res/rhigin/projects/conf/http.json", RhiginConstants.DIR_CONFIG + "http.json");
		FileUtil.rcpy("res/rhigin/projects/conf/log.json", RhiginConstants.DIR_CONFIG + "log.json");
		FileUtil.rcpy("res/rhigin/projects/conf/rhigin.json", RhiginConstants.DIR_CONFIG + "rhigin.json");

		// rhigin.jsonのファイルを書き換える.
		change("./conf/rhigin.json", "{{projectName}}", name);
		change("./conf/rhigin.json", "{{version}}", version);

		// OSに対する起動バッチファイルをコピー.
		//if (IsOs.getInstance().getOS() == IsOs.OS_WINNT || IsOs.getInstance().getOS() == IsOs.OS_WIN9X) {
			// windows用.
		//	FileUtil.rcpy("res/rhigin/projects/rhigin.cmd", "./rhigin.cmd");
		//	FileUtil.rcpy("res/rhigin/projects/rbatch.cmd", "./rbatch.cmd");

		//} else {
			// linux用.
			FileUtil.rcpy("res/rhigin/projects/rhigin", "./rhigin");
			FileUtil.rcpy("res/rhigin/projects/rbatch", "./rbatch");
			setExecPermission("./rhigin");
			setExecPermission("./rbatch");
		//}

		// プロジェクト作成完了.
		return true;
	}

	// ヘルプ表示.
	private final void help() {
		System.out.println("rproj -n [name] -v [version] -h");
		System.out.println("  -n (--name)    Set the project name. *Required");
		System.out.println("  -v (--version) Set project version.");
		System.out.println("  -h (--help)    Display help information.");
		System.out.println("");
		System.out.println("<exsample>");
		System.out.println("$ mkdir [project Directory]");
		System.out.println("$ cd [project Directory]");
		System.out.println("$ rproj -n [projectName]");
		System.out.println("");
		System.out.println("create a new project for \"rhigin\".");
		System.out.println("");
	}

	// 指定ファイル内の内容を変更.
	private final void change(String name, String keyword, String value) throws Exception {
		String file = FileUtil.getFileString(name, "UTF8");
		file = Converter.changeString(file, keyword, value);
		FileUtil.setFileString(true, name, file, "UTF8");
	}

	// 実行パーミッションをセット.
	private final void setExecPermission(String name) throws Exception {
		Files.setPosixFilePermissions(Paths.get(name), PosixFilePermissions.fromString("rwxr--r--"));
	}
}

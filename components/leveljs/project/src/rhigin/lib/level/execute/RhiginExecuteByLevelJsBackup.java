package rhigin.lib.level.execute;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import rhigin.RhiginException;
import rhigin.http.HttpElement;
import rhigin.http.Request;
import rhigin.http.Response;
import rhigin.http.execute.RhiginExecute;
import rhigin.keys.RhiginAccessKeyConstants;
import rhigin.lib.level.LevelJsBackup;
import rhigin.lib.level.LevelJsCore;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.util.Alphabet;
import rhigin.util.ArrayMap;
import rhigin.util.FileUtil;
import rhigin.util.ObjectList;

/**
 * LevelJsのバックアップ、レストアを行います.
 */
public class RhiginExecuteByLevelJsBackup implements RhiginExecute {
	/** 登録名. **/
	public static final String NAME = "leveljsbackup";
	
	public RhiginExecuteByLevelJsBackup() {}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object execute(HttpElement em, Request req, Response res, String executeCode) {
		// アクセスキーが存在しない場合は処理しない.
		if(!req.containsKey(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER)) {
			throw new RhiginException(401, "invalid access.");
		}
		// LevelJsCoreが初期化されていない場合は、エラー返却.
		LevelJsCore core = LevelJsCore.getInstance();
		if(!core.isStartup()) {
			throw new LevelJsException("LevelJs has not been initialized.");
		}
		// executeCode = /{backup or restore}/{operatorName}/{in out FileName}
		// {in out FileName}が指定なしでmethod = backupの場合は、./{operatorName}.yyyy-MM-dd.HH-mm-ss.
		int type = -1;
		if(Alphabet.indexOf(executeCode, "/backup/") == 0) {
			type = 1;
		} else if(Alphabet.indexOf(executeCode, "/restore/") == 0) {
			type = 2;
		} else {
			throw new LevelJsException("Illegal method is set.");
		}

		List<String> code = catExecuteCode(type, executeCode);
		if((type == 1 && code.size() < 1) || (type == 2 && code.size() != 2)) {
			throw new LevelJsException("Illegal parameter.");
		}
		String operatorName = code.get(0);
		String fileName = code.size() == 2 ? getFileName(operatorName) : code.get(1);
		// バックアップ.
		if(type == 1) {
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(fileName));
				long ret = LevelJsBackup.backup(out, core, operatorName);
				out.close();
				out = null;
				return new ArrayMap<String, Object>("fileName", fileName,
					"operatorName", operatorName, "count", ret);
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new LevelJsException(e);
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(Exception e) {}
				}
			}
		// リストア.
		} else {
			if(!FileUtil.isFile(fileName)) {
				throw new LevelJsException("The specified backup file does not exist.");
			}
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(fileName));
				long ret = LevelJsBackup.restore(core, in);
				return new ArrayMap<String, Object>("fileName", fileName,
					"operatorName", operatorName, "count", ret);
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new LevelJsException(e);
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch(Exception e) {}
				}
			}
		}
	}
	
	// executeCodeをカット.
	private final List<String> catExecuteCode(int type, String executeCode) {
		int f = type == 1 ? 8 : 9;
		int p = executeCode.indexOf("/", f);
		String operatorName = null;
		if(p == -1) {
			operatorName = executeCode.substring(f);
		} else {
			operatorName = executeCode.substring(f, p);
		}
		System.out.println(p);
		if(operatorName.isEmpty()) {
			throw new LevelJsException("The operator name is not set correctly.");
		}
		List<String> ret = new ObjectList<String>();
		ret.add(operatorName);
		if(p != -1) {
			ret.add(executeCode.substring(p+1));
		}
		return ret;
	}
	
	// バックアップファイル名が設定されていない場合のファイル名生成.
	@SuppressWarnings("deprecation")
	private final String getFileName(String operatorName) {
		Date d = new Date();
		return new StringBuilder("./")
			.append(operatorName).append(".")
			.append(d.getYear() + 1900)
			.append("-")
			.append(zero(d.getMonth()+1)).append(d.getMonth()+1)
			.append("-")
			.append(zero(d.getDate())).append(d.getDate())
			.append(".")
			.append(zero(d.getHours())).append(d.getHours())
			.append("-")
			.append(zero(d.getMinutes())).append(d.getMinutes())
			.append("-")
			.append(zero(d.getSeconds())).append(d.getSeconds())
			.toString();
	}
	
	// ゼロサプレス.
	private static final String zero(int n) {
		if(n <= 9) {
			return "0";
		}
		return "";
	}
}

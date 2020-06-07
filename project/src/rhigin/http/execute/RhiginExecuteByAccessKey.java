package rhigin.http.execute;

import rhigin.RhiginException;
import rhigin.http.HttpElement;
import rhigin.http.Request;
import rhigin.http.Response;
import rhigin.keys.RhiginAccessKey;
import rhigin.keys.RhiginAccessKeyFactory;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.util.Alphabet;
import rhigin.util.ArrayMap;

/**
 * Rhigin実行命令: AccessKeyの操作.
 */
public class RhiginExecuteByAccessKey implements RhiginExecute {
	/** 登録名. **/
	public static final String NAME = "accesskey";
	
	public RhiginExecuteByAccessKey() {}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object execute(HttpElement em, Request req, Response res, String executeCode) {
		final String exec = executeCode.substring(NAME.length()).trim();
		// /*accesskey/create 命令.
		if(Alphabet.indexOf(exec, "/create") == 0) {
			return create();
		// /*accesskey/delete/{削除対象のaccessKey}
		} else if(Alphabet.indexOf(exec, "/delete/") == 0) {
			return delete(exec.substring(8));
		// /*accessKey/use/{チェック対象のaccessKey}
		} else if(Alphabet.indexOf(exec, "/use/") == 0) {
			return use(exec.substring(5));
		} else {
			throw new RhiginException("No instruction is set or an instruction that does not exist is set.");
		}
	}
	
	// 新しいAccessKey,AuthCodeを生成.
	private Object create() {
		String[] keys = RhiginAccessKeyFactory.getInstance().get().create();
		return new ArrayMap<String, String>("accessKey", keys[0], "authCode", keys[1]);
	}
	
	// 指定されたAccessKeyを削除.
	private Object delete(String accessKey) {
		RhiginAccessKeyUtil.checkAccessKey(accessKey);
		RhiginAccessKey ac = RhiginAccessKeyFactory.getInstance().get();
		boolean ret = ac.delete(accessKey);
		return new ArrayMap<String, String>("result", ret, "accessKey", accessKey);
	}
	
	// 指定されたAccessKeyの存在確認.
	private Object use(String accessKey) {
		boolean ret = false;
		if(RhiginAccessKeyUtil.isAccessKey(accessKey)) {
			RhiginAccessKey ac = RhiginAccessKeyFactory.getInstance().get();
			ret = ac.delete(accessKey);
		}
		return new ArrayMap<String, String>("result", ret, "accessKey", accessKey);
	}
}
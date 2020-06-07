package rhigin.http.execute;

import java.io.StringReader;

import rhigin.RhiginException;
import rhigin.http.HttpElement;
import rhigin.http.HttpWorkerThread;
import rhigin.http.Request;
import rhigin.http.Response;
import rhigin.keys.RhiginAccessKeyConstants;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.ScriptConstants;

/**
 * Rhigin実行命令: 受信したjavascript構文を実行する.
 */
public class RhiginExecuteByJs implements RhiginExecute {
	/** 登録名. **/
	public static final String NAME = "executejs";
	
	public RhiginExecuteByJs() {}

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
		try {
			// 実行するjavascript構文を取得.
			final String execJs = req.getBodyText();
			
			// コンテキスト生成・設定.
			RhiginContext context = new RhiginContext();
			context.setAttribute("request", req);
			context.setAttribute("response", res);
			context.setAttribute(HttpWorkerThread.redirect.getName(), HttpWorkerThread.redirect);
			context.setAttribute(HttpWorkerThread.error.getName(), HttpWorkerThread.error);
			
			Object ret = "";
			try {
				// スクリプトの実行.
				ret = ExecuteScript.execute(context, new StringReader(execJs),
						NAME, ScriptConstants.HEADER, ScriptConstants.FOOTER, 1);
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(e);
			} finally {
				ExecuteScript.clearCurrentRhiginContext();
			}
			return ret;
		} finally {
			
			// スクリプト終了処理.
			ExecuteScript.callEndScripts(false);
		}
	}
}

package rhigin.http.execute;

import rhigin.http.HttpElement;
import rhigin.http.Request;
import rhigin.http.Response;

/**
 * Rhigin実行命令.
 */
public interface RhiginExecute {
	
	/**
	 * Rhigin実行命令名を取得.
	 * @return String Rhigin実行命令名が返却されます.
	 */
	public String getName();
	
	/**
	 * Rhigin実行命令を実行.
	 * @param em Http要素を設定します.
	 * @param req Requestが設定されます.
	 * @param res Responseが設定されます.
	 * @param executeCode executeCodeを設定します.
	 * @return Object オブジェクトが返却されます.
	 */
	public Object execute(HttpElement em, Request req, Response res, String executeCode);
}

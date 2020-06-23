package rhigin.http.execute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RhiginExecute管理オブジェクト.
 */
public class RhiginExecuteManager {
	private static final Map<String, RhiginExecute> EXECUTE_MAN = new ConcurrentHashMap<String, RhiginExecute>();
	private RhiginExecuteManager() {}
	
	/**
	 * 新しいRhiginExecuteを追加.
	 * @param r
	 */
	public static final void add(RhiginExecute r) {
		EXECUTE_MAN.put(r.getName().trim(), r);
	}
	
	/**
	 * 登録されているRhiginExecuteを取得.
	 * @param url
	 * @return
	 */
	public static final RhiginExecute getExecuteObject(String url) {
		return EXECUTE_MAN.get(getExecuteName(url));
	}
	
	/**
	 * 指定URLがRhiginExecuteのURLか取得.
	 * url = "/*accessKey/create" => return true;
	 * url = "/hoge/moge.html" => return false;
	 * @param url
	 * @return
	 */
	public static final boolean isExecuteUrl(String url) {
		return url == null ?
			false : url.startsWith(RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD);
	}
	
	/**
	 * RhiginExecute命令を取得.
	 * url = "/*accessKey/create" => return "accessKey";
	 * @param url
	 * @return
	 */
	public static final String getExecuteName(String url) {
		if(isExecuteUrl(url)) {
			final String ru = url.substring(RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD.length());
			int p = ru.indexOf("/");
			return ((p == -1) ? ru : ru.substring(0, p)).toLowerCase();
		}
		return url;
	}
	
	/**
	 * RhiginExecuteのExecuteCodeを取得.
	 * url = "/*accessKey/create" => return "/create";
	 * @param url
	 * @return
	 */
	public static final String getExecteCode(String url) {
		int p;
		if(isExecuteUrl(url)) {
			p = url.indexOf("/", 2);
		} else {
			p = url.indexOf("/");
		}
		if(p == -1) {
			return "";
		}
		return url.substring(p);
	}
}

package rhigin.lib.level.execute;

import java.lang.reflect.Array;
import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.client.HttpResult;
import rhigin.http.execute.RhiginExecuteClient;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.util.ArrayMap;

/**
 * LevelJsバックアップ・リストアのClient操作.
 */
public class RhiginExecuteClientByLevelJsBackup extends RhiginExecuteClient {
	private static final RhiginExecuteClientByLevelJsBackup SNGL = new RhiginExecuteClientByLevelJsBackup();
	
	/**
	 * シングルトンオブジェクトを取得.
	 * @return
	 */
	public static final RhiginExecuteClientByLevelJsBackup getInstance() {
		return SNGL;
	}
	
	/**
	 * バックアップ処理.
	 * @param url
	 * @param operatorName
	 * @return
	 */
	public Object backup(String url, String operatorName) {
		return send(url, new Object[] {"backup", operatorName}, null);
	}
	
	/**
	 * バックアップ処理.
	 * @param url
	 * @param operatorName
	 * @param fileName
	 * @return
	 */
	public Object backup(String url, String operatorName, String fileName) {
		return send(url, new Object[] {"backup", operatorName, fileName}, null);
	}
	
	/**
	 * バックアップ処理.
	 * @param url
	 * @param operatorName
	 * @param option
	 * @return
	 */
	public Object backup(String url, String operatorName, Map<String, Object> option) {
		return send(url, new Object[] {"backup", operatorName}, option);
	}
	
	/**
	 * バックアップ処理.
	 * @param url
	 * @param operatorName
	 * @param fileName
	 * @param option
	 * @return
	 */
	public Object backup(String url, String operatorName, String fileName, Map<String, Object> option) {
		return send(url, new Object[] {"backup", operatorName, fileName}, option);
	}
	
	/**
	 * レストア処理.
	 * @param url
	 * @param operatorName
	 * @param fileName
	 * @return
	 */
	public Object restore(String url, String operatorName, String fileName) {
		return send(url, new Object[] {"restore", operatorName, fileName}, null);
	}
	
	/**
	 * レストア処理.
	 * @param url
	 * @param operatorName
	 * @param fileName
	 * @param option
	 * @return
	 */
	public Object restore(String url, String operatorName, String fileName, Map<String, Object> option) {
		return send(url, new Object[] {"restore", operatorName, fileName}, option);
	}
	
	@Override
	protected HttpResult _send(String url, Object value, Map<String, Object> option) {
		// RhiginAccessKeyClientで管理しているAccessKeyをセット.
		RhiginAccessKeyUtil.settingAccessKeyAndAuthCode(option, url, option);
		// valueの存在チェック.
		// value = Object[3] [0] = methodName, [1] = operatorName [2] = fileName.
		int len;
		if(value == null || !value.getClass().isArray() || (len = Array.getLength(value)) < 2) {
			throw new LevelJsException("Invalid parameter is set.");
		}
		Object method = Array.get(value, 0);
		Object operatorName = Array.get(value, 1);
		Object fileName = len <= 2 ? null : Array.get(value, 2);
		if(method == null || !(method instanceof String) || ((String)method).isEmpty()) {
			throw new LevelJsException("Backup and restore instructions have not been specified.");
		}
		if(operatorName == null || !(operatorName instanceof String) || ((String)operatorName).isEmpty()) {
			throw new LevelJsException("The table name is not set correctly.");
		}
		if(fileName != null && !(fileName instanceof String) && ((String)fileName).isEmpty()) {
			throw new LevelJsException("The file name is not set correctly.");
		}
		// URLに実行命令をセット.
		url += getExecutePath(RhiginExecuteByLevelJsBackup.NAME) + "/" + operatorName;
		if(fileName != null) {
			url += "/" + fileName;
		}
		if(option == null) {
			option = new ArrayMap<String, Object>();
		}
		try {
			// getで送信.
			return send(url, option);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new LevelJsException(e);
		}
	}
}

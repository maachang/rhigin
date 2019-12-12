package rhigin.scripts;

import org.mozilla.javascript.Scriptable;

/**
 * java クラスをjsのrequireで取り込むためのインターフェイス.
 */
public interface JavaRequire {
	
	/**
	 * requireが呼ばれた時のオブジェクトロード.
	 * @return Scriptable
	 */
	public Scriptable load();
}
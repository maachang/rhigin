package rhigin.scripts.objects;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.BlankScriptable;
import rhigin.scripts.function.AbstractFunction;

/**
 * RhiginObject.
 * Object.method処理を実装する時に利用します.
 */
public class RhiginObject implements BlankScriptable {
	private final Object[] list;
	private final String name;
	
	/**
	 * コンストラクタ.
	 * @param name オブジェクト名を設定します.
	 * @param list オブジェクトで有効になるFunctionをAbstractFunction配列で設定します.
	 */
	public RhiginObject(String name, AbstractFunction[] list) {
		int cnt = 0;
		final int len = list.length;
		final int listLen = len << 1;
		final Object[] values = new Object[listLen];
		for(int i = 0; i < len; i ++) {
			values[cnt++] = list[i].getName();
			values[cnt++] = list[i];
		}
		this.list = values;
		this.name = name;
	}
	
	@Override
	public Object get(String k, Scriptable s) {
		final int len = list.length;
		for(int i = 0; i < len; i += 2) {
			if(list[i].equals(k)) {
				return list[i+1];
			}
		}
		return Undefined.instance;
	}
	
	@Override
	public boolean has(String k, Scriptable s) {
		final int len = list.length;
		for(int i = 0; i < len; i += 2) {
			if(list[i].equals(k)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Object[] getIds() {
		final int len = list.length;
		Object[] ret = new Object[len >> 1];
		for(int i = 0,j = 0; i < len; i += 2, j ++) {
			ret[j] = list[i];
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
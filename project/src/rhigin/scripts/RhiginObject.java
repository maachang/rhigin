package rhigin.scripts;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.FixedSearchArray;

/**
 * RhiginObject. Object.method処理を実装する時に利用します.
 */
public class RhiginObject implements RhinoScriptable {
	protected FixedSearchArray<String> searchList;
	protected RhiginFunction[] list;
	protected String name;

	/**
	 * コンストラクタ.
	 * 
	 * @param name
	 *            オブジェクト名を設定します.
	 * @param list
	 *            オブジェクトで有効になるRhiginFunction群で設定します.
	 */
	public RhiginObject(String name, RhiginFunction... list) {
		final int len = list.length;
		final FixedSearchArray<String> searchList = new FixedSearchArray<String>(len);
		for (int i = 0; i < len; i++) {
			searchList.add(list[i].getName(), i);
			list[i].PARENT = this;
		}
		this.list = list;
		this.searchList = searchList;
		this.name = name;
	}

	@Override
	public Object _get(String k, Scriptable s) {
		int no = searchList.search(k);
		if(no != -1) {
			return list[no];
		}
		return Undefined.instance;
	}
	
	@Override
	public Object _get(int no, Scriptable s) {
		return Undefined.instance;
	}

	@Override
	public boolean has(String k, Scriptable s) {
		return searchList.search(k) != -1;
	}

	@Override
	public Object[] getIds() {
		final int len = list.length;
		Object[] ret = new Object[len];
		for (int i = 0; i < len; i ++) {
			ret[i] = list[i].getName();
		}
		return ret;
	}

	@Override
	public Object getDefaultValue(Class<?> clazz) {
		return (clazz == null || String.class.equals(clazz)) ? toString() : null;
	}

	@Override
	public String getClassName() {
		return name;
	}

	@Override
	public String toString() {
		// toStringがメソッドで存在する場合は、その内容を呼び出す.
		final int no = searchList.search("toString");
		if(no != -1) {
			// エラーの場合は、標準表示.
			try {
				return "" + list[no]
					.call(null, null, null, ScriptConstants.BLANK_ARGS);
			} catch(Exception e) {}
		}
		return "[" + name + "]";
	}
	
	/**
	 * オブジェクト名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public void _put(String name, Scriptable obj, Object value) {
	}

	@Override
	public void _put(int no, Scriptable obj, Object value) {
	}
}

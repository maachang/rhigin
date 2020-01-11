package rhigin.scripts;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.BlankScriptable;
import rhigin.util.FixedSearchArray;

/**
 * インスタンス生成後のRhiginObjectの場合、こちらを利用します。
 */
public class RhiginInstanceObject implements BlankScriptable {
	protected String name;
	protected ObjectFunction objectFunction;
	protected Object[] params;
	protected RhiginFunction[] list;
	
	/**
	 * オブジェクト用メソッド作成処理.
	 */
	public static interface ObjectFunction {
		/**
		 * Function作成.
		 * @param no
		 * @param params
		 * @return
		 */
		public RhiginFunction create(int no, Object... params);
		
		/**
		 * メソッド検索ワードを取得.
		 * @return
		 */
		public FixedSearchArray<String> getWord();
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param name
	 *            オブジェクト名を設定します.
	 * @param list
	 *            オブジェクトで有効になるRhiginFunction群で設定します.
	 */
	public RhiginInstanceObject(String name, ObjectFunction of, Object... params) {
		this.name = name;
		this.objectFunction = of;
		this.params = params;
		this.list = new RhiginFunction[objectFunction.getWord().size()];
	}

	@Override
	public Object get(String k, Scriptable s) {
		int no = objectFunction.getWord().search(k);
		if(no != -1) {
			RhiginFunction ret = list[no];
			if(ret == null) {
				ret = objectFunction.create(no, params);
				ret.PARENT = this;
				list[no] = ret;
			}
			return ret;
		}
		return Undefined.instance;
	}

	@Override
	public boolean has(String k, Scriptable s) {
		return objectFunction.getWord().search(k) != -1;
	}

	@Override
	public Object[] getIds() {
		FixedSearchArray<String> s = objectFunction.getWord();
		final int len = s.size();
		Object[] ret = new Object[len];
		for (int i = 0; i < len; i ++) {
			ret[i] = s.get(i);
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
		final int no = objectFunction.getWord().search("toString");
		if(no != -1) {
			// エラーの場合は、標準表示.
			try {
				return "" + ((RhiginFunction)get("toString", null))
					.call(null, null, null, new Object[0]);
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
	
	/**
	 * パラメータを取得.
	 * @param no
	 * @return
	 */
	public Object getParams(int no) {
		return params[no];
	}
	
	/**
	 * パラメータサイズを取得.
	 * @return
	 */
	public int paramsSize() {
		return params == null ? 0 : params.length;
	}
}

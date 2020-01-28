package rhigin.http;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.JavaScriptable;
import rhigin.util.Alphabet;
import rhigin.util.AndroidMap;
import rhigin.util.ConvertGet;
import rhigin.util.Converter;
import rhigin.util.FixedArray;

/**
 * Response.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Response extends JavaScriptable.Map implements ConvertGet {
	private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=UTF-8";
	protected int status = 200;
	protected AndroidMap header = new AndroidMap();
	protected String ContentType = DEFAULT_CONTENT_TYPE;

	public void clear() {
		header.clear();
		ContentType = DEFAULT_CONTENT_TYPE;
		status = 200;
	}

	public void setStatus(int s) {
		status = s;
	}

	public int getStatus() {
		return status;
	}

	public void setHeaders(Map h) {
		if (h == null) {
			return;
		}
		Object k, v;
		Iterator it = h.keySet().iterator();
		while (it.hasNext()) {
			k = it.next();
			if (k == null) {
				continue;
			}
			v = h.get(k);
			if (v == null) {
				continue;
			}
			header.put(k.toString(), v.toString());
		}
	}

	protected Object setHeader(Object k, Object v) {
		if (k != null && v != null) {
			if (Alphabet.eq("content-type", k.toString())) {
				String ret = ContentType;
				ContentType = v.toString();
				return ret;
			}
			return header.put(k.toString(), v.toString());
		}
		return null;
	}

	protected Object getHeader(Object k) {
		if (k != null) {
			return header.get(k.toString());
		}
		return null;
	}

	protected Object removeHeader(Object k) {
		if (k != null) {
			return header.remove(k.toString());
		}
		return null;
	}

	protected static final String headers(Response h) {
		if (h == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		AndroidMap hh = h.header;
		int len = hh.size();
		if(len > 0) {
			for(int i = 0;i < len; i ++) {
				buf.append(hh.keyAt(i)).append(": ").append(hh.valueAt(i)).append("\r\n");
			}
		}
		buf.append("Content-Type: ").append(h.ContentType).append("\r\n");
		return buf.toString();
	}

	/**
	 * 取得.
	 * 
	 * @param key
	 *            d対象のキーを設定します.
	 * @return Object キーに対する要素情報が返却されます.
	 */
	@Override
	public Object get(Object key) {
		if(key == null) {
			return null;
		} else if ("state".equals(key) || "status".equals(key)) {
			return getStatus();
		} else if (Alphabet.eq("content-type", key.toString())) {
			return ContentType;
		}
		return getHeader(key);
	}

	/**
	 * 登録.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @param value
	 *            対象の要素を設定します.
	 * @return Object 前回登録されていた内容が返却されます.
	 */
	@Override
	public Object put(Object key, Object value) {
		if(key == null || value == null) {
			return null;
		} else if ("state".equals(key) || "status".equals(key)) {
			int ret = getStatus();
			setStatus(Converter.convertInt(value));
			return ret;
		}
		return setHeader(key, value);
	}
	
	/**
	 * 削除.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @return Object キーに対する要素情報が返却されます.
	 */
	@Override
	public Object remove(Object key) {
		if(key == null) {
			return null;
		} else if ("state".equals(key) || "status".equals(key)) {
			int ret = getStatus();
			setStatus(200);
			return ret;
		} else if (Alphabet.eq("content-type", key.toString())) {
			String ret = ContentType;
			ContentType = DEFAULT_CONTENT_TYPE;
			return ret;
		}
		return removeHeader(key);
	}
	
	/**
	 * 要求ヘッダ名の変換.
	 * js からアクセスされる場合、ヘッダ名は必ず
	 * [Content-Type]のように、先頭大文字、ハイフン後大文字に変換する。
	 */
	private static final String _hname(String n) {
		if(n == null || n.isEmpty()) {
			return null;
		}
		n = n.toLowerCase();
		if("state".equals(n) || "status".equals(n)) {
			return n;
		}
		int p = n.indexOf("-");
		if(p == -1 || p + 1 == n.length()) {
			return n.substring(0, 1).toUpperCase() + n.substring(1);
		}
		return new StringBuilder(n.length())
			.append(n.substring(0, 1).toUpperCase())
			.append(n.substring(1, p + 1))
			.append(n.substring(p + 1, p + 2).toUpperCase())
			.append(n.substring(p + 2))
			.toString();
	}
	
	@Override
	public boolean has(String name, Scriptable start) {
		if (this.containsKey(_hname(name))) {
			return true;
		}
		return false;
	}

	@Override
	public Object get(String name, Scriptable start) {
		name = _hname(name);
		if (this.containsKey(name)) {
			return this.get(name);
		}
		return Undefined.instance;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		this.put(_hname(name), value);
	}

	@Override
	public void delete(String name) {
		this.remove(_hname(name));
	}

	/**
	 * 存在確認.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @return Object キーに対する要素情報が返却されます.
	 */
	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		} else if ("state".equals(key) || "status".equals(key) ||
			Alphabet.eq("content-type", key.toString())) {
			return true;
		}
		return header.containsKey(key.toString());
	}

	/**
	 * ヘッダ一覧を取得.
	 * 
	 * @return List<String> ヘッダ一覧が返却されます.
	 */
	public List<String> getHeaders() {
		int len = header.size();
		List<String> ret = new FixedArray<String>(len + 1);
		for(int i = 0; i < len; i ++) {
			ret.set(i, "" + header.keyAt(i));
		}
		ret.set(len, "Content-Type");
		return ret;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public Object[] getIds() {
		int len = header.size();
		Object[] ret = new Object[len + 1];
		for(int i = 0; i < len; i ++) {
			ret[i] = header.keyAt(i);
		}
		ret[len] = "Content-Type";
		return ret;
	}

	@Override
	public int size() {
		return header.size();
	}

	@Override
	public Set keySet() {
		return header.keySet();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return header.entrySet();
	}

	@Override
	public Object getOriginal(Object n) {
		return get(n);
	}
}

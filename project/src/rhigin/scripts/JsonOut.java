package rhigin.scripts;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;


/**
 * JSONを読みやすいように文字列変換.
 */
@SuppressWarnings({"rawtypes"})
public class JsonOut {
	protected JsonOut() {}
	
	// デフォルトスペース.
	private static final int DEF_SPACE = 2;
	
	/**
	 * 文字列変換.
	 * @param m 出力対象のオブジェクトを設定します.
	 * @return 文字列が返却されます.
	 */
	public static final String toString(Object m) {
		StringBuilder buf = new StringBuilder();
		toString(DEF_SPACE, 0, buf, m);
		return buf.toString();
	}
	
	/**
	 * iteratorを文字列変換.
	 * @param it
	 * @return
	 */
	public static final String toString(Iterator it) {
		String enter = "";
		final StringBuilder buf = new StringBuilder();
		buf.append("[");
		while(it.hasNext()) {
			if(enter.length() == 0) {
				buf.append("\n");
				enter = "\n";
			} else {
				buf.append(",\n");
			}
			countSpace(DEF_SPACE, buf);
			toString(DEF_SPACE, DEF_SPACE, buf, it.next());
		}
		buf.append(enter).append("]");
		return buf.toString();
	}
	
	/**
	 * 文字列変換.
	 * @param indent インデントスペース数を設定します.
	 * @param m 出力対象のオブジェクトを設定します.
	 * @return 文字列が返却されます.
	 */
	public static final String toString(int indent, Object m) {
		StringBuilder buf = new StringBuilder();
		toString(indent, 0, buf, m);
		return buf.toString();
	}
	
	/** 文字列変換. */
	private static final void toString(int indent, int initSpace, StringBuilder buf, Object m) {
		if(m == null || m instanceof Undefined) {
			m = null;
		} else if (m instanceof Wrapper) {
			m = ((Wrapper) m).unwrap();
		} else if (m instanceof RhiginObjectWrapper) {
			m = ((RhiginObjectWrapper) m).unwrap();
		}		
		if(m == null) {
			toValue(indent, initSpace, buf, m);
		} else if(m instanceof Map) {
			toMap(indent, initSpace, buf, (Map)m);
		} else if(m instanceof List) {
			toList(indent, initSpace, buf, (List)m);
		} else if(m.getClass().isArray()) {
			toArray(indent, initSpace, buf, m);
		} else {
			toValue(indent, initSpace, buf, m);
		}
	}
	
	// インデントのスペースを設定.
	private static final void countSpace(int count, StringBuilder buf) {
		for(int i = 0; i < count; i ++) {
			buf.append(" ");
		}
	}
	
	// mapを文字列変換.
	private static final int toMap(int indent, int count, StringBuilder buf, Map m) {
		count += indent;
		if(m.size() == 0) {
			buf.append("{}");
			return count -indent;
		}
		buf.append("{\n");
		
		int n = 0;
		String key;
		Object value;
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			key = "" + it.next();
			value = m.get(key);
			if(value == null || value instanceof Undefined) {
				value = null;
			} else if (value instanceof Wrapper) {
				value = ((Wrapper) value).unwrap();
			} else if (value instanceof RhiginObjectWrapper) {
				value = ((RhiginObjectWrapper) value).unwrap();
			}		
			if(n != 0) {
				buf.append(",\n");
			}
			countSpace(count, buf);
			buf.append("\"").append(key).append("\": ");
			if(value == null) {
				count = toValue(indent, count, buf, value);
			} else if(value instanceof Map) {
				count = toMap(indent, count, buf, (Map)value);
			} else if(value instanceof List) {
				count = toList(indent, count, buf, (List)value);
			} else if(value.getClass().isArray()) {
				count = toArray(indent, count, buf, value);
			} else {
				count = toValue(indent, count, buf, value);
			}
			n ++;
		}
		buf.append("\n");
		countSpace(count - indent, buf);
		buf.append("}");
		
		return count - indent;
	}
	
	// listを文字列変換.
	private static final int toList(int indent, int count, StringBuilder buf, List m) {
		count += indent;
		if(m.size() == 0) {
			buf.append("[]");
			return count -indent;
		}
		buf.append("[\n");
		
		Object value;
		int len = m.size();
		for(int i = 0; i < len; i ++) {
			value = m.get(i);
			if(value == null || value instanceof Undefined) {
				value = null;
			} else if (value instanceof Wrapper) {
				value = ((Wrapper) value).unwrap();
			} else if (value instanceof RhiginObjectWrapper) {
				value = ((RhiginObjectWrapper) value).unwrap();
			}
			if(i != 0) {
				buf.append(",\n");
			}
			countSpace(count, buf);
			if(value == null) {
				count = toValue(indent, count, buf, value);
			} else if(value instanceof Map) {
				count = toMap(indent, count, buf, (Map)value);
			} else if(value instanceof List) {
				count = toList(indent, count, buf, (List)value);
			} else if(value.getClass().isArray()) {
				count = toArray(indent, count, buf, value);
			} else {
				count = toValue(indent, count, buf, value);
			}
		}
		buf.append("\n");
		countSpace(count - indent, buf);
		buf.append("]");
		
		return count - indent;
	}
	
	// Arrayを文字列変換.
	private static final int toArray(int indent, int count, StringBuilder buf, Object m) {
		count += indent;
		if(Array.getLength(m) == 0) {
			buf.append("[]");
			return count - indent;
		}
		buf.append("[\n");
		
		Object value;
		int len = Array.getLength(m);
		for(int i = 0; i < len; i ++) {
			value = Array.get(m, i);
			if(value == null || value instanceof Undefined) {
				value = null;
			} else if (value instanceof Wrapper) {
				value = ((Wrapper) value).unwrap();
			} else if (value instanceof RhiginObjectWrapper) {
				value = ((RhiginObjectWrapper) value).unwrap();
			}
			if(i != 0) {
				buf.append(",\n");
			}
			countSpace(count, buf);
			if(value == null) {
				count = toValue(indent, count, buf, value);
			} else if(value instanceof Map) {
				count = toMap(indent, count, buf, (Map)value);
			} else if(value instanceof List) {
				count = toList(indent, count, buf, (List)value);
			} else if(value.getClass().isArray()) {
				count = toArray(indent, count, buf, value);
			} else {
				count = toValue(indent, count, buf, value);
			}
		}
		buf.append("\n");
		countSpace(count - indent, buf);
		buf.append("]");
		
		return count - indent;
	}
	
	// 表示要素を文字列変換.
	private static final int toValue(int indent, int count, StringBuilder buf, Object m) {
		if(m == null || m instanceof Undefined || m instanceof byte[]) {
			buf.append("null");
		} else if(m instanceof Number || m instanceof Boolean) {
			buf.append(m);
		} else if(m instanceof CharSequence || m instanceof Character) {
			buf.append("\"").append(m).append("\"");
		} else if(m instanceof char[]) {
			buf.append("\"").append(new String((char[])m)).append("\"");
		} else if(m instanceof java.util.Date) {
			buf.append("\"").append(Json.dateToString((java.util.Date)m)).append("\"");
		} else if (m instanceof IdScriptableObject) {
			final java.util.Date d = Json.getJSNativeDate((IdScriptableObject) m);
			if(d == null) {
				buf.append("null");
			} else {
				buf.append("\"").append(Json.dateToString(d)).append("\"");
			}
		} else if(m instanceof Throwable) {
			buf.append("\"").append(m).append("\"");
		} else {
			buf.append("null");
		}
		return count;
	}
}

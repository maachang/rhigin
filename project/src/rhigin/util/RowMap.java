package rhigin.util;

import java.util.List;
import java.util.Set;

/**
 * １行のデータを扱う場合のMapオブジェクト.
 */
public final class RowMap implements AbstractKeyIterator.Base<String>, ConvertMap {
	private FixedSearchArray<String> header;
	private List<String> rowData;
	private int[] types;

	public RowMap() {
	}
	
	public RowMap(FixedSearchArray<String> h) {
		set(h);
	}
	
	public RowMap set(FixedSearchArray<String> h) {
		header = h;
		types = null;
		if(rowData != null) {
			int len = rowData.size();
			types = new int[len];
			for(int i = 0; i < len; i ++) {
				types[i] = -1;
			}
		}
		return this;
	}

	public RowMap set(List<String> r) {
		rowData = r;
		int len = r.size();
		if(types == null || types.length != len) {
			types = new int[len];
			for(int i = 0; i < len; i ++) {
				types[i] = -1;
			}
		}
		return this;
	}
	
	public FixedSearchArray<String> getHeader() {
		return header;
	}

	private int _getParamNo(Object key) {
		if (key == null) {
			return -1;
		}
		// 数値だった場合は、番号で処理.
		else if (Converter.isNumeric(key)) {
			int n = Converter.convertInt(key);
			if (n >= 0 && n < header.size()) {
				return header.getNo(n);
			}
			return -1;
		}
		return header.search(key.toString());
	}

	@Override
	public Object get(Object key) {
		int n = _getParamNo(key);
		if (n == -1) {
			return null;
		}
		final String ret = rowData.get(n);
		if(ret == null || ret.isEmpty()) {
			return null;
		}
		int type = types[n];
		if(type == -1) {
			if(Converter.isNumeric(ret)) {
				if(Converter.isFloat(ret)) {
					type = 2; // double.
				} else {
					type = 1; // long.
				}
			} else if(Alphabet.eq("true", ret) || Alphabet.eq("false", ret)) {
				type = 3; // boolean.
			} else if(DateConvert.isISO8601(ret)) {
				type = 4; // Date.
			} else {
				type = 0; // String.
			}
			types[n] = type;
		}
		try {
			switch(type) {
			case 1: return Converter.convertLong(ret); // long.
			case 2: return Converter.convertDouble(ret); // double.
			case 3: return Converter.convertBool(ret); // boolean.
			case 4: return DateConvert.getISO8601(ret); // Date.
			}
		} catch(Exception e) {}
		// String.
		return ret;
	}

	@Override
	public boolean containsKey(Object key) {
		return (_getParamNo(key) == -1) ? false : true;
	}

	@Override
	public int size() {
		return header.size();
	}

	@Override
	public String toString() {
		final int len = header.size();
		StringBuilder buf = new StringBuilder("{");
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				buf.append(",");
			}
			String v = rowData.get(header.getNo(i));
			buf.append("\"").append(header.get(i)).append("\":");
			if (v == null || v.length() == 0) {
				buf.append("null");
			} else if (Converter.isNumeric(v)) {
				buf.append(v);
			} else if (Alphabet.eq("true", v)) {
				buf.append("true");
			} else if (Alphabet.eq("false", v)) {
				buf.append("false");
			} else {
				buf.append("\"").append(v).append("\"");
			}
		}
		buf.append("}");
		return buf.toString();
	}
	
	@Override
	public String getKey(int no) {
		return header.get(no);
	}
	
	@Override
	public Set<String> keySet() {
		return new AbstractKeyIterator.Set<String>(this);
	}
}

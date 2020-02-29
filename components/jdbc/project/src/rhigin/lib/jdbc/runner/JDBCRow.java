package rhigin.lib.jdbc.runner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import rhigin.scripts.JavaScriptable;
import rhigin.scripts.JsonOut;
import rhigin.util.AbstractEntryIterator;
import rhigin.util.AbstractKeyIterator;
import rhigin.util.ConvertGet;
import rhigin.util.FixedSearchArray;

/**
 * JDBC-Row.
 */
public class JDBCRow implements Iterator<Map<String, Object>> {
	private JDBCConnect conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private FixedSearchArray<String> metaColumns = null;
	private String[] metaNames = null;
	private int[] metaTypes = null;
	private JDBCOneLine baseRow = null;
	private JDBCOneLine row = null;
	
	// メタデータの中身を取得.
	private final void getMeta(final ResultSetMetaData data)
		throws SQLException {
		String s;
		final int len = data.getColumnCount();
		FixedSearchArray<String> m = new FixedSearchArray<String>(len);
		String[] n = new String[len];
		int[] t = new int[len];
		for(int i = 0; i < len; i ++) {
			s = data.getColumnName(i + 1);
			m.add(s.toLowerCase(), i);
			n[i] = s;
			t[i] = data.getColumnType(i + 1);
		}
		metaColumns = m; // カラム群.
		metaNames = n; // カラム名群.
		metaTypes = t; // タイプ群.
		
		// 基本Rowを生成.
		baseRow = new JDBCOneLine(this);
	}
	
	// 行取得処理を生成.
	protected static final JDBCRow create(final ResultSet rs, final JDBCConnect conn, final Statement stmt) {
		try {
			JDBCRow ret = new JDBCRow();
			ret.conn = conn;
			ret.stmt = stmt;
			ret.rs = rs;
			ret.getMeta(rs.getMetaData());
			return ret;
		} catch(Exception e) {
			throw new JDBCException(e);
		}
	}
	
	private JDBCRow() {}
	
	/**
	 * クローズ.
	 */
	public void close() {
		if(rs != null) {
			try {
				rs.close();
			} catch(Exception e) {}
			try {
				stmt.close();
			} catch(Exception e) {}
		}
		conn = null;
		rs = null;
		stmt = null;
		row = null;
		metaColumns = null;
		metaTypes = null;
	}
	
	/**
	 * クローズ済みか取得.
	 * @return boolean
	 */
	public boolean isClose() {
		if(rs == null) {
			return true;
		} else if(conn.isClose()) {
			close();
			return true;
		}
		return false;
	}
	
	// チェック処理.
	private void check() {
		if(isClose()) {
			throw new JDBCException("Connection is already closed.");
		}
	}

	@Override
	public boolean hasNext() {
		if(isClose()) {
			return false;
		} else if(row == null && !_row()) {
			close();
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> next() {
		check();
		if(rs == null || (row == null && !_row())) {
			close();
			throw new NoSuchElementException();
		}
		final Map<String, Object> ret = row;
		row = null;
		return ret;
	}
	
	/**
	 * 全情報をListとして取得.
	 * @return
	 */
	public List<Map<String, Object>> getRows() {
		return getRows(0);
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param max
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getRows(int max) {
		check();
		max = max <= 0 ? 0 : max;
		final List<Map<String, Object>> ret = new JavaScriptable.GetList();
		while(hasNext()) {
			if(max != 0 && ret.size() >= max) {
				break;
			}
			ret.add(row.copyObject());
			row = null;
		}
		return ret;
	}
	
	/**
	 * 文字列変換.
	 * @return String
	 */
	@Override
	public String toString() {
		return JsonOut.toString(this);
	}
	
	// 1行の情報を取得.
	private boolean _row() {
		try {
			if(rs.next()) {
				row = baseRow;
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new JDBCException(e);
		}
	}
	
	// 1行のデータ.
	@SuppressWarnings("rawtypes")
	private static final class JDBCOneLine extends JavaScriptable.Map
		implements AbstractKeyIterator.Base<String>, AbstractEntryIterator.Base<String, Object>, ConvertGet<String> {
		private final JDBCRow parent;
		
		private JDBCOneLine(JDBCRow p) {
			this.parent = p;
		}
		
		@Override
		public void clear() {
		}

		@Override
		public Object put(Object name, Object value) {
			return null;
		}

		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			}
			return parent.metaColumns.search(key.toString().toLowerCase()) != -1;
		}

		@Override
		public Object get(Object key) {
			if (key == null) {
				return null;
			}
			int no = parent.metaColumns.search(key.toString().toLowerCase());
			if(no != -1) {
				try {
					return JDBCUtils.getResultColumn(parent.rs, parent.metaTypes[no], no + 1);
				} catch(Exception e) {
					throw new JDBCException(e);
				}
			}
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return parent.metaColumns.size() == 0;
		}

		@Override
		public void putAll(Map toMerge) {
		}

		@Override
		public boolean containsValue(Object value) {
			Object o;
			ResultSet rs = parent.rs;
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			try {
				if (value == null) {
					for (int i = 0; i < len; i++) {
						o = JDBCUtils.getResultColumn(rs, metaTypes[i], i + 1);
						if(o == null) {
							return true;
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						o = JDBCUtils.getResultColumn(rs, metaTypes[i], i + 1);
						if (value.equals(o)) {
							return true;
						}
					}
				}
				return false;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public int size() {
			return parent.metaTypes.length;
		}

		@Override
		public String toString() {
			Object o;
			ResultSet rs = parent.rs;
			int[] metaTypes = parent.metaTypes;
			String[] metaNames = parent.metaNames;
			final int len = metaTypes.length;
			StringBuilder buf = new StringBuilder("{");
			try {
				for (int i = 0; i < len; i++) {
					if (i != 0) {
						buf.append(",");
					}
					o = JDBCUtils.getResultColumn(rs, metaTypes[i], i + 1);
					buf.append("\"").append(metaNames[i]).append("\": \"").append(o).append("\"");
				}
				return buf.append("}").toString();
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public Collection<Object> values() {
			Object o;
			ResultSet rs = parent.rs;
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			final ArrayList<Object> ret = new ArrayList<Object>(len);
			try {
				for (int i = 0; i < len; i++) {
					o = JDBCUtils.getResultColumn(rs, metaTypes[i], i + 1);
					ret.add(o);
				}
				return ret;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public Set<String> keySet() {
			return new AbstractKeyIterator.Set<>(this);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return new AbstractEntryIterator.Set<>(this);
		}

		// original 取得.
		@Override
		public Object getOriginal(String n) {
			return get(n);
		}

		@Override
		public String getKey(int no) {
			return parent.metaNames[no];
		}

		@Override
		public Object getValue(int no) {
			try {
				return JDBCUtils.getResultColumn(parent.rs, parent.metaTypes[no], no + 1);
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}
		
		/**
		 * オブジェクトのコピー.
		 * @return
		 */
		public JDBCCopyLine copyObject() {
			return new JDBCCopyLine(parent);
		}
	}
	
	// 1行のデータ(copy).
	@SuppressWarnings("rawtypes")
	private static final class JDBCCopyLine extends JavaScriptable.Map
		implements AbstractKeyIterator.Base<String>, AbstractEntryIterator.Base<String, Object>, ConvertGet<String> {
		private final JDBCRow parent;
		private final Object[] values;
		
		private JDBCCopyLine(JDBCRow p) {
			ResultSet rs = p.rs;
			int[] metaTypes = p.metaTypes;
			final int len = metaTypes.length;
			Object[] vs = new Object[len];
			try {
				for(int i = 0; i < len; i ++) {
					vs[i] = JDBCUtils.getResultColumn(rs, metaTypes[i], i + 1);
				}
				this.parent = p;
				this.values = vs;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}
		
		@Override
		public void clear() {
		}

		@Override
		public Object put(Object name, Object value) {
			return null;
		}

		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			}
			return parent.metaColumns.search(key.toString().toLowerCase()) != -1;
		}

		@Override
		public Object get(Object key) {
			if (key == null) {
				return null;
			}
			int no = parent.metaColumns.search(key.toString().toLowerCase());
			if(no != -1) {
				return values[no];
			}
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return parent.metaColumns.size() == 0;
		}

		@Override
		public void putAll(Map toMerge) {
		}

		@Override
		public boolean containsValue(Object value) {
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			try {
				if (value == null) {
					for (int i = 0; i < len; i++) {
						if(values[i] == null) {
							return true;
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						if (values.equals(values[i])) {
							return true;
						}
					}
				}
				return false;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public int size() {
			return parent.metaTypes.length;
		}

		@Override
		public String toString() {
			int[] metaTypes = parent.metaTypes;
			String[] metaNames = parent.metaNames;
			final int len = metaTypes.length;
			StringBuilder buf = new StringBuilder("{");
			try {
				for (int i = 0; i < len; i++) {
					if (i != 0) {
						buf.append(",");
					}
					buf.append("\"").append(metaNames[i]).append("\": \"").append(values[i]).append("\"");
				}
				return buf.append("}").toString();
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public Collection<Object> values() {
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			final ArrayList<Object> ret = new ArrayList<Object>(len);
			try {
				for (int i = 0; i < len; i++) {
					ret.add(values[i]);
				}
				return ret;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}

		@Override
		public Set<String> keySet() {
			return new AbstractKeyIterator.Set<>(this);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return new AbstractEntryIterator.Set<>(this);
		}

		// original 取得.
		@Override
		public Object getOriginal(String n) {
			return get(n);
		}

		@Override
		public String getKey(int no) {
			return parent.metaNames[no];
		}

		@Override
		public Object getValue(int no) {
			return values[no];
		}
	}
}

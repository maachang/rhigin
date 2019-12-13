package rhigin.lib.jdbc.runner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import rhigin.util.ArrayMap;
import rhigin.util.ListMap;
import rhigin.util.OList;
import rhigin.util.ObjectList;

/**
 * JDBC-Row.
 */
public class JDBCRow implements Iterator<Map<String, Object>> {
	private JDBCConnect conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private ListMap meta = new ListMap();
	
	// メタデータの中身を取得.
	private static final void getMeta(final ListMap meta, final ResultSetMetaData data)
		throws SQLException {
		final int len = data.getColumnCount();
		for(int i = 1; i <= len; i ++) {
			meta.put(data.getColumnName(i), data.getColumnType(i));
		}
	}
	
	// 行取得処理を生成.
	protected static final JDBCRow create(final ResultSet rs, final JDBCConnect conn, final Statement stmt) {
		try {
			JDBCRow ret = new JDBCRow();
			ret.conn = conn;
			ret.stmt = stmt;
			ret.rs = rs;
			JDBCRow.getMeta(ret.meta, rs.getMetaData());
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
		try {
			rs.close();
		} catch(Exception e) {}
		try {
			stmt.close();
		} catch(Exception e) {}
		conn = null;
		rs = null;
		stmt = null;
		row = null;
	}
	
	/**
	 * クローズ済みか取得.
	 * @return boolean
	 */
	public boolean isClose() {
		return conn == null || conn.closeFlag || rs == null;
	}
	
	// チェック処理.
	private void check() {
		if(isClose()) {
			throw new JDBCException("Connection is already closed.");
		}
	}

	@Override
	public boolean hasNext() {
		check();
		if(rs == null) {
			return false;
		} else if(row == null) {
			if(!getRow()) {
				close();
				return false;
			}
		}
		return true;
	}

	@Override
	public Map<String, Object> next() {
		check();
		if(rs == null || (row == null && !getRow())) {
			if(!isClose()) {
				close();
			}
			throw new NoSuchElementException();
		}
		Map<String, Object> ret = row;
		row = null;
		return ret;
	}
	
	/**
	 * 全情報をListとして取得.
	 * @return
	 */
	public List<Map<String, Object>> getRows() {
		check();
		return getRows(0);
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param max
	 * @return
	 */
	public List<Map<String, Object>> getRows(int max) {
		check();
		max = max <= 0 ? 0 : max;
		List<Map<String, Object>> ret = new ObjectList<Map<String, Object>>();
		while(hasNext()) {
			if(max != 0 && ret.size() >= max) {
				break;
			}
			ret.add(row);
			row = null;
		}
		return ret;
	}
	
	// １行の保持情報.
	private ArrayMap row = null;
	
	// 1行の情報を取得.
	private boolean getRow() {
		try {
			if(rs.next()) {
				Object[] n;
				final int len = meta.size();
				final OList<Object[]> list = meta.rawData();
				final ListMap r = new ListMap(len);
				for(int i = 0; i < len; i ++) {
					n = list.get(i);
					r.put((String)n[0], JDBCUtils.getResultColumn(rs, (Integer)n[1], i + 1));
				}
				row = new ArrayMap(r);
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new JDBCException(e);
		}
	}
}

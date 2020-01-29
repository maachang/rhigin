package rhigin.lib.jdbc;

import java.util.Map;

import rhigin.lib.JDBC;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.util.Alphabet;
import rhigin.util.Args;
import rhigin.util.Converter;
import rhigin.util.CsvReader;
import rhigin.util.FileUtil;

/**
 * JDBC-CSVデータをinsertする.
 */
public class JDBCCsv {
	
	/**
	 * できること
	 * 
	 * １：[接続定義名].[テーブル名].csv ファイルを取り込んで、CSV内容をInsertできる。
	 * 
	 * ２：指定テーブルを一旦クリアーしてからインサート処理を行うモードがある。
	 * 
	 * ３：指定テーブルをクリアせずにインサート処理を行うモードがある。
	 * 
	 */
	
	//private static final String DEF_CHARSET = "Windows-31J";
	private static final String DEF_CHARSET = "UTF8";
	
	public static final void main(String[] args) throws Exception {
		// コマンド引数を解析.
		final Args params = Args.set(args);
		// help表示.
		if(params.isValue("-h", "--help")) {
			System.out.println("jcsv [-c] [-j] [-t] [-s] [-d] [-e] [-n] {file}");
			System.out.println(" Read CSV and insert into database table.");
			System.out.println("  [-c] [--conf] [--config] {args}");
			System.out.println("    Set the configuration definition file name.");
			System.out.println("    If omitted, \"jdbc\" character is specified.");
			//System.out.println();
			System.out.println("  [-j] [--jdbc] {args}");
			System.out.println("    Set the connection definition name of jdbc.");
			System.out.println("    If omitted, it must be set with the name of {file}.");
			//System.out.println();
			System.out.println("  [-t] [--table] {args}");
			System.out.println("    Set the write destination table name.");
			System.out.println("    If omitted, it must be set with the name of {file}.");
			//System.out.println();
			System.out.println("  [-s] [--charset] {args}");
			System.out.println("    Set the character code of the CSV file.");
			System.out.println("    If not specified, \""+ DEF_CHARSET + "\" will be set.");
			//System.out.println();
			System.out.println("  [-d] [--delete]");
			System.out.println("    Set to delete all database contents.");
			System.out.println("    If not set, all data will not be deleted.");
			//System.out.println();
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			//System.out.println();
			System.out.println("  [-n] [--num] {number}");
			System.out.println("    Set the start number of the numeric sequence ID.");
			System.out.println("    If not set, start from 1.");
			//System.out.println();
			System.out.println("  {file}");
			System.out.println("    If [-j or -t] is omitted, each is interpreted by the file name.");
			System.out.println("      {file} = [jdbc name].[table name].csv");
			System.out.println("    If [-j or -t] is not omitted, set an arbitrary file name.");
			System.out.println();
			System.exit(0);
			return;
		} else if(params.isValue("-v", "--version")) {
			System.out.println(JDBC.VERSION);
			System.exit(0);
			return;
		}
		int ret = 0;
		JDBCCore core = null;
		try {
			// コンフィグパラメータを取得.
			final String confName = params.get("-c", "--conf", "--config");
			
			// JDBCパラメータを取得.
			final JDBCCsv o = new JDBCCsv();
			String jdbc = params.get("-j", "--jdbc");
			String table = params.get("-t", "--table");
			boolean deleteFlag = params.isValue("-d", "--delete");
			String charset = params.get("-s", "--charset");
			if(charset == null || charset.isEmpty()) {
				charset = DEF_CHARSET;
			}
			Long startNumber = params.getLong("-n", "--num");
			if(startNumber == null) {
				startNumber = 1L;
			}
			
			// ファイル名を取得.
			String fileName = params.getLast();
			if(fileName == null || fileName.isEmpty() || !FileUtil.isFile(fileName)) {
				if(fileName == null || fileName.isEmpty()) {
					throw new JDBCException("The CSV file name to be read has not been set.");
				}
				throw new JDBCException("The specified csv file does not exist: " + fileName);
			}
			
			// ファイル名のみを取得.
			String fileOnlyName = FileUtil.getFileName(fileName);
			
			// JDBC接続名が設定されていない場合は、ファイル名から取得.
			jdbc = csvFileNameByJdbcDefine(jdbc, fileOnlyName);
			if(jdbc == null || jdbc.isEmpty()) {
				throw new JDBCException("jdbc connection name is not set.");
			}
			// テーブル名が設定されていない場合は、ファイル名から取得.
			table = csvFileNameByTableDefine(table, fileOnlyName);
			if(table == null || table.isEmpty()) {
				throw new JDBCException("Table name is not set.");
			}
			
			System.out.println("" + JDBC.NAME + " csv import version (" + JDBC.VERSION + ")");
			System.out.println("target csv : " + fileName);
			System.out.println("jdbc define: " + jdbc);
			System.out.println("table name : " + table);
			System.out.println("delete flag: " + deleteFlag);
			System.out.println();
			
			// JDBCコアを生成.
			core = new JDBCCore();
			core.startup(confName, args);
			
			// CSV実行.
			int resCount = o.execute(core, jdbc, table, deleteFlag, charset, startNumber, fileName);
			System.out.println("success    : " + resCount);
		} catch(Throwable e) {
			System.out.println("error      : " + e);
			e.printStackTrace();
			ret = 1;
		} finally {
			if(core != null) {
				core.destroy();
			}
		}
		System.out.println();
		System.exit(ret);
	}
	
	// ファイル名からJSON接続先名を取得.
	private static final String csvFileNameByJdbcDefine(String jdbc, String name) {
		if(jdbc != null && !jdbc.isEmpty()) {
			return jdbc;
		}
		int p = name.indexOf(".");
		if(p == -1 || Converter.numberOfCharToString(name, ".") < 2) {
			throw new JDBCException(
				"Acquisition of JDBC connection name from file name failed: " + name);
		}
		return name.substring(0, p);
	}
	
	// ファイル名からテーブル名を取得.
	private static final String csvFileNameByTableDefine(String table, String name) {
		if(table != null && !table.isEmpty()) {
			return table;
		}
		int p = name.indexOf(".");
		if(p == -1 || Converter.numberOfCharToString(name, ".") < 2) {
			throw new JDBCException(
				"Acquisition of table name from file name failed: " + name);
		}
		return name.substring(p + 1, name.indexOf(".", p + 1));
	}
	
	// insert文を生成.
	private static final String createInsert(CsvReader csv, String table) {
		StringBuilder buf = new StringBuilder();
		buf.append("INSERT INTO ").append(table).append("(");
		final int len = csv.getHeaderSize();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append(csv.getHeader(i));
		}
		buf.append(") VALUES (");
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append("?");
		}
		buf.append(");");
		return buf.toString();
	}
	
	// オブジェクトの内容をリスト変換.
	private static final Object[] getSqlParams(JDBCConnect conns, long[] counter, Map<String, Object> row) {
		String c;
		final int len = row.size();
		Object[] ret = new Object[len];
		for(int i = 0; i < len; i ++) {
			c = (String)row.get(""+i);
			// １６文字のシーケンスIDを付与する場合。
			if(Alphabet.eq("{seq}", c) || Alphabet.eq("{sequence}", c)) {
				c = conns.TIME12();
			// 数値のシーケンスIDを付与する場合。
			} else if(Alphabet.eq("{num}", c) || Alphabet.eq("{number}", c)) {
				c = "" + (counter[0] ++);
			}
			ret[i] = c;
		}
		return ret;
	}
	
	// バッチ送信タイミング.
	private static final int SEND_BATCH_COUNT = 50;
	
	/**
	 * CSVインサート実行.
	 * @param core
	 * @param jdbc
	 * @param table
	 * @param deleteFlag
	 * @param charset
	 * @param startNumber
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public int execute(JDBCCore core, String jdbc, String table, boolean deleteFlag,
		String charset, long startNumber, String fileName)
		throws Exception {
		int ret = 0;
		CsvReader csv = null;
		try {
			final JDBCConnect conns = core.getConnect(jdbc);
			conns.setAutoCommit(false);
			csv = new CsvReader(fileName, charset, ",");
			final String sql = createInsert(csv, table);
			int cnt = 0;
			
			// テーブル内のデータを全削除する場合.
			if(deleteFlag) {
				conns.addBatch("DELETE FROM " + table + ";");
				cnt ++;
			}
			
			// カウンター.
			long[] counter = new long[] { startNumber };
			
			// insert処理.
			while(csv.hasNext()) {
				conns.addBatch(sql, getSqlParams(conns, counter, csv.next()));
				cnt ++;
				// 一定数のバッチ実行が終わったら、データベースに一斉送信.
				if(cnt > SEND_BATCH_COUNT) {
					conns.executeBatch();
					cnt = 0;
				}
				ret ++;
			}
			if(cnt > 0) {
				conns.executeBatch();
			}
			// コミット処理.
			conns.commit();
		} finally {
			if(csv != null) {
				csv.close();
			}
		}
		return ret;
	}
}
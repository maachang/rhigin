package rhigin.lib.level;

import java.util.List;

import rhigin.lib.Level;
import rhigin.lib.level.operator.LatLonOperator;
import rhigin.lib.level.operator.ObjectOperator;
import rhigin.lib.level.operator.Operator;
import rhigin.lib.level.operator.QueueOperator;
import rhigin.lib.level.operator.SequenceOperator;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.util.Alphabet;
import rhigin.util.Args;
import rhigin.util.CsvReader;
import rhigin.util.FileUtil;
import rhigin.util.FixedSearchArray;
import rhigin.util.RowMap;
import rhigin.util.Time12SequenceId;

/**
 * LevelJsの対象オペレータにCsv情報を出力.
 */
public class LevelJsCsv {
	
	/**
	 * できること
	 * 
	 * １：[オペレータ名].csv ファイルを取り込んで、CSV内容をInsertできる。
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
			System.out.println("lcsv [-c] [-n] [-s] [-d] [-e] {file}");
			System.out.println(" Read CSV and insert into database table.");
			System.out.println("  [-c] [--conf] [--config] {args}");
			System.out.println("    Set the configuration definition file name.");
			System.out.println("    If omitted, \"level\" character is specified.");
			//System.out.println();
			System.out.println("  [-n] [--name] {args}");
			System.out.println("    Set the write destination operator name.");
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
			System.out.println("  {file}");
			System.out.println("    If [-n] is omitted, each is interpreted by the file name.");
			System.out.println("      {file} = [operator name].csv");
			System.out.println("    If [-n] is not omitted, set an arbitrary file name.");
			System.out.println();
			System.exit(0);
			return;
		} else if(params.isValue("-v", "--version")) {
			System.out.println(Level.VERSION);
			System.exit(0);
			return;
		}
		int ret = 0;
		LevelJsCore core = null;
		try {
			// コンフィグパラメータを取得.
			final String confName = params.get("-c", "--conf", "--config");
			
			// JDBCパラメータを取得.
			String opName = params.get("-n", "--name");
			boolean deleteFlag = params.isValue("-d", "--delete");
			String charset = params.get("-s", "--charset");
			if(charset == null || charset.isEmpty()) {
				charset = DEF_CHARSET;
			}
			
			// ファイル名を取得.
			String fileName = params.getLast();
			if(fileName == null || fileName.isEmpty() || !FileUtil.isFile(fileName)) {
				if(fileName == null || fileName.isEmpty()) {
					throw new LevelJsException("The CSV file name to be read has not been set.");
				}
				throw new LevelJsException("The specified csv file does not exist: " + fileName);
			}
			
			// ファイル名のみを取得.
			String fileOnlyName = FileUtil.getFileName(fileName);
			
			// オペレータ名が設定されていない場合は、ファイル名から取得.
			opName = csvFileNameByOperatorDefine(opName, fileOnlyName);
			if(opName == null || opName.isEmpty()) {
				throw new LevelJsException("Operator name is not set.");
			}
			
			System.out.println("" + Level.NAME + " csv import version (" + Level.VERSION + ")");
			System.out.println("target csv : " + fileName);
			System.out.println("operator name : " + opName);
			System.out.println("delete flag: " + deleteFlag);
			System.out.println();
			
			// JDBCコアを生成.
			core = new LevelJsCore();
			core.startup(confName, args);
			
			// CSV実行.
			int resCount = execute(core, opName, deleteFlag, charset, fileName);
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
	
	// ファイル名からオペレータ名を取得.
	private static final String csvFileNameByOperatorDefine(String name, String fname) {
		if(name != null && !name.isEmpty()) {
			return name;
		}
		int p = name.indexOf(".");
		if(p == -1) {
			throw new LevelJsException(
				"Acquisition of operator name from file name failed: " + fname);
		}
		return fname.substring(0, p);
	}
	
	// キー名 及び 要素名 を取得.
	private static final FixedSearchArray<String> getNames(boolean keyFlg, CsvReader csv) {
		String name;
		int len = csv.getHeaderSize();
		FixedSearchArray<String> ret = new FixedSearchArray<String>(len);
		for(int i = 0; i < len; i ++) {
			name = csv.getHeader(i);
			if(name.startsWith("$") == keyFlg) {
				ret.add(name, i);
			}
		}
		if(ret.getCount() == 0) {
			return null;
		}
		ret.fix();
		return ret;
	}
	
	// シーケンスIDに置き換える.
	private static final void convertSequenceId(
		Time12SequenceId seqId, FixedSearchArray<String> header, List<String> row) {
		int no;
		String c;
		int len = header.size();
		for(int i = 0; i < len; i ++) {
			no = header.getNo(i);
			c = row.get(no);
			if(Alphabet.eq("{seq}", c) || Alphabet.eq("{sequence}", c)) {
				row.set(no, Time12SequenceId.toString(seqId.next()));
			}
		}
	}
	
	/**
	 * CSVインサート実行.
	 * @param core
	 * @param deleteFlag
	 * @param charset
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static final int execute(LevelJsCore core, boolean deleteFlag,
		String charset, String fileName)
		throws Exception {
		return execute(core, null, deleteFlag, charset, fileName);
	}
	
	/**
	 * CSVインサート実行.
	 * @param core
	 * @param opName
	 * @param deleteFlag
	 * @param charset
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static final int execute(LevelJsCore core, String opName, boolean deleteFlag,
		String charset, String fileName)
		throws Exception {
		if(fileName == null || fileName.isEmpty()) {
			throw new LevelJsException("CSV file is not set.");
		}
		// オペレータ名が設定されていない場合は、ファイル名から取得.
		opName = csvFileNameByOperatorDefine(opName, fileName);
		if(opName == null || opName.isEmpty()) {
			throw new LevelJsException("Operator name is not set.");
		}
		if(charset == null || charset.isEmpty()) {
			charset = DEF_CHARSET;
		}
		int ret = 0;
		CsvReader csv = null;
		try {
			Operator operator = core.get(opName);
			if(operator == null) {
				throw new LevelJsException("Information of the specified operator name does not exist: " + opName);
			}
			csv = new CsvReader(fileName, charset, ",");
			FixedSearchArray<String> sk = null;
			
			// キー用Mapを生成.
			sk = getNames(true, csv);
			RowMap rowKey = sk == null ? null : new RowMap(sk);
			
			// value用Mapを生成.
			sk = getNames(false, csv);
			RowMap rowValue = new RowMap(sk);
			sk = null;
			
			// シーケンスIDを取得.
			Time12SequenceId seqId = new Time12SequenceId(core.getMachineId());
			
			// テーブル内のデータを全削除する場合.
			if(deleteFlag) {
				operator.trancate();
			}
			
			// データ追加.
			List<String> row = null;
			String operatorType = operator.getOperatorType();
			if(rowKey.getHeader().search("$key") == -1) {
				throw new LevelJsException("Key information frame does not exist.");
			}
			if("object".equals(operatorType)) {
				ObjectOperator op = (ObjectOperator)operator;
				while(csv.hasNext()) {
					row = csv.nextRow();
					rowKey.set(row);
					rowValue.set(row);
					convertSequenceId(seqId, rowValue.getHeader(), row);
					op.put(rowKey.get("$key"), rowValue);
					ret ++;
				}
			} else if("latlon".equals(operatorType)) {
				LatLonOperator op = (LatLonOperator)operator;
				FixedSearchArray<String> head = rowKey.getHeader();
				String qk = null;
				String lat = null;
				String lon = null;
				String secKey = null;
				if(head.search("$qk") != -1) {
					qk = "$qk";
				} else if(head.search("$quadKey") != -1) {
					qk = "$quadKey";
				} else if(head.search("$lat") != -1 && head.search("$lon") != -1) {
					lat = "$lat";
					lon = "$lon";
				}
				if(head.search("$sec") != -1) {
					secKey = "$sec";
				} else if(head.search("$secKey") != -1) {
					secKey = "$secKey";
				} else if(head.search("$second") != -1) {
					secKey = "$second";
				}
				if(qk == null && lat == null && lon == null) {
					throw new LevelJsException("Key information frame does not exist.");
				}
				while(csv.hasNext()) {
					row = csv.nextRow();
					rowKey.set(row);
					rowValue.set(row);
					convertSequenceId(seqId, rowValue.getHeader(), row);
					if(qk != null) {
						if(secKey != null) {
							op.put(rowKey.get(qk), rowKey.get(secKey), rowValue);
							ret ++;
						} else {
							op.put(rowKey.get(qk), rowValue);
							ret ++;
						}
					} else if(lat != null && lon != null) {
						if(secKey != null) {
							op.put(rowKey.get(lat), rowKey.get(lon), rowKey.get(secKey), rowValue);
							ret ++;
						} else {
							op.put(rowKey.get(lat), rowKey.get(lon), rowValue);
							ret ++;
						}
					}
				}
			} else if("sequence".equals(operatorType)) {
				Object key = null;
				SequenceOperator op = (SequenceOperator)operator;
				while(csv.hasNext()) {
					row = csv.nextRow();
					rowValue.set(row);
					convertSequenceId(seqId, rowValue.getHeader(), row);
					if(rowKey != null) {
						rowKey.set(row);
						key = rowKey.get("$key");
					}
					if(key instanceof String) {
						op.put(key, rowValue);
					} else {
						op.put(rowValue);
					}
					ret ++;
				}
			} else if("queue".equals(operatorType)) {
				QueueOperator op = (QueueOperator)operator;
				while(csv.hasNext()) {
					row = csv.nextRow();
					rowValue.set(row);
					convertSequenceId(seqId, rowValue.getHeader(), row);
					op.offer(rowValue);
					ret ++;
				}
			}
		} finally {
			if(csv != null) {
				csv.close();
			}
		}
		return ret;
	}
}
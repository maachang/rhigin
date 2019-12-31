package rhigin.lib.jdbc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import rhigin.lib.JDBC;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.lib.jdbc.runner.JDBCKind;
import rhigin.lib.jdbc.runner.JDBCRow;
import rhigin.lib.jdbc.runner.JDBCUtils;
import rhigin.scripts.JsonOut;
import rhigin.util.Alphabet;
import rhigin.util.Args;
import rhigin.util.ConsoleInKey;
import rhigin.util.ConsoleInKey.StringIgnoreCaseCompleter;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.ObjectList;

/**
 * JDBCコンソール.
 */
public class JDBCConsole {
	
	/**
	 * できること。
	 * 
	 * １：コンソール入力でSQL入力して、対象のJDBCにアクセスできる。
	 * ２：コマンドパラメータ[-f --file] でSQL実行ファイルが設定されている場合
	 *     その内容を実行する.
	 */
	
	public static final void main(String[] args) throws Exception {
		// コマンド引数を解析.
		final Args params = Args.set(args);
		// help表示.
		if(params.isValue("-h", "--help")) {
			System.out.println("jdbc [-c] [-e] [-f]");
			System.out.println(" Executes SQL statement console and file execution.");
			System.out.println("  [-c] [--conf] [--config] {args}");
			System.out.println("    Set the configuration definition file name.");
			//System.out.println();
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			//System.out.println();
			System.out.println("  [-f] [--file] {args}");
			System.out.println("    Set the SQL execution file name.");
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
			final String confName = params.get("-c", "--conf", "--config");
			
			// JDBCコンソールを起動.
			final JDBCConsole o = new JDBCConsole();
			final String sqlFileName = params.get("-f", "--file");
			
			// ファイル名が設定されているが、そのファイルが存在しない場合.
			if(sqlFileName != null && !FileUtil.isFile(sqlFileName)) {
				throw new JDBCException("The specified file does not exist: " + sqlFileName);
			}
			// JDBCコアを生成.
			core = new JDBCCore();
			core.startup(confName, args);
			
			// 実行処理.
			o.execute(sqlFileName != null, core, sqlFileName);
		} catch(Throwable e) {
			e.printStackTrace();
			ret = 1;
		} finally {
			if(core != null) {
				core.destroy();
			}
		}
		System.exit(ret);
	}

	// カレントのDBプーリング名.
	protected Object[] connectParams = null;
	
	// 前回の入力途中の情報.
	protected String backupSql = null;
	
	// SQLリストを取得.
	private List<String> getSqlList(String cmd) {
		// 前回入力のコマンド入力途中がある場合は連結.
		cmd = (backupSql == null || backupSql.isEmpty()) ? cmd : backupSql + "\n" + cmd;
		backupSql = null;
		// コマンド入力途中の場合.
		if(!JDBCUtils.endSqlExists(cmd)) {
			backupSql = cmd;
			return null;
		}
		// SQLリストを取得.
		List<String> sqlList = JDBCUtils.sqlList(cmd);
		// 最後の命令が[;]で閉じてない場合は、最後の命令は入力途中として除外.
		if(sqlList != null && sqlList.size() > 0 && !sqlList.get(sqlList.size()-1).endsWith(";")) {
			backupSql = sqlList.remove(sqlList.size()-1);
		}
		return sqlList;
	}
	
	// クローズ.
	private void close(JDBCCore core) {
		core.close();
		connectParams = null;
	}
	
	// SQLの最後のカンマを外して返却.
	private static final String cutEndSql(String sql) {
		sql = sql.trim();
		if(sql.endsWith(";")) {
			sql = sql.substring(0, sql.length()-1).trim();
		}
		return sql;
	}
	
	// コマンドの第一引数を取得.
	private static final String getCmdArgs1(String head, String cmd) {
		final String name = Converter.cutCote(cmd.substring(head.length()));
		if(name == null || name.isEmpty()) {
			return null;
		}
		return name;
	}
	
	// カレントのコネクション名を取得.
	private void currentConnect(boolean noConsole, JDBCCore core, String cmd) {
		if(Alphabet.eq("connect", cmd)) {
			if(!noConsole) {
				System.out.println(connectParams == null ? "NONE" : JsonOut.toString(connectParams));
			} else {
				throw new JDBCException("The current JDBC connection name has not been specified.");
			}
		} else {
			List<String> list = new ObjectList<String>();
			Converter.cutString(list, true, false, cmd, " \t\r\n");
			if(list.size() == 1) {
				if(!noConsole) {
					System.out.println(connectParams == null ? "NONE" : JsonOut.toString(connectParams));
				} else {
					throw new JDBCException("The current JDBC connection name has not been specified.");
				}
			} else {
				int len = list.size();
				Object[] params = new Object[len-1];
				for(int i = 1, j = 0; i < len; i ++) {
					params[j++] = list.get(i);
				}
				// jdbc定義名のみの設定で、指定名が存在しない場合はエラー.
				if(params.length == 1) {
					if(core.isRegister("" + params[0])) {
						connectParams = params;
					} else {
						throw new JDBCException("The specified JDBC connection name " + params[0] +
							" does not exist in the connection management registration name.");
					}
				// DBに直に接続する設定の場合.
				// (jdbc.jsonの定義で設定されていないDBに接続する場合)
				} else {
					connectParams = params;
				}
			}
		}
	}
	
	// SQL以外の独自コマンドを実行.
	private boolean rhiginSQL(boolean[] exitFlag, boolean noConsole, JDBCCore core, String cmd) {
		exitFlag[0] = false;
		cmd = cutEndSql(cmd);
		if (Alphabet.eq("help", cmd)) {
			if(!noConsole) {
				System.out.println("exit [quit]     Exit the console.");
				System.out.println("close           Destroys all current connections.");
				System.out.println("commit          Commit for the current connection.");
				System.out.println("rollback        Rollback for the current connection.");
				System.out.println("list            Get a list of connection definition names.");
				System.out.println("kind {name}     Displays the specified connection definition details.");
				System.out.println("                {name} Set the connection name.");
				System.out.println("connect {name}  Set and display current connection name.");
				System.out.println("                {name} Set the connection name.");
				System.out.println("");
			}
			return true;
		} else if (Alphabet.eq("exit", cmd) || Alphabet.eq("quit", cmd)) {
			if(!noConsole) {
				System.out.println("");
			}
			// このプロセス処理を終了.
			exitFlag[0] = true;
			return true;
		} else if (Alphabet.eq("close", cmd)) {
			close(core);
			if(!noConsole) {
				System.out.println("");
			}
			return true;
		} else if (Alphabet.eq("commit", cmd)) {
			core.commit();
			if(!noConsole) {
				System.out.println("");
			}
			return true;
		} else if (Alphabet.eq("rollback", cmd)) {
			core.rollback();
			if(!noConsole) {
				System.out.println("");
			}
			return true;
		} else if (Alphabet.eq("list", cmd)) {
			if(!noConsole) {
				System.out.println(JsonOut.toString(core.names()));
			}
			return true;
		} else if (Alphabet.indexOf(cmd, "kind") == 0) {
			if(!noConsole) {
				JDBCKind kind = core.getKind(getCmdArgs1("kind", cmd));
				System.out.println(JsonOut.toString(kind.getMap()));
			}
			return true;
		} else if(Alphabet.indexOf(cmd, "connect") == 0) {
			currentConnect(noConsole, core, cmd);
			return true;
		}
		return false;
	}
	
	// SQLを実行.
	private void executeSQL(boolean noConsole, JDBCCore core, JDBCConnect conns, String sql) {
		switch(JDBCUtils.sqlType(sql)) {
		case JDBCUtils.SQL_SELECT:
			if(!noConsole) {
				JDBCRow row = null;
				try {
					row = conns.query(sql);
					System.out.println(row);
					System.out.println();
				} finally {
					if(row != null) {
						row.close();
					}
				}
			}
			break;
		case JDBCUtils.SQL_INSERT:
			if(!noConsole) {
				JDBCRow row = null;
				try {
					row = conns.insert(sql);
					System.out.println(row);
				} finally {
					if(row != null) {
						row.close();
					}
				}
			} else {
				int ret = conns.update(sql);
				if(!noConsole) {
					System.out.println(ret);
				}
			}
			break;
		case JDBCUtils.SQL_SQL:
			int ret = conns.update(sql);
			if(!noConsole) {
				System.out.println(ret);
			}
			break;
		case JDBCUtils.SQL_UNKNOWN:
			throw new JDBCException("Unknown SQL statement: " + sql);
		}
	}
	
	// 補完リスト.
	private static final String[] COMPLETER_LIST = new String[] {
		// 大体のSQL予約語.
		"select", "from", "where", "is", "between", "and", "or", "not", "null",
		"order", "by", "desc", "asc", "insert", "into", "values", "update", "set",
		"delete", "create", "table", "alter", "add", "modify", "drop", "rename",
		"view", "commit", "rollback", "savepoint", "to", "distinct", "like", "as",
		"join", "inner", "left", "outer", "right", "full", "cross", "exists", "any",
		"all", "some", "with", "recursive", "union", "except", "minus", "intersect",
		"group", "sum", "avg", "max", "min", "count", "having", "concatenate",
		"concat", "upper", "lower", "ucase", "lcase", "substring", "substr",
		"mid", "trim", "ltrim", "rtrim", "both", "leading", "trailing", "translate",
		"replace", "char_length", "len", "length", "octet_length", "lenb", "lengthb",
		"mod", "abs", "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "round",
		"ceiling", "ceil", "floor", "exp", "log", "log10", "pow", "power", "sqrt",
		"pi", "sign", "current_date", "current_time", "crrent_timestamp", "sysdate",
		"now", "extract", "to_char", "str", "to_number", "to_date", "to_timestamp",
		"cast", "convert", "index", "user", "grant", "revoke", "trigger", "begin",
		"transaction", "tran", "work", "start", "save",
		// 以下は、今回のコンソール専用.
		"help", "exit", "quit", "close", "list", "kind", "connect"
	};
	
	// コンソール実行.
	protected void execute(boolean noConsole, JDBCCore core, String fileName) throws Exception {
		int len;
		String cmd;
		List<String> sqlList;
		ConsoleInKey console = null;
		BufferedReader reader = null;
		try {
			// JDBCの接続名が１件以上登録されている場合は、一番最初をカレント名とする.
			if(core.size() > 0) {
				connectParams = new Object[] {core.getName(0)};
			}
			if(!noConsole) {
				System.out.println("" + JDBC.NAME + " console version (" + JDBC.VERSION + ")");
				System.out.println(" Set \";\" at the end of the command.");
				System.out.println();
				console = new ConsoleInKey(".jdbc-cons");
				console.addTabCompleter(new StringIgnoreCaseCompleter(COMPLETER_LIST));
			} else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			}
			boolean[] exitFlag = new boolean[] {false};
			while (true) {
				try {
					// １行の情報を取得.
					if(noConsole) {
						if ((cmd = reader.readLine()) == null) {
							// ファイル終端なので終了.
							return;
						}
					} else {
						if ((cmd = console.readLine(JDBC.NAME+"> ")) == null) {
							// null返却の場合は、ctrl+cの可能性があるので、
							// 終了処理をおこなってコンソール処理終了.
							System.out.println("");
							return;
						}
					}
					// 入力情報が存在しない場合.
					if((cmd = Converter.cutComment(false, cmd).trim()).isEmpty()) {
						continue;
					}
					// SQLリストを取得.
					sqlList = getSqlList(cmd);
					cmd = null;
					if(sqlList == null) {
						// 命令が存在しない場合.
						continue;
					}
					len = sqlList.size();
					for(int i = 0; i < len; i ++) {
						if ((cmd = sqlList.get(i).trim()).length() == 0) {
							continue;
						}
						if(!noConsole) {
							System.out.println();
							System.out.println("> " + cmd);
						}
						// SQL以外のコマンド実行の場合.
						if(rhiginSQL(exitFlag, noConsole, core, cmd)) {
							// 終了フラグがONの場合は処理終了.
							if(exitFlag[0]) {
								return;
							}
							continue;
						}
						// カレントのコネクション名が存在しない場合は処理しない.
						if(connectParams == null) {
							if(noConsole) {
								throw new JDBCException("The current JDBC connection name has not been set.");
							} else {
								System.err.println("The current JDBC connection name has not been set.");
							}
							continue;
						}
						// SQL実行.
						executeSQL(noConsole, core, core.getConnect(connectParams), cmd);
					}
					if(len > 0 && !noConsole) {
						System.out.println();
					}
				} catch (Throwable e) {
					if(!noConsole) {
						// エラーの場合は、入力途中のSQLを破棄する.
						backupSql = null;
						e.printStackTrace();
					} else {
						throw e;
					}
				} finally {
					sqlList = null;
					cmd = null;
				}
			}
		} finally {
			if(console != null) {
				try {
					console.close();
				} catch(Exception e) {}
			}
			if(reader != null) {
				try {
					reader.close();
				} catch(Exception e) {}
			}
		}
	}
}

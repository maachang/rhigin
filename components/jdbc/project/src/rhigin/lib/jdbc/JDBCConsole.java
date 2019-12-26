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
import rhigin.util.Converter;

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
		final Args params = new Args(args);
		// help表示.
		if(params.isValue("-h", "--help")) {
			System.out.println("jdbc [-c or --conf or --config] [-f or --file]");
			System.out.println(" Executes SQL statement console and file execution.");
			System.out.println("  [-c]");
			System.out.println("  [--conf]");
			System.out.println("  [--config]");
			System.out.println("    Set the configuration definition file name.");
			System.out.println();
			System.out.println("  [-f]");
			System.out.println("  [--file]");
			System.out.println("    Set the SQL execution file name.");
			System.out.println();
			System.exit(0);
			return;
		}
		try {
			// JDBCコアを生成.
			final JDBCCore core = new JDBCCore();
			final String confName = params.get("-c", "--conf", "--config");
			core.startup(confName, args);
			
			// JDBCコンソールを起動.
			final JDBCConsole o = new JDBCConsole();
			final String sqlFileName = params.get("-f", "--file");
			if(sqlFileName == null) {
				o.executeConsole(core);
			} else {
				o.executeFile(core, sqlFileName);
			}
			System.exit(0);
		} catch(Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// カレントのDBプーリング名.
	protected String currentName = null;
	
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
		currentName = null;
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
		String name = Converter.cutCote(cutEndSql(cmd.substring(head.length())));
		if(name == null || name.isEmpty()) {
			return null;
		}
		return name;
	}
	
	// カレントのコネクション名を取得.
	private void current(boolean noConsole, JDBCCore core, String cmd) {
		if(Alphabet.eq("current", cmd)) {
			if(!noConsole) {
				System.out.println(currentName == null ? "NONE" : currentName);
			} else {
				throw new JDBCException("The current JDBC connection name has not been specified.");
			}
		} else {
			String name = getCmdArgs1("current", cmd);
			if(name == null || name.isEmpty()) {
				if(!noConsole) {
					System.out.println(currentName == null ? "NONE" : currentName);
				} else {
					throw new JDBCException("The current JDBC connection name has not been specified.");
				}
			} else {
				// 指定名が存在しない場合はエラー.
				if(core.isRegister(name)) {
					currentName = name;
				} else {
					throw new JDBCException("The specified JDBC connection name " + name +
						" does not exist in the connection management registration name.");
				}
			}
		}
	}
	
	// SQL以外の独自コマンドを実行.
	private boolean rhiginSQL(boolean[] exitFlag, boolean noConsole, JDBCCore core, String cmd) {
		exitFlag[0] = false;
		cmd = cutEndSql(cmd);
		if ("help".equals(cmd)) {
			if(!noConsole) {
				System.out.println("exit [quit]    Exit the console.");
				System.out.println("close          Destroys all current connections.");
				System.out.println("commit         Commit for the current connection.");
				System.out.println("rollback       Rollback for the current connection.");
				System.out.println("list           Get a list of connection definition names.");
				System.out.println("kind {name}    Displays the specified connection definition details.");
				System.out.println("current {name} Set and display current connection name.");
				System.out.println("               {name} Set the connection name.");
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
		} else if(Alphabet.indexOf(cmd, "current") == 0) {
			current(noConsole, core, cmd);
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
					row.close();
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
					row.close();
				}
			} else {
				conns.update(sql);
			}
			break;
		case JDBCUtils.SQL_SQL:
			conns.update(sql);
			break;
		case JDBCUtils.SQL_UNKNOWN:
			throw new JDBCException("Unknown SQL statement: " + sql);
		}
	}

	
	// コンソール実行.
	protected void executeConsole(JDBCCore core) throws Exception {
		System.out.println("" + JDBC.NAME + " console version (" + JDBC.VERSION + ")");
		System.out.println("");
		ConsoleInKey console = new ConsoleInKey(".jdbc-cons");
		try {
			int len;
			String cmd;
			List<String> sqlList;
			boolean[] exitFlag = new boolean[] {false};
			while (true) {
				try {
					if ((cmd = console.readLine(JDBC.NAME+"> ")) == null) {
						// null返却の場合は、ctrl+cの可能性があるので、
						// 終了処理をおこなってコンソール処理終了.
						System.out.println("");
						return;
					} else if((cmd = Converter.cutComment(false, cmd).trim()).isEmpty()) {
						continue;
					}
					// SQLリストを取得.
					sqlList = getSqlList(cmd);
					cmd = null;
					if(sqlList == null) {
						continue;
					}
					len = sqlList.size();
					for(int i = 0; i < len; i ++) {
						if ((cmd = sqlList.get(i).trim()).length() == 0) {
							continue;
						}
						System.out.println("> " + cmd);
						// SQL以外のコマンド実行の場合.
						if(rhiginSQL(exitFlag, false, core, cmd)) {
							if(exitFlag[0]) {
								return;
							}
							continue;
						}
						// カレントのコネクション名が存在しない場合は処理しない.
						if(currentName == null) {
							System.err.println("The current JDBC connection name has not been set.");
							continue;
						}
						// SQL実行.
						executeSQL(false, core, core.getConnect(currentName), cmd);
					}
				} catch (Throwable e) {
					// エラーの場合は、入力途中のSQLを破棄する.
					backupSql = null;
					e.printStackTrace();
				} finally {
					sqlList = null;
					cmd = null;
				}
			}
		} finally {
			core.destroy();
			console.close();
		}
	}
	
	// ファイル実行.
	protected void executeFile(JDBCCore core, String fileName) throws Exception {
		BufferedReader reader = null;
		try {
			int len;
			String cmd;
			List<String> sqlList;
			boolean[] exitFlag = new boolean[] {false};
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			while (true) {
				try {
					if ((cmd = reader.readLine()) == null) {
						return;
					} else if((cmd = Converter.cutComment(false, cmd).trim()).isEmpty()) {
						continue;
					}
					// SQLリストを取得.
					sqlList = getSqlList(cmd);
					cmd = null;
					if(sqlList == null) {
						continue;
					}
					len = sqlList.size();
					for(int i = 0; i < len; i ++) {
						if ((cmd = sqlList.get(i).trim()).length() == 0) {
							continue;
						}
						// SQL以外のコマンド実行の場合.
						if(rhiginSQL(exitFlag, false, core, cmd)) {
							if(exitFlag[0]) {
								return;
							}
							continue;
						}
						// カレントのコネクション名が存在しない場合は処理しない.
						if(currentName == null) {
							throw new JDBCException("The current JDBC connection name has not been set.");
						}
						// SQL実行.
						executeSQL(false, core, core.getConnect(currentName), cmd);
					}
				} finally {
					sqlList = null;
					cmd = null;
				}
			}
		} finally {
			core.destroy();
			if(reader != null) {
				try {
					reader.close();
				} catch(Exception e) {}
			}
		}
	}
}


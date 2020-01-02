package rhigin.util;

import java.util.List;

/**
 * 実行引数の取得処理. たとえば args = ["-a", "hoge"] のような実行引数が設定されていた場合,
 * 
 * String[] args = ...;
 * Args argsObject = new Args(args);
 * String value = argsObject.get("-a");
 * value.equals("hoge") == true;
 * 
 * のような形で取得が出来ます.
 */
public class Args {
	private static String[] MAIN_ARGS = null;
	private static Args THIS = null;

	/**
	 * Mainメソッドに設定されたコマンド引数を設定します.
	 * 
	 * @param args
	 * @return
	 */
	public static final Args set(String[] args) {
		if (MAIN_ARGS == null) {
			MAIN_ARGS = args;
			THIS = new Args();
		}
		return THIS;
	}

	/**
	 * Mainメソッドに設定されたコマンド引数を取得します.
	 * 
	 * @return
	 */
	public static final String[] get() {
		return MAIN_ARGS;
	}

	/**
	 * インスタンスオブジェクトを取得.
	 * 
	 * @return Args
	 */
	public static final Args getInstance() {
		return THIS;
	}

	// Argsオブジェクトの内容.
	private String[] args;

	/**
	 * コンストラクタ.
	 */
	public Args() {
		this(MAIN_ARGS);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param args
	 *            mainの引数を設定します.
	 */
	public Args(String[] args) {
		this.args = args;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param list 引数群を設定します.
	 */
	public Args(List<String> list) {
		int len = list.size();
		args = new String[len];
		for(int i = 0; i < len; i ++) {
			args[i] = list.get(i);
		}
	}

	/**
	 * このオブジェクトに設定されたコマンド引数を取得.
	 * 
	 * @return String[]
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * 指定ヘッダ名を設定して、要素を取得します.
	 * 
	 * @param name
	 * @return
	 */
	public String get(String... names) {
		final int len = names.length;
		final int lenJ = args.length - 1;
		for(int i = 0; i < len; i ++) {
			if(Converter.isNumeric(names[i])) {
				final int no = Converter.convertInt(names[i]);
				if(no >= 0 && no < args.length) {
					return args[no];
				}
			} else {
				for (int j = 0; j < lenJ; j++) {
					if (names[j].equals(args[j])) {
						return args[j + 1];
					}
				}
			}
		}
		return null;
	}

	/**
	 * 指定ヘッダ名を指定して、そのヘッダ名が存在するかチェックします.
	 * 
	 * @param names
	 * @return boolean
	 */
	public boolean isValue(String... names) {
		final int len = names.length;
		final int lenJ = args.length;
		for(int i = 0; i < len; i ++) {
			if(Converter.isNumeric(names[i])) {
				final int no = Converter.convertInt(names[i]);
				if(no >= 0 && no < args.length) {
					return true;
				}
			} else {
				for (int j = 0; j < lenJ; j++) {
					if (names[j].equals(args[j])) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 最初のパラメータを取得.
	 * @return
	 */
	public String getFirst() {
		if(args.length == 0) {
			return "";
		}
		return args[0];
	}
	
	/**
	 * 一番うしろのパラメータを取得.
	 * @return
	 */
	public String getLast() {
		if(args.length == 0) {
			return "";
		}
		return args[args.length - 1];
	}
	
	/**
	 * パラメータ数を取得.
	 * @return
	 */
	public int size() {
		return args.length;
	}

	/**
	 * boolean情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	public Boolean getBoolean(String... n) {
		return Converter.convertBool(get(n));
	}

	/**
	 * int情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Integer 情報が返却されます.
	 */
	public Integer getInt(String... n) {
		return Converter.convertInt(get(n));
	}

	/**
	 * long情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Long 情報が返却されます.
	 */
	public Long getLong(String... n) {
		return Converter.convertLong(get(n));
	}

	/**
	 * float情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Float 情報が返却されます.
	 */
	public Float getFloat(String... n) {
		return Converter.convertFloat(get(n));
	}

	/**
	 * double情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Double 情報が返却されます.
	 */
	public Double getDouble(String... n) {
		return Converter.convertDouble(get(n));
	}

	/**
	 * String情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return String 情報が返却されます.
	 */
	public String getString(String... n) {
		return Converter.convertString(get(n));
	}

	/**
	 * Date情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Date 情報が返却されます.
	 */
	public java.sql.Date getDate(String... n) {
		return Converter.convertSqlDate(get(n));
	}

	/**
	 * Time情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Time 情報が返却されます.
	 */
	public java.sql.Time getTime(String... n) {
		return Converter.convertSqlTime(get(n));
	}

	/**
	 * Timestamp情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Timestamp 情報が返却されます.
	 */
	public java.sql.Timestamp getTimestamp(String... n) {
		return Converter.convertSqlTimestamp(get(n));
	}
}

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
public class Args implements ConvertGet<String> {
	private static String[] MAIN_ARGS = null;
	private static Args THIS = null;

	/**
	 * Mainメソッドに設定されたコマンド引数を設定します.
	 * 
	 * @param args
	 */
	public static final void set(String[] args) {
		if (MAIN_ARGS == null) {
			MAIN_ARGS = args;
			THIS = new Args();
		}
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

	@Override
	public Object getOriginal(String n) {
		return get(n);
	}
}

package rhigin.util;

/**
 * 実行引数の取得処理.
 * たとえば args = ["-a", "hoge"] のような実行引数が設定されていた場合,
 * Args argsObject = new Args(args);
 * String value = argsObject.get("-a");
 * value.equals("hoge") == true
 * 
 * のような形で取得が出来ます.
 */
public class Args implements ConvertGet<String> {
	private static String[] MAIN_ARGS = null;
	private static Args THIS = null;
	
	/**
	 * Mainメソッドに設定されたコマンド引数を設定します.
	 * @param args
	 */
	public static final void set(String[] args) {
		if(MAIN_ARGS == null) {
			MAIN_ARGS = args;
			THIS = new Args();
		}
	}
	
	/**
	 * Mainメソッドに設定されたコマンド引数を取得します.
	 * @return
	 */
	public static final String[] get() {
		return MAIN_ARGS;
	}
	
	/**
	 * インスタンスオブジェクトを取得.
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
	 * @param args mainの引数を設定します.
	 */
	public Args(String[] args) {
		this.args = args;
	}
	
	/**
	 * このオブジェクトに設定されたコマンド引数を取得.
	 * @return String[]
	 */
	public String[] getArgs() {
		return args;
	}
	
	/**
	 * 指定ヘッダ名を設定して、要素を取得します.
	 * @param name
	 * @return
	 */
	public String get(String name) {
		final int len = args.length - 1;
		for(int i = 0;i < len; i ++) {
			if(name.equals(args[i])) {
				return args[i+1];
			}
		}
		return null;
	}
	
	/**
	 * 指定ヘッダ名を指定して、そのヘッダ名が存在するかチェックします.
	 * @param name
	 * @return boolean
	 */
	public boolean isValue(String name) {
		final int len = args.length;
		for(int i = 0;i < len; i ++) {
			if(name.equals(args[i])) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Object getOriginal(String n) {
		return get(n);
	}
}

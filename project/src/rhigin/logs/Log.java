package rhigin.logs;

/**
 * ログ.
 */
public interface Log {
	/**
	 * traecログを出力.
	 * @param args
	 */
	public void trace(Object... args);
	
	/**
	 * debugログを出力.
	 * @param args
	 */
	public void debug(Object... args);
	
	/**
	 * infoログを出力.
	 * @param args
	 */
	public void info(Object... args);
	
	/**
	 * warnログを出力.
	 * @param args
	 */
	public void warn(Object... args);
	
	/**
	 * errorログを出力.
	 * @param args
	 */
	public void error(Object... args);
	
	/**
	 * fatalログを出力.
	 * @param args
	 */
	public void fatal(Object... args);
	
	/**
	 * traceログが出力可能かチェック.
	 * @return
	 */
	public boolean isTraceEnabled();
	
	/**
	 * debugログが出力可能かチェック.
	 * @return
	 */
	public boolean isDebugEnabled();
	
	/**
	 * infoログが出力可能かチェック.
	 * @return
	 */
	public boolean isInfoEnabled();
	
	/**
	 * warnログが出力可能かチェック.
	 * @return
	 */
	public boolean isWarnEnabled();
	
	/**
	 * errorログが出力可能かチェック.
	 * @return
	 */
	public boolean isErrorEnabled();
	
	/**
	 * fatalログが出力可能かチェック.
	 * @return
	 */
	public boolean isFatalEnabled();
	
	/**
	 * ログ名を取得.
	 * @return
	 */
	public String getName();
}

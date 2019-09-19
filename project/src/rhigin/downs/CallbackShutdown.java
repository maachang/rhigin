package rhigin.downs;

import rhigin.util.AtomicNumber;

/**
 * シャットダウンコールバック定義. サーバーシャットダウンを実行するコールバック処理をこのオブジェクトを
 * 継承してShutdownHook.registHook()に登録することで、シャットダウン時に 処理が実行されます.
 */
public abstract class CallbackShutdown {

	/**
	 * シャットダウン実行フラグ.
	 */
	protected final AtomicNumber isShutdown = new AtomicNumber(0);

	/**
	 * シャットダウン実行フラグを取得.
	 * 
	 * @return boolean [true]の場合、既に実行されました.
	 */
	public boolean isShutdown() {
		return isShutdown.get() != 0;
	}

	/**
	 * シャットダウン完了フラグを設定.
	 */
	protected void exitShutdown() {
		isShutdown.set(1);
	}

	/**
	 * シャットダウンを実行するコールバックメソッド. ※利用する場合はこのメソッドを実装してください.
	 */
	public abstract void execution();

}

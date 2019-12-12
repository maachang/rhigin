package rhigin.lib.jdbc.pooling;

import java.sql.Connection;

import rhigin.lib.jdbc.runner.JDBCKind;

/**
 * プーリング対応コネクションオブジェクト.
 */
public interface AtomicPoolConnection extends Connection {

	/**
	 * コネクションオブジェクト破棄.
	 */
	public void destroy();

	/**
	 * オブジェクト復帰時の呼び出し処理.
	 * 
	 * @return AtomicPoolConnection オブジェクトが返却されます.
	 */
	public AtomicPoolConnection recreate();

	/**
	 * 最終設定時間を取得.
	 * 
	 * @return long 最終設定時間が返却されます.
	 */
	public long lastTime();

	/**
	 * JDBCKindを取得.
	 * 
	 * @return JDBCKind JDBCKindが返却されます.
	 */
	public JDBCKind getKind();

}

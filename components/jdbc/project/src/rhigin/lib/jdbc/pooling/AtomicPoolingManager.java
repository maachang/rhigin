package rhigin.lib.jdbc.pooling;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.lib.jdbc.runner.JDBCKind;
import rhigin.scripts.JsonOut;
import rhigin.util.Flag;

/**
 * 複数データベースプーリング管理.
 */
public class AtomicPoolingManager {

	/** プーリングデータベース管理オブジェクト群. **/
	private final Map<String, AtomicPooling> manager = new HashMap<String, AtomicPooling>();
	private final List<String> managerList = new ArrayList<String>();

	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();

	/**
	 * コンストラクタ.
	 */
	public AtomicPoolingManager() {
		destroyFlag.set(false);
	}

	/**
	 * 情報破棄.
	 */
	public void destroy() {
		if (destroyFlag.setToGetBefore(true)) {
			return;
		}
		String name;
		AtomicPooling pool;
		Iterator<String> it = manager.keySet().iterator();
		while (it.hasNext()) {
			name = it.next();
			if ((pool = manager.get(name)) != null) {
				pool.destroy();
			}
		}
		manager.clear();
		managerList.clear();
	}

	/**
	 * オブジェクトが既に破棄されているかチェック.
	 * 
	 * @return boolean [true]の場合、既に破棄されています.
	 */
	public boolean isDestroy() {
		return destroyFlag.get();
	}

	/** チェック処理. **/
	private void check() {
		if (isDestroy()) {
			throw new JDBCException("オブジェクトは既に破棄されています");
		}
	}

	/**
	 * コネクションプーリングオブジェクトを登録.
	 * 
	 * @param pool プーリングオブジェクトを設定します.
	 */
	public void register(AtomicPooling pool) {
		register(pool.getKind().getName(), pool);
	}

	/**
	 * コネクションプーリングオブジェクトを登録.
	 * 
	 * @param name プーリングオブジェクト名を設定します.
	 * @param pool プーリングオブジェクトを設定します.
	 */
	public void register(String name, AtomicPooling pool) {
		check();
		if (name == null || name.length() <= 0) {
			throw new JDBCException("登録対象のプーリングオブジェクト名が定義されていません");
		} else if (pool == null || pool.isDestroy()) {
			throw new JDBCException("登録対象のプーリングオブジェクトは有効ではありません");
		} else if (manager.containsKey(name)) {
			throw new JDBCException("対象のプーリングオブジェクト名[" + name + "]は既に存在します");
		}
		manager.put(name, pool);
		managerList.add(name);
	}

	/**
	 * コネクションプーリングオブジェクトを破棄.
	 * 
	 * @param name 対象のプーリングオブジェクト名を設定します.
	 * @return JDBCPooling 登録解除されたプーリングオブジェクトが返却されます.
	 */
	public AtomicPooling release(String name) {
		check();
		if (name == null || name.length() <= 0) {
			throw new JDBCException("登録対象のプーリングオブジェクト名が定義されていません");
		}
		if (!manager.containsKey(name)) {
			return null;
		}
		AtomicPooling ret = manager.remove(name);
		int len = managerList.size();
		for(int i = len - 1; i >= 0; i --) {
			if(name.equals(managerList.get(i))) {
				managerList.remove(i);
			}
		}
		return ret;
	}

	/**
	 * コネクションオブジェクトを取得.
	 * 
	 * @param name 対象のプーリングオブジェクト名を設定します.
	 * @return Connection コネクションオブジェクトが返却されます.
	 */
	public AtomicPooling get(String name) {
		check();
		if (name == null || name.length() <= 0) {
			throw new JDBCException("登録対象のプーリングオブジェクト名が定義されていません");
		}
		AtomicPooling pool;
		if ((pool = manager.get(name)) == null) {
			throw new JDBCException("対象のプーリングオブジェクト名[" + name + "]は存在しません");
		}
		return pool;
	}

	/**
	 * コネクションオブジェクトを取得.
	 * 
	 * @param name 対象のプーリングオブジェクト名を設定します.
	 * @return Connection コネクションオブジェクトが返却されます.
	 * @exception Exception 例外.
	 */
	public Connection getConnection(String name) throws Exception {
		return get(name).getConnection();
	}

	/**
	 * 登録名のJDBCKindを取得.
	 * 
	 * @param name 対象の登録名を設定します.
	 * @return JDBCKind JDBCKindが返却されます.
	 */
	public JDBCKind getKind(String name) {
		return get(name).getKind();
	}

	/**
	 * 登録数を取得.
	 * 
	 * @return int 登録数が返却されます.
	 */
	public int size() {
		check();
		return manager.size();
	}

	/**
	 * 登録名一覧を取得.
	 * 
	 * @return String[] 登録名一覧が返却されます.
	 */
	public String[] getNames() {
		check();
		int len = managerList.size();
		String[] ret = new String[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = managerList.get(i);
		}
		return ret;
	}

	/**
	 * 登録名を取得.
	 * 
	 * @param list 格納対象のオブジェクトを設定します.
	 */
	public void getNames(List<String> list) {
		check();
		int len = managerList.size();
		for(int i = 0; i < len; i ++) {
			list.add(managerList.get(i));
		}
	}

	/**
	 * 登録名が存在するかチェック.
	 * 
	 * @param name 対象の登録名を設定します.
	 * @return boolean [true]の場合、存在します.
	 */
	public boolean contains(String name) {
		check();
		if (name == null || name.length() <= 0) {
			return false;
		}
		return manager.containsKey(name);
	}

	/**
	 * 文字変換.
	 * 
	 * @return String 登録されている情報を文字情報に変換します.
	 */
	public String toString() {
		String name;
		final int len = managerList.size();
		final StringBuilder buf = new StringBuilder("[\n");
		for(int i = 0; i < len; i ++) {
			name = managerList.get(i);
			if(i != 0) {
				buf.append(",\n");
			}
			buf.append(JsonOut.toString(2, manager.get(name).getKind().getMap()));
		}
		buf.append("\n]");
		return buf.toString();
	}
	
	/**
	 * 登録順の名前を取得.
	 * @param no
	 * @return
	 */
	public String getName(int no) {
		return managerList.get(no);
	}
}

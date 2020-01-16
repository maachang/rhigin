package rhigin.lib.level.runner;

import rhigin.lib.level.operator.OperateIterator;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.compile.CompileCache;
import rhigin.util.OList;

/**
 * LevelJsクローズ処理.
 */
public class LevelJsCloseable implements RhiginEndScriptCall {
	
	// ローカル実態オブジェクト.
	private static final class Entity {
		public OList<OperateIterator> iteratorList;
	}
	
	// スレッドが管理するJDBCコネクション関連のオブジェクト.
	private final ThreadLocal<Entity> lo = new ThreadLocal<Entity>();
	
	/**
	 * コンストラクタ.
	 */
	public LevelJsCloseable() {}
	
	// ローカルオブジェクトを取得.
	private final Entity lo() {
		Entity ret = lo.get();
		if(ret == null) {
			ret = new Entity();
			lo.set(ret);
		}
		return ret;
	}
	
	// ローカルオブジェクトをクリア.
	private final void clearLo() {
		lo.set(null);
	}
	
	/**
	 * OperateIterator をセット.
	 * @param itr
	 * @return
	 */
	public final LevelJsCloseable reg(OperateIterator itr) {
		Entity et = lo();
		OList<OperateIterator> list = et.iteratorList;
		if(list == null) {
			list = new OList<OperateIterator>();
			et.iteratorList = list;
		}
		list.add(itr);
		return this;
	}
	
	/**
	 * OperateIteratorクリア.
	 * @return
	 */
	public final LevelJsCloseable clearIterator() {
		int len;
		Entity et;
		if((et = lo.get()) != null) {
			final OList<OperateIterator> list = et.iteratorList;
			if(list != null && (len = list.size()) > 0) {
				boolean f;
				OperateIterator c;
				for(int i = 0; i < len; i ++) {
					c = list.get(i);
					try {
						f = c.isClose();
					} catch(Exception e) {
						f = false;
					}
					if(!f) {
						try {
							c.close();
						} catch(Exception e) {}
					}
				}
				list.clear();
			}
		}
		return this;
	}
	
	/**
	 * 今回のスクリプト実行で利用したJDBCオブジェクト関連のクローズ処理.
	 * @params context
	 * @params cache
	 */
	@Override
	public final void call(RhiginContext context, CompileCache cache) {
		clearIterator();
		clearLo();
	}
}

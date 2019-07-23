package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.Entity;
import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;

/**
 * entity管理.
 */
public class EntityFunctions {
	// entity管理.
	private static final ThreadLocal<Entity> entityLocal = new ThreadLocal<Entity>();
	
	/**
	 * スクリプト終了時に呼び出します.
	 */
	public static final void exit() {
		entityLocal.set(null);
	}
	
	// entity.
	private static final class ExposeFunction extends RhiginFunction {
		private static final ExposeFunction THIS = new ExposeFunction();
		public static final ExposeFunction getInstance() {
			return THIS;
		}
		
		@Override
		public String getName() {
			return "expose";
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 2) {
				Entity entity = entityLocal.get();
				if(entity == null) {
					entity = new Entity();
					entityLocal.set(entity);
				}
				entity.expose(args);
			}
			return Undefined.instance;
		}
	}
	
	// entity.
	private static final class EntityFunction extends RhiginFunction {
		private static final EntityFunction THIS = new EntityFunction();
		public static final EntityFunction getInstance() {
			return THIS;
		}
		
		@Override
		public String getName() {
			return "entity";
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 2) {
				Entity entity = entityLocal.get();
				if(entity == null) {
					entity = new Entity();
					entityLocal.set(entity);
				}
				return entity.entity(Converter.convertString(args[0]), args[1]);
			}
			return Undefined.instance;
		}
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void putLibrary(Scriptable scope) {
		scope.put(ExposeFunction.getInstance().getName(), scope, ExposeFunction.getInstance());
		scope.put(EntityFunction.getInstance().getName(), scope, EntityFunction.getInstance());
	}
}

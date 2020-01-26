package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.Entity;
import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * entity管理.
 */
public class EntityFunctions {
	// entity管理.
	private static final ThreadLocal<Entity> local = new ThreadLocal<Entity>();

	/**
	 * スクリプト終了時に呼び出します.
	 */
	public static final void exit() {
		local.set(null);
	}

	// expose.
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			Entity entity = local.get();
			if (entity == null) {
				entity = new Entity();
				local.set(entity);
			}
			entity.expose(args);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 2) {
				Entity entity = local.get();
				if (entity == null) {
					entity = new Entity();
					local.set(entity);
				}
				return entity.entity(Converter.convertString(args[0]), args[1]);
			}
			return argsException();
		}
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("expose", scope, ExposeFunction.getInstance());
		scope.put("entity", scope, EntityFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("expose", ExposeFunction.getInstance());
		fkv.put("entity", EntityFunction.getInstance());
	}

}

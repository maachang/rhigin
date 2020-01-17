package rhigin.lib.level.runner;

import java.lang.reflect.Method;

import org.maachang.leveldb.JniBuffer;
import org.maachang.leveldb.LevelValues;
import org.maachang.leveldb.LevelValues.OriginCode;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.scripts.objects.JDateObject;
import rhigin.scripts.objects.JDateObject.JDateInstanceObject;
import rhigin.util.Converter;

/**
 * Rhiginに対応したオリジナルなLevelValues変換処理.
 */
public class RhiginOriginCode extends OriginCode {
	@Override
	public Object convert(Object o) throws Exception {
		// null or undefined の場合.
		if (o == null || o instanceof Undefined) {
			return null;
		}
		// rhinoのjavaオブジェクトwrapper対応.
		if (o instanceof Wrapper) {
			return ((Wrapper) o).unwrap();
		}
		return o;
	}
	
	/** JSDateオブジェクトの場合は、java.util.Dateに変換. **/
	public static final Object getJSDate(Object o) {
		if (o instanceof JDateInstanceObject) {
			// JDate.
			return ((JDateInstanceObject)o).getTime();
		} else if (o instanceof IdScriptableObject &&
			"Date".equals(((IdScriptableObject)o).getClassName())) {
			// NativeDate.
			try {
				// 現状リフレクションで直接取得するようにする.
				// 本来は ScriptRuntime.toNumber(NativeDate) で取得できるのだけど、
				// これは rhinoのContextの範囲内でないとエラーになるので.
				final Method md = o.getClass().getDeclaredMethod("getJSTimeValue");
				md.setAccessible(true);
				return Converter.convertLong(md.invoke(o));
			} catch (Exception e) {
				// エラーの場合は処理しない.
			}
		}
		return null;
	}
	
	// JDate用のオブジェクトコード.
	private static final int JDATE_OBJECT_CODE = USE_OBJECT_CODE;

	@Override
	public boolean encode(JniBuffer buf, Object o) throws Exception {
		// DateオブジェクトはすべてJDateに変換.
		if (o instanceof java.util.Date) {
			if(o != null) {
				long v = ((java.util.Date)o).getTime();
				LevelValues.head(buf, JDATE_OBJECT_CODE);
				LevelValues.byte8(buf, v);
			}
		} else if(o instanceof IdScriptableObject || o instanceof JDateInstanceObject) {
			final Object v = getJSDate(o);
			if(v == null) {
				return false;
			} else {
				LevelValues.head(buf, JDATE_OBJECT_CODE);
				LevelValues.byte8(buf, (Long)v);
			}
			return true;
		}
		return false;
	}

	@Override
	public Object decode(int[] pos, int objectCode, JniBuffer b, int length) throws Exception {
		switch(objectCode) {
		case JDATE_OBJECT_CODE:
			return JDateObject.newObject(LevelValues.byte8Long(b.address(), pos));
		}
		return null;
	}
}

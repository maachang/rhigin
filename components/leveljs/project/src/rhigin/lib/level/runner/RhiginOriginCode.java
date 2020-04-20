package rhigin.lib.level.runner;

import org.maachang.leveldb.JniBuffer;
import org.maachang.leveldb.LevelValues;
import org.maachang.leveldb.LevelValues.OriginCode;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginWrapUtil;
import rhigin.scripts.objects.JDateObject;

/**
 * Rhiginに対応したオリジナルなLevelValues変換処理.
 */
public class RhiginOriginCode extends OriginCode {
	/**
	 * エンコード時の変換処理.
	 * @param o 
	 * @return
	 * @exception
	 */
	@Override
	public Object inObject(Object o) throws Exception {
		// null or undefined の場合.
		if (o == null || o instanceof Undefined) {
			return null;
		// rhinoのjavaオブジェクトwrapper対応.
		//} else if (o instanceof Wrapper) {
		//	return ((Wrapper) o).unwrap();
		// rhiginのjavaオブジェクトwrapper対応.
		//} else if(o instanceof RhiginObjectWrapper) {
		//	return ((RhiginObjectWrapper) o).unwrap();
		//}
		//return o;
		}
		return RhiginWrapUtil.unwrap(o);
	}
	
	/**
	 * デコード時の変換処理.
	 * @param o 
	 * @return
	 * @exception
	 */
	@Override
	public Object outObject(Object o) throws Exception {
		return RhiginWrapUtil.wrapJavaObject(o);
	}
	
	/**
	 * java.util.Date or rhino NativeDate から UnixTimeを取得.
	 * @param o
	 * @return
	 */
	public static final Long getJSDate(Object o) {
		if(o instanceof java.util.Date) {
			return ((java.util.Date)o).getTime();
		}
		return RhiginWrapUtil.convertRhinoNativeDateByLong(o);
	}
	
	// JDate用のオブジェクトコード.
	private static final int JDATE_OBJECT_CODE = USE_OBJECT_CODE;

	/**
	 * 拡張エンコード処理.
	 * @param buf
	 * @param o
	 * @return
	 * @exception
	 */
	@Override
	public boolean encode(JniBuffer buf, Object o) throws Exception {
		if(o == null) {
			return false;
		}
		Long time = getJSDate(o);
		if(time == null) {
			return false;
		}
		LevelValues.head(buf, JDATE_OBJECT_CODE);
		LevelValues.byte8(buf, time);
		return true;
	}

	/**
	 * 拡張デコード処理.
	 * @param pos
	 * @param objectCode
	 * @param b
	 * @param length
	 * @return
	 * @exception
	 */
	@Override
	public Object decode(int[] pos, int objectCode, JniBuffer b, int length) throws Exception {
		switch(objectCode) {
		case JDATE_OBJECT_CODE:
			return JDateObject.newObject(LevelValues.byte8Long(b.address(), pos));
		}
		return null;
	}
}

package rhigin.scripts;

import java.io.OutputStream;
import java.util.Map;

import org.mozilla.javascript.Undefined;

import objectpack.SerializableCore;
import objectpack.SerializableCore.SerializableOriginCode;
import rhigin.scripts.objects.JDateObject;

/**
 * Rhiginに対応したオリジナルなSerializableCore変換処理.
 */
public class ObjectPackOriginCode extends SerializableOriginCode {
	@Override
	public Object inObject(Object o) throws Exception {
		// null or undefined の場合.
		if (o == null || o instanceof Undefined) {
			return null;
		// rhinoのjavaオブジェクトwrapper対応.
		//} else if (o instanceof Wrapper) {
		//	return ((Wrapper) o).unwrap();
		//} else if(o instanceof RhiginObjectWrapper) {
		//	return ((RhiginObjectWrapper) o).unwrap();
		//}
		//return o;
		}
		return RhiginWrapUtil.unwrap(o);
	}
	
	@Override
	public Object outObject(Object o) throws Exception {
		// rhigin用のオブジェクト変換.
		return RhiginWrapUtil.wrapJavaObject(o);
	}
	
	// JDate用のオブジェクトコード.
	private static final int JDATE_OBJECT_CODE = USE_OBJECT_CODE;

	@Override
	public boolean encode(Map<String, Integer> stringCode, OutputStream buf, Object o) throws Exception {
		Long time = null;
		if(o == null) {
			return false;
		} else if(o instanceof java.util.Date) {
			time = ((java.util.Date) o).getTime();
		} else {
			time = RhiginWrapUtil.convertRhinoNativeDateByLong(o);
		}
		if(time == null) {
			return false;
		}
		SerializableCore.head(buf, JDATE_OBJECT_CODE);
		SerializableCore.byte8(buf, time);
		return true;
	}

	@Override
	public Object decode(String[] stringMap, int objectCode, int[] pos, byte[] b, int length) throws Exception {
		switch(objectCode) {
		case JDATE_OBJECT_CODE:
			return JDateObject.newObject(SerializableCore.byte8Long(b, pos));
		}
		return null;
	}
}

package rhigin.scripts;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import rhigin.scripts.objects.JDateObject;
import rhigin.scripts.objects.JDateObject.JDateInstanceObject;
import rhigin.util.FixedArray;

@SuppressWarnings("rawtypes")
final class RhiginWrapFactory extends WrapFactory {
	private static final RhiginWrapFactory theInstance = new RhiginWrapFactory();

	private RhiginWrapFactory() {
		super.setJavaPrimitiveWrap(false);
	}

	static final WrapFactory getInstance() {
		return theInstance;
	}

	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
		if (javaObject instanceof FixedArray) {
			return new JavaScriptable.ReadArray(javaObject);
		} else if (javaObject instanceof java.util.Map) {
			return new JavaScriptable.GetMap((java.util.Map) javaObject);
		} else if (javaObject instanceof java.util.List) {
			return new JavaScriptable.GetList((java.util.List) javaObject);
		} else if(javaObject instanceof java.util.Date) {
			if(javaObject instanceof JDateInstanceObject) {
				return (JDateInstanceObject)javaObject;
			}
			return JDateObject.newObject((java.util.Date)javaObject);
		} else if (javaObject instanceof ClassLoader) {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
//		if (javaObject instanceof ClassLoader) {
//			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
//		}
		final SecurityManager sm = System.getSecurityManager();
		final ClassShutter classShutter = RhiginClassShutter.getInstance();
		String name = null;
		if (javaObject instanceof Class) {
			name = ((Class) javaObject).getName();
		} else if (javaObject instanceof Member) {
			Member member = (Member) javaObject;
			if (sm != null && !Modifier.isPublic(member.getModifiers())) {
				return null;
			}
			name = member.getDeclaringClass().getName();
		}
		if (name != null) {
			if (!classShutter.visibleToScripts(name)) {
				return null;
			} else {
				return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
			}
		}
		Class dynamicType = javaObject.getClass();
		name = dynamicType.getName();
		if (!classShutter.visibleToScripts(name)) {
			Class type = null;
			if (staticType != null && staticType.isInterface()) {
				type = staticType;
			} else {
				while (dynamicType != null) {
					dynamicType = dynamicType.getSuperclass();
					name = dynamicType.getName();
					if (classShutter.visibleToScripts(name)) {
						type = dynamicType;
						break;
					}
				}
				assert type != null : "even java.lang.Object is not accessible?";
			}
			return new RhiginJavaObject(scope, javaObject, type);
		} else {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	}

	// RhiginJavaObject.
	protected static class RhiginJavaObject extends NativeJavaObject {
		private static final long serialVersionUID = 7074055700134775639L;

		RhiginJavaObject(Scriptable scope, Object obj, Class type) {
			super(scope, null, type);
			this.javaObject = obj;
		}
	}
}

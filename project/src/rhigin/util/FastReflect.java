package rhigin.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 高速リフレクション.
 * 
 * リフレクションオブジェクトをキャッシュして高速化を図っています.
 */
@SuppressWarnings("rawtypes")
public class FastReflect {

	// 例外.
	public static final class FastReflectException extends RuntimeException {
		private static final long serialVersionUID = 1754899864781890665L;

		public FastReflectException() {
			super();
		}

		public FastReflectException(String message) {
			super(message);
		}

		public FastReflectException(Throwable e) {
			super(e instanceof InvocationTargetException ? ((InvocationTargetException)e).getCause() : e);
		}

		public FastReflectException(String message, Throwable e) {
			super(message,
				e instanceof InvocationTargetException ? ((InvocationTargetException)e).getCause() : e);
		}
	}

	// プリミティブ.
	private static final class Primitive {
		protected static final Class STRING = String.class;
		protected static final Class OBJECT = Object.class;
		protected static final Object[] NO_PARAM = new Object[0];
		protected static final Class[] NO_PARAM_CLASS = new Class[0];
		protected static final Set<Class> PRIMITIVE;
		protected static final Map<Class, Class> CONV_PRIMITIVE;
		protected static final Set<Class> NUMBER_PRIMITIVE;
		static {
			Set<Class> m = new HashSet<Class>();
			m.add(java.lang.Boolean.class);
			m.add(java.lang.Byte.class);
			m.add(java.lang.Character.class);
			m.add(java.lang.Short.class);
			m.add(java.lang.Integer.class);
			m.add(java.lang.Long.class);
			m.add(java.lang.Float.class);
			m.add(java.lang.Double.class);
			m.add(Boolean.TYPE);
			m.add(Byte.TYPE);
			m.add(Character.TYPE);
			m.add(Short.TYPE);
			m.add(Integer.TYPE);
			m.add(Long.TYPE);
			m.add(Float.TYPE);
			m.add(Double.TYPE);
			Map<Class, Class> x = new HashMap<Class, Class>();
			x.put(Boolean.TYPE, java.lang.Boolean.class);
			x.put(Byte.TYPE, java.lang.Byte.class);
			x.put(Character.TYPE, java.lang.Character.class);
			x.put(Short.TYPE, java.lang.Short.class);
			x.put(Integer.TYPE, java.lang.Integer.class);
			x.put(Long.TYPE, java.lang.Long.class);
			x.put(Float.TYPE, java.lang.Float.class);
			x.put(Double.TYPE, java.lang.Double.class);
			x.put(java.lang.Boolean.class, java.lang.Boolean.class);
			x.put(java.lang.Byte.class, java.lang.Byte.class);
			x.put(java.lang.Character.class, java.lang.Character.class);
			x.put(java.lang.Short.class, java.lang.Short.class);
			x.put(java.lang.Integer.class, java.lang.Integer.class);
			x.put(java.lang.Long.class, java.lang.Long.class);
			x.put(java.lang.Float.class, java.lang.Float.class);
			x.put(java.lang.Double.class, java.lang.Double.class);
			Set<Class> y = new HashSet<Class>();
			y.add(Byte.TYPE);
			y.add(Character.TYPE);
			y.add(Short.TYPE);
			y.add(Integer.TYPE);
			y.add(Long.TYPE);
			y.add(Float.TYPE);
			y.add(Double.TYPE);
			y.add(Byte.class);
			y.add(Character.class);
			y.add(Short.class);
			y.add(Integer.class);
			y.add(Long.class);
			y.add(Float.class);
			y.add(Double.class);
			PRIMITIVE = m;
			CONV_PRIMITIVE = x;
			NUMBER_PRIMITIVE = y;
		}
	}

	// ユーティリティ.
	private static final class Util {
		private static final Map<Class, Class> CONV_PRIMITIVE = Primitive.CONV_PRIMITIVE;
		private static final Set<Class> PRIMITIVE = Primitive.PRIMITIVE;
		private static final Object OBJECT = Primitive.OBJECT;
		private static final Object STRING = Primitive.STRING;
		private static final Set<Class> NUMBER_PRIMITIVE = Primitive.NUMBER_PRIMITIVE;

		/** 変換テーブル. **/
		private static final Map<Class, Integer> CONV_TABLE;

		static {
			Map<Class, Integer> m = new HashMap<Class, Integer>();
			m.put(Byte.class, 0);
			m.put(Character.class, 1);
			m.put(Short.class, 2);
			m.put(Integer.class, 3);
			m.put(Long.class, 4);
			m.put(Float.class, 5);
			m.put(Double.class, 6);
			CONV_TABLE = m;
		}

		/**
		 * パラメータに対する型を取得.
		 * 
		 * @param args パラメータを設定します.
		 * @return Class[] パラメータ型が返されます.
		 */
		public static final Class[] getParamsType(Object... args) {
			int argsLen;
			if (args == null || (argsLen = args.length) <= 0) {
				return Primitive.NO_PARAM_CLASS;
			}
			Class[] c = new Class[argsLen];
			Object o;
			for (int i = 0; i < argsLen; i++) {
				if ((o = args[i]) != null) {
					c[i] = o.getClass();
				}
			}
			return c;
		}

		/**
		 * パラメータに対する型を取得.
		 * 
		 * @param args パラメータを設定します.
		 * @return Class[] パラメータ型が返されます.
		 */
		public static final Class[] getParamsTypeByClass(Class[] args) {
			int argsLen;
			if (args == null || (argsLen = args.length) <= 0) {
				return Primitive.NO_PARAM_CLASS;
			}
			Class pc;
			for (int i = 0; i < argsLen; i++) {
				if (args[i] != null) {
					pc = CONV_PRIMITIVE.get(args[i]);
					if (pc != null) {
						args[i] = pc;
					}
				}
			}
			return args;
		}

		/**
		 * パラメータに対して、一致点数を取得.
		 * 
		 * @param pms  チェック元のプリミティブ条件を設定します.
		 * @param src  チェック元を設定します.
		 * @param dest チェック先を設定します.
		 * @param args 実行対象のパラメータを設定します.
		 * @param cl   対象のクラスローダーを設定します.
		 * @return int 一致点数が返されます.
		 * @exception ClassNotFoundException 該当クラスが存在しない場合に発生します.
		 */
		public static final int parmasScore(boolean[] pms, Class[] src, Class[] dest, Object[] args, ClassLoader cl) {
			// ●１つの引数が一致した場合は、100点が加算される.
			// ●１つの引数が継承で一致した場合は、継承１つの条件に対して、
			// 100点から１点減算させた値で得点加算する.
			// ●１つのsrc引数がObject型の場合は、60点が加算される.
			// ●１つの引数が数値のプリミティブ型同士の場合は、50点が加算される.
			// ●１つの引数の関係が、プリミティブ - 文字列関係の場合は、40点が加算される.
			// ●ただし、引数のどれか１つが一致しなかった場合は、-1を返す.
			int ret = 0;
			int lenJ;
			Class s, d;
			Object a;
			String ss;
			boolean one = true;
			int len = src.length;
			for (int i = 0; i < len; i++) {
				s = src[i];
				d = dest[i];
				a = args[i];
				// チェック先がNULLの場合は、相互チェックしない.
				if (d != null) {
					// チェック元と、チェック先が一致している.
					if (s == d) {
						ret += 100;
					}
					// チェック元がObjectの場合は、相互チェックしない.
					else if (s == OBJECT) {
						ret += 60;
					}
					// ・チェック元・先が、数値プリミティブ値の場合.
					else if (NUMBER_PRIMITIVE.contains(s) && NUMBER_PRIMITIVE.contains(d)) {
						ret += 50;
					}
					// ・チェック元が文字列で、チェック先が、文字列か、プリミティブ型の場合.
					else if (s == STRING && (PRIMITIVE.contains(d))) {
						ret += 40;
					}
					// ・チェック元がプリミティブか、文字列で、チェック先が、文字列の場合.
					else if (d == STRING && (PRIMITIVE.contains(s))) {
						// 呼び出し対象が文字列の場合、呼び出し元がBooleanならば、true/falseチェック
						// それ以外の場合は、数値かチェックして、それが一致する場合は、得点プラス.
						ss = (String) a;
						if ((s == Boolean.class && ("true".equals(ss) || "false".equals(ss))) || isNumber(ss)) {
							ret += 40;
						}
						// 文字列がプリミティブ型でない場合は、対象外.
						else {
							return -1;
						}
					} else {
						// チェック元に対して、チェック先の継承クラス／インターフェイスを
						// 掘り下げてチェックする.
						ClassElement em = FACTORY.getClass(d.getName());
						one = false;
						Class o;
						String[] ifce;
						int wScore = 100;
						// チェック元がインターフェイス属性の場合は、インターフェイス内容と、
						// スーパークラスのチェックを行う.
						if (s.isInterface()) {
							int befWScore;
							String sname = s.getName();
							while (true) {
								wScore--;
								befWScore = wScore;
								ifce = em.getInterfaseNames();
								if (ifce != null && (lenJ = ifce.length) > 0) {
									// インターフェース名群と、チェック元のクラス名が一致.
									if (Arrays.binarySearch(ifce, sname) != -1) {
										one = true;
										ret += wScore;
									}
									// 継承インターフェイスが１つの場合.
									else if (lenJ == 1) {
										if ((wScore = toInterface(wScore, sname, ifce[0], cl)) != -1) {
											one = true;
											ret += wScore;
										}
									}
									// 継承インターフェイスが複数の場合.
									else {
										for (int j = 0; j < lenJ; j++) {
											if ((wScore = toInterface(wScore, sname, ifce[i], cl)) != -1) {
												one = true;
												ret += wScore;
												break;
											}
										}
									}
									if (one) {
										break;
									}
								}
								wScore = befWScore;
								// スーパークラスを取得.
								em = FACTORY.getClass(em.getSuperClassName());
								// スーパークラスがオブジェクトの場合.
								if ((o = em.getClassObject()) == OBJECT) {
									return -1;
								}
								// スーパークラスと、チェック元が一致する場合.
								else if (o == s) {
									one = true;
									ret += wScore;
									break;
								}
							}
						}
						// チェック元がオブジェクトの場合は、スーパークラスのみチェック.
						else {
							while (true) {
								wScore--;
								// スーパークラスを取得.
								em = FACTORY.getClass(em.getSuperClassName());
								// スーパークラスがオブジェクトの場合.
								if ((o = em.getClassObject()) == OBJECT) {
									return -1;
								}
								// スーパークラスと、チェック元が一致する場合.
								else if (o == s) {
									one = true;
									ret += wScore;
									break;
								}
							}
						}
						// 一致条件が存在しない.
						if (!one) {
							return -1;
						}
					}
				}
				// nullに対してチェック元がプリミティブ型の場合.
				else if (pms[i]) {
					return -1;
				}
			}
			return ret;
		}

		/** interface比較. **/
		private static final int toInterface(int wScore, String sname, String name, ClassLoader cl) {
			ClassElement em = FACTORY.getClass(cl, name);
			while (em != null) {
				wScore--;
				String superClassName = em.getSuperClassName();
				if (superClassName == sname) {
					return wScore;
				}
				em = FACTORY.getClass(cl, superClassName);
			}
			return -1;
		}

		/**
		 * パラメータ変換.
		 * 
		 * @param args  変換対象のパラメータを設定します.
		 * @param types 変換対象のパラメータタイプを設定します.
		 * @return Object[] 変換されたオブジェクトが返されます.
		 */
		public static final Object[] convertParams(Object[] args, Class[] types) {
			int len = args.length;
			Class s, d;
			Object[] ret;
			if (len > 0) {
				ret = new Object[len];
				System.arraycopy(args, 0, ret, 0, len);
			} else {
				return Primitive.NO_PARAM;
			}
			for (int i = 0; i < len; i++) {
				if (ret[i] != null) {
					s = types[i];
					d = ret[i].getClass();
					if (s != d) {
						if (NUMBER_PRIMITIVE.contains(s) && NUMBER_PRIMITIVE.contains(d)) {
							ret[i] = convertNumberPrimitive(ret[i], CONV_TABLE.get(s), CONV_TABLE.get(d));
						} else if (s == STRING && (PRIMITIVE.contains(d))) {
							ret[i] = ret[i].toString();
						} else if (d == STRING && (PRIMITIVE.contains(s))) {
							if (s == Boolean.class) {
								String str = (String) args[i];
								if (str.equals("true")) {
									ret[i] = Boolean.TRUE;
								} else if (str.equals("false")) {
									ret[i] = Boolean.FALSE;
								} else {
									throw new FastReflectException("The cast of the " + (i + 1) + " argument failed");
								}
							} else {
								Integer o = CONV_TABLE.get(s);
								if (o != null) {
									if (isNumber((String) ret[i])) {
										ret[i] = convertNumber(o, (String) ret[i]);
									} else {
										return null;
									}
								}
							}
						}
					}
				}
			}
			return ret;
		}

		/** 指定タイプに対して文字列から、数値変換. **/
		private static final Object convertNumber(int type, String s) {
			s = s.trim().toLowerCase();
			if (s.endsWith("f") || s.endsWith("l")) {
				s = s.substring(0, s.length() - 1);
			}
			if (type == 5 || type == 6) {
				return convertType(type, s);
			}
			int p = s.indexOf(".");
			if (p == -1) {
				if (s.startsWith("0x")) {
					s = s.substring(2);
					int len = s.length();
					if (len > 8) {
						if (len > 16) {
							throw new FastReflectException("Numerical conversion failed.");
						}
						long ret = 0L;
						for (int i = 0; i < len; i++) {
							char c = s.charAt(i);
							if (c >= '1' && c <= '9') {
								ret |= ((int) (c - '0') << i);
							} else if (c >= 'a' && c <= 'f') {
								ret |= ((int) ((c - 'a') + 10) << i);
							}
						}
						switch (type) {
						case 0:
							return (byte) (ret & 0x00000000000000ffL);
						case 1:
							return (char) (ret & 0x000000000000ffffL);
						case 2:
							return (short) (ret & 0x000000000000ffffL);
						case 3:
							return (int) (ret & 0x00000000ffffffffL);
						case 4:
							return ret;
						}
						return null;
					} else {
						int ret = 0;
						for (int i = 0; i < len; i++) {
							char c = s.charAt(i);
							if (c >= '1' && c <= '9') {
								ret |= ((int) (c - '0') << i);
							} else if (c >= 'a' && c <= 'f') {
								ret |= ((int) ((c - 'a') + 10) << i);
							}
						}
						switch (type) {
						case 0:
							return (byte) (ret & 0x000000ff);
						case 1:
							return (char) (ret & 0x0000ffff);
						case 2:
							return (short) (ret & 0x0000ffff);
						case 3:
							return ret;
						case 4:
							return (long) ret;
						}
						return null;
					}
				}
				return convertType(type, s);
			}
			return convertType(type, s.substring(0, p));
		}

		/** 文字列に対して、プリミティブタイプ変換. **/
		private static final Object convertType(int type, String s) {
			switch (type) {
			case 0:
				return Byte.parseByte(s);
			case 1:
				return (s.length() == 1) ? s.charAt(0) : (char) (Integer.parseInt(s) & 0x0000ffff);
			case 2:
				return Short.parseShort(s);
			case 3:
				return Integer.parseInt(s);
			case 4:
				return Long.parseLong(s);
			case 5:
				return Float.parseFloat(s);
			case 6:
				return Double.parseDouble(s);
			}
			return s;
		}

		/** 数値系プリミティブから、数値系プリミティブに対して、キャスト. **/
		private static final Object convertNumberPrimitive(Object o, int srcType, int destType) {
			switch (destType) {
			case 0: {
				byte x = (Byte) o;
				switch (srcType) {
				case 0:
					return x;
				case 1:
					return (char) x;
				case 2:
					return (short) x;
				case 3:
					return (int) x;
				case 4:
					return (long) x;
				case 5:
					return (float) x;
				case 6:
					return (double) x;
				}
			}
			case 1: {
				char x = (Character) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return x;
				case 2:
					return (short) x;
				case 3:
					return (int) x;
				case 4:
					return (long) x;
				case 5:
					return (float) x;
				case 6:
					return (double) x;
				}
			}
			case 2: {
				short x = (Short) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return (char) x;
				case 2:
					return x;
				case 3:
					return (int) x;
				case 4:
					return (long) x;
				case 5:
					return (float) x;
				case 6:
					return (double) x;
				}
			}
			case 3: {
				int x = (Integer) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return (char) x;
				case 2:
					return (short) x;
				case 3:
					return x;
				case 4:
					return (long) x;
				case 5:
					return (float) x;
				case 6:
					return (double) x;
				}
			}
			case 4: {
				long x = (Long) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return (char) x;
				case 2:
					return (short) x;
				case 3:
					return (int) x;
				case 4:
					return x;
				case 5:
					return (float) x;
				case 6:
					return (double) x;
				}
			}
			case 5: {
				float x = (Float) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return (char) x;
				case 2:
					return (short) x;
				case 3:
					return (int) x;
				case 4:
					return (long) x;
				case 5:
					return x;
				case 6:
					return (double) x;
				}
			}
			case 6: {
				double x = (Double) o;
				switch (srcType) {
				case 0:
					return (byte) x;
				case 1:
					return (char) x;
				case 2:
					return (short) x;
				case 3:
					return (int) x;
				case 4:
					return (long) x;
				case 5:
					return (float) x;
				case 6:
					return x;
				}
			}
			}
			return o;
		}

		/** 対象文字列内が数値かチェック. **/
		private static final boolean isNumber(String num) {
			if (num == null || num.length() <= 0) {
				return false;
			}
			int start = 0;
			if (num.startsWith("-")) {
				start = 1;
			}
			boolean dt = false;
			int len = num.length();
			if (start < len) {
				for (int i = start; i < len; i++) {
					char c = num.charAt(i);
					if (c == '.') {
						if (dt) {
							return false;
						}
						dt = true;
					} else if ((c >= '0' && c <= '9') == false) {
						return false;
					}
				}
			} else {
				return false;
			}
			return true;
		}
	}

	// クラス要素.
	private static final class ClassElement {
		private Class clazz = null;
		private String superClazz = null;
		private String[] interfaze = null;

		private final AtomicReference<FieldObject> fileds = new AtomicReference<FieldObject>();
		private final AtomicReference<ConstructorObject> constructors = new AtomicReference<ConstructorObject>();
		private final AtomicReference<MethodObject> methods = new AtomicReference<MethodObject>();

		protected ClassElement(Class clazz) {
			try {
				this.clazz = clazz;
				this.superClazz = getSuperClazz(clazz);
				this.interfaze = getInterface(clazz);
			} catch (Exception e) {
				throw new FastReflectException(e);
			}
		}

		private static final String getSuperClazz(Class clazz) {
			Class c = clazz.getSuperclass();
			if (c == null) {
				return null;
			}
			return c.getName();
		}

		private static final String[] getInterface(Class clazz) {
			Class[] cz = clazz.getInterfaces();
			if (cz == null) {
				return null;
			}
			int len = cz.length;
			if (len > 0) {
				String[] ret = new String[len];
				for (int i = 0; i < len; i++) {
					ret[i] = cz[i].getName();
				}
				Arrays.sort(ret);
				return ret;
			}
			return null;
		}

		public Class getClassObject() {
			return clazz;
		}

		public String getSuperClassName() {
			return superClazz;
		}

		public String[] getInterfaseNames() {
			return interfaze;
		}

		/**
		 * 指定クラスフィールド名に対するフィールド要素を取得.
		 * 
		 * @param staticFlag [true]の場合、staticアクセス用として取得します.
		 * @param name       指定クラスフィールド名を設定します.
		 * @return FastFieldElement フィールド要素が返されます.
		 */
		public final Field getField(boolean staticFlag, String name) {
			FieldObject f = fileds.get();
			if (f == null) {
				f = new FieldObject(clazz);
				while (!fileds.compareAndSet(fileds.get(), f))
					;
			}
			return f.get(staticFlag, name);
		}

		/**
		 * 対象のコンストラクタを取得します.
		 * 
		 * @param types 対象のパラメータタイプを設定します.
		 * @param args  引数パラメータ型群を設定します.
		 * @param cl    対象のクラスローダを設定します.
		 * @return Constructor 対象のコンストラクタが返されます.
		 */
		public final Object newInstance(Class[] types, Object[] args, ClassLoader cl) {
			ConstructorObject c = constructors.get();
			if (c == null) {
				c = new ConstructorObject(clazz);
				while (!constructors.compareAndSet(constructors.get(), c))
					;
			}
			return c.newInstance(cl, types, args);
		}

		/**
		 * 指定クラスメソッド名に対するメソッド要素を取得.
		 * 
		 * @param result 実行結果のオブジェクトを受け取るオブジェクト配列を設定します.
		 * @param target 対象のターゲットオブジェクトを設定します.
		 * @param name   対象のフィールド名を設定します.
		 * @param cl     対象のクラスローダを設定します.
		 * @param types  対象のパラメータタイプを設定します.
		 * @param args   引数パラメータ型群を設定します.
		 * @return boolean [true]の場合、実行されました.
		 */
		public final boolean invoke(Object[] result, Object target, String name, ClassLoader cl, Class[] types, Object[] args) {
			MethodObject m = methods.get();
			if (m == null) {
				m = new MethodObject(clazz);
				while (!methods.compareAndSet(methods.get(), m))
					;
			}
			return m.invoke(result, target, name, cl, types, args);
		}

	}

	// クラス要素ファクトリ.
	private static final class ClassElementFactory {
		protected final Map<String, ClassElement> cacheClass = new ConcurrentHashMap<String, ClassElement>();

		public final ClassElement getClass(String name) {
			if (name == null || name.length() <= 0) {
				throw new FastReflectException("Class name not specified.");
			}
			ClassElement ret = cacheClass.get(name);
			if (ret == null) {
				try {
					Class c;
					if ((c = Class.forName(name)) != null) {
						ret = new ClassElement(c);
						cacheClass.put(name, ret);
					}
				} catch (Exception e) {
					throw new FastReflectException(e);
				}
			}
			return ret;
		}

		public final ClassElement getClass(ClassLoader cl, String name) {
			if (name == null || name.length() <= 0) {
				throw new FastReflectException("Class name not specified.");
			}
			ClassElement ret = cacheClass.get(name);
			if (ret == null) {
				try {
					Class c;
					if (cl == null) {
						c = Class.forName(name);
					} else {
						c = cl.loadClass(name);
					}
					if (c != null) {
						ret = new ClassElement(c);
						cacheClass.put(name, ret);
					}
				} catch (Exception e) {
					throw new FastReflectException(e);
				}
			}
			return ret;
		}
	}

	// オブジェクトのフィールド群.
	private static final class FieldObject {
		private final Map<String, Field> map = new HashMap<String, Field>();

		public FieldObject(Class clazz) {
			Field[] fs = clazz.getDeclaredFields();
			if (fs != null) {
				int len = fs.length;
				for (int i = 0; i < len; i++) {
					Field f = fs[i];
					if (Modifier.isPublic(f.getModifiers())) {
						f.setAccessible(true);
						map.put(f.getName(), f);
					}
				}
			}
		}

		public Field get(boolean staticFlag, String name) {
			Field f = map.get(name);
			if (f != null && Modifier.isStatic(f.getModifiers()) == staticFlag) {
				return f;
			}
			return null;
		}
	}

	// コンストラクタ要素.
	private static final class ConstractorElement {
		protected Constructor constructor;
		protected Class[] params;
		protected boolean[] primitives;
		protected int paramsLength;

		/** コンストラクタ. **/
		public ConstractorElement(Constructor constructor) {
			boolean[] pms = null;
			Class[] args = constructor.getParameterTypes();
			int len = (args == null) ? 0 : args.length;
			if (len == 0) {
				args = Primitive.NO_PARAM_CLASS;
			} else {
				Class pc;
				pms = new boolean[len];
				for (int i = 0; i < len; i++) {
					pms[i] = args[i].isPrimitive();
					pc = Primitive.CONV_PRIMITIVE.get(args[i]);
					if (pc != null) {
						args[i] = pc;
					}
				}
			}
			constructor.setAccessible(true);
			this.constructor = constructor;
			this.params = args;
			this.primitives = pms;
			this.paramsLength = len;
		}
	}

	// オブジェクトのコンストラクタ群.
	private static final class ConstructorObject {
		private ConstractorElement[] list;

		public ConstructorObject(Class clazz) {
			Constructor[] cs = clazz.getDeclaredConstructors();
			if (cs != null) {
				Constructor c;
				int len = cs.length;
				int cnt = 0;
				for (int i = 0; i < len; i++) {
					c = cs[i];
					if (Modifier.isPublic(c.getModifiers())) {
						cnt++;
					}
				}
				ConstractorElement[] lst = new ConstractorElement[cnt];
				cnt = 0;
				for (int i = 0; i < len; i++) {
					c = cs[i];
					if (Modifier.isPublic(c.getModifiers())) {
						lst[cnt++] = new ConstractorElement(c);
					}
				}
				list = lst;
			}
		}

		public Object newInstance(ClassLoader cl, Class[] types, Object[] args) {
			ConstractorElement emt;
			int len = list.length;
			int argsLen = (args == null) ? 0 : args.length;
			try {
				if (argsLen == 0) {
					for (int i = 0; i < len; i++) {
						if ((emt = list[i]).paramsLength == 0) {
							return emt.constructor.newInstance(Primitive.NO_PARAM);
						}
					}
				} else {
					int pf = 100 * argsLen;
					int score = -1;
					ConstractorElement targetEmt = null;
					for (int i = 0; i < len; i++) {
						if ((emt = list[i]).paramsLength == argsLen) {
							int sc = Util.parmasScore(emt.primitives, emt.params, types, args, cl);
							if (sc != -1 && score < sc) {
								if (sc == pf) {
									return emt.constructor.newInstance(args);
								}
								score = sc;
								targetEmt = emt;
							}
						}
					}
					if (targetEmt != null) {
						if ((args = Util.convertParams(args, targetEmt.params)) == null) {
							throw new FastReflectException("Parameter analysis failed.");
						}
						return targetEmt.constructor.newInstance(args);
					}
				}
				return null;
			} catch (InvocationTargetException it) {
				throw new FastReflectException(it.getCause());
			} catch (Exception e) {
				throw new FastReflectException(e);
			}
		}
	}

	// メソッド要素.
	private static final class MethodElement {
		protected MethodElement next;
		protected Method method;
		protected Class[] params;
		protected boolean[] primitives;
		protected int paramsLength;
		protected boolean isStatic;

		protected MethodElement(Method method) {
			boolean[] pms = null;
			Class[] args = method.getParameterTypes();
			int len = (args == null) ? 0 : args.length;
			if (len == 0) {
				args = Primitive.NO_PARAM_CLASS;
			} else {
				Class pc;
				pms = new boolean[len];
				for (int i = 0; i < len; i++) {
					pms[i] = args[i].isPrimitive();
					pc = Primitive.CONV_PRIMITIVE.get(args[i]);
					if (pc != null) {
						args[i] = pc;
					}
				}
			}
			method.setAccessible(true);
			this.method = method;
			this.params = args;
			this.primitives = pms;
			this.paramsLength = len;
			this.isStatic = Modifier.isStatic(method.getModifiers());
		}
	}

	// オブジェクトのメソッド群.
	private static final class MethodObject {
		/** フィールド格納Map. **/
		private final Map<String, MethodElement> map = new HashMap<String, MethodElement>();

		/**
		 * コンストラクタ.
		 * 
		 * @param clazz 対象のクラスオブジェクトを設定します.
		 * @exception Exception 例外.
		 */
		public MethodObject(Class clazz) {
			Method[] ms = clazz.getDeclaredMethods();
			if (ms != null) {
				int len = ms.length;
				for (int i = 0; i < len; i++) {
					// publicのみキャッシュ.
					Method m = ms[i];
					if (Modifier.isPublic(m.getModifiers())) {
						String name = m.getName();
						if (map.containsKey(name)) {
							MethodElement f = map.get(name);
							if (f.next == null) {
								f.next = new MethodElement(m);
							} else {
								MethodElement n = f.next;
								f.next = new MethodElement(m);
								f.next.next = n;
							}
						} else {
							map.put(name, new MethodElement(m));
						}
					}
				}
			}
		}

		/**
		 * 指定名のメソッドを実行.
		 * 
		 * @param result 処理結果を受け取るオブジェクト配列を設定します.
		 * @param target 対象のターゲットオブジェクトを設定します.
		 * @param name   対象のフィールド名を設定します.
		 * @param cl     対象のクラスローダを設定します.
		 * @param types  対象のパラメータタイプを設定します.
		 * @param args   引数パラメータ型群を設定します.
		 * @return boolean [true]の場合、実行されました.
		 */
		public boolean invoke(Object[] result, Object target, String name, ClassLoader cl, Class[] types,
				Object[] args) {
			boolean staticFlag = (target == null);
			int argsLen = (args == null) ? 0 : args.length;
			try {
				MethodElement emt = map.get(name);
				if (argsLen == 0) {
					while (emt != null) {
						if (staticFlag == emt.isStatic && argsLen == emt.paramsLength) {
							Object res = emt.method.invoke(target, Primitive.NO_PARAM);
							if (result != null) {
								result[0] = res;
							}
							return true;
						}
						emt = emt.next;
					}
				} else {
					int pf = 100 * argsLen;
					int score = -1;
					MethodElement targetEmt = null;
					while (emt != null) {
						if (staticFlag == emt.isStatic && argsLen == emt.paramsLength) {
							int sc = Util.parmasScore(emt.primitives, emt.params, types, args, cl);
							if (sc != -1 && score < sc) {
								if (sc == pf) {
									Object res = emt.method.invoke(target, args);
									if (result != null) {
										result[0] = res;
									}
									return true;
								}
								score = sc;
								targetEmt = emt;
							}
						}
						emt = emt.next;
					}
					if (targetEmt != null) {
						// 変換失敗の場合は実行失敗にする.
						args = Util.convertParams(args, targetEmt.params);
						if (args == null) {
							return false;
						}
						Object res = targetEmt.method.invoke(target, args);
						if (result != null) {
							result[0] = res;
						}
						return true;
					}
				}
				return false;
			} catch (InvocationTargetException it) {
				throw new FastReflectException(it.getCause());
			} catch (Exception e) {
				throw new FastReflectException(e);
			}
		}
	}
	
	// クラスファクトリ.
	private static final ClassElementFactory FACTORY = new ClassElementFactory();

	// オブジェクト名.
	private static final String OBJECT_NAME = Object.class.getName();

	/**
	 * キャッシュクリア.
	 */
	public static final void clearCacheAll() {
		FACTORY.cacheClass.clear();
	}

	/**
	 * 指定クラスキャッシュクリア.
	 * 
	 * @param name 対象のクラス名を設定します.
	 */
	public static final void clearCache(String name) {
		FACTORY.cacheClass.remove(name);
	}

	/**
	 * クラス情報を取得.
	 * 
	 * @param name 対象のクラス名を設定します.
	 * @return Class 対象のクラス情報が返されます.
	 */
	public static final Class getClass(String name) {
		return getClass(null, name);
	}

	/**
	 * クラス情報を取得.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param name   対象のクラス名を設定します.
	 * @return Class 対象のクラス情報が返されます.
	 */
	public static final Class getClass(ClassLoader loader, String name) {
		if (name == null) {
			throw new FastReflectException("Class name not specified.");
		}
		ClassElement em = FACTORY.getClass(loader, name);
		if (em == null) {
			throw new FastReflectException("Specified class '" + name + "' does not exist.");
		}
		return em.getClassObject();
	}

	/**
	 * コンストラクタ実行.
	 * 
	 * @param name 対象のクラス名を設定します.
	 * @return Object 生成されたオブジェクトが返されます.
	 */
	public static final Object newInstance(String name) {
		return newInstance(null, name, (Object[]) null);
	}

	/**
	 * コンストラクタ実行.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param name   対象のクラス名を設定します.
	 * @return Object 生成されたオブジェクトが返されます.
	 */
	public static final Object newInstance(ClassLoader loader, String name) {
		return newInstance(loader, name, (Object[]) null);
	}

	/**
	 * コンストラクタ実行.
	 * 
	 * @param name 対象のクラス名を設定します.
	 * @param args 対象のコンストラクタ引数を設定します.
	 * @return Object 生成されたオブジェクトが返されます.
	 */
	public static final Object newInstance(String name, Object... args) {
		return newInstance(null, name, args);
	}

	/**
	 * コンストラクタ実行.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param args      対象のコンストラクタ引数を設定します.
	 * @return Object 生成されたオブジェクトが返されます.
	 */
	public static final Object newInstance(ClassLoader loader, String clazzName, Object... args) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		if (clazzName == OBJECT_NAME) {
			return new Object();
		}
		Object ret;
		Class[] types = Util.getParamsType(args);
		String superName;
		while (true) {
			if ((ret = em.newInstance(types, args, loader)) != null) {
				return ret;
			}
			superName = em.getSuperClassName();
			em = null;
			if (superName != OBJECT_NAME && superName != null) {
				em = FACTORY.getClass(loader, superName);
			}
			if (em == null) {
				throw new FastReflectException("Constructor of target argument does not exist for specified class '" + clazzName + "'.");
			}
		}
	}

	/**
	 * コンストラクタ実行.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param args      対象のコンストラクタ引数を設定します.
	 * @param types     対象のコンストラクタ引数タイプを設定します.
	 * @return Object 生成されたオブジェクトが返されます.
	 */
	public static final Object newInstanceTo(ClassLoader loader, String clazzName, Object[] args, Class[] types) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		if (clazzName == OBJECT_NAME) {
			return new Object();
		}
		Object ret;
		String superName;
		if (args != null && args.length > 0 && types == null) {
			types = Util.getParamsType(args);
		} else if (types != null) {
			types = Util.getParamsTypeByClass(types);
		}
		while (true) {
			if ((ret = em.newInstance(types, args, loader)) != null) {
				return ret;
			}
			superName = em.getSuperClassName();
			em = null;
			if (superName != OBJECT_NAME && superName != null) {
				em = FACTORY.getClass(loader, superName);
			}
			if (em == null) {
				throw new FastReflectException("Constructor of target argument does not exist for specified class '" + clazzName + "'.");
			}
		}
	}

	/**
	 * フィールド設定.
	 * 
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のフィールド名を設定します.
	 * @param value     対象のパラメータ要素を設定します.
	 */
	public static final void setField(String clazzName, Object target, String name, Object value) {
		setField(null, clazzName, target, name, value);
	}

	/**
	 * フィールド設定.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.<BR>
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のフィールド名を設定します.
	 * @param value     対象のパラメータ要素を設定します.
	 */
	public static final void setField(ClassLoader loader, String clazzName, Object target, String name, Object value) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		String superName;
		while (true) {
			Field f = em.getField((target == null), name);
			if (f == null) {
				superName = em.getSuperClassName();
				em = null;
				if (superName != null) {
					em = FACTORY.getClass(loader, superName);
				}
				if (em == null) {
					throw new FastReflectException("Specified field '" + name + "' does not exist in class '" + clazzName + "'.");
				}
				continue;
			}
			try {
				f.set(target, value);
			} catch(Exception e) {
				throw new FastReflectException(e);
			}
			return;
		}
	}

	/**
	 * フィールド設定.
	 * 
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のフィールド名を設定します.
	 * @param value  対象のパラメータ要素を設定します.
	 */
	public static final void setField(Class clazz, Object target, String name, Object value) {
		setField(null, clazz, target, name, value);
	}

	/**
	 * フィールド設定.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のフィールド名を設定します.
	 * @param value  対象のパラメータ要素を設定します.
	 */
	public static final void setField(ClassLoader loader, Class clazz, Object target, String name, Object value) {
		String clazzName = clazz.getName();
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		String superName;
		while (true) {
			Field f = em.getField((target == null), name);
			if (f == null) {
				superName = em.getSuperClassName();
				em = null;
				if (superName != null) {
					em = FACTORY.getClass(loader, superName);
				}
				if (em == null) {
					throw new FastReflectException("Specified field '" + name + "' does not exist in class '" + clazzName + "'.");
				}
				continue;
			}
			try {
				f.set(target, value);
			} catch(Exception e) {
				throw new FastReflectException(e);
			}
			return;
		}
	}

	/**
	 * フィールド取得.
	 * 
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のフィールド名を設定します.
	 * @return Object フィールドオブジェクト内容が返されます.
	 */
	public static final Object getField(String clazzName, Object target, String name) {
		return getField(null, clazzName, target, name);
	}

	/**
	 * フィールド取得.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のフィールド名を設定します.
	 * @return Object フィールドオブジェクト内容が返されます.
	 */
	public static final Object getField(ClassLoader loader, String clazzName, Object target, String name) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		String superName;
		while (true) {
			Field f = em.getField((target == null), name);
			if (f == null) {
				superName = em.getSuperClassName();
				em = null;
				if (superName != null) {
					em = FACTORY.getClass(loader, superName);
				}
				if (em == null) {
					throw new FastReflectException("Specified field '" + name + "' does not exist in class '" + clazzName + "'.");
				}
				continue;
			}
			try {
				return f.get(target);
			} catch(Exception e) {
				throw new FastReflectException(e);
			}
		}
	}

	/**
	 * フィールド取得.
	 * 
	 * @param clazz  対象のクラス名を設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のフィールド名を設定します.
	 * @return Object フィールドオブジェクト内容が返されます.
	 */
	public static final Object getField(Class clazz, Object target, String name) {
		return getField(null, clazz, target, name);
	}

	/**
	 * フィールド取得.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param clazz  対象のクラス名を設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のフィールド名を設定します.
	 * @return Object フィールドオブジェクト内容が返されます.
	 */
	public static final Object getField(ClassLoader loader, Class clazz, Object target, String name) {
		String clazzName = clazz.getName();
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		String superName;
		while (true) {
			Field f = em.getField((target == null), name);
			if (f == null) {
				superName = em.getSuperClassName();
				em = null;
				if (superName != null) {
					em = FACTORY.getClass(loader, superName);
				}
				if (em == null) {
					throw new FastReflectException("Specified field '" + name + "' does not exist in class '" + clazzName + "'.");
				}
				continue;
			}
			try {
				return f.get(target);
			} catch(Exception e) {
				throw new FastReflectException(e);
			}
		}
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のメソッド名を設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(String clazzName, Object target, String name) {
		return invoke(null, clazzName, target, name, (Object[]) null);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のメソッド名を設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(ClassLoader loader, String clazzName, Object target, String name) {
		return invoke(loader, clazzName, target, name, (Object[]) null);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のメソッド名を設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(Class clazz, Object target, String name) {
		return invoke(null, clazz, target, name, (Object[]) null);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のメソッド名を設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(ClassLoader loader, Class clazz, Object target, String name) {
		return invoke(loader, clazz, target, name, (Object[]) null);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のメソッド名を設定します.
	 * @param args      対象のメソッドパラメータを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(String clazzName, Object target, String name, Object... args) {
		return invoke(null, clazzName, target, name, args);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のメソッド名を設定します.
	 * @param args      対象のメソッドパラメータを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(ClassLoader loader, String clazzName, Object target, String name, Object... args) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		String spclazz;
		Object[] ret = new Object[1];
		Class[] types = Util.getParamsType(args);
		while (true) {
			if (em.invoke(ret, target, name, loader, types, args)) {
				return ret[0];
			}
			spclazz = em.getSuperClassName();
			if (spclazz == null) {
				throw new FastReflectException("Specified method '" + name + "' does not exist in class '" + clazzName + "'.");
			}
			em = FACTORY.getClass(loader, spclazz);
		}
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のメソッド名を設定します.
	 * @param args   対象のメソッドパラメータを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(Class clazz, Object target, String name, Object... args) {
		return invoke(null, clazz, target, name, args);
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のメソッド名を設定します.
	 * @param args   対象のメソッドパラメータを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invoke(ClassLoader loader, Class clazz, Object target, String name, Object... args) {
		String clazzName = clazz.getName();
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		Object[] ret = new Object[1];
		Class[] types = Util.getParamsType(args);
		String spclazz;
		while (true) {
			if (em.invoke(ret, target, name, loader, types, args)) {
				return ret[0];
			}
			spclazz = em.getSuperClassName();
			if (spclazz == null) {
				throw new FastReflectException("Specified method '" + name + "' does not exist in class '" + clazzName + "'.");
			}
			em = FACTORY.getClass(loader, spclazz);
		}
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader    対象のクラスローダーを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param target    設定対象のオブジェクトを設定します.
	 *                  [null]の場合、staticアクセスで処理します.
	 * @param name      対象のメソッド名を設定します.
	 * @param args      対象のメソッドパラメータを設定します.
	 * @param types     対象のメソッドパラメータタイプを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invokeTo(ClassLoader loader, String clazzName, Object target, String name, Object[] args, Class[] types) {
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		if (args != null && args.length > 0 && types == null) {
			types = Util.getParamsType(args);
		} else if (types != null) {
			types = Util.getParamsTypeByClass(types);
		}
		Object[] ret = new Object[1];
		String spclazz;
		while (true) {
			if (em.invoke(ret, target, name, loader, types, args)) {
				return ret[0];
			}
			spclazz = em.getSuperClassName();
			if (spclazz == null) {
				throw new FastReflectException("Specified method '" + name + "' does not exist in class '" + clazzName + "'.");
			}
			em = FACTORY.getClass(loader, spclazz);
		}
	}

	/**
	 * メソッド呼び出し.
	 * 
	 * @param loader 対象のクラスローダーを設定します.
	 * @param clazz  対象のクラスを設定します.
	 * @param target 設定対象のオブジェクトを設定します.
	 *               [null]の場合、staticアクセスで処理します.
	 * @param name   対象のメソッド名を設定します.
	 * @param args   対象のメソッドパラメータを設定します.
	 * @param types  対象のメソッドパラメータタイプを設定します.
	 * @return Object 戻り値が返されます.
	 */
	public static final Object invokeTo(ClassLoader loader, Class clazz, Object target, String name, Object[] args, Class[] types) {
		String clazzName = clazz.getName();
		ClassElement em = FACTORY.getClass(loader, clazzName);
		if (em == null) {
			throw new FastReflectException("Specified class '" + clazzName + "' does not exist");
		}
		if (args != null && args.length > 0 && types == null) {
			types = Util.getParamsType(args);
		} else if (types != null) {
			types = Util.getParamsTypeByClass(types);
		}
		Object[] ret = new Object[1];
		String spclazz;
		while (true) {
			if (em.invoke(ret, target, name, loader, types, args)) {
				return ret[0];
			}
			spclazz = em.getSuperClassName();
			if (spclazz == null) {
				throw new FastReflectException("Specified method '" + name + "' does not exist in class '" + clazzName + "'.");
			}
			em = FACTORY.getClass(loader, spclazz);
		}
	}
}

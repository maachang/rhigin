package rhigin.util;

/**
 * 取得要素変換.
 */
public interface ConvertGet<N> {

	/**
	 * 取得処理.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Object 対象情報が返却されます.
	 */
	Object getOriginal(N n);

	/**
	 * boolean情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	default Boolean getBoolean(N n) {
		return Converter.convertBool(getOriginal(n));
	}

	/**
	 * int情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Integer 情報が返却されます.
	 */
	default Integer getInt(N n) {
		return Converter.convertInt(getOriginal(n));
	}

	/**
	 * long情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Long 情報が返却されます.
	 */
	default Long getLong(N n) {
		return Converter.convertLong(getOriginal(n));
	}

	/**
	 * float情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Float 情報が返却されます.
	 */
	default Float getFloat(N n) {
		return Converter.convertFloat(getOriginal(n));
	}

	/**
	 * double情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Double 情報が返却されます.
	 */
	default Double getDouble(N n) {
		return Converter.convertDouble(getOriginal(n));
	}

	/**
	 * String情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return String 情報が返却されます.
	 */
	default String getString(N n) {
		return Converter.convertString(getOriginal(n));
	}

	/**
	 * Date情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Date 情報が返却されます.
	 */
	default java.sql.Date getDate(N n) {
		return Converter.convertSqlDate(getOriginal(n));
	}

	/**
	 * Time情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Time 情報が返却されます.
	 */
	default java.sql.Time getTime(N n) {
		return Converter.convertSqlTime(getOriginal(n));
	}

	/**
	 * Timestamp情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Timestamp 情報が返却されます.
	 */
	default java.sql.Timestamp getTimestamp(N n) {
		return Converter.convertSqlTimestamp(getOriginal(n));
	}
}

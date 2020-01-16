package rhigin.lib.level.operator;

/**
 * オペレータインターフェイス.
 */
public interface Operator {
	/**
	 * オペレータ名を取得.
	 * @return String オペレータ名が返却されます.
	 */
	public String getName();
	
	/**
	 * オペレータタイプを取得.
	 * @return String オペレータタイプが返却されます.
	 *                "object" の場合は ObjectOperatorです.
	 *                "latlon" の場合は LatLonOperatorです.
	 *                "sequence" の場合は SequenceOperatorです.
	 *                "queue" の場合は QueueOperatorです.
	 */
	public String getOperatorType();
	
	/**
	 * オブジェクトが利用可能かチェック.
	 * @return [true]の場合利用可能です.
	 */
	public boolean isAvailable();
	
	/**
	 * 情報が空かチェック.
	 * 
	 * @return boolean [true]の場合、空です.
	 */
	public boolean isEmpty();
	
	/**
	 * LevelModeを取得.
	 * @return
	 */
	public OperatorMode getMode();

}

package rhigin.util;

/**
 * ランダムなUUIDを生成します.
 */
public class RandomUUID {
	private final Xor128 xor128;
	
	/**
	 * コンストラクタ.
	 */
	public RandomUUID() {
		this(System.nanoTime());
	}
	
	/**
	 * コンストラクタ.
	 * @param seet 乱数のseetを設定します.
	 */
	public RandomUUID(long seet) {
		xor128 = new Xor128(seet);
	}
	
	/**
	 * コンストラクタ.
	 * @param xor128 ランダムオブジェクトを設定します.
	 */
	public RandomUUID(Xor128 xor128) {
		this.xor128 = xor128;
	}
	
	/**
	 * ID生成.
	 * @param count 追加生成する乱数回数を設定します.
	 * @return RandomUUID このオブジェクトが返却されます.
	 */
	public RandomUUID getId(int count) {
		int no = xor128.nextInt();
		int exitCnt = count;
		boolean befFlg = false;
		// ぶつかりづらい乱数を生成するための処理.
		while(true) {
			exitCnt --;
			if(exitCnt <= 0) {
				break;
			}
			if(!befFlg && ((no & 0x00000001) == 0 || (System.nanoTime() & 0x00000001) != 0)){
				exitCnt ++;
				if(count <= exitCnt) {
					exitCnt = count;
				}
				befFlg = true;
			} else {
				befFlg = false;
			}
			no = xor128.nextInt();
		}
		return this;
	}
	
	/**
	 * UUIDで取得.
	 * @return String uuid が返却されます.
	 */
	public String getUUID() {
		return Converter.byte16ToUUID(
			xor128.nextInt(), xor128.nextInt(), xor128.nextInt(), xor128.nextInt());
	}
	
	/**
	 * intの配列で取得.
	 * @return int[] int[4]の配列が返却されます.
	 */
	public int[] getInt4() {
		return new int[] {
			xor128.nextInt(), xor128.nextInt(), xor128.nextInt(), xor128.nextInt()
		};
	}
}

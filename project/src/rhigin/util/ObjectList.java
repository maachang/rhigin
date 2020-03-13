package rhigin.util;

import java.util.AbstractList;

/**
 * オブジェクトリスト.
 */
public class ObjectList<E> extends AbstractList<E> implements ConvertGet<Integer> {
	private OList<E> list;

	/**
	 * コンストラクタ.
	 */
	public ObjectList() {
		list = new OList<E>();
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param buf
	 *            初期配列サイズを設定します.
	 */
	public ObjectList(int buf) {
		list = new OList<E>(buf);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param 初期設定情報を設定します.
	 */
	public ObjectList(Object... o) {
		list = new OList<E>(o);
	}

	@Override
	public Object getOriginal(Integer n) {
		return get(n);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean add(E n) {
		list.add(n);
		return true;
	}
	
	@Override
	public void add(int no, E n) {
		list.add(no, n);
	}

	@Override
	public E set(int index, E n) {
		return list.set(index, n);
	}

	@Override
	public E remove(int index) {
		return list.remove(index);
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}
	
	public OList<E> rawData() {
		return list;
	}
}

package rhigin.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;


/**
 * 固定配列のList実装.
 */
public class FixedArray<E> extends AbstractList<E> implements ConvertGet<Integer> {
	private Object[] array;
	
	public FixedArray() {
		array = new Object[0];
	}
	
	@SuppressWarnings("rawtypes")
	public FixedArray(Object... o) {
		if(o == null || o.length == 0) {
			array = new Object[0];
		} else if(o.length > 1) {
			array = o;
		} else {
			Object n = o[0];
			if(n.getClass().isArray()) {
				int len = Array.getLength(n);
				array = new Object[len];
				System.arraycopy(n, 0, array, 0, len);
			} else if(n instanceof List) {
				List lst = (List)n;
				int len = n == null ? 0 : lst.size();
				Object[] b = new Object[len];
				for(int i = 0; i < len; i ++) {
					b[i] = lst.get(i);
				}
				array = b;
			} else {
				array = new Object[] {n};
			}
		}
	}
	
	public FixedArray(int size) {
		array = new Object[size <= 0 ? 0 : size];
	}
	
	@Override
	public Object getOriginal(Integer n) {
		return get(n);
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean add(E n) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E n) {
		Object ret = array[index];
		array[index] = n;
		return (E)ret;
	}

	@Override
	public E remove(int index) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E)array[index];
	}

	@Override
	public int size() {
		return array.length;
	}
	
	public Object[] rawData() {
		return array;
	}
	
	public void sort() {
		Arrays.sort(array);
	}
}

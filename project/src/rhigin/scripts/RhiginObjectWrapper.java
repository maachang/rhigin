package rhigin.scripts;

/**
 * javaオブジェクトWrapper.
 * 
 * 本来rhinoのWrapperを使うべきだが、これを使うとややこしくなるので、別途実装.
 */
public interface RhiginObjectWrapper {
	
	/**
	 * unwrap.
	 * @return 元野オブジェクトが返却されます.
	 */
	public Object unwrap();
}

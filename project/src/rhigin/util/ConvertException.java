package rhigin.util;

import rhigin.RhiginException;

/**
 * 変換系例外.
 */
public class ConvertException extends RhiginException {
	private static final long serialVersionUID = 2547034651824606342L;

	public ConvertException() {
		super(500);
	}

	public ConvertException(String m) {
		super(500, m);
	}

	public ConvertException(Throwable e) {
		super(500, e);
	}

	public ConvertException(String m, Throwable e) {
		super(500, m, e);
	}
}

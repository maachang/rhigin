package rhigin.util;

import rhigin.RhiginException;

/**
 * fcompress例外.
 */
public class FCompException extends RhiginException {
	private static final long serialVersionUID = -6021604387945191923L;

	public FCompException() {
		super(500);
	}

	public FCompException(String message) {
		super(500, message);
	}

	public FCompException(Throwable e) {
		super(500, e);
	}

	public FCompException(String message, Throwable e) {
		super(500, message, e);
	}
}

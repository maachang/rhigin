package rhigin.util;

import rhigin.RhiginException;

/**
 * fcipher例外.
 */
public class FCipherException extends RhiginException {
	private static final long serialVersionUID = -7995238015802434505L;

	public FCipherException() {
		super(500);
	}

	public FCipherException(String message) {
		super(500, message);
	}

	public FCipherException(Throwable e) {
		super(500, e);
	}

	public FCipherException(String message, Throwable e) {
		super(500, message, e);
	}
}

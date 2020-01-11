package rhigin.lib.level.runner;

import rhigin.RhiginException;

/**
 * Level例外.
 */
public class LevelException extends RhiginException {
	private static final long serialVersionUID = 6766167672812368468L;

	public LevelException() {
		super(500);
	}

	public LevelException(String message) {
		super(500, message);
	}

	public LevelException(Throwable e) {
		super(500, e);
	}

	public LevelException(String message, Throwable e) {
		super(500, message, e);
	}

	public LevelException(int status) {
		super(status);
	}

	public LevelException(int status, String message) {
		super(status, message);
	}

	public LevelException(int status, Throwable e) {
		super(status, e);
	}

	public LevelException(int status, String message, Throwable e) {
		super(status, message, e);
	}
}

package rhigin.lib.level.runner;

import rhigin.RhiginException;

/**
 * Level例外.
 */
public class LevelJsException extends RhiginException {
	private static final long serialVersionUID = 6766167672812368468L;

	public LevelJsException() {
		super(500);
	}

	public LevelJsException(String message) {
		super(500, message);
	}

	public LevelJsException(Throwable e) {
		super(500, e);
	}

	public LevelJsException(String message, Throwable e) {
		super(500, message, e);
	}

	public LevelJsException(int status) {
		super(status);
	}

	public LevelJsException(int status, String message) {
		super(status, message);
	}

	public LevelJsException(int status, Throwable e) {
		super(status, e);
	}

	public LevelJsException(int status, String message, Throwable e) {
		super(status, message, e);
	}
}

package rhigin.lib.jdbc.runner;

import rhigin.RhiginException;

/**
 * JDBC例外.
 */
public class JDBCException extends RhiginException {
	private static final long serialVersionUID = 4389626805720686439L;

	public JDBCException() {
		super(500);
	}

	public JDBCException(String message) {
		super(500, message);
	}

	public JDBCException(Throwable e) {
		super(500, e);
	}

	public JDBCException(String message, Throwable e) {
		super(500, message, e);
	}

	public JDBCException(int status) {
		super(status);
	}

	public JDBCException(int status, String message) {
		super(status, message);
	}

	public JDBCException(int status, Throwable e) {
		super(status, e);
	}

	public JDBCException(int status, String message, Throwable e) {
		super(status, message, e);
	}
}

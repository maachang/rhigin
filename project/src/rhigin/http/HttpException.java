package rhigin.http;

import rhigin.RhiginException;

/**
 * HTTP例外.
 */
public class HttpException extends RhiginException {
	private static final long serialVersionUID = 7317119139199782998L;

	public HttpException(int status) {
		super(status);
	}

	public HttpException(int status, String message) {
		super(status, message);
	}

	public HttpException(int status, Throwable e) {
		super(status, e);
	}

	public HttpException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public static final void error(int status, String message) {
		error(status, message, null);
	}

	public static final void error(int status, String message, Throwable e) {
		if (message == null) {
			message = Status.getMessage(status);
		}
		if (e != null) {
			throw new HttpException(status, message, e);
		} else {
			throw new HttpException(status, message);
		}
	}
}

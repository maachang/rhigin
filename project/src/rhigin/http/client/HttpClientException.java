package rhigin.http.client;

import rhigin.RhiginException;

/**
 * HttpClient用例外.
 */
public class HttpClientException extends RhiginException {
	private static final long serialVersionUID = -2021665690645283094L;

	public HttpClientException(int status) {
		super(status);
	}

	public HttpClientException(int status, String message) {
		super(status, message);
	}

	public HttpClientException(int status, Throwable e) {
		super(status, e);
	}

	public HttpClientException(int status, String message, Throwable e) {
		super(status, message, e);
	}

	public HttpClientException() {
		this(500);
	}

	public HttpClientException(String m) {
		this(500, m);
	}

	public HttpClientException(Throwable e) {
		this(500, e);
	}

	public HttpClientException(String m, Throwable e) {
		this(500, m, e);
	}
}

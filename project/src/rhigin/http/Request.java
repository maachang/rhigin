package rhigin.http;

import java.io.IOException;

import rhigin.RhiginException;
import rhigin.net.NioReadBuffer;
import rhigin.util.Converter;

/**
 * Request.
 */
public class Request extends Header {
	protected byte[] body = null;
	protected Long contentLength = null;

	protected Request() {
		super();
	}

	public Request(NioReadBuffer buffer, int endPoint) throws IOException {
		super(buffer, endPoint);
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public byte[] getBody() {
		return body;
	}

	public String getBodyText() {
		try {
			return new String(body, charset((String) get("Content-Type")));
		} catch (RhiginException re) {
			throw re;
		} catch (Exception e) {
			throw new RhiginException(500, e);
		}
	}

	private static final String charset(String contentType) {
		int p = contentType.indexOf(" charset=");
		if (p == -1) {
			return "UTF8";
		}
		int b = p + 9;
		p = contentType.indexOf(";", b);
		if (p == -1) {
			p = contentType.length();
		}
		return contentType.substring(b, p).trim();
	}

	public long getContentLength() throws IOException {
		if (contentLength != null) {
			return contentLength;
		}
		String ret = (String) this.get("Content-Length");
		if (ret == null) {
			return -1;
		}
		contentLength = Converter.parseLong(ret);
		return contentLength;
	}

	@Override
	public Object get(Object key) {
		if ("isBodyInputStream".equals(key) || "isBodyFile".equals(key)) {
			return ((HttpElement) element).isHttpPostBodyFile();
		} else if ("inputStream".equals(key) || "body".equals(key) || "bodyFile".equals(key)) {
			HttpElement em = (HttpElement) element;
			if (em.isHttpPostBodyFile()) {
				return em.getHttpPostBodyFile(null).getInputStream();
			} else {
				return null;
			}
		} else if ("bodyName".equals(key) || "bodyFileName".equals(key)) {
			HttpElement em = (HttpElement) element;
			if (em.isHttpPostBodyFile()) {
				return em.getHttpPostBodyFile(null).getFileName();
			} else {
				return null;
			}
		}
		return super.get(key);
	}
}

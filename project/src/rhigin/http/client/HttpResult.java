package rhigin.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rhigin.RhiginException;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.Json;
import rhigin.util.AbstractKeyIterator;
import rhigin.util.ConvertMap;

/**
 * HttpClient処理結果.
 */
@SuppressWarnings("rawtypes")
public class HttpResult extends JavaScriptable.Map implements AbstractKeyIterator.Base<String>, ConvertMap {
    private byte[] headers = null;
    private String headersString = null;

    private byte[] body = null;
    private int status = -1;
    private String url = null;
    private String contentType = null;

    protected HttpResult(String url, int status, byte[] header) {
        this.url = url;
        this.status = status;
        this.headers = header;
    }

    public void clear() {
        url = null;
        headers = null;
        headersString = null;
        body = null;
        status = -1;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }

    protected String getHeader(String key) throws IOException {
        convertString();
        if (headersString == null) {
            return null;
        }

        final int p = headersString.indexOf(key + ": ");
        if (p == -1) {
            return null;
        }
        int end = headersString.indexOf("\r\n", p);
        if (end == -1) {
            return null;
        }
        return headersString.substring(p + key.length() + 2, end);
    }

    public List<String> getHeaders() throws IOException {
        convertString();
        if (headersString == null) {
            return null;
        }
        int p;
        int b = 0;
        final List<String> ret = new ArrayList<String>();
        while ((p = headersString.indexOf(": ", b)) != -1) {
            ret.add(headersString.substring(b, p));
            b = p + 2;
            p = headersString.indexOf("\r\n", b);
            if (p == -1) {
                break;
            }
            b = p + 2;
        }
        return ret;
    }

    private final void convertString() throws IOException {
        if (headers != null) {
            headersString = new String(headers, "UTF8");
            headers = null;
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
        return contentType.substring(b, p);
    }
    
    private final String getContentType() {
        if(contentType == null) {
            try {
                contentType = getHeader("Content-Type");
            } catch(Exception e) {
                contentType = null;
            }
        }
        return contentType;
    }

    protected void setResponseBody(byte[] body) {
        this.body = body;
    }

    public byte[] responseBody() {
        return body;
    }
    
    public String responseText() {
        try {
            String charset = charset(getContentType());
            return new String(body, charset);
        } catch(Exception e) {
            throw new RhiginException(500, e);
        }
    }

    public String toString() {
        return Json.encode(this);
    }
    
	@Override
	public String getKey(int no) {
		try {
			List<String> list = getHeaders();
			return list.get(no);
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}

	@Override
	public int size() {
		try {
			List<String> list = getHeaders();
			return list.size();
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
	
	@Override
	public Set keySet() {
		return new AbstractKeyIterator.KeyIteratorSet<>(this);
	}

	@Override
	public Object get(Object key) {
        if (key == null) {
            return null;
        } else if ("url".equals(key)) {
            return getUrl();
        } else if ("status".equals(key)) {
            return getStatus();
        } else if ("body".equals(key)) {
            return responseBody();
        } else if ("text".equals(key)) {
            return responseText();
        } else if("Content-Type".equals(key)) {
            return getContentType();
        }
        try {
            return getHeader(key.toString());
        } catch (Exception e) {
            return null;
        }
    }

	@Override
	public boolean containsKey(Object name) {
		return get(name) != null;
	}

	@Override
	public Object put(Object name, Object value) {
		return null;
	}

	@Override
	public Object remove(Object name) {
		return null;
	}
}

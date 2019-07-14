package rhigin.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rhigin.net.ByteArrayIO;
import rhigin.scripts.JavaScriptable;
import rhigin.util.ConvertMap;

/**
 * Httpヘッダ情報. 基本HTTPヘッダ情報のみを保持します. (bodyデータは非保持).
 */
public class Header extends JavaScriptable.Map implements ConvertMap {
    protected String method;
    protected String url;
    protected String version;
    protected byte[] headers;
    protected String headersString;

    protected Header() {
    }

    // http のendpointが検知された場合にコンストラクタ呼び出し.
    public Header(ByteArrayIO buffer, int endPoint) throws IOException {
      int firstPoint = buffer.indexOf(Analysis.ONE_LINE);
      byte[] b = new byte[firstPoint];
      buffer.read(b);
      buffer.skip(Analysis.ONE_LINE_LENGTH);

      String v = new String(b, "UTF8");
      b = null;
      analysisFirst(v);
      v = null;

      int len = endPoint + Analysis.END_LINE_LENGTH
              - (firstPoint + Analysis.ONE_LINE_LENGTH);
      b = new byte[len];
      buffer.read(b);

      this.headers = b;
      this.headersString = null;
    }

    // url method version を取得.
    protected final void analysisFirst(String v) throws IOException {
      String[] list = v.split(" ");
      if (list.length != 3) {
          throw new IOException("Received data is not an HTTP request:" + v);
      }
      this.method = list[0];
      this.url = list[1];
      this.version = list[2];
    }
    
    // 取得ヘッダバイナリを文字列のヘッダに変換.
    protected final void getHeaderString() throws IOException {
      if (headers != null) {
    	    headersString = new String(headers, "UTF8");
    	    headers = null;
      }
	}
    
    /**
     * HTTPメソッドを取得.
     * @return
     */
    public String getMethod() {
      return method;
    }

    /**
     * HTTPアクセスURLを取得.
     * @return
     */
    public String getUrl() {
      return url;
    }

    /**
     * HTTPバージョンを取得.
     * @return
     */
    public String getVersion() {
      return version;
    }
    
    /**
     * ヘッダ情報を取得.
     * @param key
     * @return
     * @throws IOException
     */
    public String getHeader(String key) throws IOException {
      getHeaderString();
      int p = headersString.indexOf(key + ": ");
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
      getHeaderString();
      int p;
      int b = 0;
      List<String> ret = new ArrayList<String>();
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

    /**
     * 取得.
     * @param key 対象のキーを設定します.
     * @return Object キーに対する要素情報が返却されます.
     */
    @Override
    public Object get(Object key) {
      if (key == null) {
        return null;
      } else if ("url".equals(key)) {
        return getUrl();
      } else if ("method".equals(key)) {
        return getMethod();
      } else if ("version".equals(key)) {
        return getVersion();
      }
      try {
        return getHeader(key.toString());
      } catch (Exception e) {
        return null;
      }
    }
    
    @Override
    public String toString() {
        try {
            List<String> list = getHeaders();
            int len = list.size();
            StringBuilder buf = new StringBuilder("{");
            for(int i = 0; i < len; i ++) {
                if(i != 0) {
                    buf.append(", ");
                }
                buf.append("\"").append(list.get(i)).append("\": \"").append(get(list.get(i))).append("\"");
            }
            return buf.append("}").toString();
        } catch(Exception e) {
        }
        return "";
    }

	@Override
	public Object[] getIds() {
		try {
			List<String> list = getHeaders();
			int len = list.size();
			Object[] ret = new Object[len];
			for(int i = 0; i < len; i ++) {
				ret[i] = list.get(i);
			}
			return ret;
		} catch(Exception e) {
			return new Object[] {};
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

package rhigin.http;

import java.io.IOException;

import rhigin.net.ByteArrayIO;
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

    public Request(ByteArrayIO buffer, int endPoint) throws IOException {
      super(buffer, endPoint);
    }

    public void setBody(byte[] body) {
      this.body = body;
    }

    public byte[] getBody() {
      return body;
    }

    public long getContentLength() throws IOException {
      if (contentLength != null) {
        return contentLength;
      }
      String ret = (String)this.get("Content-Length");
      if (ret == null) {
        return -1;
      }
      contentLength = Converter.parseLong(ret);
      return contentLength;
    }
}

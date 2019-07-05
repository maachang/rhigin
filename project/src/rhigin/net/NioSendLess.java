package rhigin.net;

import java.nio.ByteBuffer;

public class NioSendLess {
  private byte[] binary = null;
  private int length = 0;
  public void clear() {
    binary = null;
    length = 0;
  }
  public void evacuate(ByteBuffer buf) {
    int len = buf.remaining();
    if (len == 0) {
      return;
    } else if (binary == null || binary.length < len) {
      binary = new byte[len];
    }
    buf.get(binary, 0, len);
    length = len;
  }
  public void setting(ByteBuffer buf) {
    if (length == 0) {
      return;
    }
    buf.put(binary, 0, length);
    binary = null;
    length = 0;
  }
}

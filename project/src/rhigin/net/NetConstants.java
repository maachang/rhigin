package rhigin.net;

public class NetConstants {

  /**
   * NioのSelectorタイムアウト.
   */
  public static final int SELECTOR_TIMEOUT = 1000;

  /**
   * IPV4だけで処理するか : true.
   */
  public static final boolean NET_IPV4_FLAG = true;

  /**
   * DNS キャッシュ時間 : 300秒.
   */
  public static final int NET_DNS_CACHE_SECOND = 300;

  /**
   * DNS解決失敗ドメインの保有時間.
   */
  public static final int ERROR_DNS_CACHE_TIME = 0;

  /**
   * NioElement ByteArrayIOバッファサイズ.
   */
  public static final int NIO_ELEMENT_BUFFER_SIZE = 512;
}
package rhigin.http;

public class HttpConstants {
	protected HttpConstants() {
	}

	/** HTTPアクセス用フォルダ. **/
	public static final String ACCESS_PATH = "./application";

	/** メモリで受け取るContent-Type最大値. **/
	public static final int MAX_CONTENT_LENGTH = 5 * 0x00100000;

	/** 送信Body情報をGZIP圧縮させないサイズ. **/
	public static final int NOT_GZIP_BODY_LENGTH = 128;

	/** POST受信時のBody情報ファイル出力を行うための、Content-Type. **/
	public static final String POST_FILE_OUT_CONTENT_TYPE = "application/rhigin";

	/** POST受診時のBody情報ファイルディレクトリ. **/
	public static final String POST_FILE_OUT_ROOT_DIR = "./.bodys/";

	/** デフォルトのバインドポート. **/
	public static final int BIND_PORT = 3120;

	/** デフォルトのワーカースレッド数. **/
	public static final int WORKER_THREAD = -1; // CPU数に応じてスレッド数を割り当てる.

	/** CPU数に応じたワーカースレッド係数(1cpuに４スレッド). **/
	public static final int WORKER_CPU_COEFFICIENT = 4;

	/** デフォルトのコンパイルキャッシュ数. **/
	public static final int COMPILE_CACHE_SIZE = 128;

	/** デフォルトのコンパイルルートフォルダ. **/
	public static final String COMPILE_ROOT_DIR = ".";
	
	/** ブラウザアクセスの有無を示す、HTTPリクエストヘッダ. **/
	public static final String BLOWSER_ACCESS_HEADER = "X-Blowser";
	
}

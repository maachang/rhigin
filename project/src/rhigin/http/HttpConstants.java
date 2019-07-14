package rhigin.http;

public class HttpConstants {
	protected HttpConstants() {}
	
	public static final String ACCESS_PATH = "./application";

	public static final int MAX_CONTENT_LENGTH = 1 * 0x100000;
	
	public static final int NOT_GZIP_BODY_LENGTH = 512;

	public static final int BIND_PORT = 3120;

	public static final int WORKER_THREAD = 5;

	public static final int COMPILE_CACHE_SIZE = 128;

	public static final String COMPILE_ROOT_DIR = ".";
}

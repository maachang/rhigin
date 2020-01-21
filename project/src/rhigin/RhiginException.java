package rhigin;

/**
 * Rhigin用例外.
 */
public class RhiginException extends RuntimeException {
	private static final long serialVersionUID = -3354862673253639272L;
	protected int status;
	protected String msg;

	public RhiginException(int status) {
		super();
		this.status = status;
	}

	public RhiginException(int status, String message) {
		super(message);
		this.status = status;
	}

	public RhiginException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public RhiginException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public RhiginException() {
		this(500);
	}

	public RhiginException(String m) {
		this(500, m);
	}

	public RhiginException(Throwable e) {
		this(500, e);
	}

	public RhiginException(String m, Throwable e) {
		this(500, m, e);
	}

	public int getStatus() {
		return status;
	}
	
	public void setMessage(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg == null ? super.getMessage() : msg;
	}
	
	public String getLocalizedMessage() {
		return msg == null ? super.getLocalizedMessage() : msg;
	}
}

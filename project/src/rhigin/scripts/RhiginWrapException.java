package rhigin.scripts;

import org.mozilla.javascript.WrappedException;

import rhigin.RhiginException;

/**
 * Rhino用のRhigin例外.
 */
public class RhiginWrapException extends WrappedException {
	private static final long serialVersionUID = 8330918787241079938L;
	private int status = 500;
	public RhiginWrapException(String msg) {
		super(new RhiginException(msg));
		this.status= 500;
	}
	public RhiginWrapException(int status, String msg) {
		super(new RhiginException(msg));
		this.status= status;
	}
	public RhiginWrapException(Throwable e) {
		super(e instanceof RhiginException ? e : (e = new RhiginException(e)));
		this.status = e instanceof RhiginException ? ((RhiginException)e).getStatus() : 500;
	}

	public int getStatus() {
		return status;
	}
}

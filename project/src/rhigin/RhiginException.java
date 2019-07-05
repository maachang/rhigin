package rhigin;

public class RhiginException extends RuntimeException {
    private static final long serialVersionUID = -3354862673253639272L;
    protected int status;
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

    public int getStatus() {
        return status;
    }
}

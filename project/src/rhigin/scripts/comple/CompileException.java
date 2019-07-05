package rhigin.scripts.comple;

import rhigin.RhiginException;

public class CompileException extends RhiginException {
    private static final long serialVersionUID = -2078988719079761710L;
    public CompileException(int status) {
        super(status);
    }

    public CompileException(int status, String message) {
        super(status, message);
    }

    public CompileException(int status, Throwable e) {
        super(status, e);
    }

    public CompileException(int status, String message, Throwable e) {
        super(status, message, e);
    }
}

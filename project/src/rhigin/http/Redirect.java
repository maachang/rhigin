package rhigin.http;

import rhigin.RhiginException;

/**
 * リダイレクト処理.
 */
public class Redirect extends RhiginException {
    private static final long serialVersionUID = 2595316381581210308L;
    private String url;

    public Redirect(int status, String url) {
        super(status);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

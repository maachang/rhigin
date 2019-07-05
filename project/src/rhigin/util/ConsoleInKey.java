package rhigin.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * コンソール入力支援.
 */
public class ConsoleInKey implements Closeable, AutoCloseable {
    private Console console = null;
    private BufferedReader in = null;

    public ConsoleInKey() {
        BufferedReader b = null;
        Console c = System.console();
        if (c == null) {
            b = new BufferedReader(new InputStreamReader(System.in));
        }
        console = c;
        in = b;
    }

    protected void finalize() throws Exception {
        close();
    }

    /**
     * オブジェクトクローズ.
     */
    public void close() throws IOException {
        console = null;
        if (in != null) {
            in.close();
            in = null;
        }
        console = null;
    }

    /**
     * 1行入力情報を取得.
     * 
     * @param view
     *            表示条件を設定します.
     * @return String １行の入力情報が返却されます.
     * @exception IOException
     */
    public String readLine() throws IOException {
        return readLine("");
    }

    /**
     * 1行入力情報を取得.
     * 
     * @param view
     *            表示条件を設定します.
     * @return String １行の入力情報が返却されます.
     * @exception IOException
     */
    public String readLine(String view) throws IOException {
        if (view != null && view.length() != 0) {
            if (console == null) {
                System.out.print(view);
                return in.readLine();
            }
            return console.readLine(view);
        }
        if (console != null) {
            return console.readLine();
        }
        return in.readLine();
    }
}

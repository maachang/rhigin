package rhigin;

import rhigin.downs.ShutdownClient;
import rhigin.util.Converter;

/**
 * rhigin シャットダウン処理.
 */
public class RhiginDown {
	protected RhiginDown() {}
	
	/** サーバー停止処理. **/
	public static final void main(String[] args) throws Exception {
		int shutdownPort = -1;
		
		// パラメータからのポート指定がない場合は、サーバ定義を読み込み、
		// そこからポート番号で処理.
		if (args == null || args.length == 0) {
			RhiginConfig conf = RhiginStartup.initLogFactory(false, args);
			
			// シャットダウンまで待つ処理を生成.
			if(Converter.isNumeric(conf.get("http", "shutdownPort"))) {
				shutdownPort = conf.getInt("http", "shutdownPort");
			}
		}
		// パラメータ指定されている場合は、その内容を利用する.
		else {
			shutdownPort = Converter.convertInt(args[0]);
		}
		// シャットダウン実行.
		ShutdownClient.send(shutdownPort);
	}
}

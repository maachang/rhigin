package rhigin.lib.level.runner;

import java.util.Map;

import org.maachang.leveldb.LevelOption;
import org.maachang.leveldb.util.Alphabet;
import org.maachang.leveldb.util.Converter;

import rhigin.scripts.JsMap;
import rhigin.scripts.JsonOut;

/**
 * Leveldb オペレータ生成モード.
 */
public class LevelMode {
	protected LevelOption option;
	
	/**
	 * コンストラクタ.
	 * 
	 * @param args パラメータを設定します.
	 */
	public LevelMode(Object... args) {
		if(args != null && args.length >= 1) {
			if(args[0] instanceof LevelOption) {
				option = (LevelOption)args[0];
			} else {
				option = new LevelOption();
				set(args);
			}
		}
	}
	
	/**
	 * オペレータ生成モードをセット.
	 * 
	 * @param args パラメータを設定します.
	 *             args.length == 1 and args[0] = Map : Mapオブジェクトで各パラメータを設定します.
	 *             key, value ... : 偶数にキー名、奇数に要素を設定します.
	 *             
	 *             type:        キータイプ(String or Number).
	 *             writeBuffer: 書き込みバッファ数(Number).
	 *             maxOpenFile: オープン最大ファイル数(Number).
	 *             blockSize:   ブロックサイズ(Number).
	 *             blockCache:  ブロックキャッシュ(Number).
	 */
	@SuppressWarnings("rawtypes")
	public void set(Object... args) {
		int len;
		if(args == null || (len = args.length) == 0) {
			return;
		}
		if(len == 1 && args[0] instanceof Map) {
			Map map = (Map)args[0];
			Object o = map.get("type");
			if(o != null) {
				if(o instanceof String) {
					option.setType(Converter.convertString(o));
				} else if(Converter.isNumeric(o)) {
					option.setType(Converter.convertInt(o));
				}
			}
			if((o = map.get("writeBuffer")) != null && Converter.isNumeric(o)) {
				option.setWriteBufferSize(Converter.convertInt(o));
			}
			if((o = map.get("maxOpenFile")) != null && Converter.isNumeric(o)) {
				option.setMaxOpenFiles(Converter.convertInt(o));
			}
			if((o = map.get("blockSize")) != null && Converter.isNumeric(o)) {
				option.setBlockSize(Converter.convertInt(o));
			}
			if((o = map.get("blockCache")) != null && Converter.isNumeric(o)) {
				option.setBlockCache(Converter.convertInt(o));
			}
		} else if(len >= 2) {
			Object o;
			String k;
			for(int i = 0; i < len; i += 2) {
				if(args[i] != null) {
					k = Converter.convertString(args[i]);
					o = args[i + 1];
					if(Alphabet.eq(k, "type")) {
						if(o instanceof String) {
							option.setType(Converter.convertString(o));
						} else if(Converter.isNumeric(o)) {
							option.setType(Converter.convertInt(o));
						}
					} else if(Alphabet.eq(k, "writeBuffer") && Converter.isNumeric(o)) {
						option.setWriteBufferSize(Converter.convertInt(o));
					} else if(Alphabet.eq(k, "maxOpenFile") && Converter.isNumeric(o)) {
						option.setMaxOpenFiles(Converter.convertInt(o));
					} else if(Alphabet.eq(k, "blockSize") && Converter.isNumeric(o)) {
						option.setBlockSize(Converter.convertInt(o));
					} else if(Alphabet.eq(k, "blockCache") && Converter.isNumeric(o)) {
						option.setBlockCache(Converter.convertInt(o));
					}
				}
			}
		}
	}
	
	/**
	 * LevelOptionを取得.
	 * @return LevelOption LevelOptionが返却されます.
	 */
	public LevelOption getOption() {
		return option;
	}
	
	/**
	 * 設定内容をMapで取得.
	 * @return Map 設定されている内容が返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public Map get() {
		return new JsMap("type", LevelOption.stringType(option.getType())
			,"writeBuffer", option.getWriteBufferSize()
			,"maxOpenFile", option.getMaxOpenFiles()
			,"blockSize", option.getBlockSize()
			,"blockCache", option.getBlockCache()
		);
	}
	
	/**
	 * 文字列として出力.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String toString() {
		return JsonOut.toString(get());
	}
}

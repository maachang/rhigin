package rhigin.lib.level.operator;

import java.util.Map;

import org.maachang.leveldb.LevelOption;
import org.maachang.leveldb.util.Alphabet;
import org.maachang.leveldb.util.Converter;

import rhigin.scripts.JsMap;
import rhigin.scripts.JsonOut;

/**
 * オペレータ生成モード.
 */
public class OperatorMode {
	protected LevelOption option;
	
	/**
	 * コンストラクタ.
	 * 
	 * @param args パラメータを設定します.
	 */
	public OperatorMode(Object... args) {
		if(args != null && args.length >= 1) {
			if(args[0] instanceof LevelOption) {
				option = (LevelOption)args[0];
			} else if(args[0] instanceof OperatorMode) {
				option = ((OperatorMode)args[0]).getOption().copyObject();
			} else {
				option = new LevelOption();
				// 初期タイプをセット.
				option.setType(LevelOption.TYPE_NONE);
				option.setExpansion(OperatorKeyType.KEY_NONE);
				set(args);
			}
		} else {
			option = new LevelOption();
		}
	}
	
	/**
	 * オペレータ生成モードをセット.
	 * 
	 * @param args パラメータを設定します.
	 *             args.length == 1 and args[0] = Map : Mapオブジェクトで各パラメータを設定します.
	 *             key, value ... : 偶数にキー名、奇数に要素を設定します.
	 *             
	 *             type:        Operatorキータイプ(String or Number).
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
			if(o == null) {
				o = map.get("operatorType");
			}
			if(o != null) {
				int okType = -1;
				if(o instanceof String) {
					okType = OperatorKeyType.convertStringByKeyType(Converter.convertString(o));
				} else if(Converter.isNumeric(o)) {
					okType = Converter.convertInt(o);
				}
				int loType = OperatorKeyType.convertLevelOptionType(okType);
				// leveldb用のキータイプをセット.
				option.setType(loType);
				// Leveljs用のキータイプをセット.
				option.setExpansion(okType);
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
						int okType = -1;
						if(o instanceof String) {
							okType = OperatorKeyType.convertStringByKeyType(Converter.convertString(o));
						} else if(Converter.isNumeric(o)) {
							okType = Converter.convertInt(o);
						}
						int loType = OperatorKeyType.convertLevelOptionType(okType);
						// leveldb用のキータイプをセット.
						option.setType(loType);
						// Leveljs用のキータイプをセット.
						option.setExpansion(okType);
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
		return new JsMap("leveldbType", LevelOption.stringType(option.getType())
			,"operatorType", OperatorKeyType.toString(getOperatorType())
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
	
	public String getType() {
		return LevelOption.stringType(option.getType());
	}
	
	public int getWriteBuffer() {
		return option.getWriteBufferSize();
	}
	
	public int getMaxOpenFile() {
		return option.getMaxOpenFiles();
	}
	
	public int getBlockSize() {
		return option.getBlockSize();
	}
	
	public int getBlockCache() {
		return option.getBlockCache();
	}
	
	public int getOperatorType() {
		return OperatorKeyType.getKeyType(option);
	}
}

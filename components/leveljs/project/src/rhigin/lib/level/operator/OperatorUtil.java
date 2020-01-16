package rhigin.lib.level.operator;

import org.maachang.leveldb.operator.LevelOperator;

public class OperatorUtil {
	protected OperatorUtil() {}
	
	/**
	 * オペレータタイプを取得.
	 * 
	 * @param op
	 * @return
	 */
	public static final String getOperatorType(LevelOperator op) {
		if(op != null) {
			switch(op.getOperatorType()) {
			case LevelOperator.LEVEL_MAP: return "object";
			case LevelOperator.LEVEL_LAT_LON: return "latLon";
			case LevelOperator.LEVEL_SEQUENCE: return "sequence";
			case LevelOperator.LEVEL_QUEUE: return "queue";
			}
		}
		return "none";
	}

}

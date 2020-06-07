package rhigin.lib.level;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.maachang.leveldb.JniBuffer;
import org.maachang.leveldb.Lz4;
import org.maachang.leveldb.operator.LevelQueue;
import org.maachang.leveldb.operator.LevelQueue.LevelQueueIterator;

import objectpack.ObjectPack;
import objectpack.SerializableCore;
import rhigin.RhiginException;
import rhigin.lib.level.operator.OperateIterator;
import rhigin.lib.level.operator.Operator;
import rhigin.lib.level.operator.OperatorMode;
import rhigin.lib.level.operator.QueueOperator;
import rhigin.lib.level.operator.SearchOperator;
import rhigin.scripts.ObjectPackOriginCode;
import rhigin.util.ObjectList;

/**
 * LevelJs の バックアップ / リストア.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LevelJsBackup {
	// ObjectPackのRhigin拡張.
	static {
		if(!SerializableCore.isOriginCode()) {
			SerializableCore.setOriginCode(new ObjectPackOriginCode());
		}
	}
	
	/** デフォルトブロックサイズ. **/
	private static final int DEF_BLOCK_SIZE = 128;
	
	private static final String MODE_INDEX_KEY = "@index";
	private static final String MODE_OPERATOR_TYPE = "@operator";
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param core LevelJsCoreオブジェクトを設定します.
	 * @param name バックアップするオペレータ名を設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, LevelJsCore core, String name) {
		return backup(out, core, name, DEF_BLOCK_SIZE);
	}
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param core LevelJsCoreオブジェクトを設定します.
	 * @param name バックアップするオペレータ名を設定します.
	 * @param blockSize ブロックサイズを設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, LevelJsCore core, String name, int blockSize) {
		try {
			Operator op = core.get(name);
			if(op == null) {
				throw new RhiginException("The operator with the specified name '"
						+ name + "' does not exist.");
			}
			// 読み込みロックでバックアップ処理.
			ReadWriteLock rwLock = core.getManager().getReadWriteLock();
			rwLock.readLock().lock();
			try {
				// オペレータ名とオペレータモードを保存.
				Map mode = op.getMode().get();
				// 検索系のオペレータの場合は、インデックス定義も保存.
				if(op instanceof SearchOperator) {
					Object[] val;
					SearchOperator sp = (SearchOperator) op;
					List indexList = new ObjectList();
					List<String> indexNames = sp.indexs();
					int indexLen = indexNames.size();
					for(int i = 0 ; i < indexLen; i ++) {
						val = sp.getIndexConfig(indexNames.get(i));
						indexList.add(val[0]); // インデックスカラムタイプ.
						indexList.add(val[1]); // インデックスカラム名.
						val = null;
					}
					// インデックス定義は mode 内に indexとして保存.
					mode.put(MODE_INDEX_KEY, indexList);
				}
				// オペレータのモードをセット.
				mode.put(MODE_OPERATOR_TYPE, op.getOperatorType());
				List head = new ObjectList(name, mode);
				byte[] b = ObjectPack.packB(head);
				out.write(intToBinary(b.length));
				out.write(b);
				b = null; head = null;
				if(op instanceof QueueOperator) {
					return backup(out, (QueueOperator)op, blockSize);
				} else {
					return backup(out, ((SearchOperator)op).cursor(false), blockSize);
				}
			} finally {
				rwLock.readLock().unlock();
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param itr バックアップ対象のiteratorを設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, Iterator itr) {
		return backup(out, itr, DEF_BLOCK_SIZE);
	}
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param itr バックアップ対象のiteratorを設定します.
	 * @param blockSize ブロックサイズを設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, Iterator itr, int blockSize) {
		int type = -1;
		JniBuffer originBuffer = null, lz4Buffer = null;
		try {
			if(itr instanceof OperateIterator) {
				type = 0;
			} else if(itr instanceof LevelQueueIterator) {
				type = 1;
			} else {
				throw new RhiginException("Iterator not supported.");
			}
			Map v;
			Object k;
			originBuffer = new JniBuffer();
			lz4Buffer = new JniBuffer();
			long ret = 0L;
			List value = new ObjectList();
			while(itr.hasNext()) {
				v = (Map)itr.next();
				if(type == 0) {
					k = ((OperateIterator)itr).key();
				} else {
					k = ((LevelQueueIterator)itr).getKey();
				}
				value.add(k);
				value.add(v);
				k = null;
				v = null;
				if(blockSize <= value.size()) {
					ret += toLz4(out, value, originBuffer, lz4Buffer);
				}
			}
			if(value.size() > 0) {
				ret += toLz4(out, value, originBuffer, lz4Buffer);
			}
			originBuffer.clear();
			originBuffer = null;
			lz4Buffer.clear();
			lz4Buffer = null;
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(originBuffer != null) {
				try {
					originBuffer.close();
				} catch(Exception e) {
				}
			}
			if(lz4Buffer != null) {
				try {
					lz4Buffer.close();
				} catch(Exception e) {
				}
			}
			if(type == 0) {
				((OperateIterator)itr).close();
			} else if(type == 1) {
				((LevelQueueIterator)itr).close();
			}
		}
	}
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param queue バックアップ対象のQueueを設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, QueueOperator queue) {
		return backup(out, queue, DEF_BLOCK_SIZE);
	}
	
	/**
	 * バックアップ.
	 * @param out 出力先のOutputStreamを設定します.
	 * @param queue バックアップ対象のQueueを設定します.
	 * @param blockSize ブロックサイズを設定します.
	 * @return long バックアップされた件数が返却されます.
	 */
	public static final long backup(OutputStream out, QueueOperator queue, int blockSize) {
		LevelQueueIterator itr = null;
		try {
			itr = queue.getOrigin().iterator();
			return backup(out, itr, blockSize);
		} finally {
			if(itr != null) {
				itr.close();
			}
		}
	}
	
	/**
	 * バックアップの復元.
	 * @param core Coreオブジェクトを設定します.
	 * @param in バックアップデータが格納されているInputStreamを設定します.
	 * @return long 復元された件数が返却されます.
	 */
	public static final long restore(LevelJsCore core, InputStream in) {
		try {
			byte[] b = new byte[4];
			int len = in.read(b);
			if(len != 4) {
				throw new RhiginException("The backed up contents are corrupted" +
						" (Failed to get header size).");
			}
			b = new byte[len];
			int hLen = in.read(b);
			if(len != hLen) {
				throw new RhiginException("The backed up contents are corrupted" +
						" (Failed to get header).");
			}
			Object o = ObjectPack.unpackB(b);
			b = null;
			if(!(o instanceof List) || ((List)o).size() != 2
				|| !(((List)o).get(0) instanceof String)
				|| !(((List)o).get(1) instanceof Map)) {
				throw new RhiginException("The backed up contents are corrupted" +
						" (Failed to get header).");
			}
			List head = (List)o;
			o = null;
			boolean indexFlag = false;
			String name = (String)head.get(0);
			Map mode = (Map)head.get(1);
			OperatorMode opMode = new OperatorMode(mode);
			String operatorType = (String)mode.get(MODE_OPERATOR_TYPE);
			// オペレータを再作成.
			if(Operator.OBJECT.equals(operatorType)) {
				if(core.contains(name)) {
					core.delete(name);
				}
				core.createObject(name, opMode);
				indexFlag = true;
			} else if(Operator.LAT_LON.equals(operatorType)) {
				if(core.contains(name)) {
					core.delete(name);
				}
				core.createLatLon(name, opMode);
				indexFlag = true;
			} else if(Operator.SEQUENCE.equals(operatorType)) {
				if(core.contains(name)) {
					core.delete(name);
				}
				core.createSequence(name, opMode);
				indexFlag = true;
			} else if(Operator.QUEUE.equals(operatorType)) {
				if(core.contains(name)) {
					core.delete(name);
				}
				core.createQueue(name, opMode);
			} else {
				throw new RhiginException("Unknown operator type: " + operatorType);
			}
			Operator op = core.get(name);
			// managerをロックして復元.
			ReadWriteLock rwLock = core.getManager().getReadWriteLock();
			rwLock.writeLock().lock();
			try {
				// インデックス利用可能な場合は、インデックスを作成.
				if(indexFlag) {
					List indexList = (List)mode.get(MODE_INDEX_KEY);
					if(indexList != null && indexList.size() > 0) {
						SearchOperator sop = (SearchOperator)op;
						len = indexList.size();
						for(int i = 0; i < len; i += 2) {
							sop.createIndex("" + indexList.get(i), "" + indexList.get(i + 1));
						}
					}
				}
				return restore(op, in);
			} finally {
				rwLock.writeLock().unlock();
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
	
	/**
	 * バックアップの復元.
	 * @param out 出力先のOperatorオブジェクトを設定します.
	 * @param in バックアップデータが格納されているInputStreamを設定します.
	 * @return long 復元された件数が返却されます.
	 */
	public static final long restore(Operator out, InputStream in) {
		JniBuffer originBuffer = null, lz4Buffer = null;
		try {
			int len;
			List block;
			originBuffer = new JniBuffer();
			lz4Buffer = new JniBuffer();
			long ret = 0;
			while((block = toOrigin(in, originBuffer, lz4Buffer)) != null) {
				len = block.size();
				// searchOperator.
				if(out instanceof SearchOperator) {
					SearchOperator sout = (SearchOperator)out;
					for(int i = 0; i < len; i += 2) {
						sout.put(block.get(i), block.get(i + 1));
					}
				// queueOperator.
				} else {
					LevelQueue qout = ((QueueOperator)out).getOrigin();
					for(int i = 0; i < len; i += 2) {
						qout.set(block.get(i), block.get(i + 1));
					}
				}
				ret += len >> 1;
			}
			originBuffer.clear();
			originBuffer = null;
			lz4Buffer.clear();
			lz4Buffer = null;
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(originBuffer != null) {
				try {
					originBuffer.close();
				} catch(Exception e) {
				}
			}
			if(lz4Buffer != null) {
				try {
					lz4Buffer.close();
				} catch(Exception e) {
				}
			}
		}
	}
	
	// intをバイナリに変換.
	private static final byte[] intToBinary(int len) {
		return new byte[] {
				(byte)(len & 0x000000ff),
				(byte)((len & 0x0000ff00) >> 8),
				(byte)((len & 0x00ff0000) >> 16),
				(byte)((len & 0xff000000) >> 24)
		};
	}
	
	// バイナリをintに変換.
	private static final int binaryToInt(byte[] b, int off) {
		return (int)(b[off] & 0x000000ff
				| ((b[off+1] & 0x000000ff) << 8)
				| ((b[off+2] & 0x000000ff) << 16)
				| ((b[off+3] & 0x000000ff) << 24));
	}
	
	
	// 圧縮して、OutputStreamに出力.
	private static final int toLz4(
		final OutputStream out, final List value, final JniBuffer originBuffer, final JniBuffer lz4Buffer)
		throws Exception {
		// valueのデータ長が０の場合は、データ長０を書き込んで処理終了.
		if(value == null || value.size() == 0) {
			out.write(intToBinary(0));
			return 0;
		}
		// ObjectPackでリストをバイナリ変換.
		byte[] b = ObjectPack.packB(value);
		value.clear();
		// ObjectPackで変換したバイナリをセット.
		originBuffer.position(0);
		originBuffer.setBinary(b);
		b = null;
		// Lz4圧縮.
		lz4Buffer.position(0);
		Lz4.compress(lz4Buffer, originBuffer);
		originBuffer.position(0);
		b = lz4Buffer.getBinary();
		lz4Buffer.position(0);
		final int ret = b.length;
		// 圧縮されたデータ長をセット.
		out.write(intToBinary(ret));
		// 圧縮されたデータをセット.
		out.write(b);
		b = null;
		return ret;
	}
	
	// LZ4で圧縮されたブロックデータを解凍して、Key, Value変換.
	private static final List toOrigin(
		final InputStream in, final JniBuffer originBuffer, final JniBuffer lz4Buffer)
		throws Exception {
		int len, blen;
		byte[] b4 = new byte[4];
		if((len = in.read(b4)) == -1) {
			return null;
		} else if(len != 4 || (blen = binaryToInt(b4, 0)) < 0) {
			throw new RhiginException("The backed up contents are corrupted" +
					" (block size acquisition failed).");
		} else if(blen == 0) {
			return null;
		}
		b4 = null;
		byte[] b = new byte[blen];
		len = in.read(b, 0, blen);
		if(len != blen) {
			throw new RhiginException("The backed up contents are corrupted" +
					" (block data acquisition failed).");
		}
		lz4Buffer.position(0);
		originBuffer.position(0);
		// Lz4の解凍.
		lz4Buffer.setBinary(b);
		b = null;
		Lz4.decompress(originBuffer, lz4Buffer);
		lz4Buffer.position(0);
		// 解凍したバイナリをObjectPackでアンパック.
		b = originBuffer.getBinary();
		originBuffer.position(0);
		return (List)ObjectPack.unpackB(b, null);
	}
}

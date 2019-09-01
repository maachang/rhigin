package rhigin.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import rhigin.util.AtomicNumber;

/**
 * 基本Nio要素.
 */
public abstract class NioElement {
	protected volatile boolean connectionFlag = false;
	protected final AtomicNumber ops = new AtomicNumber(SelectionKey.OP_READ);
	protected NioSelector selector;
	protected SelectionKey key;
	protected InetSocketAddress access;
	protected NioReadBuffer buffer = new NioReadBuffer();
	
	protected NioSendLess less = new NioSendLess();
	protected byte[] dataBinary = null;

	protected LinkedList<InputStream> sendDataList = new LinkedList<InputStream>();

	public NioElement() {}

	/**
	 * オブジェクトクリア.
	 */
	public void clear() {
		connectionFlag = false;
		dataBinary = null;
		selector = null;
		access = null;
		if (less != null) {
		less.clear();
		less = null;
		}
		InputStream in;
		
		while(!sendDataList.isEmpty()) {
		in = sendDataList.pop();
		try {
			in.close();
		 } catch(Exception e) {}
		}
		if (buffer != null) {
		buffer.clear();
		buffer = null;
		}
		if (key != null) {
		key.attach(null);
		NioUtil.destroyKey(key);
		key = null;
		}
	}

	/**
	 * 要素が有効かチェック.
	 * 
	 * @return boolean [true]の場合、接続中です.
	 */
	public boolean isConnection() {
		return connectionFlag;
	}

	/**
	 * 対象要素と、対象Socket情報を、セレクタに登録.
	 * @param selector 登録先のセレクタを設定します.
	 * @param channel 対象のソケットチャネルを設定します.
	 * @param op 対象の処理モードを設定します.
	 * @return SelectionKey 生成されたSelectionKeyを返却します.
	 * @exception Exception O例外.
	 */
	public SelectionKey registor(NioSelector selector, SocketChannel channel, int op)
		throws Exception {
		SelectionKey ret = selector.register(channel, op, this);
		this.key = ret;
		this.selector = selector;
		this.connectionFlag = true;
		this.access = (InetSocketAddress)channel.getRemoteAddress();
		return ret;
	}

	/**
	 * SelectedKeyを取得.
	 * @return SelectionKey SelectionKeyが返却されます.
	 */
	public SelectionKey getKey() {
		return key;
	}

	/**
	 * Selectorを取得.
	 * @return NioSelector Selectorが返却されます.
	 */
	public NioSelector getSelector() {
		return selector;
	}

	/**
	 * 受信バッファを取得.
	 * @return HttpReadBuffer 受信バッファが返却されます.
	 */
	public NioReadBuffer getBuffer() {
		return buffer;
	}

	/**
	 * SendLessオブジェクトを取得.
	 * @return SendLess オブジェクトが返却されます.
	 */
	public NioSendLess getSendLess() {
		return less;
	}

	/**
	 * SendDataオブジェクトを設定.
	 * @param in 対象の送信データを設定します.
	 * @exception IOException I/O例外.
	 */
	public void setSendData(InputStream in)
		throws IOException {
		sendDataList.offer(in);
	}
	
	/**
	 * 書き込み開始を行う.
	 * @throws IOException
	 */
	public void startWrite() throws IOException {
		this.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	/**
	 * SendDataオブジェクトを取得.
	 * @return InputStream オブジェクトが返却されます.
	 */
	public InputStream getSendData() {
		if(!sendDataList.isEmpty()) {
			return sendDataList.peek();
		}
		return null;
	}
	
	/**
	 * 現在のSendDataオブジェクトを利用終了.
	 * @return InputStream オブジェクトが返却されます.
	 */
	public InputStream removeSendData() {
		if(!sendDataList.isEmpty()) {
			return sendDataList.pop();
		}
		return null;
	}

	/**
	 * データお一時受け取り用バイナリを設定.
	 * @param buf SendDataを一時的に受け取るバイナリを取得します.
	 */
	public void setSendTempBinary(byte[] buf) {
		dataBinary = buf;
	}

	/**
	 * データ一時受け取り用バイナリを取得.
	 * @return byte[] SendDataを一時的に受け取るバイナリを取得します.
	 */
	public byte[] getSendTempBinary() {
		return dataBinary;
	}

	/**
	 * interOpsの変更.
	 * @param ops 対象のOpsを設定します.
	 * @exception IOException I/O例外.
	 */
	public void interestOps(int ops) throws IOException {
		this.ops.set(ops);
		key.interestOps(ops);
		selector.wakeup();
	}

	/**
	 * 現在のinterOpsを取得.
	 * @return int 対象のOpsが返却されます.
	 */
	public int interestOps() {
		return ops.get();
	}
	
	/**
	 * アクセス先の接続情報を取得.
	 * @return InetSocketAddress リモートアクセス情報が返却されます.
	 */
	public InetSocketAddress getRemoteAddress() {
		return access;
	}
}

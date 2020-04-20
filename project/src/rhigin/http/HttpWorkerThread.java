package rhigin.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import objectpack.ObjectPack;
import objectpack.SerializableCore;
import rhigin.RhiginConstants;
import rhigin.RhiginException;
import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.net.NioReadBuffer;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.Json;
import rhigin.scripts.ObjectPackOriginCode;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RandomFunction;
import rhigin.util.Alphabet;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.FileUtil;
import rhigin.util.Wait;
import rhigin.util.Xor128;

/**
 * ワーカースレッド.
 */
public class HttpWorkerThread extends Thread {
	// ObjectPackのRhigin拡張.
	static {
		if(!SerializableCore.isOriginCode()) {
			SerializableCore.setOriginCode(new ObjectPackOriginCode());
		}
	}
	
	private static final Log LOG = LogFactory.create();
	private static final int TIMEOUT = 1000;
	private static final byte[] BLANK_BINARY = new byte[0];

	private final int no;
	private final Queue<HttpElement> queue;
	private final Wait wait;
	private final MimeType mime;
	private final byte[] tmpBuffer;
	private final Xor128 xor128;

	private volatile boolean stopFlag = true;
	private volatile boolean endThreadFlag = false;

	public HttpWorkerThread(HttpInfo info, MimeType m, int n) {
		no = n;
		mime = m;

		// HttpElement受付用.
		queue = new ConcurrentLinkedQueue<HttpElement>();
		wait = new Wait();

		// テンポラリバッファを生成.
		// この情報は、ワーカースレッドで処理される、各HttpElement.sendTempBinaryで利用される.
		tmpBuffer = new byte[info.getByteBufferLength()];

		// ランダムオブジェクト.
		xor128 = new Xor128(System.nanoTime());
	}

	/**
	 * ワーカースレッド登録.
	 *  
	 * @param em
	 * @throws IOException
	 */
	public void register(HttpElement em) throws IOException {
		// ワーカースレッドに登録.
		em.setWorkerNo(no);
		em.setSendTempBinary(tmpBuffer);
		signal(em);
	}

	/**
	 * シグナル呼び出し.
	 * 
	 * @param em
	 * @throws IOException
	 */
	public void signal(HttpElement em) throws IOException {
		queue.offer(em);
		wait.signal();
	}

	public void startThread() {
		stopFlag = false;
		setDaemon(true);
		start();
	}

	public void stopThread() {
		stopFlag = true;
	}

	public boolean isStopThread() {
		return stopFlag;
	}

	public boolean isEndThread() {
		return endThreadFlag;
	}

	public void run() {
		LOG.info(" * start rhigin workerThread(" + no + ").");

		// ワーカー単位でランダムオブジェクトをセット.
		RandomFunction.init(xor128);

		// 実行処理.
		ThreadDeath td = execute();

		LOG.info(" * stop rhigin workerThread(" + no + ").");
		endThreadFlag = true;
		if (td != null) {
			throw td;
		}
	}

	private final ThreadDeath execute() {
		HttpElement em = null;
		ThreadDeath ret = null;
		boolean endFlag = false;
		while (!endFlag && !stopFlag) {
			try {
				while (!endFlag && !stopFlag) {
					if ((em = queue.poll()) == null) {
						wait.await(TIMEOUT);
						continue;
					}
					if (executionRequest(em, tmpBuffer, xor128)) {
						try {
							executeScript(em, mime);
						} finally {
							// 大容量Body受付情報が存在する場合は、後片付けをする.
							if (em.isHttpPostBodyFile()) {
								HttpPostBodyFile f = em.getHttpPostBodyFile(null);
								f.close();
							}
						}
					}
					em = null;
				}
			} catch (Throwable to) {
				if (em != null) {
					em.clear();
				}
				LOG.debug("error", to);
				if (to instanceof InterruptedException) {
					endFlag = true;
				} else if (to instanceof ThreadDeath) {
					endFlag = true;
					ret = (ThreadDeath) to;
				}
			}
		}
		return ret;
	}

	/** Request処理. **/
	private static final boolean executionRequest(HttpElement em, byte[] tmpBuffer, Xor128 xor128) throws IOException {

		// 既に受信処理が終わっている場合.
		if (em.isEndReceive()) {
			return true;
		}

		// 受信バッファに今回分の情報をセット.
		final NioReadBuffer buffer = em.getBuffer();

		// Httpリクエストを取得.
		Request request = em.getRequest();
		if (request == null) {

			// HTTPリクエストが存在しない場合は、新規作成.
			int endPoint = Analysis.endPoint(buffer);
			if (endPoint == -1) {
				// 受信途中の場合.
				return false;
			}
			request = Analysis.getRequest(buffer, endPoint);
			em.setRequest(request);
			request.setElement(em);
		}

		final String method = request.getMethod();

		// OPTIONの場合は、Optionヘッダを返却.
		// if ("OPTIONS".equals(method)) {
		if (Alphabet.eq("options", method)) {

			// Optionsレスポンス.
			sendOptions(request.isMinHeader(), em);
			return false;
		}
		// POSTの場合は、ContentLength分の情報を取得.
		// else if ("POST".equals(method)) {
		else if (Alphabet.eq("post", method)) {
			// ContentLengthを取得.
			long contentLength = request.getContentLength();
			if (contentLength <= -1L) {
				// 存在しない場合はコネクション強制クローズ.
				// chunkedの受信は対応しない.
				// 通信クローズ.
				if (em != null) {
					em.clear();
				}
				return false;
			}

			// 大容量ファイル受信が要求されてる場合.
			if (HttpConstants.POST_FILE_OUT_CONTENT_TYPE.equals(request.getString("content-type"))) {
				// 受信データが存在する場合.
				HttpPostBodyFile file = em.getHttpPostBodyFile(xor128);
				if (buffer.size() > 0) {
					int len;
					final byte[] buf = tmpBuffer;
					while ((len = buffer.read(buf)) > 0) {
						file.write(buf, len);
					}
					// 受信完了の場合.
					if (file.getFileLength() >= contentLength) {
						file.endWrite();
						request.setBody(null);
						em.setEndReceive(true);
						em.destroyBuffer();
						return true;
					}
				}
				// PostのBody受信中.
				return false;
			}

			// 制限以上のBodyデータを超える場合は通信切断する.
			if (contentLength > HttpConstants.MAX_CONTENT_LENGTH) {
				// 通信クローズ.
				if (em != null) {
					em.clear();
				}
				return false;
			}

			// Body情報が受信完了かチェック.
			if (buffer.size() >= contentLength) {
				byte[] body = new byte[(int) contentLength];
				buffer.read(body);
				request.setBody(body);
			} else {
				// PostのBody受信中.
				return false;
			}
		}
		// POST, GET以外の場合は処理しない.
		// else if (!"GET".equals(method)) {
		else if (!Alphabet.eq("get", method)) {
			// 通信クローズ.
			if (em != null) {
				em.clear();
			}
			return false;
		}

		// 受信完了.
		em.setEndReceive(true);
		em.destroyBuffer();
		return true;
	}

	/** Response処理. **/
	@SuppressWarnings("rawtypes")
	private static final void executeScript(HttpElement em, MimeType mime) {
		// 既に送信処理が終わっている場合.
		if (em.isEndSend()) {
			return;
		}
		Request req = em.getRequest();
		boolean minHeader = req.isMinHeader();
		em.setRequest(null);
		try {

			// gzipに対応しているかチェック.
			boolean gzip = isGzip(req);

			// アクセス対象のパスを取得.
			String path = HttpConstants.ACCESS_PATH + getPath(req.getUrl());

			// main.jsが存在する場合は、urlに関係なくmain.jsを実行.
			if (!FileUtil.isFile(path + RhiginConstants.MAIN_JS)) {
				// 最後が / で終わっている場合.
				if (path.endsWith("/")) {
					path += "index";
				}

				// 実行ファイルのパスが存在しない場合.
				// ただ / で設定された場合は優先的に [/index.html or /index.htm] を探して、存在する場合はそちらを優先する.
				if ((req.getUrl().endsWith("/") && (FileUtil.isFile(path + ".html") || FileUtil.isFile(path + ".htm")))
						|| !FileUtil.isFile(path + ".js")) {
					boolean useFlag = false;

					// 普通にファイル名として存在するかチェック.
					if (gzip) {
						// gzip対応.
						if (path.endsWith("/index")) {
							if (FileUtil.isFile(path + ".html.gz")) {
								path += ".html.gz";
								useFlag = true;
							} else if (FileUtil.isFile(path + ".html")) {
								path += ".html";
								useFlag = true;
							} else if (FileUtil.isFile(path + ".htm.gz")) {
								path += ".htm.gz";
								useFlag = true;
							} else if (FileUtil.isFile(path + ".htm")) {
								path += ".htm";
								useFlag = true;
							}
						} else if (FileUtil.isFile(path + ".gz")) {
							path += ".gz";
							useFlag = true;
						} else if (FileUtil.isFile(path)) {
							// ただし[.js]に対しては、サーバ実行なので、中身が見れないようにする.
							// 例外として[/@file.js] のように、ファイルの頭にアットマークの場合は表示対象とする.
							if (path.endsWith(".js") && path.indexOf("/@") == -1) {
								useFlag = false;
							} else {
								useFlag = true;
							}
						}
					} else {
						// gzip非対応.
						if (path.endsWith("/index")) {
							if (FileUtil.isFile(path + ".html")) {
								path += ".html";
								useFlag = true;
							} else if (FileUtil.isFile(path + ".htm")) {
								path += ".htm";
								useFlag = true;
							}
						} else if (FileUtil.isFile(path)) {
							// ただし[.js]に対しては、サーバ実行なので、中身が見れないようにする.
							// 例外として[/@file.js] のように、ファイルの頭にアットマークの場合は表示対象とする.
							if (path.endsWith(".js") && path.indexOf("/@") == -1) {
								useFlag = false;
							} else {
								useFlag = true;
							}
						}
					}
					if (!useFlag) {
						// 存在しない場合.
						errorResponse(minHeader, em, 404);
					} else {
						// 存在する場合は、ファイル転送.
						Response res = new Response();
						res.setStatus(200);
						sendFile(minHeader, gzip, path, em, mime, 200, res);
					}
					return;
				}
				path += ".js";
			} else {
				// main.jsを実行させる.
				path = path + RhiginConstants.MAIN_JS;
			}

			String method = req.getMethod();
			Object params = null;
			if ("GET".equals(method)) {
				params = getParams(req.getUrl());
			} else if ("POST".equals(method)) {
				params = postParams(req);
			}
			// パラメータがnullの場合は、空のパラメータをセット.
			if (params == null) {
				params = new Params();
				// パラメータがMapの場合.
			} else if (params instanceof Map) {
				params = new Params((Map) params);
			}

			try {
				
				// レスポンス生成.
				Response res = new Response();

				// コンテキスト生成・設定.
				RhiginContext context = new RhiginContext();
				context.setAttribute("params", params);
				context.setAttribute("request", req);
				context.setAttribute("response", res);
				context.setAttribute(redirect.getName(), redirect);
				context.setAttribute(error.getName(), error);
				
				Object ret = "";
				try {
					// スクリプトの実行.
					ret = ExecuteScript.execute(context,
							CompileCache.getCache().get(path, ScriptConstants.HEADER, ScriptConstants.FOOTER).getScript());
				} catch (Redirect redirect) {
					redirectResponse(minHeader, em, redirect);
					return;
				} catch (RhiginException rhiginException) {
					// HTTPステータスが500エラー以上の場合のみ、エラー表示.
					if (rhiginException.getStatus() >= 500) {
						// スクリプトエラーを表示.
						LOG.error("scriptError:" + req.getUrl(), rhiginException);
					}
					errorResponse(minHeader, em, rhiginException.getStatus(), rhiginException.getMessage());
					return;
				} finally {
					ExecuteScript.clearCurrentRhiginContext();
				}
				
				// 戻り値がInputStreamの場合.
				if (ret instanceof InputStream) {
					// バイナリ返却.
					sendResponse(minHeader, em, res.getStatus(), res, (InputStream) ret);
				// 戻り値がInputStreamでない場合.
				} else {
					// 戻り値が無い場合.
					if (ret == null || (ret instanceof String && ((String) ret).isEmpty())) {
						ret = "";
						// success形式で返却.
						successResponse(minHeader, false, em, res.getStatus(), res, ret);
					// minHeader で ContentTypeが設定されていない場合.
					} else if(minHeader && res.get("Content-Type") == Response.DEFAULT_CONTENT_TYPE) {
						// rhiginの名前でコンテンツタイプをセット.
						res.put("Content-Type", MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE);
						// ObjectPackでjsnappy圧縮でバイナリ変換.
						ret = ObjectPack.packB(ret);
						successResponseObjectPack(minHeader, em, res.getStatus(), res, (byte[])ret);
					} else {
						// success形式で返却.
						successResponse(minHeader, gzip, em, res.getStatus(), res, ret);
					}
				}
				
			} finally {
				
				// スクリプト終了処理.
				ExecuteScript.callEndScripts(false);
			}
			
		} catch (RhiginException re) {
			if (!em.isEndSend()) {
				try {
					errorResponse(minHeader, em, re.getStatus(), re.getMessage());
				} catch (Exception ee) {
				}
			}
			LOG.error("error", re);
		} catch (Exception e) {
			if (!em.isEndSend()) {
				try {
					errorResponse(minHeader, em, 500, e.getMessage());
				} catch (Exception ee) {
				}
			}
			LOG.error("error", e);
		}
	}

	/** 要求パス取得. **/
	private static final String getPath(String url) {
		int p = url.indexOf("?");
		if (p != -1) {
			url = url.substring(0, p);
		}
		if (url.endsWith("/")) {
			url += "index";
		}
		return url;
	}

	/** GETパラメータを取得. **/
	private static final Object getParams(String url) throws IOException {
		int p = url.indexOf("?");
		if (p != -1) {
			return Analysis.paramsAnalysis(url, p + 1);
		}
		return null;
	}

	/** POSTパラメータを取得. **/
	private static final Object postParams(Request req) throws IOException {
		// 大容量ファイルの受け取りの場合は、パラメータ解析を行わない.
		if (HttpConstants.POST_FILE_OUT_CONTENT_TYPE.equals(req.get("Content-Type"))) {
			return null;
		}
		String v = req.getBodyText();
		req.setBody(null);

		// Body内容がJSON形式の場合.
		String contentType = (String) req.get("content-type");
		if (contentType.indexOf("application/json") == 0) {
			return Json.decode(v);
		} else if ("application/x-www-form-urlencoded".equals(contentType)) {
			return Analysis.paramsAnalysis(v, 0);
		} else {
			return Analysis.paramsAnalysis(v, 0);
		}
	}
	
	/** 正常結果をObjectPackで返却. **/
	private static final void successResponseObjectPack(boolean minHeader, HttpElement em, int status, Response response, byte[] value)
		throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		em.setSendBinary(stateResponse(minHeader, status, response, value, (long)value.length));
	}

	/** 正常結果をJSON返却. **/
	private static final void successResponse(boolean minHeader, boolean gzip, HttpElement em, int status, Response response, Object value)
			throws IOException {
		if (value == null) {
			sendResponse(minHeader, gzip, em, status, response, "{}");
		} else {
			sendResponse(minHeader, gzip, em, status, response, Json.encode(value));
		}
	}

	/** GZIP返却許可チェック. **/
	private static final boolean isGzip(Request req) throws IOException {
		String n = (String) req.get("accept-encoding");
		if (n == null || n.indexOf("gzip") == -1) {
			return false;
		}
		return true;
	}

	/** Options送信. **/
	private static final void sendOptions(boolean minHeader, HttpElement em) throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if(minHeader) {
			em.setSendBinary(OPSIONS_RESPONSE_M);
		} else {
			em.setSendBinary(OPSIONS_RESPONSE);
		}
	}

	/** ファイル送信. **/
	private static final void sendFile(boolean minHeader, boolean gzip, String fileName, HttpElement em, MimeType mime, int status,
			Response header) throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if (gzip && fileName.endsWith(".gz")) {
			header.put("Content-Encoding", "gzip");
			header.put("Content-Type", mime.getUrl(fileName.substring(0, fileName.length() - 3)));
		} else {
			header.put("Content-Type", mime.getUrl(fileName.substring(0, fileName.length())));
		}
		try {
			em.setSendData(new ByteArrayInputStream(
					stateResponse(minHeader, status, header, BLANK_BINARY, FileUtil.getFileLength(fileName))));
			em.setSendData(new FileInputStream(fileName));
			em.startWrite();
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/** [byte[]]レスポンス送信. **/
	private static final void sendResponse(boolean minHeader, boolean gzip, HttpElement em, int status, Response header, String body)
			throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if (gzip && body.length() > HttpConstants.NOT_GZIP_BODY_LENGTH) {
			header.put("Content-Encoding", "gzip");
			em.setSendBinary(stateResponse(minHeader, status, header, pressGzip(body), -1L));
		} else {
			em.setSendBinary(stateResponse(minHeader, status, header, body));
		}
	}

	/** [inputStream]レスポンス送信. **/
	private static final void sendResponse(boolean minHeader, HttpElement em, int status, Response header, InputStream body)
			throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		try {
			Long len;
			// 直接ファイルの場合は、そのまま転送.
			if (body instanceof FileInputStream) {
				len = (long) body.available();
				// それ以外の場合はchunked転送.
			} else {
				len = null;
				body = new HttpChunkedInputStream(Http.getHttpInfo().getByteBufferLength(), body);
			}
			em.setSendData(new ByteArrayInputStream(stateResponse(minHeader, status, header, BLANK_BINARY, len)));
			em.setSendData(body);
			em.startWrite();
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/** リダイレクト送信. **/
	private static final void redirectResponse(boolean minHeader, HttpElement em, Redirect redirect) throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		Response res = new Response();
		res.put("Location", redirect.getUrl());
		em.setSendBinary(stateResponse(minHeader, redirect.getStatus(), res, ""));
	}

	/** GZIP圧縮. **/
	private static final byte[] pressGzip(String body) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		GZIPOutputStream go = new GZIPOutputStream(bo);
		go.write(body.getBytes("UTF8"));
		go.flush();
		go.finish();
		go.close();
		return bo.toByteArray();
	}

	/** エラーレスポンスを送信. **/
	private static final void errorResponse(boolean minHeader, HttpElement em, int status) throws IOException {
		errorResponse(minHeader, em, status, null);
	}

	/** エラーレスポンスを送信. **/
	@SuppressWarnings("rawtypes")
	private static final void errorResponse(boolean minHeader, HttpElement em, int status, String message) throws IOException {
		if (message == null) {
			message = Status.getMessage(status);
		}
		// コーテーション系の情報は大文字に置き換える.
		message = Converter.changeString(message, "\"", "”");
		message = Converter.changeString(message, "\'", "’");

		// カッコ系の情報も大文字に置き換える.
		message = Converter.changeString(message, "[", "［");
		message = Converter.changeString(message, "]", "］");
		message = Converter.changeString(message, "{", "｛");
		message = Converter.changeString(message, "}", "｝");
		
		final Map ret = new ArrayMap("message", message);

		Response res = new Response();

		// 処理結果を返却.
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if(minHeader) {
			try {
				// ObjectPackでバイナリ変換.
				res.put("Content-Type", MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE);
				successResponseObjectPack(minHeader, em, res.getStatus(), res, (byte[])ObjectPack.packB(ret));
			} catch(Exception e) {
				// 何らかで失敗したら、返却はJSON形式で.
				res.remove("Content-Length");
				em.setSendBinary(stateResponse(minHeader, status, res, Json.encode(ret)));
			}
			return;
		}
		em.setSendBinary(stateResponse(minHeader, status, res, Json.encode(ret)));
	}

	/** ステータス指定Response返却用バイナリの生成. **/
	private static final byte[] stateResponse(boolean minHeader, int state, Response header, String b) throws IOException {
		return stateResponse(minHeader, state, header, b.getBytes("UTF8"), -1L);
	}

	/** ステータス指定Response返却用バイナリの生成. **/
	private static final byte[] stateResponse(boolean minHeader, int state, Response header, byte[] b, Long contentLength)
			throws IOException {
		if (contentLength != null && contentLength == -1L) {
			contentLength = (long) b.length;
		}
		final byte[] stateBinary = new StringBuilder(String.valueOf(state)).append(" ").append(Status.getMessage(state))
				.toString().getBytes("UTF8");

		StringBuilder buf = new StringBuilder(Response.headers(header));
		if (contentLength == null) {
			// content-lengthが存在しない場合はchunked転送.
			buf.append("Transfer-Encoding:chunked\r\n");
		} else {
			buf.append("Content-Length:").append(contentLength).append("\r\n");
		}
		buf.append("\r\n");
		final byte[] foot = buf.toString().getBytes("UTF8");
		buf = null;

		int pos = 0;
		byte[] ret = null;
		if(!minHeader) {
			int all = STATE_RESPONSE_1.length + stateBinary.length + STATE_RESPONSE_2.length + foot.length + b.length;
			ret = new byte[all];
			System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
			pos += STATE_RESPONSE_1.length;
			System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
			pos += stateBinary.length;
			System.arraycopy(STATE_RESPONSE_2, 0, ret, pos, STATE_RESPONSE_2.length);
			pos += STATE_RESPONSE_2.length;
			System.arraycopy(foot, 0, ret, pos, foot.length);
			pos += foot.length;
			System.arraycopy(b, 0, ret, pos, b.length);
		} else {
			int all = STATE_RESPONSE_1.length + stateBinary.length + STATE_RESPONSE_2_M.length + foot.length + b.length;
			ret = new byte[all];
			System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
			pos += STATE_RESPONSE_1.length;
			System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
			pos += stateBinary.length;
			System.arraycopy(STATE_RESPONSE_2_M, 0, ret, pos, STATE_RESPONSE_2_M.length);
			pos += STATE_RESPONSE_2_M.length;
			System.arraycopy(foot, 0, ret, pos, foot.length);
			pos += foot.length;
			System.arraycopy(b, 0, ret, pos, b.length);
		}
		return ret;
	}

	/** Optionsレスポンス. **/
	private static final byte[] OPSIONS_RESPONSE;
	private static final byte[] OPSIONS_RESPONSE_M;

	/** ステータス指定レスポンス. **/
	private static final byte[] STATE_RESPONSE_1;
	private static final byte[] STATE_RESPONSE_2;
	private static final byte[] STATE_RESPONSE_2_M;

	static {
		byte[] op;
		byte[] s1;
		byte[] s2;
		byte[] op_m;
		byte[] s2_m;
		try {
			final String serverName = RhiginConstants.NAME + "(" + RhiginConstants.VERSION + ")";
			final String serverName_m = RhiginConstants.NAME + "_m(" + RhiginConstants.VERSION + ")";
			op = ("HTTP/1.1 200 OK\r\n"
					+ "Allow:GET,POST,HEAD,OPTIONS\r\n"
					+ "Cache-Control:no-cache\r\n"
					+ "Pragma:no-cache\r\n"
					+ "Expire:-1\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Access-Control-Allow-Origin:*\r\n"
					+ "Access-Control-Allow-Headers:content-type,X-Accel-Buffering,*\r\n"
					+ "Access-Control-Allow-Methods:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
					+ "Content-Length:0\r\n\r\n"
			).getBytes("UTF8");
			s1 = ("HTTP/1.1 ").getBytes("UTF8");
			s2 = ("\r\n"
					+ "Cache-Control:no-cache\r\n"
					+ "Pragma:no-cache\r\n"
					+ "Expire:-1\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Access-Control-Allow-Origin:*\r\n"
					+ "Access-Control-Allow-Headers:content-type,X-Accel-Buffering,*\r\n"
					+ "Access-Control-Allow-Methods:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
			).getBytes("UTF8");
			
			op_m = ("HTTP/1.1 200 OK\r\n"
					+ "Allow:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName_m + "\r\n"
					+ "Connection:close\r\n"
					+ "Content-Length:0\r\n\r\n"
			).getBytes("UTF8");
			s2_m = ("\r\n"
					+ "Server:" + serverName_m + "\r\n"
					+ "Connection:close\r\n"
			).getBytes("UTF8");
		} catch (Exception e) {
			op = null;
			s1 = null;
			s2 = null;
			op_m = null;
			s2_m = null;
		}
		OPSIONS_RESPONSE = op;
		OPSIONS_RESPONSE_M = op_m;
		STATE_RESPONSE_1 = s1;
		STATE_RESPONSE_2 = s2;
		STATE_RESPONSE_2_M = s2_m;
	}

	// [js]リダイレクト用メソッド.
	private static final RhiginFunction redirect = new RhiginFunction() {
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			int status = 301;
			String url = null;
			if (args.length >= 1) {
				if (args.length >= 2) {
					if (Converter.isNumeric(args[0])) {
						status = Converter.convertInt(args[0]);
						url = "" + args[1];
					} else if (Converter.isNumeric(args[1])) {
						url = "" + args[0];
						status = Converter.convertInt(args[1]);
					} else {
						url = "" + args[0];
						status = Converter.convertInt(args[1]);
					}
				} else {
					url = "" + args[0];
				}
				throw new Redirect(status, url);
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return "redirect";
		}
	};

	// [js]エラー用メソッド.
	private static final RhiginFunction error = new RhiginFunction() {
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			int status = 500;
			String message = null;
			if (args.length >= 1) {
				if (args.length >= 2) {
					if (Converter.isNumeric(args[0])) {
						status = Converter.convertInt(args[0]);
						message = "" + args[1];
					} else if (Converter.isNumeric(args[1])) {
						message = "" + args[0];
						status = Converter.convertInt(args[1]);
					} else {
						message = "" + args[0];
						status = Converter.convertInt(args[1]);
					}
				} else {
					message = "" + args[0];
				}
				throw new HttpException(status, message);
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return "error";
		}
	};
}

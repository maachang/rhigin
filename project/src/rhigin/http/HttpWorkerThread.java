package rhigin.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import rhigin.http.execute.RhiginExecute;
import rhigin.http.execute.RhiginExecuteByAccessKey;
import rhigin.http.execute.RhiginExecuteByJs;
import rhigin.http.execute.RhiginExecuteConstants;
import rhigin.keys.RhiginAccessKeyByFCipher;
import rhigin.keys.RhiginAccessKeyConstants;
import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.net.IpPermission;
import rhigin.net.NetConstants;
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
import rhigin.util.DateConvert;
import rhigin.util.FCipher;
import rhigin.util.FileUtil;
import rhigin.util.Wait;
import rhigin.util.WatchPath;
import rhigin.util.Xor128;

/**
 * ワーカースレッド.
 */
public class HttpWorkerThread extends Thread {
	private static final Map<String, RhiginExecute> EXECUTE_MAN = new ArrayMap<String, RhiginExecute>();
	private static final Log LOG = LogFactory.create();
	private static final int TIMEOUT = 1000;
	private static final byte[] BLANK_BINARY = new byte[0];
	private static final String[] HTML_JS_HEADS = new String[] {"/$", "/@"};
	
	// ObjectPackのRhigin拡張.
	static {
		if(!SerializableCore.isOriginCode()) {
			SerializableCore.setOriginCode(new ObjectPackOriginCode());
		}
		
		// executeManを登録.
		RhiginExecute r;
		// アクセスキーのI/O.
		r = new RhiginExecuteByAccessKey();
		EXECUTE_MAN.put(r.getName().trim(), r);
		// 送信されたjsを実行.
		r = new RhiginExecuteByJs();
		EXECUTE_MAN.put(r.getName().trim(), r);
	}
	
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

	protected final ThreadDeath execute() {
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
								HttpPostBodyFile f = em.getHttpPostBodyFile();
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
	protected static final boolean executionRequest(HttpElement em, byte[] tmpBuffer, Xor128 xor128)
		throws IOException {

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
		if (Alphabet.eq("options", method)) {

			// Optionsレスポンス.
			sendOptions(request, em);
			return false;
		}
		// POSTの場合は、ContentLength分の情報を取得.
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
	
	/** ブラウザ用のレスポンス返却を行うJSのパスであるか判別. **/
	private static final boolean browserJsPath(String path) {
		int len = HTML_JS_HEADS.length;
		for(int i = 0; i < len; i ++) {
			if(path.indexOf(HTML_JS_HEADS[i]) != -1) {
				return true;
			}
		}
		return false;
	}

	/** Response処理. **/
	@SuppressWarnings("rawtypes")
	protected static final void executeScript(HttpElement em, MimeType mime) {
		// 既に送信処理が終わっている場合.
		if (em.isEndSend()) {
			return;
		}
		final WatchPath wp = WatchPath.getInstance();
		final Request req = em.getRequest();
		final boolean minHeader = req.isMinHeader();
		em.setRequest(null);
		try {
			
			// AccessKeyが付与されていてrequestMethodがPOSTの場合.
			String accessKey = req.getHeader(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER);
			if(accessKey != null && Alphabet.eq("post", req.getMethod())) {
				// 暗号を解析.
				byte[] b = RhiginAccessKeyByFCipher.decode(accessKey, req.getBody());
				req.setBody(b);
				b = null;
			}
			
			// gzipに対応しているかチェック.
			boolean gzip = isGzip(req);
			
			// URLパスを取得.
			String urlPath = getPath(req.getUrl());
			
			// Rhigin実行命令の場合.
			if(urlPath.startsWith(RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD)) {
				
				// Rhigin実行命令をセット.
				final String rhiginExecuteUrl = urlPath.substring(RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD.length());
				int p = rhiginExecuteUrl.indexOf("/");
				final String rhiginExecuteName = ((p == -1) ? rhiginExecuteUrl : rhiginExecuteUrl.substring(0, p))
						.toLowerCase();
				// IpPermissionでパーミッションチェック.
				// IpPermissionが存在する場合のみチェック.
				final IpPermission ip = IpPermission.getMainIpPermission();
				if(ip != null) {
					// デフォルトのパーミッション定義は、共通定義を利用.
					String name = NetConstants.IP_PERMISSION_DEFAULT_NAME;
					// RhiginExecute名のパーミッション定義がある場合は、そちらを利用.
					if(ip.isName(RhiginExecuteConstants.IP_PERMISSION_BY_RHIGIN_EXECUTE_HEAD + rhiginExecuteName)) {
						name = RhiginExecuteConstants.IP_PERMISSION_BY_RHIGIN_EXECUTE_HEAD + rhiginExecuteName;
					}
					// パーミッションの範囲外からのアクセスの場合.
					if(!ip.isPermission(name, em.getRemoteAddress().getAddress())) {
						// 許可されていないIPアドレスの場合.
						errorResponse(req, em, 401, "invalid access.");
						return;
					}
				}
				// executeManからRhigin実行命令を取得.
				final RhiginExecute r = EXECUTE_MAN.get(rhiginExecuteName);
				if(r == null) {
					// 存在しない場合.
					errorResponse(req, em, 404, "Unknown URL.");
					return;
				}
				Object ret = null;
				Response res = new Response();
				res.setStatus(200);
				try {
					ret = r.execute(em, req, res, rhiginExecuteUrl);
				} catch (Redirect redirect) {
					// リダイレクト.
					redirectResponse(req, em, redirect);
					return;
				} catch (RhiginException rhiginException) {
					// HTTPステータスが500エラー以上の場合のみ、エラー表示.
					if (rhiginException.getStatus() >= 500) {
						// スクリプトエラーを表示.
						LOG.error("scriptError:" + req.getUrl(), rhiginException);
					}
					errorResponse(req, em, rhiginException.getStatus(), rhiginException.getMessage());
					return;
				}
				
				// 戻り値がInputStreamの場合.
				if (ret instanceof InputStream) {
					// バイナリ返却.
					sendResponse(req, em, res.getStatus(), res, (InputStream) ret);
				// 戻り値がInputStreamでない場合.
				} else {
					// 戻り値が無い場合.
					if (ret == null || (ret instanceof String && ((String) ret).isEmpty())) {
						ret = "";
						// success形式で返却.
						successResponse(req, false, em, res.getStatus(), res, ret);
					// minHeader で ContentTypeが設定されていない場合.
					// コンテンツタイプが設定されてない場合は[Response.DEFAULT_CONTENT_TYPE]が
					// セットされるので、それをオブジェクト比較する。
					} else if(minHeader && res.get("Content-Type") == Response.DEFAULT_CONTENT_TYPE) {
						// rhiginの名前でコンテンツタイプをセット.
						res.put("Content-Type", MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE);
						// ObjectPackでjsnappy圧縮でバイナリ変換.
						ret = ObjectPack.packB(ret);
						successResponseByBinary(req, em, res.getStatus(), res, (byte[])ret);
					} else {
						// success形式で返却.
						successResponse(req, gzip, em, res.getStatus(), res, ret);
					}
				}
				return;
			}

			// アクセス対象のパスを取得.
			String path = FileUtil.getFullPath(HttpConstants.ACCESS_PATH + urlPath);

			// main.jsが存在する場合は、urlに関係なくmain.jsを実行.
			if (!(path.endsWith("/") && wp.isFile(path + RhiginConstants.MAIN_JS))) {
				// 最後が / で終わっている場合.
				if (path.endsWith("/")) {
					path += "index";
				}
				boolean useFlag = false;
				// 普通にファイル名として存在するかチェック.
				if (gzip) {
					// gzip対応.
					if (path.endsWith("/index")) {
						if (wp.isFile(path + ".html.gz")) {
							path += ".html.gz";
							useFlag = true;
						} else if (wp.isFile(path + ".htm.gz")) {
							path += ".htm.gz";
							useFlag = true;
						} else if (wp.isFile(path + ".html")) {
							path += ".html";
							useFlag = true;
						} else if (wp.isFile(path + ".htm")) {
							path += ".htm";
							useFlag = true;
						}
					} else {
						// ただし[.js]に対しては、サーバ実行なので、中身が見れないようにする.
						// 例外としてブラウザのJSとしてアクセス許可されているパスの場合はファイル返却として処理する.
						if (path.endsWith(".js")) {
							if(browserJsPath(path)) {
								if (wp.isFile(path + ".gz")) {
									path += ".gz";
									useFlag = true;
								} else if(wp.isFile(path)) {
									useFlag = true;
								}
							}
						// その他ファイルが存在する場合.
						} else if(wp.isFile(path) || wp.isFile(".gz")) {
							useFlag = true;
						}
					}
				} else {
					// gzip非対応.
					if (path.endsWith("/index")) {
						if (wp.isFile(path + ".html")) {
							path += ".html";
							useFlag = true;
						} else if (wp.isFile(path + ".htm")) {
							path += ".htm";
							useFlag = true;
						}
					} else if(path.endsWith(".js")) {
						// ただし[.js]に対しては、サーバ実行なので、中身が見れないようにする.
						// 例外としてブラウザのJSとしてアクセス許可されているパスの場合はファイル返却として処理する.
						if (browserJsPath(path) && wp.isFile(path)) {
							useFlag = true;
						}
					// その他ファイルが存在する場合.
					} else if(wp.isFile(path)) {
						useFlag = true;
					}
				}
				// 存在する場合は、ファイル転送.
				if (useFlag) {
					Response res = new Response();
					// キャッシュ通信が許可されている場合.
					if(Http.getHttpInfo().isSendFileCacheMode()) {
						// rfc822形式用にミリ秒を０で丸める.
						long localFileTime = wp.getMtime(path);
						localFileTime = (localFileTime / 1000L) * 1000L;
						Date localFileDate = new Date(localFileTime);
						// RequesのtHeaderに「if-modified-since」が存在する場合は、キャッシュ扱いで返却.
						Object ifModifiedSince = req.get("if-modified-since");
						if(ifModifiedSince != null && ifModifiedSince instanceof String) {
							Date cacheDate = DateConvert.toRfc822((String)ifModifiedSince);
							if(localFileDate.equals(cacheDate)) {
								res.put("Last-Modified:", DateConvert.toRfc822(true, localFileDate));
								if(gzip) {
									res.put("Content-Type", mime.getUrl(path.substring(0, path.length() - 3)));
								} else {
									res.put("Content-Type", mime.getUrl(path.substring(0, path.length())));
								}
								res.setStatus(304);
								sendResponse(req, gzip, em, 304, res, "");
								return;
							}
						}
						// キャッシュ情報じゃない場合は、最終更新日をセット.
						// 現在のファイル日付をセット.
						res.put("Last-Modified:", DateConvert.toRfc822(true, localFileDate));
					}
					res.setStatus(200);
					sendFile(req, gzip, path, em, mime, 200, res);
					return;
				}
				// 実行スクリプトで処理する.
				path += ".js";
				if(!wp.isFile(path)) {
					// 存在しない場合.
					errorResponse(req, em, 404);
					return;
				}
			} else {
				// main.jsを実行させる.
				path = path + RhiginConstants.MAIN_JS;
			}

			String method = req.getMethod();
			Object params = null;
			if ("GET".equals(method)) {
				params = getParams(req.getUrl());
				// アクセスキーが指定されている場合暗号の解析.
				if(params != null && accessKey != null) {
					String value = (String)((Params)params).get("params");
					if(value != null) {
						// 暗号化されたパラメータが存在する場合は、それを解析.
						value = new String(RhiginAccessKeyByFCipher.decode(accessKey, FCipher.cb64_dec(value)));
						params = Analysis.paramsAnalysis(value, 0);
					}
				}
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
				res.setStatus(200);

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
					// リダイレクト.
					redirectResponse(req, em, redirect);
					return;
				} catch (RhiginException rhiginException) {
					// HTTPステータスが500エラー以上の場合のみ、エラー表示.
					if (rhiginException.getStatus() >= 500) {
						// スクリプトエラーを表示.
						LOG.error("scriptError:" + req.getUrl(), rhiginException);
					}
					errorResponse(req, em, rhiginException.getStatus(), rhiginException.getMessage());
					return;
				} finally {
					ExecuteScript.clearCurrentRhiginContext();
				}
				
				// 戻り値がInputStreamの場合.
				if (ret instanceof InputStream) {
					// バイナリ返却.
					sendResponse(req, em, res.getStatus(), res, (InputStream) ret);
				// 戻り値がInputStreamでない場合.
				} else {
					// 戻り値が無い場合.
					if (ret == null || (ret instanceof String && ((String) ret).isEmpty())) {
						ret = "";
						// success形式で返却.
						successResponse(req, false, em, res.getStatus(), res, ret);
					// minHeader で ContentTypeが設定されていない場合.
					} else if(minHeader && res.get("Content-Type") == Response.DEFAULT_CONTENT_TYPE) {
						// rhiginの名前でコンテンツタイプをセット.
						res.put("Content-Type", MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE);
						// ObjectPackでjsnappy圧縮でバイナリ変換.
						ret = ObjectPack.packB(ret);
						successResponseByBinary(req, em, res.getStatus(), res, (byte[])ret);
						ret = null;
					} else {
						// success形式で返却.
						successResponse(req, gzip, em, res.getStatus(), res, ret);
						ret = null;
					}
				}
				
			} finally {
				
				// スクリプト終了処理.
				ExecuteScript.callEndScripts(false);
			}
			
		} catch (RhiginException re) {
			if (!em.isEndSend()) {
				try {
					errorResponse(req, em, re.getStatus(), re.getMessage());
				} catch (Exception ee) {
				}
			}
			LOG.error("error", re);
		} catch (Exception e) {
			if (!em.isEndSend()) {
				try {
					errorResponse(req, em, 500, e.getMessage());
				} catch (Exception ee) {
				}
			}
			LOG.error("error", e);
		}
	}

	/** 要求パス取得. **/
	public static final String getPath(String url) {
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
	public static final Object getParams(String url) throws IOException {
		int p = url.indexOf("?");
		if (p != -1) {
			return Analysis.paramsAnalysis(url, p + 1);
		}
		return null;
	}

	/** POSTパラメータを取得. **/
	public static final Object postParams(Request req) throws IOException {
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
	
	/** 正常結果をBinaryで返却. **/
	protected static final void successResponseByBinary(Request req, HttpElement em, int status, Response response, byte[] value)
		throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		em.setSendBinary(stateResponse(req, status, response, value, (long)value.length));
	}

	/** 正常結果をJSON返却. **/
	protected static final void successResponse(Request req, boolean gzip, HttpElement em, int status, Response response, Object value)
		throws IOException {
		if (value == null) {
			sendResponse(req, gzip, em, status, response, "{}");
		} else {
			sendResponse(req, gzip, em, status, response, Json.encode(value));
		}
	}

	/** GZIP返却許可チェック. **/
	protected static final boolean isGzip(Request req) throws IOException {
		String n = (String) req.get("accept-encoding");
		if (n == null || n.indexOf("gzip") == -1) {
			return false;
		}
		return true;
	}

	/** Options送信. **/
	protected static final void sendOptions(Request req, HttpElement em)
		throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if(req.isMinHeader()) {
			em.setSendBinary(OPSIONS_RESPONSE_M);
		} else if(!req.isBlowserHeader()) {
			em.setSendBinary(OPSIONS_RESPONSE_NB);
		} else {
			em.setSendBinary(OPSIONS_RESPONSE);
		}
	}

	/** ファイル送信. **/
	protected static final void sendFile(Request req, boolean gzip, String fileName, HttpElement em, MimeType mime, int status, Response header)
		throws IOException {
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
					stateResponse(req, status, header, BLANK_BINARY, WatchPath.getInstance().getLength(fileName))));
			em.setSendData(new FileInputStream(fileName));
			em.startWrite();
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/** [byte[]]レスポンス送信. **/
	protected static final void sendResponse(Request req, boolean gzip,
			HttpElement em, int status, Response header, String body)
		throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		if (gzip && body.length() > HttpConstants.NOT_GZIP_BODY_LENGTH) {
			header.put("Content-Encoding", "gzip");
			em.setSendBinary(stateResponse(req, status, header, pressGzip(body), -1L));
		} else {
			em.setSendBinary(stateResponse(req, status, header, body));
		}
	}

	/** [inputStream]レスポンス送信. **/
	protected static final void sendResponse(Request req, HttpElement em, int status, Response header, InputStream body)
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
			em.setSendData(new ByteArrayInputStream(stateResponse(req, status, header, BLANK_BINARY, len)));
			em.setSendData(body);
			em.startWrite();
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/** リダイレクト送信. **/
	protected static final void redirectResponse(Request req, HttpElement em, Redirect redirect)
		throws IOException {
		em.setRequest(null);
		em.destroyBuffer();
		em.setEndReceive(true);
		em.setEndSend(true);
		Response res = new Response();
		res.put("Location", redirect.getUrl());
		em.setSendBinary(stateResponse(req, redirect.getStatus(), res, ""));
	}

	/** GZIP圧縮. **/
	protected static final byte[] pressGzip(String body) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		GZIPOutputStream go = new GZIPOutputStream(bo);
		go.write(body.getBytes("UTF8"));
		go.flush();
		go.finish();
		go.close();
		return bo.toByteArray();
	}

	/** エラーレスポンスを送信. **/
	protected static final void errorResponse(Request req, HttpElement em, int status)
		throws IOException {
		errorResponse(req, em, status, null);
	}

	/** エラーレスポンスを送信. **/
	@SuppressWarnings("rawtypes")
	protected static final void errorResponse(Request req, HttpElement em, int status, String message)
		throws IOException {
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
		if(req.isMinHeader()) {
			try {
				// ObjectPackでバイナリ変換.
				res.put("Content-Type", MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE);
				successResponseByBinary(req, em, status, res, (byte[])ObjectPack.packB(ret));
			} catch(Exception e) {
				// 何らかで失敗したら、返却はJSON形式で.
				res.remove("Content-Length");
				em.setSendBinary(stateResponse(req, status, res, Json.encode(ret)));
			}
			return;
		}
		em.setSendBinary(stateResponse(req, status, res, Json.encode(ret)));
	}

	/** ステータス指定Response返却用バイナリの生成. **/
	protected static final byte[] stateResponse(Request req, int state, Response header, String b)
		throws IOException {
		return stateResponse(req, state, header, b.getBytes("UTF8"), -1L);
	}

	/** ステータス指定Response返却用バイナリの生成. **/
	protected static final byte[] stateResponse(Request req, int state, Response header, byte[] b, Long contentLength)
		throws IOException {
		// ContentLengthが-1で設定されている場合は、バイナリ長の長さをセットする.
		if (contentLength != null && contentLength == -1L) {
			contentLength = (long) b.length;
		}
		// Content-Lengthの長さが存在する場合はHttpHeaderに存在するContent-Lengthを削除.
		if(contentLength != null) {
			header.remove("Content-Length");
		}
		// HttpStatusの送信ヘッダを生成.
		final byte[] stateBinary = new StringBuilder(String.valueOf(state)).append(" ").append(Status.getMessage(state))
				.toString().getBytes("UTF8");
		// HTTPヘッダを出力.
		StringBuilder buf = new StringBuilder(Response.headers(header));
		if (contentLength == null) {
			// content-lengthが存在しない場合はchunked転送.
			// AccessKeyがあってもエンコードしない.
			buf.append("Transfer-Encoding:chunked\r\n");
		} else {
			
			// アクセスキーが指定されていて、送信バイナリがBLANK_BINARY以外の場合.
			String accessKey = req.getHeader(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER);
			if(b != BLANK_BINARY && accessKey != null) {
				try {
					// データのエンコード.
					byte[] ab = RhiginAccessKeyByFCipher.encode(accessKey, b);
					b = ab; ab = null;
				} catch(Exception e) {
					// エラーの場合は、アクセスキーでエンコードせずにそのまま返却.
					accessKey = null;
				}
				// コンテンツ長の設定.
				contentLength = (long) b.length;
				
				// アクセスキーが存在する場合は、セット.
				if(accessKey != null) {
					// RhiginAccessKeyヘッダをセット.
					buf.append(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER)
						.append(":").append(accessKey).append("\r\n");
				}
			}
			// ContentLengthヘッダをセット.
			buf.append("Content-Length:").append(contentLength).append("\r\n");
		}
		buf.append("\r\n");
		final byte[] foot = buf.toString().getBytes("UTF8");
		buf = null;

		int pos = 0;
		byte[] ret = null;
		if(req.isMinHeader()) {
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
		} else if(!req.isBlowserHeader()) {
			int all = STATE_RESPONSE_1.length + stateBinary.length + STATE_RESPONSE_2_NB.length + foot.length + b.length;
			ret = new byte[all];
			System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
			pos += STATE_RESPONSE_1.length;
			System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
			pos += stateBinary.length;
			System.arraycopy(STATE_RESPONSE_2_NB, 0, ret, pos, STATE_RESPONSE_2_NB.length);
			pos += STATE_RESPONSE_2_NB.length;
			System.arraycopy(foot, 0, ret, pos, foot.length);
			pos += foot.length;
			System.arraycopy(b, 0, ret, pos, b.length);
		} else if(header.containsKey("Last-Modified:")) {
			int all = STATE_RESPONSE_1.length + stateBinary.length + STATE_RESPONSE_2_CACHE.length + foot.length + b.length;
			ret = new byte[all];
			System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
			pos += STATE_RESPONSE_1.length;
			System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
			pos += stateBinary.length;
			System.arraycopy(STATE_RESPONSE_2_CACHE, 0, ret, pos, STATE_RESPONSE_2_CACHE.length);
			pos += STATE_RESPONSE_2_CACHE.length;
			System.arraycopy(foot, 0, ret, pos, foot.length);
			pos += foot.length;
			System.arraycopy(b, 0, ret, pos, b.length);
		} else {
			int all = STATE_RESPONSE_1.length + stateBinary.length + STATE_RESPONSE_2.length + foot.length + b.length;
			ret = new byte[all];
			System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
			pos += STATE_RESPONSE_1.length;
			System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
			pos += stateBinary.length;
			// キャッシュファイル日付が設定されていない場合.
			System.arraycopy(STATE_RESPONSE_2, 0, ret, pos, STATE_RESPONSE_2.length);
			pos += STATE_RESPONSE_2.length;
			System.arraycopy(foot, 0, ret, pos, foot.length);
			pos += foot.length;
			System.arraycopy(b, 0, ret, pos, b.length);
		}
		return ret;
	}

	/** Optionsレスポンス. **/
	private static final byte[] OPSIONS_RESPONSE;
	private static final byte[] OPSIONS_RESPONSE_NB;
	private static final byte[] OPSIONS_RESPONSE_M;

	/** ステータス指定レスポンス. **/
	private static final byte[] STATE_RESPONSE_1;
	private static final byte[] STATE_RESPONSE_2;
	private static final byte[] STATE_RESPONSE_2_CACHE;
	private static final byte[] STATE_RESPONSE_2_NB;
	private static final byte[] STATE_RESPONSE_2_M;

	static {
		byte[] op;
		byte[] s1;
		byte[] s2;
		byte[] c2;
		byte[] op_nb;
		byte[] s2_nb;
		byte[] op_m;
		byte[] s2_m;
		try {
			final String serverName = RhiginConstants.NAME + "(" + RhiginConstants.VERSION + ")";
			final String serverName_m = RhiginConstants.NAME + "_m(" + RhiginConstants.VERSION + ")";
			
			// ブラウザ用.
			op = ("HTTP/1.1 200 OK\r\n"
					+ "Allow:GET,POST,HEAD,OPTIONS\r\n"
					+ "Cache-Control:no-cache\r\n"
					+ "Pragma:no-cache\r\n"
					+ "Expire:-1\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Access-Control-Allow-Origin:*\r\n"
					+ "Access-Control-Allow-Headers:content-type,x-accel-buffering,*\r\n"
					+ "Access-Control-Allow-Methods:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
					+ "Content-Length:0\r\n\r\n"
			).getBytes("UTF8");
			s1 = ("HTTP/1.1 ").getBytes("UTF8");
			// js返却用の出力.
			s2 = ("\r\n"
					+ "Cache-Control:no-cache\r\n"
					+ "Pragma:no-cache\r\n"
					+ "Expire:-1\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Access-Control-Allow-Origin:*\r\n"
					+ "Access-Control-Allow-Headers:content-type,x-sccel-buffering,*\r\n"
					+ "Access-Control-Allow-Methods:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
			).getBytes("UTF8");
			// 固定ファイルの出力.
			c2 = ("\r\n"
					+ "Expire:-1\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Access-Control-Allow-Origin:*\r\n"
					+ "Access-Control-Allow-Headers:content-type,x-sccel-buffering,*\r\n"
					+ "Access-Control-Allow-Methods:GET,POST,HEAD,OPTIONS\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
			).getBytes("UTF8");
			
			// ブラウザアクセス以外.
			op_nb = ("HTTP/1.1 200 OK\r\n"
					+ "Allow:GET,POST,HEAD,OPTIONS\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
					+ "Content-Length:0\r\n\r\n"
			).getBytes("UTF8");
			s2_nb = ("\r\n"
					+ "X-Accel-Buffering:no\r\n"
					+ "Server:" + serverName + "\r\n"
					+ "Connection:close\r\n"
			).getBytes("UTF8");
			
			// 最小ヘッダ.
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
			c2 = null;
			op_nb = null;
			s2_nb = null;
			op_m = null;
			s2_m = null;
		}
		OPSIONS_RESPONSE = op;
		OPSIONS_RESPONSE_NB = op_nb;
		OPSIONS_RESPONSE_M = op_m;
		STATE_RESPONSE_1 = s1;
		STATE_RESPONSE_2 = s2;
		STATE_RESPONSE_2_CACHE = c2;
		STATE_RESPONSE_2_NB = s2_nb;
		STATE_RESPONSE_2_M = s2_m;
	}

	// [js]リダイレクト用メソッド.
	public static final RhiginFunction redirect = new RhiginFunction() {
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
	public static final RhiginFunction error = new RhiginFunction() {
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

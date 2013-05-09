/****************************************************************
*	CopyLeft: 请遵守GPL版权许可；同时保留所有文字和注释；	*
*****************************************************************
 * @author liusheng<nike.lius@gmail.com>
 * 定义HttpCodecFactory解析器的别名；
 * 定义 makeHttpRequest/makeHttpResponse 生成特定HTTP请求
 */
package com.googlecode.asyncweb2.httpcodec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.http.DefaultHttpRequest;
import org.apache.mina.filter.codec.http.DefaultHttpResponse;
import org.apache.mina.filter.codec.http.HttpMethod;
import org.apache.mina.filter.codec.http.HttpResponseStatus;
import org.apache.mina.filter.codec.http.HttpVersion;
import org.apache.mina.filter.codec.http.MutableHttpMessage;
import org.apache.mina.filter.codec.http.MutableHttpRequest;
import org.apache.mina.filter.codec.http.MutableHttpResponse;

import com.googlecode.asyncweb2.utils.MyLog;
import com.googlecode.asyncweb2.utils.MyUtil;

public class HttpCodec extends HttpCodecFactory2 implements ProtocolCodecFactory {//HttpCodecFactory,HttpCodecFactory2
	private static final MyLog _log = MyLog.getLog(HttpCodec.class);//输入调试信息
	// ------------------------------------
	public static MutableHttpRequest makeRequest(String method, URI requri, String ctype, byte[] body, Properties headers) throws Exception {
		if (requri==null) throw new java.lang.IllegalArgumentException("requri=null");
		MutableHttpRequest minaReq = new DefaultHttpRequest();
		minaReq.setKeepAlive(false);
//		minaReq.setCookies(new Cookie[]{"c", "d"});
//		minaReq.setCookies("G=makeHttpRequest");
		minaReq.setProtocolVersion(HttpVersion.HTTP_1_1);
		int port = requri.getPort();
		if (port==-1)	port = "https".equalsIgnoreCase(requri.getScheme()) ? 443 : 80;
		minaReq.setHeader("Host", requri.getHost()+":"+port);//支持HTTP代理模式；
		if (method!=null)	minaReq.setMethod(toHttpMethod(method));
		if (requri!=null)	minaReq.setRequestUri(requri);
		if (ctype!=null)	minaReq.setContentType(ctype);
		if (body!=null)		minaReq.setContent(IoBuffer.wrap(body));
		if (headers!=null) {
			for(Iterator<Object> i=headers.keySet().iterator(); i.hasNext(); ) {
				String hkey = (String) i.next();
				minaReq.setHeader(hkey, headers.getProperty(hkey));
			}
		}
//		if (TRACE.isDebugEnabled()) printInfo(minaReq, "makeHttpRequest()", "GBK");
		return minaReq;
	}	
	public static DefaultHttpResponse makeResponse(DefaultHttpResponse minaResponse, int code, String msg, Properties headers) {
		return makeResponse(null, code, msg, headers, null, null);
	}
	public static DefaultHttpResponse makeResponse(DefaultHttpResponse minaResponse, int code, String msg, Properties headers, String ctype, byte[] body) {
		if (minaResponse==null)	 minaResponse = new DefaultHttpResponse();
		if (headers!=null) {
			for(Map.Entry<?,?> e: headers.entrySet()) {
				String k = e.getKey().toString();
				String v = e.getValue().toString();
				minaResponse.setHeader(k, v);
				_log.debug("# makeResponse().Header(%s) = %s", k, v);
			}
		}
		if (ctype!=null)	minaResponse.setContentType(ctype);
		if (body!=null)		minaResponse.setContent(IoBuffer.wrap(body));//没有自动设置长度
		int clen = (body!=null ? body.length : 0);
		minaResponse.setHeader("Content-Length", Integer.toString(clen));
		minaResponse.setHeader("Server", HttpCodec.class.getName());
		minaResponse.setStatus(HttpResponseStatus.forId(code), msg);//允许msg==null
		minaResponse.setProtocolVersion(HttpVersion.HTTP_1_1);
		minaResponse.setKeepAlive(true);
		return minaResponse;
	}
	/**
	 * 在Http-Proxy/Http-Forward模式中，将从后端收到的resp发给前端，处理Trunk模式。
	 * @param resp
	 * @param inDebug
	 * @return
	 */
	public static MutableHttpResponse makeResponseNoTrunk(MutableHttpResponse resp, boolean inDebug){
		String trunked = resp.getHeader("Transfer-Encoding");
		if (!"trunked".equalsIgnoreCase(trunked))	return resp;
		//TODO 处理不正确，没有第一个trunk长度。
		return makeResponseNoTrunk_1(resp, inDebug);
//		return makeResponseNoTrunk_2(resp, inDebug);
	}
	static MutableHttpResponse makeResponseNoTrunk_2(MutableHttpResponse resp, boolean inDebug){
		//shrink() java.lang.IllegalStateException: Derived buffers and their parent can't be expanded.
		byte[] body = resp.getContent().shrink().array();
		resp.setContent(IoBuffer.wrap(body));
		resp.setHeader("Content-Length", Integer.toString(body.length));
		resp.removeHeader("Transfer-Encoding");//TODO IOException: Bogus chunk size
		if (inDebug || _log.isDebugEnabled()) {			
			System.err.println("makeResponse4Trunk().BODY = \n"+new String(body));
			MyUtil.writeBinary("trunk_body.dat", body);
			HttpCodec.printInfo(resp, "HttpCodec.makeResponse4Trunk()", "GBK");
			System.err.println("body.length = "+body.length);
		}
		return resp;
	}
	static MutableHttpResponse makeResponseNoTrunk_1(MutableHttpResponse resp, boolean inDebug){
		if (resp.getContent()==null)	return resp;
		int offset = resp.getContent().arrayOffset(); 
		int	remain = resp.getContent().remaining();
		byte[] total = resp.getContent().array();//去掉content中的head信息！！！
//		int	bodySize = total.length-offset-remain;//total.length-offset
		int	bodySize = total.length-offset;//total.length-offset
		resp.setContent(IoBuffer.wrap(total, offset, bodySize));//没有自动设置长度
		resp.setHeader("Content-Length", Integer.toString(bodySize));
		resp.removeHeader("Transfer-Encoding");//TODO IOException: Bogus chunk size
		if (inDebug || _log.isDebugEnabled()) {			
			System.err.println("makeResponse4Trunk().BODY = \n"+new String(total, offset, bodySize));
			MyUtil.writeBinary("trunk.dat", total);
			HttpCodec.printInfo(resp, "HttpCodec.makeResponse4Trunk()", "GBK");
			System.err.println("total.length = "+total.length);
			System.err.println("remaining()  = "+remain);
			System.err.println("bodyOffset   = "+offset);
			System.err.println("bodySize     = "+bodySize);
		}
		return resp;
	}
	public static void printInfo(Object httpMsg, String info, String bodyCharset) {
		if (httpMsg instanceof MutableHttpRequest) {
			MutableHttpRequest req = (MutableHttpRequest)httpMsg;
			_log.debug("# printHttp(%s)...%s", info, httpMsg.getClass());
			_log.debug("REQ - %s %s %s", req.getMethod(), req.getRequestUri(), req.getProtocolVersion());
			_log.debug("REQ - Cookies\t= %s", req.getCookies());
			for(Map.Entry<?,?> e : req.getHeaders().entrySet()) {
				_log.debug("REQ H %s \t= %s", e.getKey(), e.getValue());
			}
			for(Map.Entry<?,?> e : req.getParameters().entrySet()) {
				_log.debug("REQ P %s \t= %s", e.getKey(), e.getValue());
			}
			_log.debug("REQ - Content \t= %s", printContent(req.getContent(), bodyCharset));
		}else if (httpMsg instanceof MutableHttpResponse) {
			MutableHttpResponse res = (MutableHttpResponse)httpMsg;
			_log.debug("# printHttp(%s)...%s", info, httpMsg.getClass());
			_log.debug("RES - %s %s", res.getProtocolVersion(), res.getStatus());			
//			_log.debug("RES - Content = ...");
			Map<String, List<String>> headers = res.getHeaders();
			for(Map.Entry<String, List<String>> me: res.getHeaders().entrySet()) {
				_log.debug("RES.%s \t= %s", me.getKey(), me.getValue());
			}
			//输出了整个HTTP响应，含头信
			_log.debug("RES - Cookies = %s", res.getCookies());
			_log.debug("RES - Content-Type = %s", res.getContentType());
			_log.debug("RES - Content = %s", printContent(res.getContent(), bodyCharset));
		}
	}
	public static String printContent(Object content, String charset) {
		if (content ==null) {
			_log.warn("E content=null");
			return null;
		}
		_log.info("# printContent()...");
		if (content instanceof IoBuffer) {
			IoBuffer buf = (IoBuffer)content;
			int offset = buf.arrayOffset() + buf.limit();
			byte[] bin = buf.array(); 
//			_log.debug("# IoBuffer.arrayOffset() = %s", buf.arrayOffset());
//			_log.debug("# IoBuffer.position()    = %s", buf.position());
//			_log.debug("# IoBuffer.capacity()    = %s", buf.capacity());
//			_log.debug("# IoBuffer.limit()       = %s", buf.limit());
//			_log.debug("# IoBuffer.array() = [%d] %s", offset, new String(bin, 0, offset));
			//resetIoBuffer(buf);//20100412 自动复原。E resetIoBuffer() java.nio.InvalidMarkException
			if (charset!=null) {
				try {
					return new String(bin, 0, offset, charset);
				}catch(Exception e) {
					return new String(bin, 0, offset);
				}
			}else {	
				return MyUtil.bcd(bin, 0, offset);
			}
		}else{
			_log.warn("E Unknown Type: %s", content);
		}
		return content.toString();
	}
	static HttpMethod toHttpMethod(String method) {
		if (method!=null)	method = method.toUpperCase();
		if ("GET".equals(method)) 		return HttpMethod.GET;
		if ("POST".equals(method))		return HttpMethod.POST;
		if ("PUT".equals(method))		return HttpMethod.PUT;
		if ("DELETE".equals(method))	return HttpMethod.DELETE;
		if ("HEAD".equals(method))		return HttpMethod.HEAD;
		if ("TRACE".equals(method))		return HttpMethod.TRACE;
		if ("OPTIONS".equals(method))	return HttpMethod.OPTIONS;
		if ("CONNECT".equals(method))	return HttpMethod.CONNECT;
		return HttpMethod.GET;
	}
	/*
	//public static IHttp.HRequest toIHttpRequest(HSessionInf session, MutableHttpRequest req) {
	public static IHttp.HRequest toIHttpRequest(MutableHttpRequest req) {
		IHttp.HRequest ireq = new IHttp.HRequest();
        ireq.setURI(req.getRequestUri());//同时设置querystring/path/secure信息
//		_log.debug("### req.getRequestUri() = %s", req.getRequestUri());
//		_log.debug("### req.getRequestUri().path = %s", req.getRequestUri().getPath());
//		_log.debug("### req.getRequestUri().fragment = %s", req.getRequestUri().getFragment());
//		_log.debug("### ireq.getURI() = %s", ireq.getURI());
//		_log.debug("### ireq.getURI().fragment = %s", ireq.getURI().getFragment());
		ireq.ver 	= (HttpVersion.HTTP_1_1.equals(req.getProtocolVersion()) ? 11 : 10);
        ireq.method	= req.getMethod().toString();
        ireq.host 	= req.getHeader("host");
        if (ireq.host.contains(":")) {
            ireq.domain = ireq.host.split(":")[0];
            ireq.port = Integer.parseInt(ireq.host.split(":")[1]);
        } else {
            ireq.domain = ireq.host;
            ireq.port = (ireq.secure ? 443 : 80);
        }
        //Router.detectChanges();	Router.route(request);
        //ireq.remote = (session==null) ? "" : session.getRemoteInfo();
        ireq.body = ((IoBuffer)req.getContent()).asInputStream();
        //ireq.resolveContentType(req.getHeader("Content-Type"));
        HttpMethod hm = req.getMethod();
        if (HttpMethod.POST.equals(hm) || HttpMethod.PUT.equals(hm)) {
        	ireq.resolveContentType(req.getContentType());
        }        
        ireq.resolveAcceptFormat();
        //处理其他参数。
        for (String key : req.getHeaders().keySet()) {
            IHttp.HHeader hd = new IHttp.HHeader();
            hd.name = key;//key.toLowerCase();
            hd.values = req.getHeaders().get(key);
            ireq.headers.put(hd.name, hd);
            //_log.debug("# toIHttpRequest().Header(%s) = %s", hd.name, hd.values);
        }//	_log.debug("# toIHttpRequest().Headers = %s", req.getHeaders().keySet());
        Set<String> cookiesNameSet = new HashSet<String>();//用于日志显示。
        for (org.apache.mina.filter.codec.http.Cookie minaCookie : req.getCookies()) {
            IHttp.HCookie icookie = new IHttp.HCookie();
            icookie.name 	= minaCookie.getName();
            icookie.path 	= minaCookie.getPath();
            icookie.secure 	= minaCookie.isSecure();
            icookie.value 	= minaCookie.getValue();
            ireq.cookies.put(icookie.name, icookie);
            cookiesNameSet.add(icookie.name);
            _log.debug("# toIHttpRequest().Cookie(%s) = %s", icookie.name, icookie.toString());
        }//	_log.debug("# toIHttpRequest().Cookies = %s", cookiesNameSet.toString());
        return ireq;
    }*/
	public static InputStream getBodyStream(MutableHttpMessage httpMsg) {
		return ((IoBuffer)httpMsg.getContent()).asInputStream();
	}
	public static String getBodyCharset(MutableHttpMessage req, String defaultCharset) {
		String ctype = req.getHeader("Content-Type");
		if (ctype==null)	return defaultCharset;
		int	pos = ctype.indexOf("charset=");
		if (pos==-1)		return defaultCharset;
		String charset = ctype.substring(pos+8);
		return charset;
	}
	private static void resetIoBuffer(IoBuffer iobuffer) {
		try {
			iobuffer.reset();//Mina2M4有效，但是M6/jdk6抛出异常。
		}catch(Exception e) {
			_log.debug("E resetIoBuffer() %s", e.toString());
		}
	}
	private static void reset_to_body_start(MutableHttpMessage httpMsg) {
		if (httpMsg==null)					return;
		Object iob = httpMsg.getContent();
		if (iob==null)		return;
		if (iob instanceof IoBuffer) {
			resetIoBuffer((IoBuffer)httpMsg.getContent());
		}	
//		1116.173704.515 [pool-1-thread-1 ] DEBUG L:47  HttpServerHProc        - # doRequest4Http_print()...org.apache.mina.filter.codec.http.DefaultHttpRequest@1742700
//		java.nio.InvalidMarkException
//			at java.nio.Buffer.reset(Unknown Source)
//			at org.apache.mina.core.buffer.AbstractIoBuffer.reset(AbstractIoBuffer.java:419)
//			at com.bs3.nio.mina2.codec.HttpCodec.reset_to_body_start(HttpCodec.java:245)
//			at com.bs3.nio.mina2.codec.HttpCodec.getBodyBinary(HttpCodec.java:260)
//			at com.bs3.nio.mina2.codec.HttpCodec.getBodyString(HttpCodec.java:265)
//			at com.umpay.v3.demo.HttpServerHProc.doRequest4Http_print(HttpServerHProc.java:37)
//			at com.umpay.v3.demo.HttpServerHProc.doRequest4Http(HttpServerHProc.java:27)

	}
	public static byte[] getBodyBinary(MutableHttpRequest req) throws NumberFormatException, IOException {
		byte[] body_bin = null;
		if (req.getMethod().equals(HttpMethod.POST) || req.getMethod().equals(HttpMethod.PUT)) {
			String slen = req.getHeader("Content-Length");
			if (slen==null)	slen = req.getHeader("Content-length");
			if (slen==null)	slen = req.getHeader("content-length");
			InputStream is = getBodyStream(req);
			if (slen!=null) {
				body_bin = MyUtil.readFully(is, Integer.parseInt(slen));
			}else {
				body_bin = MyUtil.readFully(is, true, 4096);
			}
			HttpCodec.reset_to_body_start(req);
		}//else { body_bin = new byte[0]; }
		return body_bin;
	}
	public static String getBodyString(MutableHttpRequest req, String charset0) throws NumberFormatException, IOException {
		byte[] body_bin = getBodyBinary(req);
		if (body_bin==null)		return null;
		String charset = getBodyCharset(req, charset0);
		return new String(body_bin, charset);
	}
	public static byte[] getBodyBinary(MutableHttpResponse resp) throws NumberFormatException, IOException {
		byte[] body_bin = null;
		if (true) {
			String slen = resp.getHeader("Content-Length");
			if (slen==null)	slen = resp.getHeader("Content-length");
			if (slen==null)	slen = resp.getHeader("content-length");
			InputStream is = getBodyStream(resp);
			if (slen!=null) {
				body_bin = MyUtil.readFully(is, Integer.parseInt(slen));
			}else {
				body_bin = MyUtil.readFully(is, true, 4096);
			}
			HttpCodec.reset_to_body_start(resp);
		}//else { body_bin = new byte[0]; }
		return body_bin;
	}
	public static String getBodyString(MutableHttpResponse resp, String charset0) throws NumberFormatException, IOException {
		byte[] body_bin = getBodyBinary(resp);
		if (body_bin==null)		return null;
		String charset = getBodyCharset(resp, charset0);
		return new String(body_bin, charset);
	}

}

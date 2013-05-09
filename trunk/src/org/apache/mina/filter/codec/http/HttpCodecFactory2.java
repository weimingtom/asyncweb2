package org.apache.mina.filter.codec.http;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import com.bs3.utils.MyLog;
/**
 * 基于HttpCodecFactory修改，确保同一个IoSession使用1个Decoder或Encoder对象。
 * @author lius
 *
 */
public class HttpCodecFactory2 implements ProtocolCodecFactory {
	private static final MyLog _log = MyLog.getLog(HttpCodecFactory2.class);
    //----------
    //return (ProtocolEncoder) session.getAttribute(ENCODER);
    //----------
	public HttpCodecFactory2() {}
    //----------
	protected void setAttr(IoSession session, Class<?> type, Object value) {
        session.setAttribute(ProtocolEncoder.class, value);
        _log.debug("### setAttr(%s, %s) = %s", session.getId(), type.getName(), value);
	}
	protected Object getAttr(IoSession session, Class<?> type) {
		Object value = session.getAttribute(ProtocolEncoder.class);
		if (value!=null) {
		_log.debug("### getAttr(%s, %s) = %s", session.getId(), type.getName(), value);
		}
		return value;
	}
   //----------
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    	//优先从缓存中获取，确保一个IoSession使用同一个ProtocolEncoder对象。
    	Object cached = this.getAttr(session, ProtocolEncoder.class);
    	if (cached!=null)	return (ProtocolEncoder)cached;
    	//创建新的Encoder并缓存。
    	ProtocolEncoder encoder = null;
        if (session.getService() instanceof IoAcceptor) {
        	encoder = new HttpResponseEncoder();
        } else {
        	encoder = new HttpRequestEncoder();
        }
        this.setAttr(session, encoder.getClass(), encoder);
        return encoder;
    }
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    	//优先从缓存中获取，确保一个IoSession使用同一个ProtocolEncoder对象。
    	Object cached = this.getAttr(session, ProtocolDecoder.class);
    	if (cached!=null)	return (ProtocolDecoder)cached;
    	//创建新的Encoder并缓存。
    	ProtocolDecoder decoder = null;
       if (session.getService() instanceof IoAcceptor) {
    	   decoder = new HttpRequestDecoder();
        } else {
        	decoder = new HttpResponseDecoder();
        }
       this.setAttr(session, decoder.getClass(), decoder);
       return decoder;
    }
}

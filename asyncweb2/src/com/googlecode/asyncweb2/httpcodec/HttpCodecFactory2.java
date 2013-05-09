package com.googlecode.asyncweb2.httpcodec;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.http.HttpRequestDecoder;
import org.apache.mina.filter.codec.http.HttpRequestEncoder;
import org.apache.mina.filter.codec.http.HttpResponseDecoder;
import org.apache.mina.filter.codec.http.HttpResponseEncoder;

import com.googlecode.asyncweb2.utils.MyLog;
import com.googlecode.asyncweb2.utils.MyUtil;
/**
 * 基于HttpCodecFactory修改，确保同一个IoSession使用1个Decoder或Encoder对象。
 * @author lius
 *
 */
public class HttpCodecFactory2 implements ProtocolCodecFactory {
	private static final MyLog _log = MyLog.getLog(HttpCodecFactory2.class);
    //----------
	
	public static String getSessionId(IoSession session) {
//		return Long.toHexString(this.m_session.getId());
//		return MyUtil.int2id(session.getId());//String.format("%016X", session.getId());
		if (session==null)	return null;
		return MyUtil.int2id((int)session.getId());//String.format("%08X", session.getId());
	}
	protected Object getAttr(IoSession session, Class<?> type) {
		Object value = session.getAttribute(type);
		if(value==null) {
			if(ProtocolDecoder.class.equals(type)) {
				value = this.newDecoder(session);
			}else if(ProtocolEncoder.class.equals(type)) {
				value = this.newEncoder(session);
			}else {
				_log.warn("E getAttr(%s).attr(%s).type unknown", getSessionId(session), type.getSimpleName());
			}
			if(value!=null)	{
				this.setAttr(session, type, value);//缓存起来，确保每个IoSession只有唯一attr实例
			}
			if(value==null){
				_log.warn("E getAttr(%s).attr(%s).new = null", getSessionId(session), type.getSimpleName());
			}
		}
		return value;
	}
	private void setAttr(IoSession session, Class<?> type, Object value) {
        session.setAttribute(type, value);
        _log.debug("# setAttr(%s).attr(%s) = %s", getSessionId(session), type.getSimpleName(), value);
	}
	//----------
	//优先从缓存中获取，确保一个IoSession使用同一个ProtocolEncoder对象。
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    	Object inst = this.getAttr(session, ProtocolEncoder.class);//自动调用newEncoder()创建
    	return (ProtocolEncoder)inst;
    }
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    	Object inst = this.getAttr(session, ProtocolDecoder.class);//自动调用newDecoder()创建
    	return (ProtocolDecoder)inst;
    }
    protected ProtocolEncoder newEncoder(IoSession session) {
        if (session.getService() instanceof IoAcceptor) {
        	return new HttpResponseEncoder();
        } else {
        	return new HttpRequestEncoder();
        }
    }
    protected ProtocolDecoder newDecoder(IoSession session) {
    	if (session.getService() instanceof IoAcceptor) {
     		return new HttpRequestDecoder();
    	} else {
    		return new HttpResponseDecoder();
    	}
    }
}

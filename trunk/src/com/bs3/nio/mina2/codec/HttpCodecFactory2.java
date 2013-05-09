package com.bs3.nio.mina2.codec;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.http.HttpRequestDecoder;
import org.apache.mina.filter.codec.http.HttpRequestEncoder;
import org.apache.mina.filter.codec.http.HttpResponseDecoder;
import org.apache.mina.filter.codec.http.HttpResponseEncoder;
import com.bs3.nio.mina2.IMina2;
import com.bs3.utils.MyLog;
/**
 * ����HttpCodecFactory�޸ģ�ȷ��ͬһ��IoSessionʹ��1��Decoder��Encoder����
 * @author lius
 *
 */
public class HttpCodecFactory2 implements ProtocolCodecFactory {
	private static final MyLog _log = MyLog.getLog(HttpCodecFactory2.class);
    //----------
	protected Object getAttr(IoSession session, Class<?> type) {
		Object value = session.getAttribute(type);
		if(value==null) {
			if(ProtocolDecoder.class.equals(type)) 		value = this.newDecoder(session);
			else if(ProtocolEncoder.class.equals(type))	value = this.newEncoder(session);
			else 			_log.warn("E getAttr(%s).attr(%s).type unknown", IMina2.getSessionId(session), type.getSimpleName());
			if(value!=null)	this.setAttr(session, type, value);//����������ȷ��ÿ��IoSessionֻ��Ψһattrʵ��
			if(value==null)	_log.warn("E getAttr(%s).attr(%s).new = null", IMina2.getSessionId(session), type.getSimpleName());
		}
		return value;
	}
	private void setAttr(IoSession session, Class<?> type, Object value) {
        session.setAttribute(type, value);
        _log.debug("# setAttr(%s).attr(%s) = %s", IMina2.getSessionId(session), type.getSimpleName(), value);
	}
	//----------
	//���ȴӻ����л�ȡ��ȷ��һ��IoSessionʹ��ͬһ��ProtocolEncoder����
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    	Object inst = this.getAttr(session, ProtocolEncoder.class);//�Զ�����newEncoder()����
    	return (ProtocolEncoder)inst;
    }
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    	Object inst = this.getAttr(session, ProtocolDecoder.class);//�Զ�����newDecoder()����
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
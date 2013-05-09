package org.apache.mina.filter.codec.http;

import java.util.Map;
import java.util.TreeMap;

/**
 * 基于DefaultHttpMessage扩展，提供protected访问权限，修改cookies结构。
 * 	protected final Set<Cookie> cookies = new TreeSet<Cookie>(CookieComparator.INSTANCE);
 * 	protected final Map<String,Cookie> cookies = new TreeMap<String,Cookie>(HttpHeaderNameComparator.INSTANCE);
 * @author Liusheng
 *
 */
public class DefaultHttpMessage2 extends DefaultHttpMessage{
	private static final long serialVersionUID = 4915122328252892057L;
	protected final Map<String,Cookie> cookies = new TreeMap<String,Cookie>();
}

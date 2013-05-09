/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package com.bs3.nio.mina2.codec.http;
/*
 * @see http://amozon.javaeye.com/blog/322528 Mina2.0 example HttpServer
 * MINA1.X和MINA2.0的区别主要在于jdk1.4,1.5和jdk1.6对于nio的变化，还有加入了强大的IoBuffer。
 * 1.x中间有个httpServer的例子在2.0当中编译无法通过，其中对于streamhandler和http协议的实现还是很有价值的。
 * 经过改写，终于可以运行了。使用浏览器访问http://localhost:8080/可以看到运行结果
 */
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.bs3.utils.MyLog;


/**
 *    
 * (<b>Entry point</b>) HTTP server * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $
 * 
 */
public class HttpServerMain {
	private static final MyLog _log = MyLog.getLog(HttpServerMain.class);
    /** Default HTTP port */
    private static int DEFAULT_PORT = 8081;
    /** Tile server revision number */
    public static final String VERSION_STRING = "$Revision: 555855 $ $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $";
	static class Test {
	    public static void main(String[] args) throws Exception {
	        int port = DEFAULT_PORT;
	        for (int i = 0; i < args.length; i++) {
	            if (args[i].equals("-port")) {
	                port = Integer.parseInt(args[i + 1]);
	            }
	        }
	        // Create an acceptor
	    	NioSocketAcceptor acceptor = new NioSocketAcceptor();
	    	//错误 IoConnector connector = new NioSocketConnector();
	        // Create a service configuration
	    	acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new HttpServerProtocolCodecFactory()));
	//    	acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	        acceptor.setHandler(new HttpServerHandler());
	        acceptor.bind(new InetSocketAddress(port));
	        _log.debug("VERSION : %s", VERSION_STRING);
	        _log.debug("Server  : %d", port);
	    }
	}
}

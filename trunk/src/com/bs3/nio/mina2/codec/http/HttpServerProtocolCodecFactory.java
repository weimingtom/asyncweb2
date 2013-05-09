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

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.bs3.utils.MyNet;

/**
 * Provides a protocol codec for HTTP server.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $
 */
public class HttpServerProtocolCodecFactory extends DemuxingProtocolCodecFactory {
    public HttpServerProtocolCodecFactory() {
//        super.addMessageDecoder(HttpRequestDecoder.class);
        super.addMessageDecoder(HttpRequestDecoder2.class);
        super.addMessageEncoder(HttpResponseMessage.class, HttpResponseEncoder.class);
    }
	//private static final MyLog _log = MyLog.getLog(HttpServerProtocolCodecFactory.class);
    static class Test {
	    public static void main(String[] args) throws Exception {
			MyNet.Http2 net = MyNet.getHttp();
			String uri = "http://localhost:8081/";
			String str = "BODY\r\n\r\n";
			String res = net.http("POST", uri, str, null);//ERROR
			System.out.println(res);
		}
    }
}

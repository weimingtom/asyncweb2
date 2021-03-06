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
package org.apache.mina.filter.codec.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.CrLfDecodingState;
import org.apache.mina.filter.codec.statemachine.DecodingState;
import org.apache.mina.filter.codec.statemachine.DecodingStateMachine;
import org.apache.mina.filter.codec.statemachine.FixedLengthDecodingState;

import com.googlecode.asyncweb2.utils.MyLog;


/**
 * Parses HTTP requests.
 * Clients should register a <code>HttpRequestParserListener</code>
 * in order to receive notifications at important stages of request
 * building.<br/>
 *
 * <code>HttpRequestParser</code>s should not be built for each request
 * as each parser constructs an underlying state machine which is
 * relatively costly to build.<br/> Instead, parsers should be pooled.<br/>
 *
 * Note, however, that a parser <i>must</i> be <code>prepare</code>d before
 * each new parse.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
abstract class HttpRequestDecodingState extends DecodingStateMachine {
    //private static final Logger LOG = LoggerFactory.getLogger(HttpRequestDecodingState.class);
	private static final MyLog LOG = MyLog.getLog(HttpRequestDecodingState.class);
    /**
     * The request we are building
     */
    private MutableHttpRequest request;

    @Override
    protected DecodingState init() throws Exception {
        request = new DefaultHttpRequest();
        return SKIP_EMPTY_LINES;
    }

    @Override
    protected void destroy() throws Exception {
        request = null;
    }

    private final DecodingState SKIP_EMPTY_LINES = new CrLfDecodingState() {

        @Override
        protected DecodingState finishDecode(boolean foundCRLF,
                ProtocolDecoderOutput out) throws Exception {
            if (foundCRLF) {
                return this;
            } else {
                return READ_REQUEST_LINE;
            }
        }
    };

    private final DecodingState READ_REQUEST_LINE = new HttpRequestLineDecodingState() {
        @Override
        protected DecodingState finishDecode(List<Object> childProducts, ProtocolDecoderOutput out) throws Exception {
        	if (childProducts.size()<=1) {// 20090720 DONE 处理close时候IndexOutOfBoundsException错误。（HttpRequestDecodingState.java:88）
        		return null;
        	}else {
        		URI requestUri = (URI) childProducts.get(1);//java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
	            request.setMethod((HttpMethod) childProducts.get(0));
	            request.setRequestUri(requestUri);
	            request.setProtocolVersion((HttpVersion) childProducts.get(2));
	            request.setParameters(requestUri.getRawQuery());
	            return READ_HEADERS;
        	}
        }
//      0720.152311.477 [pool-1-thread-2 ] DEBUG L:166 DecodingStateMachine - Ignoring the exception caused by a closed session.
//      java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
//      	at java.util.ArrayList.RangeCheck(ArrayList.java:546)
//      	at java.util.ArrayList.get(ArrayList.java:321)
//      	at org.apache.mina.filter.codec.http.db.finishDecode(HttpRequestDecodingState.java:88)
//      	at org.apache.mina.filter.codec.statemachine.DecodingStateMachine.finishDecode(DecodingStateMachine.java:170)
//      	at org.apache.mina.filter.codec.statemachine.DecodingStateMachine.finishDecode(DecodingStateMachine.java:153)
//      	at org.apache.mina.filter.codec.statemachine.DecodingStateProtocolDecoder.finishDecode(DecodingStateProtocolDecoder.java:100)
//      	at org.apache.mina.filter.codec.ProtocolCodecFilter.sessionClosed(ProtocolCodecFilter.java:347)
    };

    private final DecodingState READ_HEADERS = new HttpHeaderDecodingState() {
        @Override
        @SuppressWarnings("unchecked")
        protected DecodingState finishDecode(List<Object> childProducts, ProtocolDecoderOutput out) throws Exception {
        	
        	{// yinshu 2013.4.26 修改福建http报文头和报文体中间的2个回车换行符变成了1个的问题
	        	if (childProducts.size() == 0){
	        		return null;
	        	}
        	}
            Map<String, List<String>> headers =
                (Map<String, List<String>>) childProducts.get(0);
            
            // Set cookies.
            List<String> cookies = headers.get(
                    HttpHeaderConstants.KEY_COOKIE);
            if (cookies != null && !cookies.isEmpty()) {
                if (cookies.size() > 1) {
                	LOG.warn("Ignoring extra cookie headers: %d", cookies.subList(1, cookies.size()));
                }
                request.setCookies(cookies.get(0));
            }

            // Set headers.
            request.setHeaders(headers);
            LOG.debug("Decoded header: %s", request.getHeaders());
            // Select appropriate body decoding state.
            boolean isChunked = false;
            if (request.getProtocolVersion() == HttpVersion.HTTP_1_1) {
                LOG.debug("Request is HTTP 1/1. Checking for transfer coding");
                isChunked = isChunked(request);
            } else {
                LOG.debug("Request is not HTTP 1/1. Using content length");
            }
            DecodingState nextState;
            if (isChunked) {
                LOG.debug("Using chunked decoder for request");
                nextState = new ChunkedBodyDecodingState() {
                    @Override
                    protected DecodingState finishDecode(
                            List<Object> childProducts,
                            ProtocolDecoderOutput out) throws Exception {
                        if (childProducts.size() != 1) {
                            int chunkSize = 0;
                            for (Object product : childProducts) {
                                IoBuffer chunk = (IoBuffer) product;
                                chunkSize += chunk.remaining();
                            }
                            IoBuffer body = IoBuffer.allocate(chunkSize);
                            for (Object product : childProducts) {
                                IoBuffer chunk = (IoBuffer) product;
                                body.put(chunk);
                            }
                            body.flip();
                            request.setContent(body);
                        } else {
                            request.setContent((IoBuffer) childProducts.get(0));
                        }

                        out.write(request);
                        return null;
                    }
                };
            } else {
                int length = getContentLength(request);
                if (length > 0) {
                        LOG.debug("Using fixed length decoder for request with length %d", length);
                    nextState = new FixedLengthDecodingState(length) {
                        @Override
                        protected DecodingState finishDecode(IoBuffer readData,
                                ProtocolDecoderOutput out) throws Exception {
                            request.setContent(readData);
                            out.write(request);
                            return null;
                        }
                    };
                } else {
                    LOG.debug("No entity body for this request");
                    out.write(request);
                    nextState = null;
                }
            }
            return nextState;
        }

        /**
         * Obtains the content length from the specified request
         *
         * @param request  The request
         * @return         The content length, or 0 if not specified
         * @throws HttpDecoderException If an invalid content length is specified
         */
        private int getContentLength(HttpRequest request)
                throws ProtocolDecoderException {
            int length = 0;
            String lengthValue = request.getHeader(
                    HttpHeaderConstants.KEY_CONTENT_LENGTH);
            if (lengthValue != null) {
                try {
                    length = Integer.parseInt(lengthValue);
                } catch (NumberFormatException e) {
                    HttpCodecUtils.throwDecoderException(
                            "Invalid content length: " + length,
                            HttpResponseStatus.BAD_REQUEST);
                }
            }
            return length;
        }

        /**
         * Determines whether a specified request employs a chunked
         * transfer coding
         *
         * @param request  The request
         * @return         <code>true</code> iff the request employs a
         *                 chunked transfer coding
         * @throws HttpDecoderException
         *                 If the request employs an unsupported coding
         */
        private boolean isChunked(HttpRequest request)
                throws ProtocolDecoderException {
            boolean isChunked = false;
            String coding = request.getHeader(
                    HttpHeaderConstants.KEY_TRANSFER_ENCODING);
            if (coding == null) {
                coding = request.getHeader(
                        HttpHeaderConstants.KEY_TRANSFER_CODING);
            }

            if (coding != null) {
                int extensionIndex = coding.indexOf(';');
                if (extensionIndex != -1) {
                    coding = coding.substring(0, extensionIndex);
                }
                if (HttpHeaderConstants.VALUE_CHUNKED.equalsIgnoreCase(coding)) {
                    isChunked = true;
                } else {
                    // As we only support chunked encoding, any other encoding
                    // is unsupported
                    HttpCodecUtils.throwDecoderException(
                            "Unknown transfer coding " + coding,
                            HttpResponseStatus.NOT_IMPLEMENTED);
                }
            }
            return isChunked;
        }
    };
}

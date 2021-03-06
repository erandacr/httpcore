/*
 * $HeadURL$
 * $Revision$
 * $Date$
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http;


import junit.framework.TestCase;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.mockup.SimpleHttpRequestHandlerResolver;
import org.apache.http.mockup.TestHttpSSLClient;
import org.apache.http.mockup.TestHttpSSLServer;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.protocol.BufferingHttpClientHandler;
import org.apache.http.nio.protocol.BufferingHttpServiceHandler;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpExpectationVerifier;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/**
 * Base class for all HttpCore NIO tests
 *
 */
public class HttpCoreNIOSSLTestBase extends TestCase {

    public HttpCoreNIOSSLTestBase(String testName) {
        super(testName);
    }

    protected TestHttpSSLServer server;
    protected TestHttpSSLClient client;
    
    @Override
    protected void setUp() throws Exception {
        HttpParams serverParams = new BasicHttpParams();
        serverParams
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "TEST-SERVER/1.1");
        
        this.server = new TestHttpSSLServer(serverParams);
        
        HttpParams clientParams = new BasicHttpParams();
        clientParams
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
            .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.USER_AGENT, "TEST-CLIENT/1.1");
        
        this.client = new TestHttpSSLClient(clientParams);
    }

    @Override
    protected void tearDown() throws Exception {
        this.server.shutdown();
        this.client.shutdown();
    }
    
    protected NHttpServiceHandler createHttpServiceHandler(
            final HttpRequestHandler requestHandler,
            final HttpExpectationVerifier expectationVerifier,
            final EventListener eventListener) {
        
        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        BufferingHttpServiceHandler serviceHandler = new BufferingHttpServiceHandler(
                httpproc,
                new DefaultHttpResponseFactory(),
                new DefaultConnectionReuseStrategy(),
                this.server.getParams());

        serviceHandler.setHandlerResolver(
                new SimpleHttpRequestHandlerResolver(requestHandler));
        serviceHandler.setExpectationVerifier(expectationVerifier);
        serviceHandler.setEventListener(eventListener);
        
        return serviceHandler;
    }
    
    protected NHttpClientHandler createHttpClientHandler(
            final HttpRequestExecutionHandler requestExecutionHandler,
            final EventListener eventListener) {
        
        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        httpproc.addInterceptor(new RequestConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        httpproc.addInterceptor(new RequestExpectContinue());

        BufferingHttpClientHandler clientHandler = new BufferingHttpClientHandler(
                httpproc,
                requestExecutionHandler,
                new DefaultConnectionReuseStrategy(),
                this.client.getParams());

        clientHandler.setEventListener(eventListener);
        
        return clientHandler;
    }
    
}

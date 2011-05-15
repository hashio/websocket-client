/*
 * The MIT License
 * 
 * Copyright (c) 2011 Takahiro Hashimoto
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.a840.websocket.handshake;

import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jp.a840.websocket.BufferManager;
import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.proxy.ProxyCredentials;
import jp.a840.websocket.util.StringUtil;

/**
 * HTTP Proxy Handshake class
 * 
 * <pre>
 * client => server
 * CONNECT ws.example.com HTTP/1.1
 * Host: ws.example.com
 * 
 * server => client
 * HTTP/1.1 200 Connection established
 * Proxy-agent:
 * </pre>
 *
 * @author Takahiro Hashimoto
 */
public class ProxyHandshake {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ProxyHandshake.class
			.getName());

	/** The origin. */
	private final InetSocketAddress origin;
	
	/** The credentials. */
	private ProxyCredentials credentials;
	
	/** The selector. */
	private Selector selector;
	
	private HttpResponseHeaderParser httpResponseHeaderParser;

	/**
	 * Instantiates a new proxy handshake.
	 *
	 * @param origin the origin
	 */
	public ProxyHandshake(InetSocketAddress origin){
		this(origin, null);
	}

	/**
	 * Instantiates a new proxy handshake.
	 *
	 * @param origin the origin
	 * @param credentials the credentials
	 */
	public ProxyHandshake(InetSocketAddress origin, ProxyCredentials credentials){
		this.origin = origin;
		this.credentials = credentials;
	}

	/**
	 * Do handshake.
	 *
	 * @param socket the socket
	 * @throws WebSocketException the web socket exception
	 */
	public void doHandshake(SocketChannel socket) throws WebSocketException {
		try {
			BufferManager bufferManager = new BufferManager();
			httpResponseHeaderParser = new HttpResponseHeaderParser();

			// create selector for Proxy handshake
			selector = Selector.open();
			socket.register(selector, OP_READ);

			ByteBuffer request = createHandshakeRequest();
			socket.write(request);
			
			ByteBuffer responseBuffer = ByteBuffer.allocate(8192);
			selector.select();
			boolean completed = false;
			do {
				responseBuffer.clear();
				socket.read(responseBuffer);
				responseBuffer.flip();
				responseBuffer = bufferManager.getBuffer(responseBuffer);
				completed = parseHandshakeResponseHeader(responseBuffer);
				if(!completed){
					bufferManager.storeFragmentBuffer(responseBuffer);
				}
			} while(!completed);
			
			HttpHeader responseHeader = httpResponseHeaderParser.getResponseHeader();
		} catch (IOException ioe) {
			throw new WebSocketException(3100, ioe);
		} finally {
			try{
				if(selector != null){
					selector.close();
				}
			}catch(IOException e){
				;
			}
		}
	}

	/**
	 * Creates the handshake request.
	 *
	 * @return the byte buffer
	 */
	public ByteBuffer createHandshakeRequest() {		
		// Send GET request to server
		StringBuilder sb = new StringBuilder();
		String host = origin.getHostName() + ":" + origin.getPort();
		sb.append("CONNECT " + host + " HTTP/1.1\r\n");
		StringUtil.addHeader(sb, "Host", host);
		sb.append("\r\n");

		try{
			return ByteBuffer.wrap(sb.toString().getBytes("US-ASCII"));
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}

	/**
	 * Parses the handshake response header.
	 *
	 * @param buffer the buffer
	 * @return true, if successful
	 * @throws WebSocketException the web socket exception
	 */
	protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
			throws WebSocketException {
		// METHOD
		// HTTP/1.1 101 Switching Protocols
		String line = StringUtil.readLine(buffer);
		if(line == null){
			return false;
		}
		if (!(line.startsWith("HTTP/1.0") || line.startsWith("HTTP/1.1"))) {
			throw new WebSocketException(3101,
					"Invalid server response.(HTTP version) " + line);
		}
		int responseStatus = Integer.valueOf(line.substring(9, 12));
		if (responseStatus != 200) {
			if(responseStatus == 407){
				throw new WebSocketException(3102,
						"Proxy Authentication is not supported yet. (Status Code) " + line);
			}else{
				throw new WebSocketException(3102,
					"Invalid server response.(Status Code) " + line);
			}
		}

		httpResponseHeaderParser.parse(buffer);
		return httpResponseHeaderParser.isCompleted();
	}

}

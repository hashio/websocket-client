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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.util.PacketDumpUtil;


/**
 * The Class SSLHandshake.
 *
 * @author Takahiro Hashimoto
 */
public class SSLHandshake {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SSLHandshake.class.getName());

	/** The ctx. */
	private SSLContext ctx;
	
	/** The engine. */
	private SSLEngine engine;

	/** The delegated task executor. */
	private ExecutorService delegatedTaskExecutor;

	/** The dummy. */
	private static ByteBuffer dummy = ByteBuffer.allocate(1024);
	
	/** The selector. */
	private Selector selector;
	
	/** The socket. */
	private SocketChannel socket;
	
	/**
	 * Instantiates a new sSL handshake.
	 *
	 * @param endpoint the endpoint
	 * @throws WebSocketException the web socket exception
	 */
	public SSLHandshake(InetSocketAddress endpoint) throws WebSocketException {
		try {
			ctx = SSLContext.getInstance("TLS");
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance("SunX509");
			tmf.init((KeyStore)null);
			ctx.init(null, tmf.getTrustManagers(), null);

			// Create SSLEngine
			engine = ctx.createSSLEngine(endpoint.getHostName(),
					endpoint.getPort());
			engine.setUseClientMode(true);
		} catch (NoSuchAlgorithmException e) {
			throw new WebSocketException(3810, e);
		} catch (KeyStoreException e) {
			throw new WebSocketException(3811, e);
		} catch (KeyManagementException e) {
			throw new WebSocketException(3812, e);
		}
		delegatedTaskExecutor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Do handshake.
	 *
	 * @param socket the socket
	 * @throws WebSocketException the web socket exception
	 */
	public void doHandshake(SocketChannel socket) throws WebSocketException {
		this.socket = socket;
		
		try {
			// create selector for SSL handshake
			selector = Selector.open();
			this.socket.register(selector, OP_READ);

			engine.beginHandshake();
			ByteBuffer netBuffer = ByteBuffer.allocate(0x8000);
			while (true) {
				// handling Handshake Status
				/*
				 * normal handshake status transition
				 * - beginHandshake()
				 *   next state -> NEED_WRAP
				 * - wrap(dummy)
				 *   next state -> NEED_TASK
				 * - runDelegatedTask()
				 *   next state -> NEED_UNWRAP
				 * - unwrap(server response)
				 *   next state -> NEED_TASK
				 *   ...
				 *   FINISH or NOT_HANDSHAKING is end of handshake
				 */
				HandshakeStatus haStatus = engine.getHandshakeStatus();
				if(log.isLoggable(Level.FINER)){
					log.finer("SSL HandshakeStatus: " + haStatus);
				}
				switch (haStatus) {
				case NEED_WRAP:
					wrap(dummy, netBuffer);
					if(netBuffer.hasRemaining()){
						if(PacketDumpUtil.isDump(PacketDumpUtil.HS_UP)){
							PacketDumpUtil.printPacketDump("SSL_HS_UP", netBuffer);
						}
						socket.write(netBuffer);
					}
					break;
				case NEED_UNWRAP:
					selector.select();
					netBuffer.clear();
					socket.read(netBuffer);
					netBuffer.flip();
					if(PacketDumpUtil.isDump(PacketDumpUtil.HS_DOWN)){
						PacketDumpUtil.printPacketDump("SSL_HS_DOWN", netBuffer);
					}
					SSLEngineResult res;
					do {
						res = engine.unwrap(netBuffer, dummy);
						if(log.isLoggable(Level.FINER)){
							log.finer("res: \n" + res);
							log.finer("buffer remaing: " + netBuffer.remaining());
						}
					} while (res.getStatus() == SSLEngineResult.Status.OK
							&& res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
							&& netBuffer.remaining() > 0);
					break;
				case NEED_TASK:
					runDelegatedTasks();
					break;
				case NOT_HANDSHAKING:
				case FINISHED:
					return;
				}
			}
		} catch (SSLException e) {
			throw new WebSocketException(3815, e);
		} catch (IOException ioe) {
			throw new WebSocketException(3816, ioe);
		} finally {
			try{
				selector.close();
			}catch(IOException ioe){
				;
			}
		}
	}
	
	/**
	 * Wrap.
	 *
	 * @param localBuffer the local buffer
	 * @param netBuffer the net buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void wrap(ByteBuffer localBuffer, ByteBuffer netBuffer) throws WebSocketException {
		try {
			netBuffer.clear();
			SSLEngineResult result = engine.wrap(localBuffer, netBuffer);
			if(log.isLoggable(Level.FINEST)){
				log.finest("SSLEngineResult\n" + result);
			}
			netBuffer.flip();
		} catch (SSLException e) {
			throw new WebSocketException(3817, e);
		}
	}
	
	/**
	 * Unwrap.
	 *
	 * @param netBuffer the net buffer
	 * @param localBuffer the local buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void unwrap(ByteBuffer netBuffer, ByteBuffer localBuffer) throws WebSocketException {
		try {
			localBuffer.clear();
			SSLEngineResult result = engine.unwrap(netBuffer, localBuffer);
			if(log.isLoggable(Level.FINEST)){
				log.finest("SSLEngineResult\n" + result);
			}
			localBuffer.flip();
		} catch (SSLException se) {
			throw new WebSocketException(3818, se);
		}
	}
	
    /**
     * Run delegated tasks.
     */
    private void runDelegatedTasks() {
    	Runnable task = null;
        while ((task = engine.getDelegatedTask()) != null) {
            delegatedTaskExecutor.execute(task);
        }
    }

}

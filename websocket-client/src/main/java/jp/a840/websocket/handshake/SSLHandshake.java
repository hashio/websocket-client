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

public class SSLHandshake {
	
	private static Logger log = Logger.getLogger(SSLHandshake.class.getName());

	private SSLContext ctx;
	private SSLEngine engine;

	private ExecutorService delegatedTaskExecutor;

	private static ByteBuffer dummy = ByteBuffer.allocate(65535);
	private Selector selector;
	private SocketChannel socket;
	
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
	
	public void setSocketChannel(SocketChannel socket) throws IOException {
		this.socket = socket;
	}
	
	public void doHandshake() throws WebSocketException {
		try {
			// create selector for SSL handshake
			selector = Selector.open();
			this.socket.register(selector, OP_READ);

			engine.beginHandshake();
			ByteBuffer netBuffer = ByteBuffer.allocate(65535);
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
						socket.write(netBuffer);
					}
					break;
				case NEED_UNWRAP:
					selector.select();
					netBuffer.clear();
					socket.read(netBuffer);
					netBuffer.flip();
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
	
	public void wrap(ByteBuffer localBuffer, ByteBuffer netBuffer) throws WebSocketException {
		try {
			netBuffer.clear();
			engine.wrap(localBuffer, netBuffer);
			netBuffer.flip();
		} catch (SSLException e) {
			throw new WebSocketException(3817, e);
		}
	}
	
	public void unwrap(ByteBuffer netBuffer, ByteBuffer localBuffer) throws WebSocketException {
		try {
			localBuffer.clear();
			engine.unwrap(netBuffer, localBuffer);
			localBuffer.flip();
		} catch (SSLException se) {
			throw new WebSocketException(3818, se);
		}
	}
	
    private void runDelegatedTasks() {
    	Runnable task = null;
        while ((task = engine.getDelegatedTask()) != null) {
            delegatedTaskExecutor.execute(task);
        }
    }

}

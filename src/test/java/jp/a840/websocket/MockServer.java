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
package jp.a840.websocket;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;

import jp.a840.websocket.util.PacketDumpUtil;

import org.junit.Assert;

/**
 * The Class MockServer.
 *
 * @author Takahiro Hashimoto
 */
public class MockServer extends Thread {
	
	/** The scenario list. */
	private List<Scenario> scenarioList = new ArrayList<MockServer.Scenario>();

	/** The port. */
	private int port;

	/** The exchanger. */
	private Exchanger<Throwable> exchanger = new Exchanger<Throwable>();

	/** The start latch. */
	private CountDownLatch startLatch;
	
	/** The mask. */
	private boolean mask;
	
	/**
	 * Instantiates a new mock server.
	 *
	 * @param port the port
	 * @param mask the mask
	 */
	public MockServer(int port, boolean mask) {
		this.port = port;
		this.mask = mask;
		
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				try{
					exchanger.exchange(e);
				}catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Gets the throwable.
	 *
	 * @return the throwable
	 */
	public Throwable getThrowable(){
		try{
			return exchanger.exchange(null);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		ServerSocketChannel serverSocketChannel = null;
		SocketChannel socket = null;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
			// start listening
			startLatch.countDown();
			socket = serverSocketChannel.accept();
			socket.configureBlocking(false);
			Selector selector = Selector.open();
			

			int requestCount = 1;
			int responseCount = 1;
			for (Scenario scenario : scenarioList) {
				switch (scenario.getScenarioType()) {
				case READ:
					PacketDumpUtil.printPacketDump("response" + responseCount, scenario.getResponse());
					responseCount++;
					socket.register(selector, OP_WRITE);
					selector.select();
					socket.write(scenario.getResponse());
					break;
				case WRITE:
					socket.register(selector, OP_READ);
					selector.select();
					ByteBuffer buffer = ByteBuffer.allocate(8192);
					socket.read(buffer);
					buffer.flip();
					PacketDumpUtil.printPacketDump("request" + requestCount, buffer);
					requestCount++;
					scenario.verifyRequest(buffer);
					break;
				case CLOSE:
					ByteBuffer responseBuffer = scenario.getResponse();
					if(responseBuffer != null){
						socket.register(selector, OP_WRITE);
						selector.select();
						PacketDumpUtil.printPacketDump("response" + responseCount, scenario.getResponse());
						responseCount++;
						socket.write(scenario.getResponse());
					}
					socket.close();
				}
			}
			exchanger.exchange(null);
		} catch (Throwable t) {
			try{
				exchanger.exchange(t);
			}catch(InterruptedException e){
				;
			}
		} finally {
			try{
				if(socket != null){
					socket.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			try{
				if(serverSocketChannel != null){
					serverSocketChannel.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * The Enum ScenarioType.
	 *
	 * @author Takahiro Hashimoto
	 */
	enum ScenarioType {
		
		/** The READ. */
		READ, 
		/** The WRITE. */
		WRITE,
		/** The CLOSE. */
		CLOSE;
	}

	/**
	 * Adds the response.
	 *
	 * @param response the response
	 */
	public void addResponse(ByteBuffer response) {
		Scenario scenario = new Scenario(ScenarioType.READ);
		scenario.setResponse(response);
		scenarioList.add(scenario);
	}

	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 */
	public void addRequest(VerifyRequest verifyRequest) {
		addRequest(verifyRequest, this.mask);
	}
	
	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 * @param mask the mask
	 */
	public void addRequest(VerifyRequest verifyRequest, boolean mask) {
		Scenario scenario = new Scenario(ScenarioType.WRITE);
		if(mask){
			verifyRequest = new VerifyUnmaskRequest(verifyRequest);
		}
		scenario.setVerifyRequest(verifyRequest);
		scenarioList.add(scenario);
	}
	
	/**
	 * Adds the close.
	 *
	 * @param response the response
	 */
	public void addClose(ByteBuffer response) {
		Scenario scenario = new Scenario(ScenarioType.CLOSE);
		scenario.setResponse(response);
		scenarioList.add(scenario);
	}	

	/**
	 * The Interface VerifyRequest.
	 *
	 * @author Takahiro Hashimoto
	 */
	public interface VerifyRequest {
		
		/**
		 * Verify.
		 *
		 * @param request the request
		 */
		public void verify(ByteBuffer request);
	}
	
	/**
	 * The Class VerifyUnmaskRequest.
	 *
	 * @author Takahiro Hashimoto
	 */
	public class VerifyUnmaskRequest implements VerifyRequest {
		
		/** The delegate. */
		private VerifyRequest delegate;
		
		/**
		 * Instantiates a new verify unmask request.
		 *
		 * @param vr the vr
		 */
		public VerifyUnmaskRequest(VerifyRequest vr){
			delegate = vr;
		}
		
		/* (non-Javadoc)
		 * @see jp.a840.websocket.MockServer.VerifyRequest#verify(java.nio.ByteBuffer)
		 */
		public void verify(ByteBuffer request){
			ByteBuffer unmaskedBuffer = ByteBuffer.allocate(request.remaining() - 4);
			byte[] seed = new byte[4];
			request.get(seed);
			
			int c = 0;
			while(unmaskedBuffer.hasRemaining()){
				unmaskedBuffer.put((byte)(request.get() ^ seed[c]));
				c++;
				if(c >= seed.length){
					c = 0;
				}
			}
			unmaskedBuffer.flip();
			delegate.verify(unmaskedBuffer);
		}
	}
	
	/**
	 * The Class Scenario.
	 *
	 * @author Takahiro Hashimoto
	 */
	public class Scenario {
		
		/** The scenario type_. */
		private ScenarioType scenarioType_;

		/**
		 * Instantiates a new scenario.
		 *
		 * @param scenarioType the scenario type
		 */
		public Scenario(ScenarioType scenarioType){
			scenarioType_ = scenarioType;
		}
		
		/** The response. */
		private ByteBuffer response;
		
		/** The verify request. */
		private VerifyRequest verifyRequest;

		/**
		 * Gets the response.
		 *
		 * @return the response
		 */
		public ByteBuffer getResponse() {
			return response;
		}

		/**
		 * Sets the response.
		 *
		 * @param response the new response
		 */
		public void setResponse(ByteBuffer response) {
			this.response = response;
		}

		/**
		 * Sets the verify request.
		 *
		 * @param verifyRequest the new verify request
		 */
		public void setVerifyRequest(VerifyRequest verifyRequest) {
			this.verifyRequest = verifyRequest;
		}

		/**
		 * Verify request.
		 *
		 * @param request the request
		 * @return true, if successful
		 */
		public void verifyRequest(ByteBuffer request) {
			verifyRequest.verify(request);
		}

		/**
		 * Gets the scenario type.
		 *
		 * @return the scenario type
		 */
		public ScenarioType getScenarioType() {
			return scenarioType_;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		startLatch = new CountDownLatch(1);
		super.start();
		try{
			startLatch.await();
		}catch(InterruptedException e){
			Assert.fail(e.getMessage());
		}
	}
}

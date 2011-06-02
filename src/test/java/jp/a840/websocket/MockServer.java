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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import static java.nio.channels.SelectionKey.*;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import javax.net.ServerSocketFactory;

import jp.a840.websocket.util.PacketDumpUtil;

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

	/**
	 * Instantiates a new mock server.
	 *
	 * @param port the port
	 */
	public MockServer(int port) {
		this.port = port;
		
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
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
			SocketChannel socket = serverSocketChannel.accept();
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
		Scenario scenario = new Scenario(ScenarioType.WRITE);
		scenario.setVerifyRequest(verifyRequest);
		scenarioList.add(scenario);
	}
	
	public void addConnectionClose(ByteBuffer response) {
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
	 * The Class Scenario.
	 *
	 * @author Takahiro Hashimoto
	 */
	public class Scenario {
		private ScenarioType scenarioType_;

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
}

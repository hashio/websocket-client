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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import javax.net.ServerSocketFactory;

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
			ServerSocket serverSocket = ServerSocketFactory.getDefault()
					.createServerSocket(this.port);
			Socket socket = serverSocket.accept();
			for (Scenario scenario : scenarioList) {
				switch (scenario.getScenarioType()) {
				case READ:
					OutputStream os = socket.getOutputStream();
					os.write(scenario.getResponse());
					break;
				case WRITE:
					InputStream is = socket.getInputStream();
					byte[] buf = new byte[is.available()];
					is.read(buf);
					scenario.verifyRequest(buf);
					break;
				}
			}
			exchanger.exchange(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
 WRITE;
	}

	/**
	 * Adds the response.
	 *
	 * @param response the response
	 */
	public void addResponse(byte[] response) {
		Scenario scenario = new Scenario();
		scenario.setResponse(response);
		scenarioList.add(scenario);
	}

	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 */
	public void addRequest(VerifyRequest verifyRequest) {
		Scenario scenario = new Scenario();
		scenario.setVerifyRequest(verifyRequest);
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
		 * @return true, if successful
		 */
		public boolean verify(byte[] request);
	}
	
	/**
	 * The Class Scenario.
	 *
	 * @author Takahiro Hashimoto
	 */
	public class Scenario {

		/** The response. */
		private byte[] response;
		
		/** The verify request. */
		private VerifyRequest verifyRequest;

		/**
		 * Gets the response.
		 *
		 * @return the response
		 */
		public byte[] getResponse() {
			return response;
		}

		/**
		 * Sets the response.
		 *
		 * @param response the new response
		 */
		public void setResponse(byte[] response) {
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
		public boolean verifyRequest(byte[] request) {
			return verifyRequest.verify(request);
		}

		/**
		 * Gets the scenario type.
		 *
		 * @return the scenario type
		 */
		public ScenarioType getScenarioType() {
			if (response == null) {
				return ScenarioType.WRITE;
			}
			return ScenarioType.READ;
		}
	}
}

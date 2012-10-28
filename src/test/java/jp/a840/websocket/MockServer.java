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
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.Maskable;
import jp.a840.websocket.frame.rfc6455.FrameRfc6455;
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

    private Scenario errorScenario;

	/** The port. */
	private int port;

    private Queue<Throwable> throwableQueue = new LinkedBlockingQueue<Throwable>();

	/** The start latch. */
	private CountDownLatch startLatch;
	
	/** The client version. */
	private int version;
	
	/**
	 * Instantiates a new mock server.
	 *
	 * @param port the port
	 * @param version the version
	 */
	public MockServer(int port, int version) {
		this.port = port;
		this.version = version;
		
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
                throwableQueue.offer(e);
			}
		});
	}
	
	/**
	 * Gets the throwable.
	 *
	 * @return the throwable
	 */
	public Throwable getThrowable(){
        return throwableQueue.poll();
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
                Thread.sleep(scenario.getSleepTime());
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
                    socket = null;
				}
			}
		} catch (Throwable t) {
//            startLatch.countDown();
            throwableQueue.offer(t);
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
		CLOSE
	}

	/**
	 * Adds the response.
	 *
	 * @param response the response
	 */
	public void addResponse(ByteBuffer response) {
        addResponse(response, 0);
	}

    /**
     * Adds the response
     *
     * @param response
     * @param sleepTime
     */
    public void addResponse(ByteBuffer response, long sleepTime) {
        Scenario scenario = new Scenario(ScenarioType.READ, sleepTime);
      	scenario.setResponse(response);
      	scenarioList.add(scenario);
    }

    /**
     * Adds the response
     *
     * @param frame
     */
    public void addResponse(Frame frame) {
        addResponse(frame, 0);
    }

    /**
     * Adds the response
     *
     * @param frame
     * @param sleepTime
     */
    public void addResponse(Frame frame, long sleepTime) {
   		Scenario scenario = new Scenario(ScenarioType.READ, sleepTime);
        if(frame instanceof Maskable){
            ((Maskable)frame).unmask();
        }
   		scenario.setResponse(frame.toByteBuffer());
   		scenarioList.add(scenario);
   	}

	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 */
	public void addRequest(VerifyRequest verifyRequest) {
		addRequest(verifyRequest, this.version, 0);
	}
	
	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 * @param version
	 */
    public void addRequest(VerifyRequest verifyRequest, int version) {
        addRequest(verifyRequest, version, 0);
    }

    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
   	 * @param version
     * @param sleepTime
   	 */
	public void addRequest(VerifyRequest verifyRequest, int version, long sleepTime) {
		Scenario scenario = new Scenario(ScenarioType.WRITE, sleepTime);
		verifyRequest = new VerifyUnmaskRequest(verifyRequest, version);
		scenario.setVerifyRequest(verifyRequest);
		scenarioList.add(scenario);
	}

    /**
     * Adds the request
     *
     * @param verifyRequest
     * @param version
     */
    public void addHttpRequest(VerifyRequest verifyRequest, int version) {
        addHttpRequest(verifyRequest, version, 0);
    }

    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
   	 * @param version
     * @param sleepTime
   	 */
   	public void addHttpRequest(VerifyRequest verifyRequest, int version, long sleepTime) {
   		Scenario scenario = new Scenario(ScenarioType.WRITE, sleepTime);
   		scenario.setVerifyRequest(verifyRequest);
   		scenarioList.add(scenario);
   	}

    /**
   	 * Adds the close.
   	 *
   	 * @param response the response
   	 */
   	public void addClose(ByteBuffer response) {
        addClose(response, 0);
    }

	/**
	 * Adds the close.
	 *
	 * @param response the response
     * @param sleepTime
	 */
	public void addClose(ByteBuffer response, long sleepTime) {
		Scenario scenario = new Scenario(ScenarioType.CLOSE, sleepTime);
		scenario.setResponse(response);
		scenarioList.add(scenario);
	}	

    /**
   	 * Adds the close.
   	 *
   	 * @param frame the frame
   	 */
    public void addClose(Frame frame) {
        addClose(frame, 0);
    }

   	public void addClose(Frame frame, long sleepTime) {
   		Scenario scenario = new Scenario(ScenarioType.CLOSE);
        if(frame instanceof Maskable){
            ((Maskable)frame).unmask();
        }
   		scenario.setResponse(frame.toByteBuffer());
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

        private int version;

		/**
		 * Instantiates a new verify unmask request.
		 *
		 * @param vr the vr
		 */
		public VerifyUnmaskRequest(VerifyRequest vr, int version){
			this.delegate = vr;
            this.version = version;
		}
		
		/* (non-Javadoc)
		 * @see jp.a840.websocket.MockServer.VerifyRequest#verify(java.nio.ByteBuffer)
		 */
		public void verify(ByteBuffer request){
			ByteBuffer unmaskedBuffer = ByteBuffer.allocate(request.remaining() - 4);
			byte[] seed = new byte[4];
            if(version > 6){
                int limit = request.limit();
                request.limit(request.position() + 2);
                unmaskedBuffer.put(request);
                request.limit(limit);
            }
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
		private final ScenarioType scenarioType;

        private final long sleepTime;

		/**
		 * Instantiates a new scenario.
		 *
		 * @param scenarioType the scenario type
		 */
		public Scenario(ScenarioType scenarioType){
			this.scenarioType = scenarioType;
            this.sleepTime = 0L;
		}
		
        public Scenario(ScenarioType scenarioType, long sleepTime){
      		this.scenarioType = scenarioType;
            this.sleepTime = sleepTime;
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
			return this.response;
		}

        public long getSleepTime() {
            return this.sleepTime;
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
			this.verifyRequest.verify(request);
		}

		/**
		 * Gets the scenario type.
		 *
		 * @return the scenario type
		 */
		public ScenarioType getScenarioType() {
			return this.scenarioType;
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

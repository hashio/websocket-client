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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.Maskable;
import jp.a840.websocket.frame.rfc6455.FrameRfc6455;
import jp.a840.websocket.frame.rfc6455.enums.PayloadLengthType;
import jp.a840.websocket.util.MaskDecoder;
import jp.a840.websocket.util.PacketDumpUtil;

import org.junit.Assert;

/**
 * The Class MockServer.
 *
 * @author Takahiro Hashimoto
 */
public class MockServer extends Thread {
	
	/** The scenario list. */
	private ScenarioSequencer seq = new ScenarioSequencer();

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
            ByteBuffer buffer1 = ByteBuffer.allocate(8192);
            ByteBuffer buffer2 = ByteBuffer.allocate(8192);
            int offset = 0;
            int position = 0;
            Scenario current = null;
			while (seq.hasNext()) {
                if(current == null){
                    current = seq.next();
                }
                Thread.sleep(seq.getSleepTime());
				switch (seq.getScenarioType()) {
				case READ:
                    ByteBuffer response = seq.getResponse();
                    current = null;
					PacketDumpUtil.printPacketDump("response" + responseCount, response);
					responseCount++;
					socket.register(selector, OP_WRITE);
					selector.select();
					socket.write(response);
					continue;
				case WRITE:
                    requestCount++;
                    if(position == buffer1.position()){
    					socket.register(selector, OP_READ);
	    				selector.select();
                        buffer1.mark();
                        buffer1.position(buffer1.position() + offset);
                        buffer1.limit(buffer1.capacity());
					    socket.read(buffer1);
                        buffer1.limit(buffer1.position());
					    buffer1.reset();
                    }

                    ByteBuffer buffer;
                    if(RequestType.HTTP.equals(seq.getRequestType())){
                        seq.verifyRequest(buffer1);
                        current = null;
                        offset = 0;
                        continue;
                    }else{
                        buffer = readBuffer(seq.isMask(), buffer1.slice());
                        if(buffer != null){
                            seq.verifyRequest(buffer);
                            current = null;
                            buffer1.position(buffer1.position() + buffer.capacity());
                            offset = 0;
                        }else{
                            if(buffer1.remaining() == 0){
                                current = null;
                            }
                            continue;
                        }
                    }
                    if(buffer1.remaining() > 0){
                        buffer2.put(buffer1);
                        offset = buffer2.position();
                        buffer2.rewind();
                        buffer1.clear();
                        ByteBuffer tmp = buffer1;
                        buffer1 = buffer2;
                        buffer2 = tmp;
                    }
					continue;
				case CLOSE:
					ByteBuffer responseBuffer = seq.getResponse();
                    current = null;
					if(responseBuffer != null){
						socket.register(selector, OP_WRITE);
						selector.select();
						PacketDumpUtil.printPacketDump("response" + responseCount, responseBuffer);
						responseCount++;
						socket.write(responseBuffer);
					}
					socket.close();
                    socket = null;
                    continue;
				}
			}
		} catch (Throwable t) {
//            startLatch.countDown();
            t.printStackTrace();
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

    private ByteBuffer readBuffer(boolean mask, ByteBuffer request){
        int min = mask ? 6 : 2;
        if(request.remaining() < min){
            return null;
         }

       byte header1 = request.get();
       byte header2 = request.get();

       PayloadLengthType payloadLengthType = PayloadLengthType.valueOf(header2);
       byte payloadLength1 = (byte) (header2 & 0x7F);
       long payloadLength2 = payloadLength1;

       switch (payloadLengthType) {
           case LEN_16:
               min += 2;
               if(request.remaining() < min){
                   return null;
                }

               payloadLength2 = 0xFFFF & request.getShort();
               break;
           case LEN_63:
               min += 8;
               if(request.remaining() < min){
                   return null;
                }
               payloadLength2 = 0x7FFFFFFFFFFFFFFFL & request.getLong();
               break;
       }

        byte[] seed = new byte[4];
        if(mask){
           request.get(seed);
        }
       if(request.remaining() < payloadLength2){
           request.position(request.position() - min);
           return null;
       }

        request.limit(min + (int)payloadLength2);

       if(mask){
           MaskDecoder decoder = new MaskDecoder(seed);
           decoder.decode(request);
       }
       System.out.println("PL:" + payloadLength2 + " PO:" + request.position() + " OFFSET:" + request.arrayOffset() + "CAPA:" + request.capacity());
        request.rewind();
        ByteBuffer tmp = request.slice();
       return tmp;
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

    enum RequestType {
        HTTP,WebSocket
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
      	seq.add(scenario);
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
   		seq.add(scenario);
   	}

	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 */
	public void addRequest(VerifyRequest verifyRequest) {
		addRequest(verifyRequest, false, this.version, 0);
	}
	
    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
   	 */
   	public void addMaskRequest(VerifyRequest verifyRequest) {
   		addRequest(verifyRequest, true, this.version, 0);
   	}

	/**
	 * Adds the request.
	 *
	 * @param verifyRequest the verify request
	 * @param version
	 */
    public void addRequest(VerifyRequest verifyRequest, int version) {
        addRequest(verifyRequest, false, version, 0);
    }

    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
   	 * @param version
   	 */
     public void addMaskRequest(VerifyRequest verifyRequest, int version) {
         addRequest(verifyRequest, true, version, 0);
     }

    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
   	 * @param version
     * @param sleepTime
   	 */
       public void addRequest(VerifyRequest verifyRequest, int version, long sleepTime) {
           addRequest(verifyRequest, false, version, sleepTime);
       }

       /**
      	 * Adds the request.
      	 *
      	 * @param verifyRequest the verify request
      	 * @param version
         * @param sleepTime
      	 */
        public void addMaskRequest(VerifyRequest verifyRequest, int version, long sleepTime) {
            addRequest(verifyRequest, true, version, sleepTime);
        }

    /**
   	 * Adds the request.
   	 *
   	 * @param verifyRequest the verify request
     * @param mask
   	 * @param version
     * @param sleepTime
   	 */
	public void addRequest(VerifyRequest verifyRequest, boolean mask, int version, long sleepTime) {
		Scenario scenario = new Scenario(ScenarioType.WRITE, sleepTime);
        scenario.setMask(mask);
        verifyRequest = new VerifyRequestDelegate(verifyRequest, version);
		scenario.setVerifyRequest(verifyRequest);
		seq.add(scenario);
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
   		Scenario scenario = new Scenario(ScenarioType.WRITE, RequestType.HTTP, sleepTime);
        scenario.setMask(false);
        verifyRequest = new VerifyRequestDelegate(verifyRequest, version);
   		scenario.setVerifyRequest(verifyRequest);
   		seq.add(scenario);
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
		seq.add(scenario);
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
   		seq.add(scenario);
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
	private class VerifyRequestDelegate implements VerifyRequest {

		/** The delegate. */
		private VerifyRequest delegate;

        private int version;

		/**
		 * Instantiates a new verify unmask request.
		 *
		 * @param vr the vr
		 */
		public VerifyRequestDelegate(final VerifyRequest vr, int version){
			this.delegate = new VerifyRequest() {
                public void verify(ByteBuffer request) {
//                    PacketDumpUtil.printPacketDump("request", request);
                    vr.verify(request);
                }
            };
            this.version = version;
		}
		
		/* (non-Javadoc)
		 * @see jp.a840.websocket.MockServer.VerifyRequest#verify(java.nio.ByteBuffer)
		 */
		public void verify(ByteBuffer request){
            int originalLimit = request.limit();
            try{
        		delegate.verify(request);
            }finally{
                request.position(request.limit());
                request.limit(originalLimit);
            }
		}
	}
	
    public class ScenarioSequencer {
        private List<Scenario> list = new ArrayList();

        private Iterator<Scenario> it;

        private Scenario current;

        public void add(Scenario scenario){
            list.add(scenario);
        }

        public boolean hasNext(){
            if(it == null){
                it = list.iterator();
            }
            return it.hasNext();
        }

        public Scenario next(){
            current = it.next();
            return current;
        }

        /**
         * Verify request.
         *
         * @param request the request
         * @return true, if successful
         */
        public void verifyRequest(ByteBuffer request) {
            current.verifyRequest.verify(request);
        }

        public long getSleepTime(){
            return current.getSleepTime();
        }

        /**
      		 * Gets the scenario type.
      		 *
      		 * @return the scenario type
      		 */
      		public ScenarioType getScenarioType() {
      			return current.scenarioType;
      		}

        public RequestType getRequestType(){
            return current.getRequestType();
        }

        /**
      		 * Gets the response.
      		 *
      		 * @return the response
      		 */
      		public ByteBuffer getResponse() {
      			ByteBuffer buf = current.response;
                return buf;
      		}

        public boolean isMask(){
            return current.isMask();
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
        private RequestType requestType;
        private boolean mask = false;

        /**
		 * Instantiates a new scenario.
		 *
		 * @param scenarioType the scenario type
		 */
		public Scenario(ScenarioType scenarioType){
			this.scenarioType = scenarioType;
            this.requestType = RequestType.WebSocket;
            this.sleepTime = 0L;
		}
		
        public Scenario(ScenarioType scenarioType, long sleepTime){
      		this.scenarioType = scenarioType;
            this.requestType = RequestType.WebSocket;
            this.sleepTime = sleepTime;
      	}

        public Scenario(ScenarioType scenarioType, RequestType requestType){
            this.scenarioType = scenarioType;
            this.requestType = requestType;
            this.sleepTime = 0L;
        }

        public Scenario(ScenarioType scenarioType, RequestType requestType, long sleepTime){
      		this.scenarioType = scenarioType;
            this.requestType = requestType;
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

        public RequestType getRequestType(){
            return this.requestType;
        }

        public boolean isMask() {
            return mask;
        }

        public void setMask(boolean mask) {
            this.mask = mask;
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

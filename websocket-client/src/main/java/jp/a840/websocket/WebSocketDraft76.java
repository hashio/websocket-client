package jp.a840.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.push.beans.RateBean;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameBuilder;
import jp.a840.websocket.frame.FrameBuilderDraft76;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.handler.WebSocketPipeline;


/**
 * A simple websocket client
 * @author t-hashimoto
 *
 */
public class WebSocketDraft76 extends WebSocketBase {
	private static Logger logger = Logger.getLogger(WebSocketDraft76.class.getName());
	
	private static final int VERSION = 6;
		
	private FrameBuilderDraft76 builder = new FrameBuilderDraft76();
	
	public WebSocketDraft76(String url, WebSocketHandler handler, String... protocols) throws MalformedURLException, IOException {
		super(url, handler, protocols);
		
		this.origin = System.getProperty("websocket.origin");
	}
	
	protected void initializePipeline(WebSocketPipeline pipeline){
	}
	

	/**
	 * Create a handshake requtest
	 * 
	 * Sample (Draft76)
	 * client => server
	 *   GET /chat HTTP/1.1
	 *   Host: server.example.com
	 *   Upgrade: websocket
	 *   Connection: Upgrade
	 *   Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
	 *   Sec-WebSocket-Origin: http://example.com
	 *   Sec-WebSocket-Protocol: chat, superchat
	 *   Sec-WebSocket-Version:6
	 *   
	 * @param socket
	 */
	@Override
	protected ByteBuffer createHandshakeRequest(){
		// Send GET request to server
		StringBuilder sb = new StringBuilder();
		sb.append("GET " + path + " HTTP/1.1\r\n");
		addHeader(sb, "Host", endpoint.getHostName());
		addHeader(sb, "Upgrade", "websocket");
		addHeader(sb, "Connection", "Upgrade");
		addHeader(sb, "Sec-WebSocket-Key1", SecWebSocketKey.generateKey());
		addHeader(sb, "Sec-WebSocket-Key2", SecWebSocketKey.generateKey());
		if (origin != null) {
			addHeader(sb, "Origin", origin);
		}
		if (protocols != null && protocols.length > 0) {
			addHeader(sb, "Sec-WebSocket-Protocol", join(",", protocols));
		}
		addHeader(sb, "Sec-WebSocket-Version", String.valueOf(getWebSocketVersion()));
		sb.append("\r\n");
		
		ByteBuffer buf = ByteBuffer.allocate(512);
		buf.put(sb.toString().getBytes());
		buf.put(SecWebSocketKey.generateKey3());
		buf.put((byte)0xd);
		buf.put((byte)0xa);
		buf.flip();
		
		return buf;
	}
	
	/**
	 * check handshake response
	 * 
	 * server => client
	 * HTTP/1.1 101 Switching Protocols
	 * Upgrade: websocket
	 * Connection: Upgrade
	 * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
	 * Sec-WebSocket-Protocol: chat
	 * 
	 * @param buffer
	 */
	protected void handshakeResponse(ByteBuffer buffer) throws WebSocketException {		
		String line = readLine(downstreamBuffer);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(line);
		}
		if (!line.startsWith("HTTP/1.1")) {
			throw new WebSocketException(3001,
					"Invalid server response.(HTTP version) " + line);
		}
		if (!"101".equals(line.substring(9, 12))) {
			throw new WebSocketException(3001,
					"Invalid server response.(Status Code) " + line);
		}

		// header lines
		do {
			line = readLine(downstreamBuffer);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(line);
			}
			if (line.indexOf(':') > 0) {
				String[] keyValue = line.split(":", 2);
				if(keyValue.length > 1){
					responseHeaderMap.put(keyValue[0].trim(), keyValue[1].trim());
				}
			}
		} while ("\r\n".compareTo(line) != 0);
	}
	
	private String generateWebSocketKey(){
		return SecWebSocketKey.generateKey();
	}
	
	protected void readFrame(List<Frame> frameList, ByteBuffer buffer)
			throws IOException {
		Frame frame = null;
		FrameHeader header = null;
		if (header == null) {
			// 1. create frame header
			header = builder.createFrameHeader(buffer);
			if (header == null) {
				handler.onError(this, new WebSocketException(3200));
				buffer.clear();
				return;
			}

			byte[] bodyData;
			if ((buffer.limit() - buffer.position()) < header.getFrameLength()) {
				if (header.getPayloadLength() <= 0xFFFF) {
					bodyData = new byte[(int) header.getPayloadLength()];
					int bufferLength = buffer.limit() - buffer.position();
					buffer.get(bodyData, 0, (int)Math.min(bufferLength, header.getPayloadLength()));
					if(bufferLength < header.getPayloadLength()){
						// read large buffer
						ByteBuffer largeBuffer = ByteBuffer.wrap(bodyData);
						socket.read(largeBuffer);
					}
				} else {
					// TODO large frame data
					throw new IllegalStateException("Not supported yet");
				}
			} else {
				bodyData = new byte[(int) header.getPayloadLength()];
				buffer.get(bodyData);
			}

			if (bodyData != null) {
				frame = builder.createFrame(header, bodyData);
				frameList.add(frame);
			}

			if (buffer.position() < buffer.limit()) {
				readFrame(frameList, buffer);
			}
		}
	}
	
	public static void main(String[] argv) throws Exception {
		WebSocketDraft76 ws = new WebSocketDraft76("ws://localhost:8088/rate",
		new WebSocketHandlerAdapter() {
			@Override
			public void onOpen(WebSocket socket) {
				logger.info("onOpen");
			}

			@Override
			public void onClose(WebSocket socket) {
				logger.info("onClose");
			}

			@Override
			public void onError(WebSocket socket, WebSocketException e) {
				e.printStackTrace();
			}

					@Override
					public void onMessage(WebSocket socket, Frame frame) {
						try {
							ByteArrayInputStream bais = new ByteArrayInputStream(
									frame.toByteBuffer().array());
							ObjectInputStream ois = new ObjectInputStream(bais);
							RateBean bean = (RateBean) ois.readObject();
							logger.info(bean.getCurrencyPair() + " "
									+ bean.getBid());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
		}
		,"rate");
		ws.setBlockingMode(false);
		ws.connect(); // connect and start receive messages;		
		Thread.sleep(60000);
		ws.close();
	}

	@Override
	protected int getWebSocketVersion() {
		return VERSION;
	}
	
	private static class SecWebSocketKey{
	    private static final Random random = new Random();

	    private static final long LARGEST_INTEGER = 4294967295L;
	    
	    private static final char[] CHARS = new char[84];
	    
	    static {
	    	int i = 0;
	    	for(int c = 0x21; c <= 0x2F; c++){
	    		CHARS[i++] = (char)c; 
	    	}
	    	for(int c = 0x3A; c <= 0x7E; c++){
	    		CHARS[i++] = (char)c; 
	    	}	    	
	    }
	    
		public static String generateKey(){
			int spaces = random.nextInt(12) + 1;
			long max = LARGEST_INTEGER / spaces;
			long number = Math.abs(random.nextLong()) % max;
			long product = number * spaces;
			
			StringBuilder key = new StringBuilder();
			key.append(product);
			
			int charsNum = random.nextInt(12) + 1;
			for(int i = 0; i < charsNum; i++){
				int position = random.nextInt(key.length());
				char c = CHARS[random.nextInt(CHARS.length)];
				key.insert(position, c);
			}
			
	        for (int i = 0; i < spaces; i++) {
	            int position = random.nextInt(key.length() - 1) + 1;
	            key.insert(position, ' ');
	        }
			return key.toString();
		}
		
		public static byte[] generateKey3(){
	        byte[] key3 = new byte[8];
	        random.nextBytes(key3);
	        return key3;
		}
	}
}

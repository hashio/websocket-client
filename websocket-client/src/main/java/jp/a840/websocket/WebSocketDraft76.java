package jp.a840.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.frame.draft76.BinaryFrame;
import jp.a840.websocket.frame.draft76.FrameBuilderDraft76;
import jp.a840.websocket.frame.draft76.TextFrame;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handshake.Handshake;

/**
 * A simple websocket client
 * this class is implement the WebSocket Draft76 specification.
 * 
 * @see http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76
 * @author t-hashimoto
 * 
 */
public class WebSocketDraft76 extends WebSocketBase {
	private static Logger logger = Logger.getLogger(WebSocketDraft76.class
			.getName());

	private static final int VERSION = 76;

	private FrameBuilderDraft76 builder = new FrameBuilderDraft76();

	public WebSocketDraft76(String url, WebSocketHandler handler,
			String... protocols) throws WebSocketException {
		super(url, handler, protocols);
	}

	@Override
	protected void initializePipeline(WebSocketPipeline pipeline) {
		super.initializePipeline(pipeline);
	}
	
	
	@Override
	protected Handshake newHandshakeInstance() {
		return new Handshake() {
			private ByteBuffer bodyBuf = ByteBuffer.allocate(16);
			
			/**
			 * Create a handshake requtest
			 * 
			 * <pre>
			 * Sample (Draft76) client => server
			 * GET /demo HTTP/1.1
			 * Host: example.com
			 * Connection: Upgrade
			 * Sec-WebSocket-Key2: 12998 5 Y3 1  .P00
			 * Sec-WebSocket-Protocol: sample
			 * Upgrade: WebSocket
			 * Sec-WebSocket-Key1: 4 @1  46546xW%0l 1 5
			 * Origin: http://example.com
			 * 
			 * ^n:ds[4U
			 * </pre>
			 * 
			 * @param socket
			 */
			@Override
			public ByteBuffer createHandshakeRequest() {
				bodyBuf.clear(); // initilize a buffer
				
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
				sb.append("\r\n");

				ByteBuffer buf = ByteBuffer.allocate(512);
				buf.put(sb.toString().getBytes());
				buf.put(SecWebSocketKey.generateKey3());
				buf.put((byte) 0xd);
				buf.put((byte) 0xa);
				buf.flip();

				return buf;
			}

			/**
			 * check handshake response
			 * 
			 * <pre>
			 * server => client
			 * HTTP/1.1 101 WebSocket Protocol Handshake
			 * Upgrade: WebSocket
			 * Connection: Upgrade
			 * Sec-WebSocket-Origin: http://example.com
			 * Sec-WebSocket-Location: ws://example.com/demo
			 * Sec-WebSocket-Protocol: sample
			 * 
			 * 8jKS'y:G*Co,Wxa-
			 * </pre>
			 * 
			 * @param buffer
			 */
			@Override
			protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
					throws WebSocketException {
				if(!super.parseHandshakeResponseHeader(buffer)){
					return false;
				}
				if(!"websocket".equalsIgnoreCase(this.getResponseHeaderMap().get("upgrade"))){
					throw new WebSocketException(3500, "Upgrade response header is not match websocket. Upgrade: " + responseHeaderMap.get("upgrade"));
				}
				if(!"upgrade".equalsIgnoreCase(this.getResponseHeaderMap().get("connection"))){
					throw new WebSocketException(3501, "Connection response header is not match Upgrade. Connection: " + responseHeaderMap.get("connection"));
				}
				String serverOrigin = this.getResponseHeaderMap().get("sec-websocket-origin");
				if(origin != null && serverOrigin != null && !serverOrigin.equals(origin)){
					throw new WebSocketException(3502, "Sec-WebSocket-Origin response header is not match request Origin header. Origin: " + origin + " Sec-WebSocket-Origin: " + serverOrigin);
				}
				String serverLocation = this.getResponseHeaderMap().get("sec-websocket-location");
				try{
					// reformat location URI.
					// drop custom port
					URI uri = new URI(location.getScheme(),
								  	location.getHost(),
								  	location.getPath(),
								  	location.getFragment()
								  	);
					if(serverLocation != null && !serverLocation.equals(uri.toString())){
						throw new WebSocketException(3503, "Sec-WebSocket-Location response header is not match request URL. request uri: " + uri.toString() + " Sec-WebSocket-Location: " + serverLocation);
					}
				}catch(URISyntaxException e){
					;
				}
				String protocolStr = this.getResponseHeaderMap().get("sec-websocket-protocol");
				if(protocolStr != null){
					serverProtocols = protocolStr.split(",");
				}
				return true;
			}

			@Override
			protected boolean parseHandshakeResponseBody(ByteBuffer buffer) throws WebSocketException {
				if(!super.parseHandshakeResponseBody(buffer)){
					return false;
				}
				
				if(buffer.remaining() < bodyBuf.capacity()){
					return false;
				}

				buffer.get(bodyBuf.array(), 0, bodyBuf.capacity());				
				return true;
			}

		};
	}	


	
	

	@Override
	protected int getWebSocketVersion() {
		return VERSION;
	}

	private static class SecWebSocketKey {
		private static final Random random = new Random();

		private static final long LARGEST_INTEGER = 4294967295L;

		private static final char[] CHARS = new char[84];

		static {
			int i = 0;
			for (int c = 0x21; c <= 0x2F; c++) {
				CHARS[i++] = (char) c;
			}
			for (int c = 0x3A; c <= 0x7E; c++) {
				CHARS[i++] = (char) c;
			}
		}

		public static String generateKey() {
			int spaces = random.nextInt(12) + 1;
			long max = LARGEST_INTEGER / spaces;
			long number = Math.abs(random.nextLong()) % max;
			long product = number * spaces;

			StringBuilder key = new StringBuilder();
			key.append(product);

			int charsNum = random.nextInt(12) + 1;
			for (int i = 0; i < charsNum; i++) {
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

		public static byte[] generateKey3() {
			byte[] key3 = new byte[8];
			random.nextBytes(key3);
			return key3;
		}
	}

	@Override
	public Frame createFrame(Object obj) throws WebSocketException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			
			byte[] bodyData = baos.toByteArray();
			return new BinaryFrame(bodyData);
		} catch (Exception e) {
			throw new WebSocketException(3550, e);
		}
	}

	@Override
	public Frame createFrame(String str) throws WebSocketException {
		return new TextFrame(str);
	}

	@Override
	protected FrameParser newFrameParserInstance() {
		return new FrameParser() {
			
			@Override
			protected FrameHeader createFrameHeader(ByteBuffer chunkData) {
				return builder.createFrameHeader(chunkData);
			}

			@Override
			protected Frame createFrame(FrameHeader h, byte[] bodyData) {
				return builder.createFrame(h, bodyData);
			}
			
		};
	}
}

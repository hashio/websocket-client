package jp.a840.websocket;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.frame.draft06.BinaryFrame;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06;
import jp.a840.websocket.frame.draft06.FrameHeaderDraft06;
import jp.a840.websocket.frame.draft06.TextFrame;
import jp.a840.websocket.handler.StreamHandler;
import jp.a840.websocket.handler.StreamHandlerAdapter;
import jp.a840.websocket.handler.StreamHandlerChain;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handshake.Handshake;
import util.Base64;


/**
 * A simple websocket client
 * 
 *  this class is implement the WebSocket Draft06 specification.
 *  
 * @see http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-06
 * @author t-hashimoto
 *
 */
public class WebSocketDraft06 extends WebSocketBase {
	private static Logger logger = Logger.getLogger(WebSocketDraft06.class.getName());
	
	private static final int VERSION = 6;
	
	protected Set<String> extensions = new HashSet<String>();
	
	protected String[] serverExtentions;
	
	private static Random random = new Random();
	                 
	public WebSocketDraft06(String url, WebSocketHandler handler, String... protocols) throws WebSocketException {
		super(url, handler, protocols);
	}
	
	@Override
	protected void initializePipeline(WebSocketPipeline pipeline){
		pipeline.addStreamHandler(new StreamHandlerAdapter() {
			
			public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
					Frame frame, StreamHandlerChain chain) throws WebSocketException {
				ByteBuffer buf = ByteBuffer.allocate(4 + buffer.remaining()); // mask-key + header + body
				buf.putInt(random.nextInt());
				buf.put(buffer);
				buf.flip();
				
				byte[] maskkey = new byte[4];
				buf.get(maskkey, 0, 4);
				int m = 0;
				while(buf.hasRemaining()){
					int position = buf.position();
					buf.put((byte)(buf.get(position) ^ maskkey[m++ % 4]));
				}
				buf.flip();
				chain.nextUpstreamHandler(ws, buf, frame);
			}
		});
		super.initializePipeline(pipeline);
	}
	
	@Override
	protected Handshake newHandshakeInstance(){
		return new Handshake() {
			/**
			 * Create a handshake requtest
			 * 
			 * <pre>
			 * Sample (Draft06)
			 * client => server
			 *   GET /chat HTTP/1.1
			 *   Host: server.example.com
			 *   Upgrade: websocket
			 *   Connection: Upgrade
			 *   Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
			 *   Sec-WebSocket-Origin: http://example.com
			 *   Sec-WebSocket-Protocol: chat, superchat
			 *   Sec-WebSocket-Version:6
			 * </pre>
			 * 
			 * @param socket
			 */
			@Override
			public ByteBuffer createHandshakeRequest() throws WebSocketException {
				// Send GET request to server
				StringBuilder sb = new StringBuilder();
				sb.append("GET " + path + " HTTP/1.1\r\n");
				addHeader(sb, "Host", endpoint.getHostName());
				addHeader(sb, "Upgrade", "websocket");
				addHeader(sb, "Connection", "Upgrade");
				addHeader(sb, "Sec-WebSocket-Key", generateWebSocketKey());
				if (origin != null) {
					addHeader(sb, "Sec-WebSocket-Origin", origin);
				}
				if (protocols != null && protocols.length > 0) {
					addHeader(sb, "Sec-WebSocket-Protocol", join(",", protocols));
				}
				// TODO Sec-WebSocket-Extensions
				if(extensions.size() > 0){
					addHeader(sb, "Sec-WebSocket-Extensions", join(",", extensions));
				}
				addHeader(sb, "Sec-WebSocket-Version", String.valueOf(getWebSocketVersion()));
				sb.append("\r\n");
				return ByteBuffer.wrap(sb.toString().getBytes());
			}

			/**
			 * check handshake response
			 * 
			 * <pre>
			 * server => client
			 * HTTP/1.1 101 Switching Protocols
			 * Upgrade: websocket
			 * Connection: Upgrade
			 * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
			 * Sec-WebSocket-Protocol: chat
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
					throw new WebSocketException(3600, "Upgrade response header is not match websocket. Upgrade: " + responseHeaderMap.get("upgrade"));
				}
				if(!"upgrade".equalsIgnoreCase(this.getResponseHeaderMap().get("connection"))){
					throw new WebSocketException(3601, "Connection response header is not match Upgrade. Connection: " + responseHeaderMap.get("connection"));
				}
				if(!this.getResponseHeaderMap().containsKey("sec-websocket-accept")){
					throw new WebSocketException(3602, "Sec-WebSocket-Accept response header is not found");
				}
				String protocolStr = this.getResponseHeaderMap().get("sec-websocket-protocol");
				if(protocolStr != null){
					serverProtocols = protocolStr.split(",");
				}
				String extensionsStr = this.getResponseHeaderMap().get("sec-websocket-extensions");
				if(extensionsStr != null){
					serverExtentions = extensionsStr.split(",");
				}
				return true;
			}
			
			
		};
	}
	
	private String generateWebSocketKey(){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return Base64.encodeToString(md.digest(UUID.randomUUID().toString().getBytes()), false);
		}catch(NoSuchAlgorithmException e){
			return null;
		}
	}
	
	
	@Override
	protected FrameParser newFrameParserInstance() {
		return new FrameParser() {
			private FrameHeaderDraft06 previousCreatedFrameHeader;
			@Override
			protected FrameHeader createFrameHeader(ByteBuffer chunkData) {
				FrameHeaderDraft06 header = FrameBuilderDraft06.createFrameHeader(chunkData, previousCreatedFrameHeader);
				if(!header.isContinuation()){
					previousCreatedFrameHeader = header;
				}
				return header;
			}

			@Override
			protected Frame createFrame(FrameHeader h, byte[] bodyData) {
				return FrameBuilderDraft06.createFrame((FrameHeaderDraft06)h, bodyData);
			}
			
		};
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
			throw new WebSocketException(3650, e);
		}
	}

	@Override
	public Frame createFrame(String str) throws WebSocketException {
		return new TextFrame(str);
	}

	@Override
	protected int getWebSocketVersion() {
		return VERSION;
	}
	
	public void addExtension(String extension){
		extensions.add(extension);
	}
	
	public void removeExtension(String extension){
		extensions.remove(extension);
	}

}

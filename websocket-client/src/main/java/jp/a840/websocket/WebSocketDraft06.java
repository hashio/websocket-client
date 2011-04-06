package jp.a840.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.frame.draft06.BinaryFrame;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06;
import jp.a840.websocket.frame.draft76.TextFrame;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handshake.Handshake;
import util.Base64;


/**
 * A simple websocket client
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
	                 
	private FrameBuilderDraft06 builder = new FrameBuilderDraft06();
	
	public WebSocketDraft06(String url, WebSocketHandler handler, String... protocols) throws URISyntaxException, IOException {
		super(url, handler, protocols);
		
		this.origin = System.getProperty("websocket.origin");
	}
	
	@Override
	protected void initializePipeline(WebSocketPipeline pipeline){
		super.initializePipeline(pipeline);
	}
	
	@Override
	protected Handshake newHandshakeInstance(){
		return new Handshake() {
			/**
			 * Create a handshake requtest
			 * 
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
			 * server => client
			 * HTTP/1.1 101 Switching Protocols
			 * Upgrade: websocket
			 * Connection: Upgrade
			 * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
			 * Sec-WebSocket-Protocol: chat
			 * 
			 * @param buffer
			 */
			@Override
			protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
					throws WebSocketException {
				if(!super.parseHandshakeResponseHeader(buffer)){
					return false;
				}
				if(!"websocket".equalsIgnoreCase(responseHeaderMap.get("upgrade"))){
					throw new WebSocketException(3101, "Upgrade response header is not match websocket. Upgrade: " + responseHeaderMap.get("upgrade"));
				}
				if(!"upgrade".equalsIgnoreCase(responseHeaderMap.get("connection"))){
					throw new WebSocketException(3101, "Connection response header is not match Upgrade. Connection: " + responseHeaderMap.get("connection"));
				}
				if(!responseHeaderMap.containsKey("sec-websocket-accept")){
					throw new WebSocketException(3101, "Sec-WebSocket-Accept response header is not found");
				}
				String protocolStr = responseHeaderMap.get("sec-websocket-protocol");
				if(protocolStr != null){
					serverProtocols = protocolStr.split(",");
				}
				String extensionsStr = responseHeaderMap.get("sec-websocket-extensions");
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

	
	@Override
	public Frame createFrame(Object obj) throws WebSocketException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			
			byte[] bodyData = baos.toByteArray();
			return new BinaryFrame(bodyData);
		} catch (Exception e) {
			throw new WebSocketException(3801, e);
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

package jp.a840.websocket;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameBuilder;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.handler.WebSocketPipeline;
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
	                 
	private FrameBuilder builder = new FrameBuilder();
	
	public WebSocketDraft06(String url, WebSocketHandler handler, String... protocols) throws URISyntaxException, IOException {
		super(url, handler, protocols);
		
		this.origin = System.getProperty("websocket.origin");
	}
	
	protected void initializePipeline(WebSocketPipeline pipeline){
	}
	
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
	protected ByteBuffer createHandshakeRequest(){
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
	protected void handshakeResponse(ByteBuffer buffer)
			throws WebSocketException {
		super.handshakeResponse(buffer);
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
	}
	
	private String generateWebSocketKey(){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return Base64.encodeToString(md.digest(UUID.randomUUID().toString().getBytes()), false);
		}catch(NoSuchAlgorithmException e){
			return null;
		}
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
					buffer.get(bodyData, buffer.position(), buffer.limit());
					ByteBuffer largeBuffer = ByteBuffer.wrap(bodyData);
					largeBuffer.position(buffer.limit() - buffer.position());
					socket.read(largeBuffer);
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
		WebSocketDraft06 ws = new WebSocketDraft06("ws://localhost/rate",
		new WebSocketHandlerAdapter() {
			@Override
			public void onOpen(WebSocket socket) {
				logger.info("onOpen");
			}
		}
		,"rate");
		ws.connect(); // connect and start receive messages;		
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

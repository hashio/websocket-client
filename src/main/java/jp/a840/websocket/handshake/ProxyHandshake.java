package jp.a840.websocket.handshake;

import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jp.a840.websocket.BufferManager;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.proxy.ProxyCredentials;
import jp.a840.websocket.util.StringUtil;

/**
 * HTTP Proxy Handshake class
 * 
 * <pre>
 * client => server
 * CONNECT ws.example.com HTTP/1.1
 * Host: ws.example.com
 * 
 * server => client
 * HTTP/1.1 200 Connection established
 * Proxy-agent: 
 * </pre>
 */
public class ProxyHandshake {
	private static Logger log = Logger.getLogger(ProxyHandshake.class
			.getName());

	private final InetSocketAddress origin;
	
	private ProxyCredentials credentials;
	
	private Selector selector;

	public ProxyHandshake(InetSocketAddress origin){
		this(origin, null);
	}

	public ProxyHandshake(InetSocketAddress origin, ProxyCredentials credentials){
		this.origin = origin;
		this.credentials = credentials;
	}

	public void doHandshake(SocketChannel socket) throws WebSocketException {
		try {
			BufferManager bufferManager = new BufferManager();

			// create selector for Proxy handshake
			selector = Selector.open();
			socket.register(selector, OP_READ);

			ByteBuffer request = createHandshakeRequest();
			socket.write(request);
			
			ByteBuffer responseBuffer = ByteBuffer.allocate(8192);
			selector.select();
			boolean completed = false;
			do {
				responseBuffer.clear();
				socket.read(responseBuffer);
				responseBuffer.flip();
				responseBuffer = bufferManager.getBuffer(responseBuffer);
				completed = parseHandshakeResponseHeader(responseBuffer);
				if(!completed){
					bufferManager.storeFragmentBuffer(responseBuffer);
				}
			} while(!completed);
		} catch (IOException ioe) {
			throw new WebSocketException(3100, ioe);
		} finally {
			try{
				if(selector != null){
					selector.close();
				}
			}catch(IOException e){
				;
			}
		}
	}

	public ByteBuffer createHandshakeRequest() {		
		// Send GET request to server
		StringBuilder sb = new StringBuilder();
		String host = origin.getHostName() + ":" + origin.getPort();
		sb.append("CONNECT " + host + " HTTP/1.1\r\n");
		StringUtil.addHeader(sb, "Host", host);
		sb.append("\r\n");

		try{
			return ByteBuffer.wrap(sb.toString().getBytes("US-ASCII"));
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}

	protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
			throws WebSocketException {
		// METHOD
		// HTTP/1.1 101 Switching Protocols
		String line = StringUtil.readLine(buffer);
		if(line == null){
			return false;
		}
		if (!(line.startsWith("HTTP/1.0") || line.startsWith("HTTP/1.1"))) {
			throw new WebSocketException(3101,
					"Invalid server response.(HTTP version) " + line);
		}
		int responseStatus = Integer.valueOf(line.substring(9, 12));
		if (responseStatus != 200) {
			if(responseStatus == 407){
				throw new WebSocketException(3102,
						"Proxy Authentication is not supported yet. (Status Code) " + line);
			}else{
				throw new WebSocketException(3102,
					"Invalid server response.(Status Code) " + line);
			}
		}

		Map<String,String> headerMap = new HashMap<String,String>();
		// header lines
		do {
			line = StringUtil.readLine(buffer);
			if(line == null){
				return false;
			}
			if (line.indexOf(':') > 0) {
				String[] keyValue = line.split(":", 2);
				if (keyValue.length > 1) {
					headerMap.put(keyValue[0].trim().toLowerCase(),
							keyValue[1].trim().toLowerCase());
				}
			}
			if ("\r\n".compareTo(line) == 0) {
				return true;
			}
			if (!buffer.hasRemaining()) {
				return false;
			}
		} while (true);
	}

}

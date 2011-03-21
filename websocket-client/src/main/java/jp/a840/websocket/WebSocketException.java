package jp.a840.websocket;

/**
 * WebSocket Exception class
 * 
 * WebSocket status code spec. (Draft06)
 * 0    - 999  : Not used
 * 1000 - 1999 : reserved for WebSocket protocol
 * 2000 - 2999 : reserved for WebSocket Extentions
 * 3000 - 3999 : reserved for Libraries and Frameworks(MAY)
 * 4000 - 4999 : reserved for Your Application(MAY)
 * 
 * @author t-hashimoto
 */
public class WebSocketException extends Exception {

	private int statusCode;
	
	public WebSocketException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public WebSocketException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public WebSocketException(int statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

	public WebSocketException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

}

package jp.a840.websocket;

/**
 * WebSocket factory class
 * 
 * @author t-hashimoto
 *
 */
public class WebSockets {
	/**
	 * Create a websocket of current spec
	 * 
	 * @param url
	 * @param handler
	 * @param protocols
	 * @return websocket
	 * @throws WebSocketException
	 */
	public static WebSocket create(String url, WebSocketHandler handler, String... protocols) throws WebSocketException {
		return new WebSocketDraft06(url, handler, protocols);
	}

	/**
	 * Create a websocket of Draft76
	 * 
	 * @param url
	 * @param handler
	 * @param protocols
	 * @return websocket
	 * @throws WebSocketException
	 */
	public static WebSocket createDraft76(String url, WebSocketHandler handler, String... protocols) throws WebSocketException {
		return new WebSocketDraft76(url, handler, protocols);
	}

	/**
	 * Create a websocket of Draft06
	 * 
	 * @param url
	 * @param handler
	 * @param protocols
	 * @return websocket
	 * @throws WebSocketException
	 */
	public static WebSocket createDraft06(String url, WebSocketHandler handler, String... protocols) throws WebSocketException {
		return new WebSocketDraft06(url, handler, protocols);
	}
}

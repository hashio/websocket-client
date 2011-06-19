package jp.a840.websocket.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.util.StringUtil;

abstract public class AbstractAuthenticator implements Authenticator {
	/** The PROXY_AUTHENTICATE. */
//	private final String authenticateHeaderName_;
//	private static String PROXY_AUTHENTICATE = "Proxy-Authenticate";

	protected WebSocket websocket;
	
	protected Credentials credentials;

	public AbstractAuthenticator(){
	}
	
	public String getCredentials(String method, String requestUri, HttpHeader header, String authenticateHeaderName) throws WebSocketException {
		List<String> proxyAuthenticateList = header
				.getHeaderValues(authenticateHeaderName);
		
		List<Challenge> challengeList = new ArrayList<Challenge>();
		for (String proxyAuthenticateStr : proxyAuthenticateList) {
			// key:   Proxy-Authenticate
			// value: Basic realm="WallyWorld"
			String[] parts = proxyAuthenticateStr.split(" +", 2);
			String authScheme = parts[0];
			String authParams = parts[1];

			Map<String, String> paramMap = StringUtil.parseKeyValues(authParams, ',');

			challengeList.add(new Challenge(method, requestUri, authScheme, paramMap));
		}
		
		return getCredentials(challengeList);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.proxy.ProxyCredentials#getCredentials()
	 */
	abstract public String getCredentials(List<Challenge> challengeList) throws WebSocketException;
	
	public void init(WebSocket websocket, Credentials credentials) {
		this.websocket = websocket;
		this.credentials = credentials;
	}
}

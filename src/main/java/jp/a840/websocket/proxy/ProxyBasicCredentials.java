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
package jp.a840.websocket.proxy;

import java.util.List;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.WebSocketException;
import util.Base64;

/**
 * The Class ProxyBasicCredentials.
 */
public class ProxyBasicCredentials implements ProxyCredentials {

	private static String PROXY_AUTHENTICATE = "Proxy-Authenticate";
	
	private static String AUTH_SCHEME = "Basic";
	
	/** The realm. */
	private String realm;
	
	/** The user id. */
	private String userId;
	
	/** The password. */
	private String password;
	
	/**
	 * Instantiates a new proxy basic credentials.
	 *
	 * @param realm the realm
	 * @param userId the user id
	 * @param password the password
	 */
	public ProxyBasicCredentials(String realm, String userId, String password){
		this.realm = realm;
		this.userId = userId;
		this.password = password;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.proxy.ProxyCredentials#getCredentials()
	 */
	public String getCredentials(HttpHeader header) throws WebSocketException {
		List<String> proxyAuthenticateList = header.getHeaderValues(PROXY_AUTHENTICATE);
		for(String proxyAuthenticateStr : proxyAuthenticateList){
			// Proxy-Authenticate: Basic realm="WallyWorld"
			String[] parts = proxyAuthenticateStr.split("[ =]+");
			String authScheme = parts[0];
			String dummy = parts[1];
			String serverRealm = parts[2].replaceAll("\"", "");

			if (AUTH_SCHEME.equalsIgnoreCase(authScheme)) {
				if (!realm.equalsIgnoreCase(serverRealm)) {
					throw new WebSocketException(3841,
							"Realm does not match. Server Realm: "
									+ serverRealm);
				}
				String credentials = userId + ":" + password;
				return AUTH_SCHEME + " "
						+ Base64.encodeToString(credentials.getBytes(), false);
			}
		}
		return null;
	}

	/**
	 * Gets the realm.
	 *
	 * @return the realm
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}

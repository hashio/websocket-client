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
package jp.a840.websocket.auth;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.util.StringUtil;


/**
 * The Class ProxyDigestCredentials.
 *
 * RFC 2617
 *
 * * server response
 *   HTTP/1.1 401 Unauthorized
 *   Proxy-Authenticate: Digest
 *   realm="testrealm@host.com",
 *   qop="auth,auth-int",
 *   nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
 *   opaque="5ccc069c403ebaf9f0171e9517f40e41"
 *    
 * client request
 *   Proxy-Authorization: Digest username="Mufasa",
 *   realm="testrealm@host.com",
 *   nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
 *   uri="/dir/index.html",
 *   qop=auth,
 *   nc=00000001,
 *   cnonce="0a4f113b",
 *   response="6629fae49393a05397450978507c4ef1",
 *   opaque="5ccc069c403ebaf9f0171e9517f40e41"
 *
 * @author Takahiro Hashimoto
 * @see http://www.ietf.org/rfc/rfc2617.txt
 */
public class DigestAuthenticator extends AbstractAuthenticator {
	
	/** The log. */
	private Logger log = Logger.getLogger(DigestAuthenticator.class.getCanonicalName());
	
	/** The AUT h_ scheme. */
	private static String AUTH_SCHEME = "Digest";

	/* (non-Javadoc)
	 * @see jp.a840.websocket.auth.AbstractAuthenticator#getCredentials(java.util.List)
	 */
	@Override
	public String getCredentials(List<Challenge> challengeList)
			throws WebSocketException {
		for(Challenge challenge : challengeList){
			if (AUTH_SCHEME.equalsIgnoreCase(challenge.getScheme())) {
				return getCredentials(challenge);
			}
		}
		return null;
	}
	
	/**
	 * Gets the credentials.
	 *
	 * @param challenge the challenge
	 * @return the credentials
	 * @throws WebSocketException the web socket exception
	 */
	public String getCredentials(Challenge challenge) throws WebSocketException {
		String username = this.credentials.getUsername();
		String password = this.credentials.getPassword();
		
		String method = challenge.getMethod();
		String uri = challenge.getRequestUri();
		
		String opaque = challenge.getParam("opaque");
		String nonce = challenge.getParam("nonce");
		String qop = challenge.getParam("qop");
		// qop support 'auth' only. cause of websocket can't hash for entity-body
		if(qop != null){
			String[] qops = qop.split(",");
			if(qop.length() > 1){
				for(int i = 0; i < qops.length; i++){
					if("auth".equals(qops[i].trim())){
						qop = "auth";
						break;
					}
				}
			}
		}
		String realm = challenge.getParam("realm");
		String algorithm = challenge.getParam("algorithm");
		if(algorithm == null){
			algorithm = "MD5";
		}

		// cnonce is random string
		String cnonce = generateCnonce();
		
		String nc = "00000001";
		
		StringBuilder sb = new StringBuilder(AUTH_SCHEME);
		sb.append(" ");
		StringUtil.addQuotedParam(sb, "username", username).append(", ");
		StringUtil.addQuotedParam(sb, "realm", realm).append(", ");
		StringUtil.addQuotedParam(sb, "nonce", nonce).append(", ");
		StringUtil.addQuotedParam(sb, "uri", uri).append(", ");
		
		if(qop != null){
			// qop,nc did not receive then must not send cnonce
			StringUtil.addParam(sb, "qop", qop).append(", ");
			StringUtil.addParam(sb, "nc", nc).append(", ");
			StringUtil.addQuotedParam(sb, "cnonce", cnonce).append(", ");
		}

		// H(username, realm, password)  *H is MD5(default)
		String a1 = StringUtil.toMD5HexString(StringUtil.join(":", username, realm, password));
		if("MD5-sess".equals(algorithm)){
			a1 = StringUtil.toMD5HexString(StringUtil.join(":", a1, nonce, cnonce));
		}
	
		String a2 = null;
		if(qop == null || "auth".equals(qop)){
			// H(method, uri)  *H is MD5(default)
			a2 = StringUtil.toMD5HexString(StringUtil.join(":", method, uri));
		} else if("auth-int".equals(qop)){
			throw new WebSocketException(3999, "Unsupported qop option 'auth-int'.");			
		} else {
			throw new WebSocketException(3999, "Unsupported qop option. qop:" + qop);						
		}
		
		String response = null;
		if(qop != null){
			response = StringUtil.toMD5HexString(StringUtil.join(":", a1, nonce, nc, cnonce, qop, a2));
		}else{
			response = StringUtil.toMD5HexString(StringUtil.join(":", a1, nonce, a2));			
		}

		if(opaque != null){
			StringUtil.addQuotedParam(sb, "opaque", opaque).append(", ");
		}
		
		StringUtil.addQuotedParam(sb, "response", response);
		
		if(log.isLoggable(Level.FINER)){
			log.finer(sb.toString() + "(, algorithm=" + algorithm + ", A1=" + a1 + ", A2=" + a2 + ")");
		}

		return sb.toString();
	}
	
	/**
	 * Generate cnonce.
	 *
	 * @return the string
	 */
	private String generateCnonce(){
		// cnonce is random string
		byte[] cnonceBytes = new byte[16];
		Random random = new Random();
		random.nextBytes(cnonceBytes);
		String cnonce = StringUtil.toHexString(cnonceBytes);
		return cnonce;
	}
}

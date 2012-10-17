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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.a840.websocket.exception.WebSocketException;

/**
 * The Class DefaultAuthenticator.
 *
 * @author Takahiro Hashimoto
 */
public class DefaultAuthenticator extends AbstractAuthenticator {
	
	/** The basic authenticator. */
	private BasicAuthenticator basicAuthenticator;
	
	/** The digest authenticator. */
	private DigestAuthenticator digestAuthenticator;
	
	/**
	 * Instantiates a new default authenticator.
	 */
	public DefaultAuthenticator(){
		this.basicAuthenticator = new BasicAuthenticator();
		this.digestAuthenticator = new DigestAuthenticator();
	}

	/**
	 * Instantiates a new default authenticator.
	 *
	 * @param basicAuthenticator the basic authenticator
	 * @param digestAuthenticator the digest authenticator
	 */
	public DefaultAuthenticator(BasicAuthenticator basicAuthenticator,
			DigestAuthenticator digestAuthenticator){
		this.basicAuthenticator = basicAuthenticator;
		this.digestAuthenticator = digestAuthenticator;		
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.auth.AbstractAuthenticator#getCredentials(java.util.List)
	 */
	@Override
	public String getCredentials(List<Challenge> challengeList)
			throws WebSocketException {
		Map<String, Challenge> schemeMap = new HashMap<String, Challenge>();
		for(Challenge challenge : challengeList){
			schemeMap.put(challenge.getScheme(), challenge);
		}
		
		if(schemeMap.containsKey("Digest")){
			this.digestAuthenticator.init(this.websocket, this.credentials);
			return this.digestAuthenticator.getCredentials(schemeMap.get("Digest"));
		}else if(schemeMap.containsKey("Basic")){
			this.basicAuthenticator.init(this.websocket, this.credentials);
			return this.basicAuthenticator.getCredentials(schemeMap.get("Basic"));
		}
		return null;
	}
}

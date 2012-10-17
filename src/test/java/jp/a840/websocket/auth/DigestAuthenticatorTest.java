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

import jp.a840.websocket.http.HttpHeader;
import jp.a840.websocket.TestCase;
import jp.a840.websocket.WebSocket;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * The Class DigestAuthenticatorTest.
 *
 * @author Takahiro Hashimoto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DigestAuthenticator.class)
public class DigestAuthenticatorTest extends TestCase {
	
	/**
	 * Gets the credentials1.
	 *
	 * @return the credentials1
	 * @throws Exception the exception
	 */
	@Test
	public void getCredentials1() throws Exception {
		WebSocket websocket = Mockito.mock(WebSocket.class);		
		
		DigestAuthenticator authenticator = new DigestAuthenticator();
		authenticator = PowerMockito.spy(authenticator);
		PowerMockito.when(authenticator, "generateCnonce").thenReturn("0a4f113b");
		authenticator.init(websocket, new Credentials("Mufasa", "Circle Of Life"));
		
		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
		Assert.assertEquals("Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\", qop=auth, nc=00000001, cnonce=\"0a4f113b\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\", response=\"6629fae49393a05397450978507c4ef1\"",
				authenticator.getCredentials("GET", "/dir/index.html", header, "Proxy-Authenticate"));
	}
}

package jp.a840.websocket.auth;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.TestCase;
import jp.a840.websocket.WebSocket;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DigestAuthenticator.class)
public class DefaultAuthenticatorTest extends TestCase {
	@Test
	public void getCredentials1() throws Exception {
		WebSocket websocket = Mockito.mock(WebSocket.class);		
		
		DigestAuthenticator digestAuthenticator = new DigestAuthenticator();
		digestAuthenticator = PowerMockito.spy(digestAuthenticator);
		PowerMockito.when(digestAuthenticator, "generateCnonce").thenReturn("0a4f113b");
		
		DefaultAuthenticator authenticator = new DefaultAuthenticator(
				new BasicAuthenticator(),
				digestAuthenticator
		);
		authenticator.init(websocket, new Credentials("Mufasa", "Circle Of Life"));

		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Basic realm=\"testrealm@host.com\"");
		Assert.assertEquals("Basic TXVmYXNhOkNpcmNsZSBPZiBMaWZl", authenticator.getCredentials("CONNECT", "host:8080", header, "Proxy-Authenticate"));
	}

	@Test
	public void getCredentials2() throws Exception {
		WebSocket websocket = Mockito.mock(WebSocket.class);		
		
		DigestAuthenticator digestAuthenticator = new DigestAuthenticator();
		digestAuthenticator = PowerMockito.spy(digestAuthenticator);
		PowerMockito.when(digestAuthenticator, "generateCnonce").thenReturn("0a4f113b");
		
		DefaultAuthenticator authenticator = new DefaultAuthenticator(
				new BasicAuthenticator(),
				digestAuthenticator
		);
		authenticator.init(websocket, new Credentials("Mufasa", "Circle Of Life"));

		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Basic realm=\"testrealm@host.com\"");
		header.addHeader("Proxy-Authenticate", "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
		Assert.assertEquals("Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\", qop=auth, nc=00000001, cnonce=\"0a4f113b\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\", response=\"6629fae49393a05397450978507c4ef1\"",
				authenticator.getCredentials("GET", "/dir/index.html", header, "Proxy-Authenticate"));
	}
}

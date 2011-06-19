package jp.a840.websocket.auth;

import java.net.URI;

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
public class DigestAuthenticatorTest extends TestCase {
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

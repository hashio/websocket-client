package jp.a840.websocket.auth;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.auth.BasicAuthenticator;
import jp.a840.websocket.auth.Credentials;

import org.junit.Assert;
import org.junit.Test;


public class BasicAuthenticatorTest {
	@Test
	public void getCredentials1() throws Exception {
		BasicAuthenticator authenticator = new BasicAuthenticator();
		authenticator.init(null, new Credentials("test", "pass"));
		
		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Basic realm=\"Test Realm\"");
		Assert.assertEquals("Basic dGVzdDpwYXNz", authenticator.getCredentials("CONNECT", "host:8080", header, "Proxy-Authenticate"));
		
	}
}

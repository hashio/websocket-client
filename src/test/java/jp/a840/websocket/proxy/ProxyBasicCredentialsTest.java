package jp.a840.websocket.proxy;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.WebSocketException;

import org.junit.Assert;
import org.junit.Test;


public class ProxyBasicCredentialsTest {
	@Test
	public void getCredentials1() throws Exception {
		ProxyBasicCredentials credentials = new ProxyBasicCredentials("Test Realm", "test", "pass");
		
		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Basic realm=\"Test Realm\"");
		Assert.assertEquals("Basic dGVzdDpwYXNz", credentials.getCredentials(header));
		
	}

	@Test
	public void getCredentialsError1() throws Exception {
		ProxyBasicCredentials credentials = new ProxyBasicCredentials("TestError Realm", "test", "pass");
		
		HttpHeader header = new HttpHeader();
		header.addHeader("Proxy-Authenticate", "Basic realm=\"Test Realm\"");
		try{
			credentials.getCredentials(header);
		}catch(WebSocketException e){
			Assert.assertEquals(3841, e.getStatusCode());
		}		
	}
}

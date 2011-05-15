package jp.a840.websocket;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HttpHeaderTest {
	
	private HttpHeader header;
	
	@Before
	public void createMockHttpHeader(){
		header = new HttpHeader();
		
		header.addHeader("test1", "1");
		header.addHeader("multiTest", "1");
		header.addHeader("multiTest", "2");
		header.addHeader("multiTest", "3");
		header.addHeader("nullTest", null);
		header.addHeader("removeTest", "remove");	
	}
	
	@Test
	public void containsHeader1(){	
		Assert.assertTrue(header.containsHeader("test1"));		
	}

	@Test
	public void getHeaderValue1(){
		String test1Value = header.getHeaderValue("test1");
		Assert.assertEquals("1", test1Value);		
	}
	
	@Test
	public void getHeaderValue2(){
		Assert.assertNull(header.getHeaderValue("nullTest"));
	}
	
	@Test
	public void getHeaderValues1(){
		List<String> test1Values = header.getHeaderValues("multiTest");
		Assert.assertNotNull(test1Values);
		Assert.assertEquals(3, test1Values.size());
		Assert.assertEquals("1", test1Values.get(0));
		Assert.assertEquals("2", test1Values.get(1));
		Assert.assertEquals("3", test1Values.get(2));
	}
	
	@Test
	public void removeHeader1(){
		Assert.assertNotNull(header.getHeaderValue("removeTest"));
		header.removeHeader("removeTest");
		Assert.assertNull(header.getHeaderValue("removeTest"));
	}
}

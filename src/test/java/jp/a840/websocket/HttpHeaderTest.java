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
package jp.a840.websocket;

import java.util.List;

import jp.a840.websocket.http.HttpHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * The Class HttpHeaderTest.
 *
 * @author Takahiro Hashimoto
 */
public class HttpHeaderTest {
	
	/** The header. */
	private HttpHeader header;
	
	/**
	 * Creates the mock http header.
	 */
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
	
	/**
	 * Contains header1.
	 */
	@Test
	public void containsHeader1(){	
		Assert.assertTrue(header.containsHeader("test1"));		
	}

	/**
	 * Gets the header value1.
	 *
	 * @return the header value1
	 */
	@Test
	public void getHeaderValue1(){
		String test1Value = header.getHeaderValue("test1");
		Assert.assertEquals("1", test1Value);		
	}
	
	/**
	 * Gets the header value2.
	 *
	 * @return the header value2
	 */
	@Test
	public void getHeaderValue2(){
		Assert.assertNull(header.getHeaderValue("nullTest"));
	}
	
	/**
	 * Gets the header values1.
	 *
	 * @return the header values1
	 */
	@Test
	public void getHeaderValues1(){
		List<String> test1Values = header.getHeaderValues("multiTest");
		Assert.assertNotNull(test1Values);
		Assert.assertEquals(3, test1Values.size());
		Assert.assertEquals("1", test1Values.get(0));
		Assert.assertEquals("2", test1Values.get(1));
		Assert.assertEquals("3", test1Values.get(2));
	}
	
	/**
	 * Removes the header1.
	 */
	@Test
	public void removeHeader1(){
		Assert.assertNotNull(header.getHeaderValue("removeTest"));
		header.removeHeader("removeTest");
		Assert.assertNull(header.getHeaderValue("removeTest"));
	}
}

package jp.a840.websocket.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class StringUtilTest {
	@Test
	public void parseKeyValue1(){
		Map<String,String> m = StringUtil.parseKeyValues("foo=\" bar boo \", aaa=bbb, bab=\" bla,bla\"", ',');
		Assert.assertEquals(" bar boo ",m.get("foo"));
		Assert.assertEquals("bbb",m.get("aaa"));
		Assert.assertEquals(" bla,bla",m.get("bab"));
	}
}

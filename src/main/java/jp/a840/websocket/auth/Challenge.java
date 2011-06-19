package jp.a840.websocket.auth;

import java.util.Map;

public class Challenge {
	private String scheme;
	private String method;
	private String requestUri;
	private Map<String,String> paramMap;
	
	public Challenge(String method, String requestUri, String scheme, Map<String, String> paramMap){
		this.method = method;
		this.requestUri = requestUri;
		this.scheme = scheme;
		this.paramMap = paramMap;
	}
	
	public String getScheme(){
		return this.scheme;
	}
	
	public String getParam(String name){
		return this.paramMap.get(name);
	}

	public String getMethod() {
		return method;
	}

	public String getRequestUri() {
		return requestUri;
	}
}

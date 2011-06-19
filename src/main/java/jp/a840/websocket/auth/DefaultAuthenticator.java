package jp.a840.websocket.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.a840.websocket.WebSocketException;

public class DefaultAuthenticator extends AbstractAuthenticator {
	private BasicAuthenticator basicAuthenticator;
	private DigestAuthenticator digestAuthenticator;
	
	public DefaultAuthenticator(){
		this.basicAuthenticator = new BasicAuthenticator();
		this.digestAuthenticator = new DigestAuthenticator();
	}

	public DefaultAuthenticator(BasicAuthenticator basicAuthenticator,
			DigestAuthenticator digestAuthenticator){
		this.basicAuthenticator = basicAuthenticator;
		this.digestAuthenticator = digestAuthenticator;		
	}
	
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

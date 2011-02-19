package jp.a840.push.subscriber;

public interface Message {
	public Object getProperty(String key);
	public Object getBody();
}

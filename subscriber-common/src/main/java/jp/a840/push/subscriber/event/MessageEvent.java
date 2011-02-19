package jp.a840.push.subscriber.event;

import jp.a840.push.subscriber.Message;

public class MessageEvent extends Event {
	public MessageEvent(Message source) {
		super(source);
	}
	
	public Message getMessage(){
		return (Message)source;
	}
}

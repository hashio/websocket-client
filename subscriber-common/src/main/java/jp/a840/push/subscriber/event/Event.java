package jp.a840.push.subscriber.event;

abstract public class Event {
	protected final Object source;

    public Event(Object source){
		this.source = source;
	}
	
	public Object getSource(){
		return source;
	}

}

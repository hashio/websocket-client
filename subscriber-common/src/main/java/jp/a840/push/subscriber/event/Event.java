package jp.a840.push.subscriber.event;

abstract public class Event {
	protected final Object source;

	/**
     * イベント発信元のオブジェクトを与えてオブジェクトを生成します.
     * @param source
	 */
    public Event(Object source){
		this.source = source;
	}
	
    /**
     * イベント発信元のオブジェクトを返します.
     */
	public Object getSource(){
		return source;
	}

}

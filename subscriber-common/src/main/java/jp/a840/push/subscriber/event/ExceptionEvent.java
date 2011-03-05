package jp.a840.push.subscriber.event;

public class ExceptionEvent extends Event{
	protected final Exception exception;

	public ExceptionEvent(Object source, Exception exception){
		super(source);
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

}

package jp.a840.push.subscriber.event;

public class ExceptionEvent extends Event{
	protected final Exception exception;

    /**
     * イベント発信元のオブジェクトと発生した例外を与えてオブジェクトを生成します.
     * @param source
     * @param exception
     */
	public ExceptionEvent(Object source, Exception exception){
		super(source);
		this.exception = exception;
	}

    /**
     * 発生した例外を返します.
     * @return
     */
	public Exception getException() {
		return exception;
	}

}

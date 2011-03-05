package jp.a840.push.subscriber.exception;

public class TimeoutException extends RuntimeException {

	public TimeoutException() {
		super();
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}

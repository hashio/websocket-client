package jp.a840.push.subscriber.exception;

/**
 * レスポンスが一定時間たってもこなかった時のException
 * 
 */
public class TimeoutException extends RuntimeException {

	/**
	 * デフォルトコンストラクタ
	 * 
	 */
	public TimeoutException() {
		super();
	}

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param message
	 */
	public TimeoutException(String message) {
		super(message);
	}

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param cause
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param message
	 * @param cause
	 */
	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}

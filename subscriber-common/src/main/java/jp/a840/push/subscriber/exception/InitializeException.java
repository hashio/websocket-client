package jp.a840.push.subscriber.exception;

public class InitializeException extends Exception {

    /**
     * 
     */
    public InitializeException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public InitializeException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InitializeException(Throwable cause) {
        super(cause);
    }


}

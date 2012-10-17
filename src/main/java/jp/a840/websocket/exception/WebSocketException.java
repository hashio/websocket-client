/*
 * The MIT License
 * 
 * Copyright (c) 2011 Takahiro Hashimoto
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.a840.websocket.exception;


/**
 * WebSocket Exception class
 * 
 * WebSocket status code spec. (Draft06)
 * 0    - 999  : Not used
 * 1000 - 1999 : reserved for WebSocket protocol
 * 2000 - 2999 : reserved for WebSocket Extentions
 * 3000 - 3999 : reserved for Libraries and Frameworks(MAY)
 * 4000 - 4999 : reserved for Your Application(MAY)
 * 
 * @author t-hashimoto
 */
public class WebSocketException extends Exception {

	/** The status code. */
	private int statusCode;
	
	/**
	 * Instantiates a new web socket exception.
	 *
	 * @param statusCode the status code
	 */
	public WebSocketException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	/**
	 * Instantiates a new web socket exception.
	 *
	 * @param statusCode the status code
	 * @param message the message
	 */
	public WebSocketException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Instantiates a new web socket exception.
	 *
	 * @param statusCode the status code
	 * @param cause the cause
	 */
	public WebSocketException(int statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

	/**
	 * Instantiates a new web socket exception.
	 *
	 * @param statusCode the status code
	 * @param message the message
	 * @param cause the cause
	 */
	public WebSocketException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "[" + statusCode + "] " + super.getMessage();
	}

	/**
	 * Gets the status code.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}
}

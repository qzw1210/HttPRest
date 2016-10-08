package esky.framework.cashew.web.exception;

import esky.framework.cashew.exception.FrameworkException;

@SuppressWarnings("serial")
public class IllegalRequestException extends FrameworkException {
	
	public IllegalRequestException() {
		super();
	}

	public IllegalRequestException(String message) {
		super(message);
	}

	public IllegalRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalRequestException(Throwable cause) {
		super(cause);
	}
}

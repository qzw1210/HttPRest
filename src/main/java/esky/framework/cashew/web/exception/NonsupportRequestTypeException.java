package esky.framework.cashew.web.exception;

import esky.framework.cashew.exception.FrameworkException;

@SuppressWarnings("serial")
public class NonsupportRequestTypeException extends FrameworkException {
	
	public NonsupportRequestTypeException() {
		super();
	}

	public NonsupportRequestTypeException(String message) {
		super(message);
	}

	public NonsupportRequestTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonsupportRequestTypeException(Throwable cause) {
		super(cause);
	}
}
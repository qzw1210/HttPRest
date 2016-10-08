package esky.framework.cashew.web.exception;

import esky.framework.cashew.exception.FrameworkException;

@SuppressWarnings("serial")
public class NoSuchMethodException extends FrameworkException {

	public NoSuchMethodException() {
		super();
	}

	public NoSuchMethodException(String message) {
		super(message);
	}

	public NoSuchMethodException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchMethodException(Throwable cause) {
		super(cause);
	}

}

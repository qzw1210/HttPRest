package esky.framework.cashew.exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class FileUploadIOException extends IOException {
	
	public FileUploadIOException() {
		super();
	}

	public FileUploadIOException(String message) {
		super(message);
	}
}

package de.mprengemann.hwr.timetabel.exceptions;


public class ConnectionTimeoutException extends TimetableException {
	private static final long serialVersionUID = -8948550026188672880L;

	public ConnectionTimeoutException() {
		super.errorType = TimetableErrorType.TIMEOUT;
	}

	public ConnectionTimeoutException(String detailMessage) {
		super(detailMessage);
		super.errorType = TimetableErrorType.TIMEOUT;
	}

	public ConnectionTimeoutException(Throwable throwable) {
		super(throwable);
		super.errorType = TimetableErrorType.TIMEOUT;
	}

	public ConnectionTimeoutException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		super.errorType = TimetableErrorType.TIMEOUT;
	}

}

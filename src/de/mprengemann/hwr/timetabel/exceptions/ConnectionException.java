package de.mprengemann.hwr.timetabel.exceptions;


public class ConnectionException extends TimetableException {
	private static final long serialVersionUID = -8131317137119547843L;

	public ConnectionException() {
		super.errorType = TimetableErrorType.CONNECTION;
	}

	public ConnectionException(String detailMessage) {
		super(detailMessage);
		super.errorType = TimetableErrorType.CONNECTION;
	}

	public ConnectionException(Throwable throwable) {
		super(throwable);
		super.errorType = TimetableErrorType.CONNECTION;
	}

	public ConnectionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		super.errorType = TimetableErrorType.CONNECTION;
	}

}

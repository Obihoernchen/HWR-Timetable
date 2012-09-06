package de.mprengemann.hwr.timetabel.exceptions;


public class IPoolFormatException extends TimetableException {
	private static final long serialVersionUID = 4786981922310199924L;

	public IPoolFormatException() {
		super.errorType = TimetableErrorType.FORMAT;
	}

	public IPoolFormatException(String detailMessage) {
		super(detailMessage);
		super.errorType = TimetableErrorType.FORMAT;
	}

	public IPoolFormatException(Throwable throwable) {
		super(throwable);
		super.errorType = TimetableErrorType.FORMAT;
	}

	public IPoolFormatException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		super.errorType = TimetableErrorType.FORMAT;
	}

}

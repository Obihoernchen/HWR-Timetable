package de.mprengemann.hwr.timetabel.exceptions;


public class TimetableException extends Exception {
	
	public enum TimetableErrorType{
		GENERAL, CONNECTION, TIMEOUT, FORMAT, STORAGE, UNKNOWN_TIMETABLE
	}
	protected TimetableErrorType errorType = TimetableErrorType.GENERAL;
	
	private static final long serialVersionUID = -4169428160136560220L;

	public TimetableException() {		
	}

	public TimetableException(String detailMessage) {
		super(detailMessage);
	}

	public TimetableException(Throwable throwable) {
		super(throwable);
	}

	public TimetableException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public TimetableErrorType getType() {
		return this.errorType;
	}

}

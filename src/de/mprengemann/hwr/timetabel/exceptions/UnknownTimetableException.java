package de.mprengemann.hwr.timetabel.exceptions;


public class UnknownTimetableException extends TimetableException {
	private static final long serialVersionUID = -1121892257521270130L;

	public UnknownTimetableException() {
		super.errorType = TimetableErrorType.UNKNOWN_TIMETABLE;
	}

	public UnknownTimetableException(String detailMessage) {
		super(detailMessage);
		super.errorType = TimetableErrorType.UNKNOWN_TIMETABLE;
	}

	public UnknownTimetableException(Throwable throwable) {
		super(throwable);
		super.errorType = TimetableErrorType.UNKNOWN_TIMETABLE;
	}

	public UnknownTimetableException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		super.errorType = TimetableErrorType.UNKNOWN_TIMETABLE;
	}

}

package de.mprengemann.hwr.timetabel.exceptions;


public class StorageException extends TimetableException {
	private static final long serialVersionUID = 1703803777724592095L;

	public StorageException() {
		super.errorType = TimetableErrorType.STORAGE;
	}

	public StorageException(String detailMessage) {
		super(detailMessage);
		super.errorType = TimetableErrorType.STORAGE;
	}

	public StorageException(Throwable throwable) {
		super(throwable);
		super.errorType = TimetableErrorType.STORAGE;
	}

	public StorageException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		super.errorType = TimetableErrorType.STORAGE;
	}

}

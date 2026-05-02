package edu.sc.bse3211.meetingplanner;

public class TimeConflictException extends Exception {

	private static final long serialVersionUID = 8147822812157714367L;

	public TimeConflictException() {
		super();
	}

	public TimeConflictException(String message) {
		super(message);
	}

	public TimeConflictException(Throwable cause) {
		super(cause);
	}

	public TimeConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeConflictException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

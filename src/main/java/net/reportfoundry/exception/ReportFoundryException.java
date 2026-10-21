package net.reportfoundry.exception;

public class ReportFoundryException extends RuntimeException {

    public ReportFoundryException(String message) {
        super(message);
    }

    public ReportFoundryException(String message, Throwable cause) {
        super(message, cause);
    }
}

package pl.pw.cyber.dbaccess.common.result;

public class ResultExecutionException extends RuntimeException {

    public ResultExecutionException(String message) {
        super(message);
    }

    public ResultExecutionException(Throwable cause) {
        super(cause);
    }

    public ResultExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

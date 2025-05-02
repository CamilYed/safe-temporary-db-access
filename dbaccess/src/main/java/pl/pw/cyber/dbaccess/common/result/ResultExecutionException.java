package pl.pw.cyber.dbaccess.common.result;

public sealed class ResultExecutionException extends RuntimeException
  permits ResultExecutionException.DatabaseNotResolvable, ResultExecutionException.DatabaseUnexpectedError {

    public ResultExecutionException(String message) {
        super(message);
    }

    public ResultExecutionException(Throwable cause) {
        super(cause);
    }

    public ResultExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class DatabaseNotResolvable extends ResultExecutionException {
        public DatabaseNotResolvable(String message) {
            super(message);
        }
    }
    public static final class DatabaseUnexpectedError extends ResultExecutionException {
        public DatabaseUnexpectedError(String message) {
            super(message);
        }
    }
}


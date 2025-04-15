package pl.pw.cyber.dbaccess.common.result;

public class ResultExecutionException extends RuntimeException {
    public ResultExecutionException(Exception cause) {
        super("Execution failed", cause);
    }
}

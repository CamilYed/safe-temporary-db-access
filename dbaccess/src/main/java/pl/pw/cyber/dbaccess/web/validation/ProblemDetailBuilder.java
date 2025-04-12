package pl.pw.cyber.dbaccess.web.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;

public final class ProblemDetailBuilder {

    private ProblemDetailBuilder() {}

    public static ProblemDetail validationError(List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("/errors/invalid-request"));
        problem.setTitle("Invalid Request");
        problem.setDetail("One or more validation errors occurred");
        problem.setProperty("errors", errors);
        return problem;
    }
}

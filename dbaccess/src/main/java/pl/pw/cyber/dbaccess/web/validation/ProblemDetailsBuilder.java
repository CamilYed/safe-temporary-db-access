package pl.pw.cyber.dbaccess.web.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;

public final class ProblemDetailsBuilder {

    private ProblemDetailsBuilder() {}

    public static ProblemDetail validationError(List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("/errors/invalid-request"));
        problem.setTitle("Invalid Request");
        problem.setDetail("One or more validation errors occurred");
        problem.setProperty("errors", errors);
        return problem;
    }

    public static ProblemDetail tooManyRequest(String type) {
        var problem = ProblemDetail.forStatus(429);
        problem.setTitle("Too Many Requests");
        problem.setDetail("Rate limit exceeded. Try again later.");
        problem.setType(URI.create(type));
        return problem;
    }
}

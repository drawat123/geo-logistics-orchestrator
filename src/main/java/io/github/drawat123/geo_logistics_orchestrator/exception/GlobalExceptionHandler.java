package io.github.drawat123.geo_logistics_orchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PathNotFoundException.class)
    public ProblemDetail handlePathNotFound(PathNotFoundException ex) {
        // ProblemDetail is the modern Spring 6+ standard for error responses
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Path Not Found");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        return problemDetail;
    }
}
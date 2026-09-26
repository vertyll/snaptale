package com.vertyll.snaptale.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;

import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final String CODE_PROPERTY = "code";
    static final String ARGS_PROPERTY = "args";
    static final String ERRORS_PROPERTY = "errors";

    private static final Set<String> CONSTRAINT_ATTRIBUTES_WITHOUT_MEANING =
            Set.of("message", "groups", "payload", "regexp", "flags");

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException exception) {
        return problem(exception.status(), exception.messageKey(), exception.messageArgs());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.debug("[DEBUG] Database constraint violation", exception);
        return problem(HttpStatus.CONFLICT, MessageKeys.DUPLICATE_ENTRY, Map.of());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMaxUploadSizeExceededException(
        MaxUploadSizeExceededException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        ProblemDetail problem = problem(HttpStatus.CONTENT_TOO_LARGE, MessageKeys.UPLOAD_TOO_LARGE, Map.of());
        return handleExceptionInternal(exception, problem, headers, HttpStatus.CONTENT_TOO_LARGE, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        Map<String, FieldMessage> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    ApiExceptionHandler::fieldMessage,
                    (first, _) -> first,
                    LinkedHashMap::new
                )
            );
        ProblemDetail problem = problem(status, MessageKeys.INVALID_FORM, Map.of());
        problem.setProperty(ERRORS_PROPERTY, errors);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
        Exception exception,
        @Nullable Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        if (body instanceof ProblemDetail problem && !hasCode(problem)) {
            problem.setDetail(null);
            problem.setProperty(CODE_PROPERTY, MessageKeys.HTTP_STATUS_PREFIX + statusCode.value());
            problem.setProperty(ARGS_PROPERTY, Map.of());
        }
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }

    private static ProblemDetail problem(HttpStatusCode status, String code, Map<String, Object> args) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setProperty(CODE_PROPERTY, code);
        problem.setProperty(ARGS_PROPERTY, args);
        return problem;
    }

    private static boolean hasCode(ProblemDetail problem) {
        Map<String, Object> properties = problem.getProperties();
        return properties != null && properties.containsKey(CODE_PROPERTY);
    }

    private static FieldMessage fieldMessage(FieldError error) {
        if (!error.contains(ConstraintViolation.class)) {
            return new FieldMessage(MessageKeys.INVALID_VALUE, Map.of());
        }
        ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
        Map<String, Object> args = violation.getConstraintDescriptor()
            .getAttributes()
            .entrySet()
            .stream()
            .filter(attribute -> !CONSTRAINT_ATTRIBUTES_WITHOUT_MEANING.contains(attribute.getKey()))
            .filter(attribute -> attribute.getValue() instanceof Number || attribute.getValue() instanceof String)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new FieldMessage(violation.getMessageTemplate(), args);
    }

    record FieldMessage(String code, Map<String, Object> args) {
    }
}

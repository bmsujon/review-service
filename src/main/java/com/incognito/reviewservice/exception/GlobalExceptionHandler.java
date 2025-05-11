    package com.incognito.reviewservice.exception;

    import lombok.extern.slf4j.Slf4j;
    import org.apache.coyote.BadRequestException;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.HttpStatusCode;
    import org.springframework.http.ResponseEntity;
    import org.springframework.http.converter.HttpMessageNotReadableException;
    import org.springframework.validation.FieldError;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.context.request.WebRequest;
    import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

    import java.time.LocalDateTime;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @ControllerAdvice
    @Slf4j
    public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        // Handles validation errors from @Valid
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                MethodArgumentNotValidException ex, HttpHeaders headers,
                HttpStatusCode status, WebRequest request) {
            log.warn("Validation error: {}", ex.getMessage());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value());
            body.put("error", "Validation Error");

            // Get all errors
            List<String> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            body.put("messages", errors);
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, headers, status);
        }

        // Example: Handle a custom business exception
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Object> handleResourceNotFoundException(
                ResourceNotFoundException ex, WebRequest request) {
            log.warn("Resource not found: {}", ex.getMessage());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        // Generic fallback handler for other exceptions
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleAllOtherExceptions(
                Exception ex, WebRequest request) {
            log.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Log stack trace for unexpected errors
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("error", "Internal Server Error");
            body.put("message", "An unexpected error occurred. Please try again later.");
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // In GlobalExceptionHandler.java
        @Override
        protected ResponseEntity<Object> handleHttpMessageNotReadable(
                HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
            log.warn("Malformed JSON request: {}", ex.getMessage());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value()); // Typically 400
            body.put("error", "Malformed JSON");
            body.put("message", "The request body is not valid JSON or cannot be processed.");
            // You could include ex.getLocalizedMessage() or a more specific detail if safe and useful
            body.put("path", request.getDescription(false).replace("uri=", ""));
            return new ResponseEntity<>(body, headers, status);
        }

        // In GlobalExceptionHandler.java
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<Object> handleBadRequestException(
                BadRequestException ex, WebRequest request) {
            log.warn("Bad request: {}", ex.getMessage());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Bad Request");
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

    }
    
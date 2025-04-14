package com.soaresdev.picpaytestjr.exceptions;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlerCenter {
    private final Logger logger = LoggerFactory.getLogger(ExceptionHandlerCenter.class.getName());

    @ExceptionHandler(InvalidUserTypeException.class)
    public ResponseEntity<StandardError> invalidUserType(InvalidUserTypeException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(getStandardError(HttpStatus.BAD_REQUEST, e, request));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<StandardError> entityExistsException(EntityExistsException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).
                body(getStandardError(HttpStatus.CONFLICT, e, request));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getStandardError(HttpStatus.NOT_FOUND, e, request));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<StandardError> nullPointerException(NullPointerException e, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(getStandardError(HttpStatus.INTERNAL_SERVER_ERROR, e, request));
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<StandardError> transferException(TransferException e, HttpServletRequest request) {
        return ResponseEntity.unprocessableEntity().body(getStandardError(HttpStatus.UNPROCESSABLE_ENTITY, e, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardRequestError> methodArgumentNotValid(MethodArgumentNotValidException e,
                                                                         HttpServletRequest request) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted().toList();
        StandardRequestError insertDTOError = getStandardRequestError(HttpStatus.BAD_REQUEST,
                request, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(insertDTOError);
    }

    private StandardError getStandardError(HttpStatus hs, Exception e, HttpServletRequest request) {
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(hs.value());
        standardError.setError(e.getClass().getSimpleName());
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());

        if(logger.isWarnEnabled())
            logger.warn("Error: {}", standardError);

        return standardError;
    }

    private StandardRequestError getStandardRequestError(HttpStatus httpStatus, HttpServletRequest request, List<String> errors) {
        StandardRequestError insertDTOError = new StandardRequestError();
        insertDTOError.setTimestamp(Instant.now());
        insertDTOError.setStatus(httpStatus.value());
        insertDTOError.setErrors(errors);
        insertDTOError.setPath(request.getRequestURI());

        if(logger.isWarnEnabled())
            logger.warn("Errors: {}", insertDTOError);

        return insertDTOError;
    }
}
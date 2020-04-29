package net.streamarchive.infrastructure.handlers.misc;


import net.streamarchive.infrastructure.exceptions.NotFoundException;
import net.streamarchive.infrastructure.exceptions.StreamNotFoundException;
import net.streamarchive.infrastructure.exceptions.WrongParamsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = {WrongParamsException.class})
    protected ResponseEntity<Object> handleBadRequest(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value
            = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value
            = {StreamNotFoundException.class})
    protected ResponseEntity<Object> handleStreamNotFound(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}


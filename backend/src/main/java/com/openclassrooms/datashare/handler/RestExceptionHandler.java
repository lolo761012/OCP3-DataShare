package com.openclassrooms.datashare.handler;

import com.openclassrooms.datashare.exception.EmailAlreadyUsedException;
import com.openclassrooms.datashare.exception.FileTooLargeException;
import com.openclassrooms.datashare.exception.InvalidDownloadPasswordException;
import com.openclassrooms.datashare.exception.StoredFileExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;


import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorDetails> handleEmailAlreadyUsed(
            EmailAlreadyUsedException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentials(
            BadCredentialsException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(
            MethodArgumentNotValidException exception,
            WebRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParameter(
            MissingServletRequestParameterException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, FileTooLargeException.class})
    public ResponseEntity<ErrorDetails> handleFileTooLarge(
            Exception exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded file exceeds the maximum allowed size",
                request
        );
    }


    @ExceptionHandler({
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorDetails> handleBadRequest(
            RuntimeException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorDetails> handleMissingPart(
            MissingServletRequestPartException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFound(
            NoResourceFoundException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorDetails> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            WebRequest request) {

        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleStoredFileNotFound(
            StoredFileNotFoundException exception,
            WebRequest request) {

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleUnexpectedException(
            Exception exception,
            WebRequest request) {

        log.error("Unexpected error", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                request
        );
    }

    private ResponseEntity<ErrorDetails> buildResponse(
            HttpStatus status,
            String message,
            WebRequest request) {

        ErrorDetails details = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(status).body(details);
    }

    @ExceptionHandler(StoredFileExpiredException.class)
    public ResponseEntity<ErrorDetails> handleStoredFileExpired(
            StoredFileExpiredException exception,
            WebRequest request) {

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ExceptionHandler(InvalidDownloadPasswordException.class)
    public ResponseEntity<ErrorDetails> handleInvalidDownloadPassword(
            InvalidDownloadPasswordException exception,
            WebRequest request) {

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}
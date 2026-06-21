package com.erpapi.gzerp.exceptionHandler;

import com.erpapi.gzerp.Exceptions.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;



@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource, ArrayList<String> errors) {
        this.messageSource = messageSource;
    }


    @Override
    protected @Nullable ResponseEntity<Object>
    handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                 HttpHeaders headers,
                                 HttpStatusCode status,
                                 WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Teste, deu o Argument Not Valid aqui."); // Invalid Request, please verify the following fields and try again
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> errorsTest = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            errorsTest.add(message);
        }
        problemDetail.setProperty("errors", errorsTest);

        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object>
    handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                 HttpHeaders headers,
                                 HttpStatusCode status,
                                 WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Teste, deu o MessageNotReadable"); // "The request contains invalid / malformed values"
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));
        Throwable cause = ex.getCause();
        if (cause != null) {
            problemDetail.setProperty("cause", ex.getCause().getMessage());
        } else {
            problemDetail.setProperty("cause", ex.getMessage());
        }
        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);

    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> HandleUserAlreadyExistException(UserAlreadyExistException ex,
                                                                  HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Company already registered, please login in a partner account");
        problemDetail.setTitle("Company already registered");
        problemDetail.setType(URI.create("/Errors/CompanyAlreadyExist"));
        problemDetail.setProperty("errors", ex.getConflictedFields());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }
}

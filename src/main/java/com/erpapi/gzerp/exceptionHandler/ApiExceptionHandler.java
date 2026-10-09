package com.erpapi.gzerp.exceptionHandler;

import com.erpapi.gzerp.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
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

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Override
    protected @Nullable ResponseEntity<Object>
    handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                 HttpHeaders headers,
                                 HttpStatusCode status,
                                 WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid Request, please verify the following fields and try again");
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
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request contains invalid / malformed values, please verify and try again");
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));
        Throwable cause = ex.getCause();
        if (cause != null) {
            problemDetail.setProperty("cause", ex.getCause().getMessage());
        } else {
            problemDetail.setProperty("cause", ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);

    }

    @ExceptionHandler(EmployeeAlreadyExistException.class)
    public ResponseEntity<Object> HandleUserAlreadyExistException(EmployeeAlreadyExistException ex,
                                                                  HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Company already registered, please login in a partner account");
        problemDetail.setTitle("Company already registered");
        problemDetail.setType(URI.create("/Errors/CompanyAlreadyExist"));
        problemDetail.setProperty("errors", ex.getConflictedFields());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> HandleInvalidCredentialsException(InvalidCredentialsException ex,
                                                                   HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Invalid Credentials");
        problemDetail.setType(URI.create("/Errors/InvalidCredentials"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> HandleInvalidRefreshTokenException(InvalidRefreshTokenException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Refresh Token invalid");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);

    }


    @ExceptionHandler(InvalidCharactersException.class)
    public ResponseEntity<ProblemDetail> HandleInvalidCharactersException(InvalidCharactersException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Invalid Character in Form");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> HandleAuth(AuthenticationException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,"Not authorized.");
        problem.setDetail(ex.getMessage());
        problem.setTitle("Bad credentials");
        return ResponseEntity.status(401).body(problem);
    }

    @ExceptionHandler(HmacSHA256GenerationException.class)
    public ResponseEntity<ProblemDetail> HandleHmacSHA256GenerationException(HmacSHA256GenerationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Error in HmacSHA256 for Sign generation");
        problem.setDetail(ex.getMessage());
        problem.setTitle("Hmac for Sigh Generation for a shopee Request failed.");
        return ResponseEntity.status(500).body(problem);
    }

    @ExceptionHandler(ExchangeCodeForTokensShopeeException.class)
    public ResponseEntity<ProblemDetail> HandleExchangeCodeForTokensShopeeException(ExchangeCodeForTokensShopeeException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,"Error in the exchange of code for Tokens in the shopee sincronization");
        problem.setTitle("Error in the Exchange Code For Tokens in the shopee Process");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }


    @ExceptionHandler(ShopeeAuthResponseException.class)
    public ResponseEntity<ProblemDetail> HandleShopeeAuthResponseException(ShopeeAuthResponseException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Error in get the access Tokens");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(InvalidShopeeStateException.class)
    public ResponseEntity<ProblemDetail> HandleInvalidShopeeStateException(InvalidShopeeStateException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.REQUEST_TIMEOUT,"Invalid state");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(problem);
    }

}

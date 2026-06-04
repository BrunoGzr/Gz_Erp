package com.erpapi.gzerp.exceptionHandler;

import com.erpapi.gzerp.Exceptions.EmptyResultDataAccessExceptionCustom;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;
    
    private List<ErrorMessages>  createListOfErrors(){
        List<ErrorMessages> errors = new ArrayList<>();
        return errors;
    };

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Override
    protected @Nullable ResponseEntity<Object>
    handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                 HttpHeaders headers,
                                 HttpStatusCode status,
                                 WebRequest request) {

        List<ErrorMessages> errorList = createListOfErrors();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String userMessage = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            String devMessage = fieldError.toString();
            errorList.add(new ErrorMessages(userMessage,devMessage));
        }
        return handleExceptionInternal(ex, errorList, headers, HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler({EmptyResultDataAccessExceptionCustom.class})
    public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessExceptionCustom ex, WebRequest request) {

        String userMessage = messageSource.getMessage(
                "invalid.resource",
                null,
                "Teste",
                LocaleContextHolder.getLocale());
        String devMessage = Arrays.toString(ex.getStackTrace());
        List<ErrorMessages> errorList = Arrays.asList(new ErrorMessages(userMessage, devMessage));
        return handleExceptionInternal(ex, errorList, new HttpHeaders(), HttpStatus.NOT_FOUND, request);


    }

    @Override
    protected @Nullable ResponseEntity<Object>
    handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                 HttpHeaders headers,
                                 HttpStatusCode status,
                                 WebRequest request) {

        String userMessage = messageSource.getMessage(
                "invalid.message",
                null,
                "Teste",
                LocaleContextHolder.getLocale());
        String devMessage = ex.getCause().toString();
        List<ErrorMessages> errorList = Arrays.asList(new ErrorMessages(userMessage, devMessage));
        return handleExceptionInternal(ex, errorList, new HttpHeaders(), status, request);


    }
    
    public static class ErrorMessages {
        private String messageUser;
        private String messageDeveloper;

        public ErrorMessages(String messageUser, String messageDeveloper) {
            this.messageUser = messageUser;
            this.messageDeveloper = messageDeveloper;
        }

        @Override
        public String toString() {
            return "ErrorMessages{" +
                    "messageUser='" + messageUser + '\'' +
                    ", messageDeveloper='" + messageDeveloper + '\'' +
                    '}';
        }

        public String getMessageUser() {
            return messageUser;
        }

        public String getMessageDeveloper() {
            return messageDeveloper;
        }
    }
}




package com.erpapi.gzerp.exceptionHandler;

import com.erpapi.gzerp.exceptions.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Request, please verify the following fields and try again");
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));

        List<String> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

            String message;

            if (fieldError.isBindingFailure()) {
                // Erro de conversão (String → Enum/Boolean/etc.)
                String field = fieldError.getField();
                Class<?> fieldType = ex.getBindingResult().getFieldType(field);

                if (fieldType != null && fieldType.isEnum()) {
                    String allowed = Arrays.stream(fieldType.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    message = "Valor inválido para '" + field
                            + "'. Valores aceitos: " + allowed + ".";
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    message = "Valor inválido para '" + field
                            + "'. Valores aceitos: true, false.";
                } else {
                    message = "Valor inválido para '" + field + "'.";
                }
            } else {
                // Erro de validação normal (@NotBlank, @Positive, etc.)
                message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            }

            errors.add(message);
        }

        problemDetail.setProperty("errors", errors);

        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReference(
            PropertyReferenceException ex,
            HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Request, please verify the following fields and try again");
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        // ex.getPropertyName() devolve "campoInexistente"
        String campo = ex.getPropertyName();
        String message = "Campo de ordenação inválido: '" + campo + "'.";

        problemDetail.setProperty("errors", List.of(message));

        return problemDetail;
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // 1. Monta o envelope padrão que você já usa
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Request, please verify the following fields and try again"
        );
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create("/Errors/Validation"));

        // 2. Extrai qual campo falhou e qual era o tipo esperado (ex: StockFilter)
        String fieldName = "unknown";
        Class<?> requiredType = null;

        if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            fieldName = mismatchEx.getName();
            requiredType = mismatchEx.getRequiredType();
        }

        // 3. Monta a mensagem amigável
        String friendlyMessage;
        if (requiredType != null && requiredType.isEnum()) {
            String allowed = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            friendlyMessage = "Invalid value for '" + fieldName + "'. Allowed values: " + allowed + ".";
        } else {
            friendlyMessage = "Invalid value for '" + fieldName + "'.";
        }

        // 4. Define a lista de erros no mesmo formato do seu handleMethodArgumentNotValid
        problemDetail.setProperty("errors", List.of(friendlyMessage));

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

    @ExceptionHandler(MarketplaceTokensAlreadyConnectedException.class)
    public ResponseEntity<ProblemDetail> HandleMarketplaceTokensAlreadyConnectedException(MarketplaceTokensAlreadyConnectedException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Shopee loggin Denied");
        problem.setTitle("Shopee Account already connected to another account.");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ProblemDetail> HandleInvalidCategoryException(InvalidCategoryException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Invalid Category");
        problem.setTitle("Problem with the Category : ");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }


    @ExceptionHandler(TenantNotFoundException.class)
    public ProblemDetail handleTenantNotFound(TenantNotFoundException ex) {
        log.error("Tenant inconsistente: id={} presente no JWT mas ausente no banco", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("/Errors/Internal"));
        pd.setProperty("errors", List.of("Contate o suporte."));
        return pd;

    }

    @ExceptionHandler(UnknownProductException.class)
    public ProblemDetail handleUnknownProductException(UnknownProductException ex,
                                                       HttpServletRequest request){
        String url = request.getRequestURI();
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }
        log.error("Erro no endpoint: {} {} - Mensagem: {}",request.getMethod(), url, ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Item Not found in operation: {" + ex.getMessage() +"}");
        pd.setTitle("Not found");

        return pd;
    }

    @ExceptionHandler(TenantFraudException.class)
    public ProblemDetail handleTenantFraudException(TenantFraudException ex,
                                                    HttpServletRequest request){
        String url = request.getRequestURI();
        url = request.getQueryString() == null ? url : url +"?" + request.getQueryString();
        log.error("Possivel atacante no endpoint: {} {} - Mensagem: {} \n" +
                " ",request.getMethod(),url,ex.getMessage());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "Item not found");
        pd.setTitle("Not found");

        return pd;
    }

    @ExceptionHandler(InvalidProductException.class)
    public ProblemDetail handleInvalidProductException(InvalidProductException ex){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Invalid Product, conflict in : " +  ex.getMessage());
        return pd;
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                 "Entity: " + ex.getMessage() + " Not found");
        return pd;
    }

}

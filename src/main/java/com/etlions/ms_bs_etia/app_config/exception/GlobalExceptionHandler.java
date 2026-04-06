package com.etlions.ms_bs_etia.app_config.exception;

import com.etlions.openapicodegen.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "ERR-404", ex.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return buildResponse(HttpStatus.valueOf(422), "ERR-422", ex.getMessage(), null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-400", ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(SunatValidationException.class)
    public ResponseEntity<ErrorResponse> handleSunatValidation(SunatValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-SUNAT", ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-400",
                "Error de validación en los campos enviados", errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR-500",
                "Error interno del servidor: " + ex.getMessage(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                         String code,
                                                         String message,
                                                         List<String> errors) {
        ErrorResponse body = new ErrorResponse()
                .code(code)
                .message(message)
                .timestamp(new Date())
                .errors(errors != null ? errors : List.of());
        return ResponseEntity.status(status).body(body);
    }
}


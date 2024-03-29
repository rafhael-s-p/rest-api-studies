package com.studies.foodorders.api.exceptionhandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.studies.foodorders.core.validation.ValidationException;
import com.studies.foodorders.domain.exceptions.BusinessException;
import com.studies.foodorders.domain.exceptions.NotFoundEntityException;
import com.studies.foodorders.domain.exceptions.UsedEntityException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String GENERIC_END_USER_MESSAGE
            = "An unexpected internal system error has occurred. Please try again and if "
            + "the problem persists, contact your system administrator.";

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler({ ValidationException.class })
    public ResponseEntity<Object> handleValidationException(ValidationException e, WebRequest request) {
        return handleValidationInternal(e, e.getBindingResult(), new HttpHeaders(),
                HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).headers(headers).build();
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
                                                         WebRequest request) {

        return handleValidationInternal(ex, ex.getBindingResult(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleValidationInternal(ex, ex.getBindingResult(), headers, status, request);
    }


    private ResponseEntity<Object> handleValidationInternal(Exception ex, BindingResult bindingResult, HttpHeaders headers,
                                                            HttpStatus status, WebRequest request) {
        ApiErrorType apiErrorType = ApiErrorType.INVALID_PARAMETER;
        String detail = "Invalid Fields";

        List<ApiError.Object> problemObjects = bindingResult.getAllErrors().stream()
                .map(objectError -> {
                    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());

                    String name = objectError.getObjectName();

                    if (objectError instanceof FieldError) {
                        name = ((FieldError) objectError).getField();
                    }

                    return ApiError.Object.builder()
                            .name(name)
                            .userMessage(message)
                            .build();
                })
                .collect(Collectors.toList());

        ApiError apiError = createApiErrorBuilder(status, apiErrorType, detail)
                .userMessage(detail)
                .objects(problemObjects)
                .build();

        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception e, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiErrorType errorType = ApiErrorType.INTERNAL_SERVER_ERROR;
        String detail = GENERIC_END_USER_MESSAGE;

        log.error(e.getMessage(), e);

        ApiError error = createApiErrorBuilder(status, errorType, detail).build();

        return handleExceptionInternal(e, error, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiErrorType errorType = ApiErrorType.RESOURCE_NOT_FOUND;
        String detail = String.format("Resource %s not found.", e.getRequestURL());
        ApiError error = createApiErrorBuilder(status, errorType, detail).build();
        return handleExceptionInternal(e, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (e instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(
                    (MethodArgumentTypeMismatchException) e, headers, status, request);
        }
        return super.handleTypeMismatch(e, headers, status, request);
    }

    private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        ApiErrorType errorType = ApiErrorType.INVALID_PARAMETER;

        String detail = String.format("URL Parameter '%s' received value '%s'. Expected Type: %s",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(GENERIC_END_USER_MESSAGE)
                .build();

        return handleExceptionInternal(ex, error, headers, status, request);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof InvalidFormatException) {
            return handleInvalidFormatException((InvalidFormatException) rootCause, headers, status, request);
        } else if (rootCause instanceof PropertyBindingException) {
            return handlePropertyBindingException((PropertyBindingException) rootCause, headers, status, request);
        }
        ApiErrorType errorType = ApiErrorType.INCOMPREHENSIBLE_MESSAGE;
        String detail = "Bad Formatted Json Or Invalid Request";
        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(GENERIC_END_USER_MESSAGE)
                .build();

        return handleExceptionInternal(e, error, headers, status, request);
    }

    private ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException e, HttpHeaders headers,
                                                                HttpStatus status, WebRequest request) {
        String path = joinPath(e.getPath());
        ApiErrorType errorType = ApiErrorType.INVALID_REQUEST;
        String detail = String.format("Property %s received invalid value %s. %s is expected.",
                path, e.getValue(), e.getTargetType().getSimpleName());
        ApiError apiError = createApiErrorBuilder(status, errorType, detail)
                                .userMessage(GENERIC_END_USER_MESSAGE)
                                .build();

        return handleExceptionInternal(e, apiError, headers, status, request);
    }

    private ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException e,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

		String path = joinPath(e.getPath());
        ApiErrorType errorType = ApiErrorType.INVALID_REQUEST;
		String detail = String.format("Property '%s' does not exist. ", path);
        ApiError apiError = createApiErrorBuilder(status, errorType, detail)
                                .userMessage(GENERIC_END_USER_MESSAGE)
                                .build();

		return handleExceptionInternal(e, apiError, headers, status, request);
	}

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiErrorType errorType = ApiErrorType.BUSINESS_ERROR;
        String detail = e.getMessage();

        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(e, error, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleEntidadeNaoEncontrada(AccessDeniedException ex, WebRequest request) {

        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiErrorType errorType = ApiErrorType.ACCESS_DENIED;
        String detail = ex.getMessage();

        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(detail)
                .userMessage("You do not have permission to perform this operation.")
                .build();

        return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<?> handleNotFoundEntityException(NotFoundEntityException e, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiErrorType errorType = ApiErrorType.RESOURCE_NOT_FOUND;
        String detail = e.getMessage();
        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(e, error, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(UsedEntityException.class)
    public ResponseEntity<?> handleUsedEntityException(UsedEntityException e, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ApiErrorType errorType = ApiErrorType.USED_ENTITY;
        String detail = e.getMessage();
        ApiError error = createApiErrorBuilder(status, errorType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(e, error, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (body == null) {
            body = ApiError.builder()
                    .timestamp(OffsetDateTime.now())
                    .title(status.getReasonPhrase())
                    .status(status.value())
                    .userMessage(GENERIC_END_USER_MESSAGE)
                    .build();
        }
        else if (body instanceof String) {
            body = ApiError.builder()
                    .timestamp(OffsetDateTime.now())
                    .title((String) body)
                    .status(status.value())
                    .userMessage(GENERIC_END_USER_MESSAGE)
                    .build();
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    private ApiError.ApiErrorBuilder createApiErrorBuilder(HttpStatus status, ApiErrorType errorType, String detail) {
        return ApiError.builder()
                .status(status.value())
                .type(errorType.getUri())
                .title(errorType.getTitle())
                .detail(detail)
                .timestamp(OffsetDateTime.now());
    }

    private String joinPath(List<Reference> references) {
        return references.stream()
                .map(ref -> ref.getFieldName())
                .collect(Collectors.joining("."));
    }
}

package com.trodix.clipystream.core.exception;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This class generates a custom json response for Exceptions.
 * 
 * <p>
 * <i>Examples for creating a custom payload exception response:</i>
 * </p>
 * 
 * <pre>
 * &#64;ExceptionHandler(BadRequestException.class)
 * public ResponseEntity&#60;?&#62; handleBadRequestException(BadRequestException exception, WebRequest request) {
 *     ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
 *             request.getDescription(false));
 *
 *     return new ResponseEntity&#60;&#62;(errorDetails, HttpStatus.NOT_FOUND);
 * }
 * </pre>
 * 
 * <p>
 * <i>Examples for creating a standard payload for a custom exception :</i>
 * </p>
 * 
 * <pre>
 * &#64;ExceptionHandler(ResourceNotFoundException.class)
 * public void handleResourceNotFoundException(final ResourceNotFoundException exception, final HttpServletResponse response) throws IOException {
 *     response.sendError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
 * }
 * </pre>
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ===============================
    // = Handle specific exceptions
    // ===============================

    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(final ResourceNotFoundException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public void handleBadRequestException(final BadRequestException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(final AccessDeniedException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.FORBIDDEN.value(), exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public void handleBadCredentialsException(final BadCredentialsException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public void handleUnauthorizedException(final UnauthorizedException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void handleMaxUploadSizeExceededException(final MaxUploadSizeExceededException exception, final WebRequest request,
            final HttpServletResponse response)
            throws IOException {

        long maxFileSizeLimit = -1L;
        if (exception.getCause().getCause() instanceof SizeLimitExceededException) {
            final SizeLimitExceededException cause = (SizeLimitExceededException) exception.getCause().getCause();
            maxFileSizeLimit = cause.getPermittedSize();
        }

        final String message = MessageFormat.format("Uploaded file is too large. File size cannot exceed {0} MB", maxFileSizeLimit / 1024 / 1024);

        response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value(), message);
    }


    /**
     * Handle invalid path variable. ex: String to UUID conversion exception
     * 
     * @param exception
     * @param response
     * @throws IOException
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception, final HttpServletResponse response)
            throws IOException {

        response.sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    /**
     * Handle validation exceptions
     * 
     * @param exception
     * @param response
     * @throws IOException
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
            final WebRequest request) {

        final List<String> errorList = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        final ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errorList);

        return handleExceptionInternal(ex, errorDetails, headers, errorDetails.getStatus(), request);
    }



    // ===============================
    // = Handle global exceptions
    // ===============================


    @ExceptionHandler(Exception.class)
    public void handleGlobalException(final Exception exception, final HttpServletResponse response)
            throws IOException {

        // response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An internal server error occured");
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
    }
}

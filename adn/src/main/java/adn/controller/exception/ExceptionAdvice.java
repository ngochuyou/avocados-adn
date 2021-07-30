/**
 * 
 */
package adn.controller.exception;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import adn.controller.BaseController;

/**
 * @author Ngoc Huy
 *
 */
@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { AccessDeniedException.class })
	public ResponseEntity<?> handleUnauthorized(RuntimeException ex, WebRequest request) {
		return handleExceptionInternal(ex, BaseController.ACCESS_DENIED, new HttpHeaders(), HttpStatus.UNAUTHORIZED,
				request);
	}

	@ExceptionHandler(value = { FileSizeLimitExceededException.class })
	public ResponseEntity<?> handleFileSizeLimitExceeded(Exception ex, WebRequest request) {
		return handleExceptionInternal(ex,
				String.format("File size is too large, expect size to be less than %d MB",
						BaseController.MAXIMUM_FILE_SIZE / (1024 * 1024)),
				new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { NoSuchFieldException.class })
	public ResponseEntity<?> handleUnknownColumnInFetch(Exception ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { UnauthorisedDepartmentException.class })
	public ResponseEntity<?> handleUnauthorisedDepartment(RuntimeException ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

}

/**
 * 
 */
package adn.controller.exception;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import adn.controller.BaseController;
import adn.controller.query.BadColumnsRequestException;
import adn.service.specification.InvalidCriteriaException;

/**
 * @author Ngoc Huy
 *
 */
@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { AccessDeniedException.class })
	public ResponseEntity<?> handleUnauthorized(AccessDeniedException ex, WebRequest request) {
		return handleExceptionInternal(ex, BaseController.ACCESS_DENIED, new HttpHeaders(), HttpStatus.UNAUTHORIZED,
				request);
	}

	@ExceptionHandler(value = { FileSizeLimitExceededException.class })
	public ResponseEntity<?> handleFileSizeLimitExceeded(FileSizeLimitExceededException ex, WebRequest request) {
		return handleExceptionInternal(ex,
				String.format("File size is too large, expect size to be less than %d MB",
						BaseController.MAXIMUM_FILE_SIZE / (1024 * 1024)),
				new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { NoSuchFieldException.class })
	public ResponseEntity<?> handleUnknownColumnInFetch(NoSuchFieldException ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { UnauthorisedDepartmentException.class })
	public ResponseEntity<?> handleUnauthorisedDepartment(UnauthorisedDepartmentException ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(value = { IllegalArgumentException.class })
	public ResponseEntity<?> handlePossibleEmptyPredicate(IllegalArgumentException ex, WebRequest request) {
		if (ex.getCause() instanceof InvalidCriteriaException) {
			return handleExceptionInternal(ex, BaseController.INVALID_SEARCH_CRITERIA, new HttpHeaders(), HttpStatus.BAD_REQUEST,
					request);
		}

		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		ObjectError source = ex.getBindingResult().getAllErrors().get(0);

		if (source != null) {
			try {
				MethodInvocationException mie = source.unwrap(MethodInvocationException.class);
				Throwable possibleBCRE = mie.getCause();

				if (possibleBCRE instanceof BadColumnsRequestException) {
					return handleExceptionInternal(ex, possibleBCRE.getMessage(), new HttpHeaders(),
							HttpStatus.BAD_REQUEST, request);
				}
			} catch (IllegalArgumentException iae) {}
		}

		return super.handleBindException(ex, headers, status, request);
	}

}

/**
 * 
 */
package adn.controller.exception;

import java.util.Map;

import org.hibernate.ObjectNotFoundException;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import adn.application.Common;
import adn.controller.BaseController;
import adn.controller.query.BadColumnsRequestException;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.internal.InvalidCriteriaException;

/**
 * @author Ngoc Huy
 *
 */
@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	private static final Map<String, String> MAX_UPLOAD_SIZE_EXCEEDED = Map.of(Common.MESSAGE,
			String.format("File size is too large, expect size to be less than %d MB",
					BaseController.MAXIMUM_FILE_SIZE / (1024 * 1024)));

	@ExceptionHandler(value = { AccessDeniedException.class })
	public ResponseEntity<?> handleUnauthorized(AccessDeniedException ex, WebRequest request) {
		return handleExceptionInternal(ex, Common.ACCESS_DENIED, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(value = { MaxUploadSizeExceededException.class })
	public ResponseEntity<?> handleFileSizeLimitExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
		return handleExceptionInternal(ex, MAX_UPLOAD_SIZE_EXCEEDED, new HttpHeaders(), HttpStatus.BAD_REQUEST,
				request);
	}

	@ExceptionHandler(value = { NoSuchFieldException.class })
	public ResponseEntity<?> handleUnknownColumnInFetch(NoSuchFieldException ex, WebRequest request) {
		return handleExceptionInternal(ex, Map.of(Common.MESSAGE, ex.getMessage()), new HttpHeaders(),
				HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { UnauthorisedDepartmentException.class })
	public ResponseEntity<?> handleUnauthorisedDepartment(UnauthorisedDepartmentException ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(value = { UnauthorizedCredential.class })
	public ResponseEntity<?> handleUnauthorisedCredential(UnauthorizedCredential ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(value = { IllegalArgumentException.class })
	public ResponseEntity<?> handlePossibleEmptyPredicate(IllegalArgumentException ex, WebRequest request) {
		if (ex.getCause() instanceof InvalidCriteriaException) {
			return handleExceptionInternal(ex, Common.INVALID_SEARCH_CRITERIA, new HttpHeaders(),
					HttpStatus.BAD_REQUEST, request);
		}

		ex.printStackTrace();
		
		return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

	@ExceptionHandler(value = { ObjectNotFoundException.class })
	public ResponseEntity<?> handleObjectNotFoundException(ObjectNotFoundException ex, WebRequest request) {
		return handleExceptionInternal(ex, Common.notfound(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
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

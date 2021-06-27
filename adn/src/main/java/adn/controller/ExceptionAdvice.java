/**
 * 
 */
package adn.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

}

/**
 * 
 */
package adn.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import adn.service.transaction.GlobalTransaction;
import adn.service.transaction.GlobalTransactionManager;
import adn.service.transaction.TransactionException;

/**
 * @author Ngoc Huy
 *
 */
@ControllerAdvice
public class ADNExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private GlobalTransactionManager globalTransactionManager;

	@ExceptionHandler(TransactionException.class)
	public ResponseEntity<String> handleTransactionException(HttpServletRequest req, TransactionException ex) {
		globalTransactionManager.lockTransaction(ex.getTransactionId());

		GlobalTransaction transaction = globalTransactionManager.getTransaction(ex.getTransactionId());
		String message = "FATAL: Failed to execute serive";
		
		try {
			transaction.rollback();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message += " and could not rollback transaction";
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
	}

}

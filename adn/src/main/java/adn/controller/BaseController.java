/**
 * 
 */
package adn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import adn.application.managers.AuthenticationBasedEMFactory;
import adn.application.managers.ModelManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class BaseController {

	@Autowired
	protected ModelManager modelManager;

	@Autowired
	protected AuthenticationBasedEMFactory factoryManager;

	protected final String HASROLE_ADMIN = "hasRole('ADMIN')";

	public <T> ResponseEntity<T> handleSuccess(T body) {

		return new ResponseEntity<T>(body, null, HttpStatus.OK);
	}

	public <T> ResponseEntity<T> handleFailure(T body, int status) {

		return new ResponseEntity<T>(body, HttpStatus.valueOf(status));
	}

}

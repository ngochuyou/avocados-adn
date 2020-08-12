/**
 * 
 */
package adn.model.specification.generic;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import adn.model.Result;
import adn.model.entities.Account;
import adn.model.specification.GenericSpecification;
import adn.model.specification.Specification;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Account.class)
public class AccountSpecification implements Specification<Account> {

	@Override
	public Result<Account> isSatisfiedBy(Account instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null || instance.getId().length() < 8 || instance.getId().length() > 255) {
			messageSet.put("id", "Id length must be between 8 and 255");
			flag = false;
		}

		if (!Strings.isEmail(instance.getEmail())) {
			messageSet.put("email", "Invalid email");
			flag = false;
		}

		if (!Strings.isDigits(instance.getPhone())) {
			messageSet.put("phone", "Invalid phone number");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getLastName())) {
			messageSet.put("lastName", "Lastname can not be empty");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getFirstName())) {
			messageSet.put("firstName", "Firstname can not be empty");
			flag = false;
		}

		if (Strings.isBCrypt(instance.getPassword())) {
			messageSet.put("password", "Invalid password");
			flag = false;
		}

		if (instance.getRole() == null) {
			messageSet.put("role", "Role can not be empty");
			flag = false;
		}

		if (instance.getGender() == null) {
			messageSet.put("gender", "Gender can not be empty");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(HttpStatus.BAD_GATEWAY.ordinal(), instance, messageSet);
	}

}

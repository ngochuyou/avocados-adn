/**
 * 
 */
package adn.model.specification.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		Set<Integer> status = new HashSet<>();
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null || instance.getId().length() < 8 || instance.getId().length() > 255) {
			status.add(Result.BAD);
			messageSet.put("id", "Id length must be between 8 and 255");
			flag = false;
		}

		if (!Strings.isEmail(instance.getEmail())) {
			status.add(Result.BAD);
			messageSet.put("email", "Invalid email");
			flag = false;
		}

		if (!Strings.isDigits(instance.getPhone())) {
			status.add(Result.BAD);
			messageSet.put("phone", "Invalid phone number");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getLastName())) {
			status.add(Result.BAD);
			messageSet.put("lastName", "Lastname can not be empty");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getFirstName())) {
			status.add(Result.BAD);
			messageSet.put("firstName", "Firstname can not be empty");
			flag = false;
		}

		if (Strings.isBCrypt(instance.getPassword())) {
			status.add(Result.BAD);
			messageSet.put("password", "Invalid password");
			flag = false;
		}

		if (instance.getRole() == null) {
			status.add(Result.BAD);
			messageSet.put("role", "Role can not be empty");
			flag = false;
		}

		if (instance.getGender() == null) {
			status.add(Result.BAD);
			messageSet.put("gender", "Gender can not be empty");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(status, instance, messageSet);
	}

}

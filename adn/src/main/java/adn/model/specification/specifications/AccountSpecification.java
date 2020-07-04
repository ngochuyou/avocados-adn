/**
 * 
 */
package adn.model.specification.specifications;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import adn.model.ModelResult;
import adn.model.entities.Account;
import adn.model.specification.CompositeSpecification;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class AccountSpecification extends CompositeSpecification<Account> {

	@Override
	public ModelResult<Account> isSatisfiedBy(Account instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Set<Integer> status = new HashSet<>();
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null || instance.getId().length() < 8 || instance.getId().length() > 255) {
			status.add(400);
			messageSet.put("id", "Id length must be between 8 and 255");
			flag = false;
		}

		if (!Strings.isEmail(instance.getEmail())) {
			status.add(400);
			messageSet.put("email", "Invalid email");
			flag = false;
		}

		if (!Strings.isDigits(instance.getPhone())) {
			status.add(400);
			messageSet.put("phone", "Invalid phone number");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getLastName())) {
			status.add(400);
			messageSet.put("lastName", "Lastname can not be empty");
			flag = false;
		}

		if (StringUtils.isEmpty(instance.getFirstName())) {
			status.add(400);
			messageSet.put("firstName", "Firstname can not be empty");
			flag = false;
		}

		if (Strings.isBCrypt(instance.getPassword())) {
			status.add(400);
			messageSet.put("password", "Invalid password");
			flag = false;
		}

		if (instance.getRole() == null) {
			status.add(400);
			messageSet.put("role", "Role can not be empty");
			flag = false;
		}

		if (instance.getGender() == null) {
			status.add(400);
			messageSet.put("gender", "Gender can not be empty");
			flag = false;
		}

		return flag ? ModelResult.success(instance) : ModelResult.error(status, instance, messageSet);
	}

}

/**
 * 
 */
package adn.model.models;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.model.Genetized;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Genetized(entityGene = Account.class)
public class AccountModel extends Model {

	protected String username;

	protected String email;

	protected String phone;

	protected String firstName;

	protected String lastName;

	protected String photo;

	protected String password;

	protected String role;

	protected String gender;

	@Override
	public String getId() {
		return username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

}

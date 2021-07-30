/**
 * 
 */
package adn.model.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import adn.model.entities.constants.Gender;
import adn.service.internal.Role;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "accounts")
public class Account extends adn.model.entities.Entity {

	@Transient
	public static transient final String ACTIVE_FIELD_NAME = "active";

	@Transient
	public static transient final String ROLE_FIELD_NAME = "role";

	@Transient
	public static transient final String VERSION_FIELD_NAME = "updatedDate";

	@Transient
	public static transient final String ID_FIELD_NAME = "id";

	@Id
	@JsonProperty("username")
	private String id;

	@Column(nullable = false)
	private String email;

	private String phone;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(updatable = false, length = DefaultResourceIdentifierGenerator.IDENTIFIER_LENGTH)
	private String photo;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, columnDefinition = "VARCHAR(20)")
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, columnDefinition = "VARCHAR(20)")
	private Gender gender;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "deactivated_date")
	private LocalDate deactivatedDate;

	@CreationTimestamp
	@Column(name = "created_date", nullable = false, updatable = false)
	private LocalDate createdDate;

	@UpdateTimestamp
	@Column(name = "updated_date", nullable = false)
	private LocalDateTime updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	@JsonIgnore
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@JsonIgnore
	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	@JsonIgnore
	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@JsonIgnore
	public LocalDate getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(LocalDate deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

}

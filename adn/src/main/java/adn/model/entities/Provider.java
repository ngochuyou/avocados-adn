/**
 * 
 */
package adn.model.entities;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.application.Common;
import adn.model.entities.converters.StringListConverter;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "providers")
public class Provider extends PermanentEntity implements NamedResource {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = Common.UUID_MYSQL_COLUMN_DEFINITION)
	protected UUID id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(length = _Provider.WEBSITE_MAX_LENGTH)
	private String website;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false, name = "phone_numbers")
	@Convert(converter = StringListConverter.class)
	private List<String> phoneNumbers;

	@Column(name = "representator_name")
	private String representatorName;

	@JsonIgnore
	@OneToMany(mappedBy = _ProductCost.provider)
	private List<ProductCost> productCosts;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getRepresentatorName() {
		return representatorName;
	}

	public void setRepresentatorName(String representatorName) {
		this.representatorName = representatorName;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public List<ProductCost> getProductCosts() {
		return productCosts;
	}

	public void setProductCosts(List<ProductCost> productDetails) {
		this.productCosts = productDetails;
	}

}

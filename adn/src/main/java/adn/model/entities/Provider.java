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

import adn.model.DepartmentScoped;
import adn.model.entities.converters.StringListConverter;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "providers")
public class Provider extends Factor implements DepartmentScoped {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = "BINARY(16)")
	protected UUID id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, name = "phone_numbers")
	@Convert(converter = StringListConverter.class)
	private List<String> phoneNumbers;

	@Column(name = "representator_name")
	private String representatorName;

	@Column(length = _Provider.WEBSITE_MAX_LENGTH)
	private String website;

	@JsonIgnore
	@OneToMany(mappedBy = "provider")
	private List<ProductProviderDetail> productDetails;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public List<ProductProviderDetail> getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(List<ProductProviderDetail> productDetails) {
		this.productDetails = productDetails;
	}

}

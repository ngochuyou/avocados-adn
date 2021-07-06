/**
 * 
 */
package adn.model.entities;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import adn.model.entities.converters.StringSetConverter;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "providers")
public class Provider extends Factor {

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, name = "phone_numbers")
	@Convert(converter = StringSetConverter.class)
	private Set<String> phoneNumbers;

	@Column(name = "representator_name")
	private String representatorName;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "provider", fetch = FetchType.LAZY)
	private List<ProductProviderDetail> productDetails;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "provider", fetch = FetchType.LAZY)
	private List<MaterialProviderDetail> materialDetails;

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

	public Set<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(Set<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getRepresentatorName() {
		return representatorName;
	}

	public void setRepresentatorName(String representatorName) {
		this.representatorName = representatorName;
	}

	public List<ProductProviderDetail> getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(List<ProductProviderDetail> productDetails) {
		this.productDetails = productDetails;
	}

	public List<MaterialProviderDetail> getMaterialDetails() {
		return materialDetails;
	}

	public void setMaterialDetails(List<MaterialProviderDetail> materialDetails) {
		this.materialDetails = materialDetails;
	}

}
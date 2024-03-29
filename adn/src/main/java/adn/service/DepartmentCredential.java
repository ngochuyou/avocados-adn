/**
 * 
 */
package adn.service;

import static adn.application.context.builders.DepartmentScopeContext.customerService;
import static adn.application.context.builders.DepartmentScopeContext.personnel;
import static adn.application.context.builders.DepartmentScopeContext.sale;
import static adn.application.context.builders.DepartmentScopeContext.stock;
import static adn.application.context.builders.DepartmentScopeContext.unknown;
import static adn.service.internal.Role.PERSONNEL;

import java.util.Map;
import java.util.UUID;

import adn.application.context.builders.CredentialFactory;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.OnMemoryCredential;

/**
 * @author Ngoc Huy
 *
 */
public class DepartmentCredential implements OnMemoryCredential {

	private final String departmentId;

	private final int hashCode;

	public DepartmentCredential(UUID uuid) {
		super();
		departmentId = uuid.toString();
		hashCode = departmentId.hashCode();
	}

	public DepartmentCredential(String departmentId) {
		super();
		this.departmentId = departmentId;
		hashCode = departmentId.hashCode();
	}

	public String getDepartmentId() {
		return departmentId;
	}

	@Override
	public String evaluate() {
		return departmentId;
	}

	@Override
	public int getPosition() {
		return CredentialFactory.DEPARTMENT_ID_CREDENTIAL_POSITION;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		DepartmentCredential other = (DepartmentCredential) obj;

		return departmentId.equals(other.departmentId);
	}

	// @formatter:off
	@Credentials
	public static final Map<UUID, DepartmentCredential> CREDENTIALS = Map.of(
			stock(), new DepartmentCredential(stock()),
			sale(), new DepartmentCredential(sale()),
			personnel(), new DepartmentCredential(personnel()),
			customerService(), new DepartmentCredential(customerService()),
			unknown(), new DepartmentCredential(unknown()));
	// @formatter:on

	public static final Credential STOCK_CREDENTIAL = CredentialFactory.compound(PERSONNEL, stock());
	public static final Credential SALE_CREDENTIAL = CredentialFactory.compound(PERSONNEL, sale());
	public static final Credential PERSONNEL_CREDENTIAL = CredentialFactory.compound(PERSONNEL, personnel());
	public static final Credential CUSTOMER_SERVICE_CREDENTIAL = CredentialFactory.compound(PERSONNEL, customerService());

	@Override
	public boolean contains(Credential credential) {
		return equal(credential);
	}

	@Override
	public boolean contains(String evaluation) {
		return departmentId.equals(evaluation);
	}

}

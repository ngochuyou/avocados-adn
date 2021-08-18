/**
 * 
 */
package adn.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.springframework.boot.context.properties.ConstructorBinding;

import adn.application.context.builders.CredentialFactory;
import adn.application.context.builders.DepartmentScopeContext;
import adn.model.factory.authentication.OnMemoryCredential;

/**
 * @author Ngoc Huy
 *
 */
@ConstructorBinding
public class DepartmentCredential implements OnMemoryCredential {

	private final String departmentId;

	public DepartmentCredential(UUID uuid) {
		super();
		this.departmentId = uuid.toString();
	}

	public DepartmentCredential(String departmentId) {
		super();
		this.departmentId = departmentId;
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
	// @formatter:off
 

	@Credentials
	public static final Collection<DepartmentCredential> CREDENTIALS = Collections
			.unmodifiableCollection(Arrays.asList(
					new DepartmentCredential(DepartmentScopeContext.stock()),
					new DepartmentCredential(DepartmentScopeContext.sale()),
					new DepartmentCredential(DepartmentScopeContext.personnel()),
					new DepartmentCredential(DepartmentScopeContext.customerService()),
					new DepartmentCredential(DepartmentScopeContext.unknown())));
	// @formatter:on
}

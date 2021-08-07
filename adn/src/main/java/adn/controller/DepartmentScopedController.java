/**
 * 
 */
package adn.controller;

import static adn.service.DepartmentScoping.assertDepartment;
import static adn.service.DepartmentScoping.sale;
import static adn.service.DepartmentScoping.stock;

import java.util.UUID;

import adn.application.context.ContextProvider;
import adn.service.services.DepartmentService;

/**
 * @author Ngoc Huy
 *
 */
public abstract class DepartmentScopedController extends BaseController {

	protected final DepartmentService departmentService;

	public DepartmentScopedController(DepartmentService departmentService) {
		super();
		this.departmentService = departmentService;
	}

	protected UUID getPrincipalDepartment() {
		return departmentService.getPersonnelDepartmentId(ContextProvider.getPrincipalName());
	}

	protected void assertSaleDepartment() {
		assertDepartment(getPrincipalDepartment(), sale());
	}

	protected void assertStockDepartment() {
		assertDepartment(getPrincipalDepartment(), stock());
	}

}

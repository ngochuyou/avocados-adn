import { departmentScope } from '../../config/default';
import { useAuth } from '../../hooks/authentication-hooks';

import Account from '../../models/Account';

export function HeadScope({ children }) {
	const { principal } = useAuth();

	if (principal == null) {
		return null;
	}

	return principal.role === Account.Role.HEAD ? children : null;
}

export function StockScope({ children }) {
	const { principal } = useAuth();

	if (principal == null) {
		return null;
	}
	
	const { departmentId } = principal;

	return principal.role === Account.Role.HEAD || departmentId === departmentScope.Stock ? children : null;
}

export function SaleScope({ children }) {
	const { principal } = useAuth();

	if (principal == null) {
		return null;
	}

	const { departmentId } = principal;

	return principal.role === Account.Role.HEAD || departmentId === departmentScope.Sale ? children : null;
}

export function PersonnelScope({ children }) {
	const { principal } = useAuth();

	if (principal == null) {
		return null;
	}

	const { departmentId } = principal;

	return principal.role === Account.Role.HEAD || departmentId === departmentScope.Personnel ? children : null;
}

export function CustomerServiceScope({ children }) {
	const { principal } = useAuth();

	if (principal == null) {
		return null;
	}

	const { departmentId } = principal;

	return principal.role === Account.Role.HEAD || departmentId === departmentScope.CustomerService ? children : null;
}
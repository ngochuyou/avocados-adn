import {
	createContext, useContext, useEffect,
	useCallback, useState
} from 'react';
import { fetchPrincipal } from '../auth';
import { getPersonnelDepartmentId } from '../actions/account';

import Account from '../models/Account';

const AuthenticationContext = createContext({});

export const useAuth = () => useContext(AuthenticationContext);

const doFetchPrincipal = async (setPrincipal = () => null) => {
	let [principal, err] = await fetchPrincipal([ "username", "role", "firstName", "lastName", "photo" ]);

	if (err) {
		console.error(err);
		return;
	}

	if (principal.role === Account.Role.PERSONNEL && principal.departmentId == null) {
		const [departmentId, err] = await getPersonnelDepartmentId({ username: principal.username });

		if (err) {
			console.error(err);
			return;
		}

		principal = new Account({
			...principal,
			"departmentId": departmentId
		});
	}

	setPrincipal(principal);
};

export default function AuthenticationContextProvider({ children }) {
	const [principal, setPrincipal] = useState(null);

	useEffect(() => {
		doFetchPrincipal(setPrincipal);

		return () => setPrincipal(null);
	}, []);

	const fetchPrincipal = useCallback(() => doFetchPrincipal(setPrincipal), []);
	const evictPrincipal = useCallback(() => setPrincipal(null), []);
	const modifyPrincipal = useCallback((nextState) => setPrincipal({
		...principal,
		...nextState
	}), [principal]);

	return <AuthenticationContext.Provider value={{
		principal, evictPrincipal, fetchPrincipal,
		modifyPrincipal, setPrincipal
	}}>
		{ children }
	</AuthenticationContext.Provider>;
}
import { createContext, useReducer, useContext, useEffect } from 'react';
import { fetchPrincipal } from '../auth';
import { getPersonnelDepartmentId } from '../actions/account';

import Account from '../models/Account';

const AuthenticationContext = createContext({});

export const useAuth = () => useContext(AuthenticationContext);

export default function AuthenticationContextProvider({ children }) {
	const [principal, setPrincipal] = useReducer((principal, nextPrincipal) =>
			principal === null ? nextPrincipal : { ...principal, ...nextPrincipal },
			null);

	useEffect(() => {
		const doFetchPrincipal = async () => {
			let principal = await fetchPrincipal([ "username", "role" ]);

			if (principal != null && principal.role === Account.Role.PERSONNEL && principal.departmentId == null) {
				const [departmentId, err] = await getPersonnelDepartmentId({ username: principal.username });

				if (err) {
					console.error(err);
					return;
				}

				principal = {
					...principal,
					"departmentId": departmentId
				};
			}

			setPrincipal(principal);
		};

		doFetchPrincipal();

		return () => setPrincipal(null);
	}, []);

	return <AuthenticationContext.Provider value={{ principal, setPrincipal }}>
		{ children }
	</AuthenticationContext.Provider>;
}
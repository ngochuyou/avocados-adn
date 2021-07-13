import { createContext, useReducer, useContext, useEffect } from 'react';
import { fetchPrincipal } from '../auth';

const AuthenticationContext = createContext({});

export const useAuth = () => useContext(AuthenticationContext);

export default function AuthenticationContextProvider({ children }) {
	const [principal, setPrincipal] = useReducer((principal, nextPrincipal) =>
			principal === null ? nextPrincipal : { ...principal, ...nextPrincipal },
			null);
	const logout = () => setPrincipal(null);

	useEffect(() => {
		const doFetchPrincipal = async () => {
			const res = await fetchPrincipal();

			setPrincipal(res);
		};

		doFetchPrincipal();

		return () => logout();
	}, []);

	return <AuthenticationContext.Provider value={{ principal, logout }}>
		{ children }
	</AuthenticationContext.Provider>;
}
import { createContext, useReducer, useContext, useEffect } from 'react';
import { fetchPrincipal } from '../auth';

const AuthenticationContext = createContext({});

export const useAuth = () => useContext(AuthenticationContext);

export default function AuthenticationContextProvider({ children }) {
	const [principal, setPrincipal] = useReducer((principal, nextPrincipal) =>
			principal === null ? nextPrincipal : { ...principal, ...nextPrincipal },
			null);

	useEffect(() => {
		const doFetchPrincipal = async () => {
			const res = await fetchPrincipal([ "username", "role", "photo" ]);

			setPrincipal(res);
		};

		doFetchPrincipal();

		return () => setPrincipal(null);
	}, []);

	return <AuthenticationContext.Provider value={{ principal, setPrincipal }}>
		{ children }
	</AuthenticationContext.Provider>;
}
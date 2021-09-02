import { createContext, useContext, useCallback } from 'react';
import { useDispatch } from './hooks';

import { PUSH_LIST } from '../actions/common';

import { hasLength } from '../utils';

const GlobalProviderContext = createContext();

export const useProvider = () => useContext(GlobalProviderContext);

const STORE = {
	elements: {
		wasInit: false,
		map: {}	
	}
};

export default function GlobalProviderContextProvider({ children }) {
	const [store, dispatch] = useDispatch(STORE, dispatchers);
	const push = useCallback((providers) => {
		if (!Array.isArray(providers) || !hasLength(providers)) {
			return;
		}

		dispatch({
			type: PUSH_LIST,
			payload: providers
		});
	}, [dispatch]);
	
	return (
		<GlobalProviderContext.Provider value={{
			store, push
		}}>
			{ children }
		</GlobalProviderContext.Provider>
	);
}

const dispatchers = {
	PUSH_LIST: (payload, oldState) => {
		const { elements } = oldState;
		const newElements = {...elements.map};

		payload.forEach(ele => newElements[ele.id] = ele);

		return {
			...oldState,
			elements: {
				...elements,
				wasInit: true,
				map: newElements
			}
		};
	}
};
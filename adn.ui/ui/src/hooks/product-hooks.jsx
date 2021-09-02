import { createContext, useContext, useCallback } from 'react';
import { useDispatch } from './hooks';

import { PUSH_LIST } from '../actions/common';

import { hasLength } from '../utils';

const GlobalProductContext = createContext();

export const useProduct = () => useContext(GlobalProductContext);

const STORE = {
	elements: {
		wasInit: false,
		map: {}	
	}
};

export default function GlobalProductContextProvider({ children }) {
	const [store, dispatch] = useDispatch(STORE, dispatchers);
	const push = useCallback((products) => {
		if (!Array.isArray(products) || !hasLength(products)) {
			return;
		}

		dispatch({
			type: PUSH_LIST,
			payload: products
		});
	}, [dispatch]);
	
	return (
		<GlobalProductContext.Provider value={{
			store, push
		}}>
			{ children }
		</GlobalProductContext.Provider>
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
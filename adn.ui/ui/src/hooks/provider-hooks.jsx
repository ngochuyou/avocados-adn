import { createContext, useContext, useCallback } from 'react';
import { useDispatch } from './hooks';

import { SET_LIST, PUSH_LIST, SET_AMOUNT, SET_LIST_ELEMENT } from '../actions/common';

import { atom, optional, hasLength, isNegative } from '../utils';

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
	const setProviders = useCallback((providers) => {
		if (!Array.isArray(providers)) {
			return;
		}

		dispatch({
			type: SET_LIST,
			payload: providers
		});
	}, [dispatch]);
	const setTotal = useCallback(total => {
		if (isNegative(total)) {
			return;
		}

		dispatch({
			type: SET_AMOUNT,
			payload: total
		});
	}, [dispatch]);
	
	return (
		<GlobalProviderContext.Provider value={{
			store, push, setTotal, setProviders
		}}>
			{ children }
		</GlobalProviderContext.Provider>
	);
}

const dispatchers = {
	SET_LIST: (payload, oldState) => {
		return {
			...oldState,
			elements: {
				...oldState.elements,
				map: atom(payload)
			}
		};
	},
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
	},
	SET_AMOUNT: (payload, oldState) => {
		const { elements } = oldState;

		return {
			...oldState,
			elements: {
				...elements,
				total: payload
			}
		}
	}
};

const GlobalProductCostContext = createContext();
export const useProductCost = () => useContext(GlobalProductCostContext);

const PRODUCT_COST_STORE = {
	elements: {
		wasInit: false,
		map: {},
		total: 0
	}
};

export function GlobalProductCostContextProvider({ children }) {
	const [store, dispatch] = useDispatch(PRODUCT_COST_STORE, productCostDispatchers);
	const push = useCallback(details => {
		if (!Array.isArray(details) || !hasLength(details)) {
			return;
		}
		
		dispatch({
			type: PUSH_LIST,
			payload: details
		});
	}, [dispatch]);
	const setTotal = useCallback(total => {
		if (isNegative(total)) {
			return;
		}

		dispatch({
			type: SET_AMOUNT,
			payload: total
		});
	}, [dispatch]);
	const update = useCallback(details => {
		if (!Array.isArray(details) || !hasLength(details)) {
			return;
		}

		dispatch({
			type: SET_LIST_ELEMENT,
			payload: details
		});
	}, [dispatch]);

	return (
		<GlobalProductCostContext.Provider value={{
			store, push, setTotal, update
		}}>
			{ children }
		</GlobalProductCostContext.Provider>
	);
}

export const resolveDetailId = (detail) => {
	const { id } = detail;

	if (id != null) {
		return `${id.productId}${id.providerId}${id.createdTimestamp}`;
	}

	return `${detail.product.id}${detail.provider.id}${detail.createdTimestamp}`;
};

export const extractProductIdFromDetailId = (detailId) => hasLength(detailId) ? detailId.substring(0, 11) : null;

const productCostDispatchers = {
	PUSH_LIST: (payload, oldState) => {
		const { elements } = oldState;
		const newElements = {...elements.map};

		payload.forEach(ele => newElements[resolveDetailId(ele)] = ele);

		return {
			...oldState,
			elements: {
				...elements,
				wasInit: true,
				map: newElements
			}
		};
	},
	SET_AMOUNT: (payload, oldState) => {
		const { elements } = oldState;

		return {
			...oldState,
			elements: {
				...elements,
				total: payload
			}
		}
	},
	SET_LIST_ELEMENT: (payload, oldState) => {
		const { elements } = oldState;
		const newElements = {...elements.map};

		payload.forEach(newElementState => {
			const id = resolveDetailId(newElementState);
			const oldElementState = optional(newElements[id]).else({});

			newElements[id] = { ...oldElementState, ...newElementState };
		});

		return {
			...oldState,
			elements: {
				...elements,
				map: newElements
			}
		};
	}
};
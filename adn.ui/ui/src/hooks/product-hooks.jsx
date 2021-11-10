import { useEffect, createContext, useContext, useCallback } from 'react';
import { useDispatch } from './hooks';

import { hasLength, isObj, locateBookmarks, mergeBookmarks, atom } from '../utils';

const GlobalProductContext = createContext();

export const useProduct = () => useContext(GlobalProductContext);

const STORE = {
	product: {
		elements: {}
	},
	category: {
		elements: []
	},
	bookmarks: {}
};

export default function GlobalProductContextProvider({ children }) {
	const [store, dispatch] = useDispatch(STORE, dispatchers);

	const setCategories = useCallback((categories) => {
		if (!Array.isArray(categories) || !hasLength(categories)) {
			return;
		}

		dispatch({
			type: SET_CATEGORY_LIST,
			payload: categories
		});
	}, [dispatch]);

	const setProducts = useCallback((products) => {
		if (!Array.isArray(products)) {
			return;
		}

		dispatch({
			type: PUSH_PRODUCTS,
			payload: products
		});
	}, [dispatch]);
	const mergeProducts = useCallback((productsMap) => {
		if (Array.isArray(productsMap)) {
			productsMap = atom(productsMap);
		}

		dispatch({
			type: MERGE_PRODUCTS,
			payload: productsMap
		});
	}, [dispatch]);
	const mergePrices = useCallback((prices) => {
		if (!isObj(prices)) {
			prices = atom(prices);
		}

		dispatch({
			type: MERGE_PRODUCTS_PRICES,
			payload: prices
		});
	}, [dispatch]);

	const initBookmarks = useCallback((bookmarks) => {
		if (!isObj(bookmarks)) {
			return;
		}

		dispatch({
			type: INIT_BOOKMARKS,
			payload: bookmarks
		});
	}, [dispatch]);
	const mergeBookmarks = useCallback((bookmark) => {
		if (isNaN(bookmark)) {
			return;
		}

		dispatch({
			type: MERGE_BOOKMARK,
			payload: +bookmark
		});
	}, [dispatch]);

	useEffect(() => initBookmarks(locateBookmarks()), [initBookmarks]);

	return (
		<GlobalProductContext.Provider value={{
			store,
			setProducts, mergeProducts,
			setCategories, initBookmarks,
			mergeBookmarks, mergePrices
		}}>
			{ children }
		</GlobalProductContext.Provider>
	);
}

const SET_CATEGORY_LIST = "SET_CATEGORY_LIST";

const PUSH_PRODUCTS = "PUSH_PRODUCTS";
const MERGE_PRODUCTS = "MERGE_PRODUCTS";
const MERGE_PRODUCTS_PRICES = "MERGE_PRODUCTS_PRICES";

const INIT_BOOKMARKS = "INIT_BOOKMARKS";
const MERGE_BOOKMARK = "MERGE_BOOKMARK";

const dispatchers = {
	PUSH_PRODUCTS: (payload, oldState) => {
		const newElements = {};

		payload.forEach(product => newElements[product.id] = product);

		return {
			...oldState,
			product: {
				...oldState.product,
				elements: newElements
			}
		};
	},
	MERGE_PRODUCTS: (payload, oldState) => {
		const { product: { elements } } = oldState;

		Object.entries(payload).forEach(pair => {
			const id = +pair[0];
			
			let currentState = elements[id];

			elements[id] = {
				...currentState,
				...pair[1]
			};
		});

		return {
			...oldState,
			product: {
				...oldState.product,
				elements
			}
		};
	},
	SET_CATEGORY_LIST: (payload, oldState) => {
		return {
			...oldState,
			category: {
				...oldState.category,
				elements: payload
			}
		};
	},
	INIT_BOOKMARKS: (payload, oldState) => {
		return {
			...oldState,
			bookmarks: payload
		};
	},
	MERGE_BOOKMARK: (payload, oldState) => {
		const bookmarks = {...oldState.bookmarks};

		if (bookmarks[payload] !== undefined) {
			delete bookmarks[payload];
		} else {
			bookmarks[payload] = null;
		}
		
		mergeBookmarks(bookmarks);

		return {
			...oldState,
			bookmarks: bookmarks
		};
	},
	MERGE_PRODUCTS_PRICES: (payload, oldState) => {
		const elements = {...oldState.product.elements};
		
		Object.entries(payload).forEach(pair => {
			const id = +pair[0];
			let currentState = elements[id];

			elements[id] = {
				...currentState,
				"price": pair[1]
			};
		});

		return {
			...oldState,
			product: {
				...oldState.product,
				elements
			}
		};
	}
};

const GlobalCartContext = createContext();

export const useCart = () => useContext(GlobalCartContext);

const CART_STORE = {
	items: {}
};

const SET_CART_ITEMS = "SET_CART_ITEMS";
const ADD_CART_ITEM = "ADD_CART_ITEM";

const CART_DISPATCHERS = {
	SET_CART_ITEMS: (payload, oldState) => {
		if (Array.isArray(payload)) {
			payload = atom(payload);
		}

		return {
			...oldState,
			items: payload
		}
	},
	ADD_CART_ITEM: (payload, oldState) => {
		const items = {...oldState.items};

		payload.forEach(item => items[item.id] = item);

		return {
			...oldState,
			items
		};
	}
};

export function GlobalCartContextProvider({ children }) {
	const [store, dispatch] = useDispatch({...CART_STORE}, CART_DISPATCHERS);

	const setItems = useCallback((items) => {
		if (!Array.isArray(items)) {
			return;
		}

		dispatch({
			type: SET_CART_ITEMS,
			payload: items
		});
	}, [dispatch]);

	const addItem = useCallback((items) => {
		if (!Array.isArray(items)) {
			return;
		}

		dispatch({
			type: ADD_CART_ITEM,
			payload: items
		});
	}, [dispatch]);
	
	return (
		<GlobalCartContext.Provider value={{
			store, setItems, addItem
		}}>
			{children}
		</GlobalCartContext.Provider>
	);
}
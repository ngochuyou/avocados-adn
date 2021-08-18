import {
	createContext, useContext, useReducer,
	useState, useCallback
} from 'react';

import {
	// eslint-disable-next-line
	SET_LIST, SET_INDIVIDUAL_VIEW_TARGET,
	// eslint-disable-next-line
	TOGGLE_INDIVIDUAL_VIEW_VISION, SET_LIST_ELEMENT,
	// eslint-disable-next-line
	SET_MODEL
} from '../actions/common';
import { getAllCategories } from '../actions/product';

import { isObj, isString, hasLength } from '../utils';

export const FETCHED_PRODUCT_COLUMNS = ["id", "name", "price", "rating", "images"]
export const OBTAINED_PRODUCT_COLUMNS = ["description", "stockDetails"];

const ShoppingContext = createContext();

export const useShopping = () => useContext(ShoppingContext);

const CATEGORY_STORE = {
	elements: [],
	view: {
		target: null
	}
};

const log = (message) => console.log(`[shopping-hooks]: ${message}`);

const cactegoryDispatchers = {
	SET_LIST: (payload, oldState) => {
		if (!Array.isArray(payload)) {
			return oldState;
		}

		return {
			...oldState,
			elements: payload
		};
	},
	SET_INDIVIDUAL_VIEW_TARGET: (payload, oldState) => {
		if (!isObj(payload)) {
			return oldState;
		}

		const { view } = oldState;

		return {
			...oldState,
			view: {
				...view,
				target: payload
			}
		};
	}
};

const PRODUCT_STORE = {
	list: {
		elements: {},
		fetchStatus: {},
		fetchStatusByCategories: {}
	},
	view: {
		target: null
	}
};

const productDispatchers = {
	SET_LIST: (payload, oldState) => {
		const { categoryId, products } = payload;

		if (!isString(categoryId) || !Array.isArray(products)) {
			return oldState;
		}

		const { list, list: {
				elements,
				fetchStatusByCategories
		} } = oldState;

		return {
			...oldState,
			list: {
				...list,
				elements: {
					...elements,
					[categoryId]: products
				},
				fetchStatusByCategories: {
					...fetchStatusByCategories,
					[categoryId]: true
				}
			}
		};
	},
	SET_LIST_ELEMENT: (payload, oldState) => {
		const { model, categoryId } = payload;

		if (!isObj(model) || !isString(categoryId)) {
			return oldState;
		}

		const { list, list: { elements, fetchStatus } } = oldState;
		const productsByCategory = elements[categoryId];

		return {
			...oldState,
			list: {
				...list,
				elements: {
					...elements,
					[categoryId]: !hasLength(productsByCategory) ? [model] : [...productsByCategory].map(product => product.id === model.id ? model : product)
				},
				fetchStatus: {
					...fetchStatus,
					[model.id]: true
				}
			}
		};
	},
	SET_MODEL: (payload, oldState) => {
		const { name, value } = payload;

		if (!isString(name)) {
			return oldState;
		}

		const { view, view: { target } } = oldState;

		if (target == null) {
			return oldState;
		}

		return {
			...oldState,
			view: {
				...view,
				target: {
					...target,
					[name]: value
				}
			}
		};
	},
	TOGGLE_INDIVIDUAL_VIEW_VISION: (payload, oldState) => {
		if (typeof payload !== 'boolean') {
			return oldState;
		}

		const { view } = oldState;

		return {
			...oldState,
			view: { ...view, visible: payload }
		};
	},
	SET_INDIVIDUAL_VIEW_TARGET: (payload, oldState) => {
		if (!isObj(payload)) {
			return oldState;
		}

		const { view } = oldState;

		return {
			...oldState,
			view: {
				...view,
				target: payload
			}
		};
	}
};

export default function ShoppingContextProvider({ children }) {
	const [categoryStore, dispatchCategoryStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = cactegoryDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...CATEGORY_STORE }
	);
	const [productStore, dispatchProductStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = productDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...PRODUCT_STORE }
	);
	const [wasInit, setWasInit] = useState(false);
	const init = useCallback(async () => {
		setWasInit(true);
		log('init');

		const [categories, fetchCategoriesErr] = await getAllCategories();

		if (fetchCategoriesErr) {
			console.error(fetchCategoriesErr);
			return;
		}

		dispatchCategoryStore({
			type: SET_LIST,
			payload: categories
		});
	}, []);

	return (
		<ShoppingContext.Provider value={{
			categoryStore, dispatchCategoryStore,
			productStore, dispatchProductStore,
			wasInit, init
		}}>
			{ children }
		</ShoppingContext.Provider>
	);
}
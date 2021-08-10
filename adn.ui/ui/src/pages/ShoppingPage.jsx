import { createContext, useContext, useReducer, useState } from 'react';

import { useEffect } from 'react';
import { useParams } from 'react-router-dom';
// aaldredkq
// atidman64
import {
	getAllCategories, getProductListByCategory,
	obtainProduct
} from '../actions/product';
import {
	SET_LIST, SET_INDIVIDUAL_VIEW_TARGET,
	TOGGLE_INDIVIDUAL_VIEW_VISION, SET_LIST_ELEMENT
} from '../actions/common';

import ProductList from '../components/product/ProductList';
import Rating from '../components/utils/Rating';
import { DomainImage } from '../components/utils/Gallery';

const Context = createContext();

const useGlobalContext = () => useContext(Context);
// 20JGU-FORMA
const CATEGORY_STORE = {
	elements: [],
	view: {
		target: null
	}
};

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
		if (payload == null || typeof payload !== 'object') {
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
		elements: {}
	},
	view: {
		visible: false,
		target: {
			categoryId: "",
			productId: ""
		}
	}
};

const productDispatchers = {
	SET_LIST: (payload, oldState) => {
		const { categoryId, products } = payload;

		if (typeof categoryId !== 'string' || !Array.isArray(products)) {
			return oldState;
		}

		const { list, list: { elements } } = oldState;

		return {
			...oldState,
			list: {
				...list,
				elements: {
					...elements,
					[categoryId]: products
				}
			}
		};
	},
	SET_LIST_ELEMENT: (payload, oldState) => {
		const { id, category } = payload;

		if (typeof id !== 'string' || category == null || typeof category.id !== 'string') {
			return oldState;
		}

		const { elements } = oldState;
		const { id: categoryId } = category;

		return {
			...oldState,
			list: {
				elements: {
					[categoryId]: [...elements[categoryId]].map(product => product.id === id ? payload : product)
				}
			}
		};
	},
	TOGGLE_INDIVIDUAL_VIEW_VISION: (payload, oldState) => {
		if (typeof payload !== 'boolean') {
			return oldState;
		}

		const { view } = payload;

		return {
			...oldState,
			view: { ...view, visible: payload }
		};
	},
	SET_INDIVIDUAL_VIEW_TARGET: (payload, oldState) => {
		const { categoryId, productId } = payload;

		if (typeof categoryId !== 'string' || typeof productId !== 'string') {
			return oldState;
		}

		const { view } = oldState;

		return {
			...oldState,
			view: {
				...view,
				target: { categoryId, productId }
			}
		};
	}
};

function ContextProvider({ children }) {
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

	return (
		<Context.Provider value={{
			categoryStore, dispatchCategoryStore,
			productStore, dispatchProductStore
		}}>
			{ children }
		</Context.Provider>
	);
}

const FETCHED_PRODUCT_COLUMNS = ["id", "name", "price", "rating", "images"]
const OBTAINED_PRODUCT_COLUMNS = ["description", "stockDetails"];

export default function ShoppingPage() {
	return (
		<ContextProvider>
			<Main />
		</ContextProvider>
	);
}

function Main() {
	const { categoryName } = useParams();
	const {
		dispatchCategoryStore, dispatchProductStore
	} = useGlobalContext();

	useEffect(() => {
		const doFetch = async () => {
			const [categories, fetchCategoriesErr] = await getAllCategories();

			if (fetchCategoriesErr) {
				console.error(fetchCategoriesErr);
				return;
			}

			dispatchCategoryStore({
				type: SET_LIST,
				payload: categories
			});

			let category = (categoryName == null || categoryName.length === 0 ? categories[0] : categories.filter(ele => ele.name === categoryName)[0]);

			category = (category == null ? categories[0] : category);

			dispatchCategoryStore({
				type: SET_INDIVIDUAL_VIEW_TARGET,
				payload: category
			});

			const [products, fetchProductsErr] = await getProductListByCategory({
				identifier: category.name,
				identifierName: "name",
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (fetchProductsErr) {
				console.error(fetchProductsErr);
				return;
			}

			dispatchProductStore({
				type: SET_LIST,
				payload: {
					categoryId: category.id,
					products
				}
			});
		};

		doFetch();
	}, [categoryName, dispatchCategoryStore, dispatchProductStore]);

	return (
		<div className="uk-background-muted">
			<Navbar />
			<Body />
		</div>
	);
}

function Body() {
	const {
		productStore: {
			list: { elements: productsListMap },
			view: { visible: individualViewVisible }
		},
		categoryStore: { view: { target: viewedCategory } },
		dispatchProductStore
	} = useGlobalContext();
	const onSelectProduct = async (product) => {
		const [model, obtainProductErr] = await obtainProduct({ id: product.id, columns: OBTAINED_PRODUCT_COLUMNS });

		if (obtainProductErr) {
			console.error(obtainProductErr);
			return;
		}

		console.log(model);
		return;
		dispatchProductStore({
			type: TOGGLE_INDIVIDUAL_VIEW_VISION,
			payload: true
		});
		dispatchProductStore({
			type: SET_INDIVIDUAL_VIEW_TARGET,
			payload: product
		});
	};
	const category = viewedCategory == null ? {} : viewedCategory;
	const productList = productsListMap[category.id];

	return (
		<main uk-height-viewport="offset-top: true">
			<div className="uk-grid-small uk-margin-top" uk-grid="">
				<div className="uk-width-4-5 uk-padding uk-position-relative">
					<ProductList
						header={
							<span className="uk-text-lead">{category.name}</span>
						}
						onItemClick={onSelectProduct}
						list={productList}
					/>
					{ individualViewVisible ? <ProductView /> : null }
				</div>
				<div className="uk-width-1-5">
				</div>
			</div>
		</main>
	);
}

function ProductView() {
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const {
		productStore: {
			list: { elements },
			view: { target: {
				categoryId: targetedCategoryId,
				productId: targetedProductId
			} }
		},
		dispatchProductStore
	} = useGlobalContext();
	const close = () => {
		setContainerClassName('fade-out');
		setTimeout(() => dispatchProductStore({
			type: TOGGLE_INDIVIDUAL_VIEW_VISION,
			payload: false
		}), 180);
	};
	const model = elements[targetedCategoryId].filter(product => product.id === targetedProductId);

	return (
		<div
			className={`uk-position-absolute uk-height-1-1 uk-width-1-1 uk-background-default ${containerClassName}`}
			style={{ top: 0, left: 0, opacity: 1 }}
		>
			<div
				className="uk-padding-small uk-grid-collapse" uk-grid=""
				uk-height-match=""
			>
				<div className="uk-width-auto uk-padding-small">
					<h4 className="uk-text-lead colors">{model.name}</h4>
				</div>
				<div className="uk-width-auto">
					<div className="uk-width-small uk-position-relative uk-height-1-1">
						<Rating value={ model.rating } max={5} />	
					</div>
				</div>
				<div className="uk-width-expand">
				</div>
				<div className="uk-width-auto uk-box-shadow-hover-small">
					<div
						className="uk-position-relative uk-width-small uk-height-1-1 pointer noselect"
						onClick={close}
					>
						<div className="uk-position-center" uk-icon="icon: arrow-left; ratio: 1.5"></div>
					</div>
				</div>
			</div>
			<div className="uk-padding uk-padding-remove-top uk-grid-small uk-margin-remove-top" uk-grid="">
				<div className="uk-width-2-3">
					<div
						className="uk-position-relative uk-visible-toggle uk-light"
						tabIndex="-1" uk-slideshow="autoplay: false; autoplay-interval: 2000; animation: scale;"
					>
						<ul className="uk-slideshow-items">
						{
							model.images.map((img, index) => (
								<li key={index}>
									<DomainImage url={`/product/image/${img}`} name={img} />
								</li>
							))
						}
						</ul>
						<div
							className="pointer uk-position-center-left uk-position-small uk-hidden-hover"
							uk-slidenav-previous="" uk-slideshow-item="previous"></div>
						<div
							className="pointer uk-position-center-right uk-position-small uk-hidden-hover"
							uk-slidenav-next="" uk-slideshow-item="next"></div>
					</div>
				</div>
				<div className="uk-width-1-3">
					
				</div>
			</div>
		</div>
	);
}

function Navbar() {
	const {
		productStore: { list: { elements: productListMap }},
		categoryStore: { elements, view: { target } },
		dispatchCategoryStore, dispatchProductStore
	} = useGlobalContext();
	const selectCategory = async (category) => {
		if (target != null && category.id === target.id) {
			return;
		}

		if (productListMap[category.id] == null) {
			const [products, fetchProductsErr] = await getProductListByCategory({
				identifier: category.id,
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (fetchProductsErr) {
				console.error(fetchProductsErr);
				return;
			}

			dispatchProductStore({
				type: SET_LIST,
				payload: {
					categoryId: category.id,
					products
				}
			});
		}

		dispatchCategoryStore({
			type: SET_INDIVIDUAL_VIEW_TARGET,
			payload: category
		});
		window.history.replaceState(null, null, `/shop/${category.name}`);
	};

	return (
		<nav className="uk-navbar-container uk-position-relative uk-navbar-transparent uk-background-default" uk-nav="">
			<div className="uk-navbar-left" uk-height-match="">
				<ul
					className="uk-navbar-nav uk-margin-large-left"
					style={{height: "50px"}}
				>
					<li className="uk-padding-small-left uk-padding-small-right uk-position-relative">
						<div className="uk-position-center pointer noselect colors">Categories</div>
						<div className="uk-padding-remove" uk-dropdown="">
							<ul className="uk-nav uk-dropdown-nav">
							{
								elements.map((category) => (
									<li
										key={category.id}
										className="uk-padding-small pointer noselect uk-box-shadow-hover-small"
										onClick={() => selectCategory(category)}
									>{category.name}</li>
								))
							}
							</ul>
						</div>
					</li>
				</ul>
			</div>
			<div className="uk-navbar-center">
				<div className="uk-navbar-item uk-logo colors">Avocados</div>
			</div>
			<div className="uk-navbar-right">
			</div>
		</nav>
	);
}
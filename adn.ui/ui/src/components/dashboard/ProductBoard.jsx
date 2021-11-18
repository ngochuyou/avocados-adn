import { useEffect, useState, useMemo } from 'react';
import { Route, useParams, useHistory, useLocation, Link } from 'react-router-dom';

import {
	getProductList, getProductPrices, getProductPrice,
	approveProductPrice, submitProductPrice, createProduct,
	obtainProduct, updateProduct
} from '../../actions/product';

import Account from '../../models/Account';
import ProductPrice from '../../models/ProductPrice';

import { routes } from '../../config/default';

import { useNavbar } from './Navbar';
import { useProduct } from '../../hooks/product-hooks';
import { useAuth } from '../../hooks/authentication-hooks';
import { useInputSet } from '../../hooks/hooks';

import { ProductTable } from '../product/ProductList';
import ProductForm from '../product/ProductForm';
import Navbar from './Navbar';
import { ConfirmModal } from '../utils/ConfirmModal';
import PagedComponent from '../utils/PagedComponent';
import OrderSelector from '../utils/Sort';

import {
	asIf, formatVND, formatDatetime, now, datetimeLocalString,
	ErrorTracker, hasLength, updateURLQuery, isString
} from '../../utils';

const FETCHED_PRODUCT_COLUMNS = ["id", "code", "name", "images"];

export default function ProductBoard() {
	const {
		dashboard: {
			product: {
				list: { mapping: productListMapping },
				prices: { mapping: productPriceMapping },
				creation: { mapping: productCreationMapping },
				edit: { mapping: productEditMapping }
			}
		}
	} = routes;

	return (
		<div>
			<Navbar />
			<Route
				path={productPriceMapping}
				render={props => <PriceManagement { ...props } />}
			/>
			<Route
				path={productCreationMapping}
				render={props => <ProductCreator { ...props } />}
			/>
			<Route
				path={productListMapping}
				render={props => <ProductList { ...props } />}
			/>
			<Route
				path={productEditMapping}
				render={props => <ProductEditor { ...props } />}
			/>
		</div>
	);
}

function ProductEditor() {
	const { productId } = useParams();
	const [model, setModel] = useState(null);
	const [errors, setErrors] = useState({});
	const [noti, setNoti] = useState();

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await obtainProduct({
				id: productId,
				columns: ["id", "code", "name", "material", "locked", "images", "category", "description"]
			});

			if (err) {
				return console.error(err);
			}

			setModel(res);
		};

		doFetch();
	}, [productId]);

	const onSuccess = async (model) => {
		const [, err] = await updateProduct({
			...model,
			images: model.images.map(image => isString(image) ? image : image.file )
		});

		if (err) {
			setErrors(err);
			return console.error(err);
		}

		setErrors({});
		setNoti("Successfully updated Product");
		
		return setTimeout(() => setNoti(null), 1000);
	};

	return (
		<main className="uk-padding-small">
			{
				noti != null ? (
					<div
						className="uk-alert-primary uk-position-fixed uk-position-top-center uk-width-2xlarge" uk-alert=""
						style={{zIndex: "999"}}
					>
						<p>{noti}</p>
					</div>
				) : null
			}
			<h3 className="uk-heading-line">
				<span>Edit Product {model && model.name}</span>
			</h3>
			<ProductForm
				initModel={model}
				onSuccess={onSuccess}
				errors={errors}
			/>
		</main>
	);
}

function ProductList() {
	const {
		dashboard: {
			product: {
				 list: { url: productListUrl },
				 edit: { url: productEditUrl }
			}
		}
	} = routes;
	const { push } = useHistory();
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);
	const { setOnEntered } = useNavbar();
	const {
		store: { product: { elements: productsMap } },
		setProducts
	} = useProduct();

	useEffect(() => {
		const doFetch = async () => {
			let [res, err] = await getProductList({
				internal: true,
				columns: FETCHED_PRODUCT_COLUMNS,
				name: urlParams.get("name"),
				page: urlParams.get("page"),
				size: urlParams.get("size"),
				sort: urlParams.get("sort")
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);
		};

		doFetch();
	}, [urlParams, setProducts]);

	useEffect(() => {
		setOnEntered((key) => push(`${productListUrl}?${updateURLQuery(urlParams, "name", name => key.toLowerCase())}`));
	}, [urlParams, push, setOnEntered, productListUrl]);

	const products = Object.values(productsMap);

	return (
		<main className="uk-padding-small">
			<h3 className="uk-heading-line">
				<span>Product List</span>
			</h3>
			<div className="uk-margin uk-text-right">
				<Link to={productListUrl}>Reset filter</Link>
			</div>
			<PagedComponent
				pageCount={products.length}
				onNextPageRequest={() => push(`${productListUrl}?${updateURLQuery(urlParams, "page", page => !hasLength(page) ? 1 : +page + 1)}`)}
				onPreviousPageRequest={() => push(`${productListUrl}?${updateURLQuery(urlParams, "page", page => +page - 1)}`)}
				currentPage={urlParams.get("page")}
			>
				<ProductTable
					list={products}
					onRowSelect={(p) => push(`${productEditUrl}/${p.id}`)}
					columns={[ "images", "code", "name" ]}
				/>
			</PagedComponent>
		</main>
	);
}

function ProductCreator() {
	const [errors, setErrors] = useState({});
	const {
		dashboard: { product: { list: { url: productListUrl } } }
	} = routes;
	const { push } = useHistory();

	const onSuccess = async (model) => {
		const [, err] = await createProduct({
			...model,
			images: model.images.map(image => image.file)
		});

		if (err) {
			setErrors(err);
			console.error(err);
			return;
		}

		setErrors({});
		push(productListUrl);
	};

	return (
		<div className="uk-padding-small">
			<h4>Create a new Product</h4>
			<ProductForm
				onSuccess={onSuccess}
				errors={errors}
			/>
		</div>
	);
}

function PriceManagement() {
	const {
		dashboard: {
			product: { prices: { url: productPriceUrl } }
		}
	} = routes;
	const {
		store: {
			product: { elements: productMap }
		},
		mergePrices, setProducts
	} = useProduct();
	const products = Object.values(productMap);
	const { productId } = useParams();
	const { push } = useHistory();

	useEffect(() => {
		const doFetch = async () => {
			let [res, err] = await getProductList({
				internal: true,
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);

			[res, err] = await getProductPrices(res.map(p => p.id));

			if (err) {
				console.error(err);
				return;
			}

			mergePrices(res);
		};

		doFetch();
	}, [setProducts, mergePrices]);

	return (
		<div className="uk-padding-small">
			<h4>Product Prices</h4>
			{
				asIf(productId == null)
				.then(() => <ProductTable
					list={products}
					onRowSelect={(p) => push(`${productPriceUrl}/${p.id}`)}
					columns={[ "images", "code", "name" ]}
					extras={[
						{
							header: <th key="price">Current Price</th>,
							row: (p) => (
								<td key="price">
								{
									asIf(p.price != null)
									.then(() => <span className="uk-text-lead colors">{formatVND(p.price)}</span>)
									.else(() => <span className="uk-text-muted uk-text-bold">Currently empty</span>)
								}
								</td>
							)
						}
					]}
				/>)
				.else(() => <IndividualPrice />)
			}
		</div>
	);
}

function IndividualPrice() {
	const {
		dashboard: {
			product: { prices: { url: productPriceUrl } }
		}
	} = routes;
	const { setBackBtnState } = useNavbar();
	const { productId } = useParams();
	const { push } = useHistory();
	const { principal } = useAuth();
	const [priceList, setPriceList] = useState([]);
	const [confirmModal, setConfirmModal] = useState(null);
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);

	useEffect(() => {
		setBackBtnState({
			visible: true,
			callback: () => push(productPriceUrl)
		});

		return () => setBackBtnState();
	}, [setBackBtnState, push, productPriceUrl]);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await getProductPrice({
				productId,
				columns: [
					"appliedTimestamp", "droppedTimestamp",
					"price", "approvedTimestamp"
				],
				page: urlParams.get("page"),
				size: urlParams.get("size"),
				sort: urlParams.get("sort"),
				from: urlParams.get("from"),
				to: urlParams.get("to")
			});

			if (err) {
				console.error(err);
				return;
			}

			setPriceList(res.map(ele => ({
				...ele,
				formattedAppliedTimestamp: formatDatetime(ele.appliedTimestamp),
				formattedDroppedTimestamp: formatDatetime(ele.droppedTimestamp),
				formattedApprovedTimestamp: formatDatetime(ele.approvedTimestamp),
				price: formatVND(ele.price)
			})));
		};

		doFetch();
	}, [productId, urlParams]);

	const onApprove = (index) => {
		const ele = priceList[index];

		setConfirmModal(
			<ConfirmModal
				background="uk-background-muted"
				message={`Are you sure you want to apply ${ele.price} on ${ele.formattedAppliedTimestamp} and drop it on ${ele.formattedDroppedTimestamp}?`}
				onNo={() => setConfirmModal(null)}
				onYes={() => approve(ele, index)}
			/>
		);
	};

	const approve = async (ele, index) => {
		const { appliedTimestamp, droppedTimestamp } = ele;
		const [res, err] = await approveProductPrice({
			productId, appliedTimestamp, droppedTimestamp
		});

		if (err) {
			console.error(err);
			return;
		}

		const { approvedTimestamp } = res;

		setPriceList(priceList.map((p, i) => asIf(i === index).then(() => ({
			...p,
			approvedTimestamp,
			formattedApprovedTimestamp: formatDatetime(approvedTimestamp),
			approvedBy: principal
		})).else(() => p)));
		setConfirmModal(null);
	};

	return (
		<div>
			{ confirmModal }
			<h5 className="uk-heading uk-heading-line colors uk-text-right">
				<span>
					{`Price schedule for Product ID: `}
					<span className="uk-text-bold colors">{productId}</span>
				</span>
			</h5>
			<PagedComponent
				pageCount={priceList.length}
				onNextPageRequest={() => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "page", p => hasLength(p) ? +p + 1 : 1)}`)}
				onPreviousPageRequest={() => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "page", p => +p - 1)}`)}
				currentPage={urlParams.get("page")}
			>
				<div className="uk-margin">
					<label className="uk-label backgroundf">Sort</label>
					<div>
						<OrderSelector
							className="uk-width-auto uk-margin-right"
							onAscRequested={() => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "sort", sort => `appliedTimestamp,asc`)}`)}
							onDesRequested={() => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "sort", sort => `appliedTimestamp,desc`)}`)}
							labels={["Applied timestamp ascending", "Applied timestamp descending"]}
						/>
					</div>
				</div>
				<div className="uk-margin uk-grid uk-child-width-1-2" uk-grid="">
					<div>
						<label className="uk-label backgroundf">From</label>
						<input
							className="uk-input uk-text-large"
							type="date"
							onChange={(event) => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "from", () => event.target.value)}`)}
						/>
					</div>
					<div>
						<label className="uk-label backgroundf">To</label>
						<input
							className="uk-input uk-text-large"
							type="date"
							onChange={(event) => push(`${productPriceUrl}/${productId}?${updateURLQuery(urlParams, "to", () => event.target.value)}`)}
						/>
					</div>
				</div>
				<table className="uk-table uk-table-divider">
					<thead>
						<tr>
							<th>Applied timestamp</th>
							<th>Dropped timestamp</th>
							<th>Price</th>
							<th>Approved timestamp</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
					{
						priceList.map((ele, index) => (
							<tr key={index}>
								<td>{ele.formattedAppliedTimestamp}</td>
								<td>{ele.formattedDroppedTimestamp}</td>
								<td>
									<span className="colors">{ele.price}</span>
								</td>
								<td>
								{
									asIf(ele.approvedTimestamp != null)
									.then(() => <span className="uk-text-primary">
										{ele.formattedApprovedTimestamp}
									</span>)
									.else(() => <span className="uk-text-muted">Not yet approved</span>)
								}
								</td>
								<td>
								{
									asIf(ele.approvedTimestamp == null && principal.role === Account.Role.HEAD)
									.then(() => (
										<button
											className="uk-button uk-button-danger"
											onClick={() => onApprove(index)}
										>Approve</button>
									)).else()
								}
								</td>
							</tr>
						))
					}
					</tbody>
				</table>
			</PagedComponent>
			<IndividualPriceCreator
				onSuccess={(newPrice) => setPriceList([
					...priceList,
					{
						...newPrice,
						formattedAppliedTimestamp: formatDatetime(newPrice.appliedTimestamp),
						formattedDroppedTimestamp: formatDatetime(newPrice.droppedTimestamp),
						price: formatVND(newPrice.price)
					}
				])}
			/>
		</div>
	);
}

function IndividualPriceCreator({
	onSuccess = () => null
}) {
	const { productId } = useParams();
	const [appliedProps, , appliedError, setAppliedError] = useInputSet(datetimeLocalString(now()));
	const [droppedProps, , droppedError, setDroppedError] = useInputSet(datetimeLocalString(now()));
	const [priceProps, , priceError, setPriceError] = useInputSet(100000);

	const onSubmit = async (e) => {
		e.preventDefault();
		e.stopPropagation();

		let tracker = new ErrorTracker();
		const appliedTimestamp = appliedProps.value;
		const droppedTimestamp = droppedProps.value;
		const price = priceProps.value;

		setAppliedError(tracker.add(ProductPrice.validators.appliedTimestamp(appliedTimestamp)));
		setDroppedError(tracker.add(ProductPrice.validators.droppedTimestamp(droppedTimestamp)));
		setPriceError(tracker.add(ProductPrice.validators.price(price)));

		if (tracker.foundError()) {
			return;
		}

		const [res, err] = await submitProductPrice({
			productId, appliedTimestamp, droppedTimestamp, price
		});

		if (err) {
			console.error(err);
			setAppliedError(err.appliedTimestamp);
			setDroppedError(err.droppedTimestamp);
			setPriceError(err.price);
			return;
		}

		onSuccess(res);
	}

	return (
		<div>
			<h5 className="uk-heading uk-heading-line colors uk-text-right">
				<span>
					{`Submit a new Price for Product ID: `}
					<span className="uk-text-bold colors">{productId}</span>
				</span>
			</h5>
			<form onSubmit={onSubmit}>
				<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
					<div>
						<div className="uk-margin">
							<label
								htmlFor="applied"
								className="uk-label backgroundf"
							>Applied timestamp</label>
							<input
								{...appliedProps}
								className="uk-input uk-text-large"
								type="datetime-local"
								id="applied"
								step="1"
							/>
							<p className="uk-text-danger">{appliedError}</p>
						</div>
						<div className="uk-margin">
							<label
								htmlFor="dropped"
								className="uk-label backgroundf"
							>Dropped timestamp</label>
							<input
								{...droppedProps}
								className="uk-input uk-text-large"
								type="datetime-local"
								id="dropped"
								step="1"
							/>
							<p className="uk-text-danger">{droppedError}</p>
						</div>
					</div>
					<div>
						<div className="uk-margin">
							<label
								htmlFor="price"
								className="uk-label backgroundf"
							>Price</label>
							<input
								{...priceProps}
								className="uk-input"
								type="number"
								id="price"
								min="0"
								step="0.0001"
							/>
							<p className="uk-text-danger">{priceError}</p>
						</div>
					</div>
				</div>
				<div className="uk-margin uk-text-right">
					<button className="uk-button backgroundf">Submit</button>
				</div>
			</form>
		</div>
	);
}

// import {
// 	useContext, createContext, useReducer, useEffect,
// 	Fragment, useState
// } from 'react'

// import Navbar from './Navbar.jsx';
// import Paging from '../utils/Paging.jsx';
// import { PureGallery } from '../utils/Gallery.jsx';
// import ProductList from '../product/ProductList.jsx';

// import { Category, Product } from '../../models/Factor';

// import {
// 	SET_VIEW, SET_LIST, FULFILL_PAGE, SET_PAGE,
// 	TOGGLE_FORM_VISION, MODIFY_MODEL, SET_ERROR, PUSH_LIST,
// 	CREATE, EDIT, SET_ACTION, SET_INDIVIDUAL_VIEW_TARGET,
// 	TOGGLE_INDIVIDUAL_VIEW_VISION, TOGGLE_LIST_VISION,
// 	SET_MODEL, MODIFY_PAGINATION_STATE, /*SET_LIST_ELEMENT*/
// } from '../../actions/common';
// import {
// 	fetchCategoryCount, fetchCategoryList,
// 	createCategory, updateCategory,
// 	updateCategoryActivationState, getAllCategories,
// 	createProduct, getProductListByCategory,
// 	updateProduct
// } from '../../actions/product';

// import { spread, isEmpty } from '../../utils';

// const Context = createContext();

// const useGlobalContext = () => useContext(Context);

// const productDispatchers = {
// 	MODIFY_MODEL: (payload, oldState) => {
// 		const { name, value } = payload;

// 		if (typeof name !== 'string') {
// 			return oldState;
// 		}

// 		const { form, form: { model } } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				model: {
// 					...model,
// 					[name]: value
// 				}
// 			}
// 		};
// 	},
// 	SET_LIST_VIEW_ELEMENTS: (payload, oldState) => {
// 		if (!Array.isArray(payload)) {
// 			return oldState;
// 		}

// 		const { list, list: { view } } = oldState;

// 		return {
// 			...oldState,
// 			list: {
// 				...list,
// 				view: {
// 					...view,
// 					elements: payload
// 				}
// 			}
// 		};
// 	},
// 	SET_LIST: (payload, oldState) => {
// 		const { categoryId, elements } = payload;

// 		if (typeof categoryId !== 'string' || !Array.isArray(elements)) {
// 			return oldState;
// 		}

// 		const { list, list: { elements: currentElements } } = oldState;
// 		const listBycategory = currentElements[categoryId];

// 		return  {
// 			...oldState,
// 			list: {
// 				...list,
// 				elements: {
// 					...currentElements,
// 					[categoryId]: listBycategory == null ? elements : [...listBycategory, ...elements]
// 				}
// 			}
// 		};
// 	},
// 	SET_CATEGORY_SELECT_LIST_VIEW: (payload, oldState) => {
// 		if (payload != null && !Array.isArray(payload)) {
// 			return oldState;
// 		}

// 		const { form, form: { categorySelect } } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 					categorySelect: {
// 					...categorySelect,
// 					listView: payload
// 				}	
// 			}
// 		};
// 	},
// 	SET_ERROR: (payload, oldState) => {
// 		if (payload == null || typeof payload !== 'object') {
// 			return oldState;
// 		}

// 		const { form } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				errors: payload
// 			}
// 		};
// 	},
// 	TOGGLE_CATEGORY_SELECT_VISION: (payload, oldState) => {
// 		const { form, form: { categorySelect } } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				categorySelect: {
// 					...categorySelect,
// 					visible: !categorySelect.visible
// 				}
// 			}
// 		};
// 	},
// 	MODIFY_PAGINATION_STATE: (payload, oldState) => {
// 		if (payload == null || typeof payload !== 'object') {
// 			return oldState;
// 		}

// 		const { list, list: { pagination } } = oldState;

// 		return {
// 			...oldState,
// 			list: {
// 				...list,
// 				pagination: { ...pagination, ...payload }
// 			}
// 		};
// 	},
// 	SET_ACTION: (payload, oldState) => {
// 		if (typeof payload !== 'string') {
// 			return oldState;
// 		}

// 		const { form } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				action: payload
// 			}
// 		};
// 	},
// 	TOGGLE_FORM_VISION: (payload, oldState) => {
// 		const { form } = oldState;

// 		return {
// 			...oldState,
// 			form: { ...form, visible: !form.visible }
// 		};
// 	},
// 	TOGGLE_LIST_VISION: (payload, oldState) => {
// 		const { list } = oldState;

// 		return {
// 			...oldState,
// 			list: { ...list, visible: !list.visible }
// 		};
// 	},
// 	SET_MODEL: (payload, oldState) => {
// 		if (payload == null || typeof payload !== 'object') {
// 			return oldState;
// 		}

// 		const { form } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				model: payload
// 			}
// 		};
// 	},
// 	SET_CALLBACK_ON_CLOSE: (payload, oldState) => {
// 		if (typeof payload !== 'function') {
// 			return oldState;
// 		}

// 		const { form } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				callbackOnClose: payload
// 			}
// 		};
// 	},
// 	SET_CATEGORY_SELECT_LIST: (payload, oldState) => {
// 		if (!Array.isArray(payload)) {
// 			return oldState;
// 		}

// 		const { form, form: { categorySelect } } = oldState;

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				categorySelect: {
// 					...categorySelect,
// 					list: payload
// 				}
// 			}
// 		};
// 	},
// 	PUSH_INTO_CATEGORY_SELECT: (payload, oldState) => {
// 		if (!Array.isArray(payload)) {
// 			return oldState;
// 		}

// 		const { form, form: { categorySelect, categorySelect: { list } } } = oldState;
// 		// filter out existing elements
// 		const currentIds = Object.fromEntries(list.map(ele => [ele.id, -1024]));

// 		return {
// 			...oldState,
// 			form: {
// 				...form,
// 				categorySelect: {
// 					...categorySelect,
// 					list: [...list, ...payload.filter(ele => currentIds[ele.id] !== -1024)]
// 				}
// 			}
// 		};
// 	},
// 	SET_SUBVIEW_ELEMENTS: (payload, oldState) => {
// 		if (payload != null && !Array.isArray(payload)) {
// 			return oldState;
// 		}

// 		const { list, list: { view } } = oldState;

// 		return {
// 			...oldState,
// 			list: {
// 				...list,
// 				view: {
// 					...view,
// 					subview: payload
// 				}
// 			}
// 		};
// 	},
// 	SET_LIST_ELEMENT: (payload, oldState) => {
// 		const { categoryId, productId, model } = payload;

// 		if (typeof categoryId !== 'string' || typeof productId !== 'string' || model == null || typeof model !== 'object') {
// 			return oldState;
// 		}

// 		const { list, list: { elements } } = oldState;

// 		return {
// 			...oldState,
// 			list: {
// 				...list,
// 				elements: {
// 					...elements,
// 					[categoryId]: [...elements[categoryId]].map(product => product.id === productId ? model : product)
// 				}
// 			}
// 		};
// 	},
// 	PUSH_LIST: (payload, oldState) => {
// 		// push a product into the main list
// 		const { categoryId, model } = payload;

// 		if (typeof categoryId !== 'string' || model == null || typeof model !== 'object') {
// 			return oldState;
// 		}

// 		const { list, list: { elements } } = oldState;

// 		return {
// 			...oldState,
// 			list: {
// 				...list,
// 				elements: {
// 					...elements,
// 					[categoryId]: elements[categoryId] != null ? [...elements[categoryId], model] : [model]
// 				}
// 			}
// 		}
// 	}
// };

// function ContextProvider({ children }) {
// 	const [layoutStore, dispatchLayoutStore] = useReducer(
// 		(oldState, { type = null, payload = null} = {}) => {
// 			switch(type) {
// 				case SET_VIEW: {
// 					if (payload == null || typeof payload !== 'object') {
// 						return oldState;
// 					}

// 					return { ...oldState, view: payload };
// 				}
// 				default: return oldState;
// 			}
// 		}, { ...LAYOUT_STORE }
// 	);
// 	const [categoryStore, dispatchCategoryStore] = useReducer(
// 		(oldState, { type = null, payload = null } = {}) => {
// 			switch(type) {
// 				case MODIFY_MODEL: {
// 					const { name, value } = payload;

// 					if (typeof name !== 'string') {
// 						return oldState;
// 					}

// 					const { form, form: { model } } = oldState;

// 					return {
// 						...oldState,
// 						form: {
// 							...form,
// 							model: {
// 								...model,
// 								[name]: value
// 							}
// 						}
// 					};
// 				}
// 				case SET_PAGE: {
// 					if (typeof payload !== 'number') {
// 						return oldState;
// 					}

// 					const { list, list: { pagination } } = oldState;

// 					return {
// 						...oldState,
// 						list: {
// 							...list,
// 							pagination: {
// 								...pagination,
// 								page: payload
// 							}
// 						}
// 					};
// 				}
// 				case TOGGLE_INDIVIDUAL_VIEW_VISION: {
// 					const { individualView } = oldState;

// 					return {
// 						...oldState,
// 						individualView: {
// 							...individualView,
// 							visible: !individualView.visible
// 						}
// 					};
// 				}
// 				case SET_INDIVIDUAL_VIEW_TARGET: {
// 					if (typeof payload !== 'string') {
// 						return oldState;
// 					}

// 					const { individualView } = oldState;

// 					return {
// 						...oldState,
// 						individualView: {
// 							...individualView,
// 							target: payload
// 						}
// 					};
// 				}
// 				case SET_MODEL: {
// 					if (payload == null || typeof payload !== 'object') {
// 						return oldState;
// 					}

// 					const { form } = oldState;

// 					return {
// 						...oldState,
// 						form: {
// 							...form,
// 							model: payload
// 						}
// 					};
// 				}
// 				case SET_ERROR: {
// 					if (typeof payload !== 'string') {
// 						return oldState;
// 					}

// 					const { form } = oldState;

// 					return {
// 						...oldState,
// 						form: {
// 							...form,
// 							error: payload
// 						}
// 					};
// 				}
// 				case FULFILL_PAGE: {
// 					const { page , list: categoryList } = payload;

// 					if (typeof page !== 'number' || !Array.isArray(categoryList)) {
// 						return oldState;
// 					}

// 					const { list: {
// 						elements, pagination,
// 						pagination: { pageMap, fetchStatus  }
// 					} } = oldState;

// 					return {
// 						...oldState,
// 						list: {
// 							elements: [...elements, ...categoryList],
// 							pagination: {
// 								...pagination,
// 								page,
// 								pageMap: {
// 									...pageMap,
// 									[page]: categoryList
// 								},
// 								fetchStatus: fetchStatus.map((ele, index) => index !== page ? ele : true)
// 							}
// 						}
// 					};
// 				}
// 				case SET_LIST: {
// 					if (payload == null || typeof payload !== 'object') {
// 						return oldState;
// 					}

// 					return { ...oldState, list: payload, isInitialized: true };
// 				}
// 				case PUSH_LIST: {
// 					const { page, list } = payload;

// 					if (typeof page !== 'number' || !Array.isArray(list)) {
// 						return oldState;
// 					}

// 					const { list: { amount, elements, pagination } } = oldState;
// 					let newPageMap = { ...pagination.pageMap };
// 					let newAmount = amount;

// 					list.forEach(newElement => {
// 						let i = 0;

// 						for (let ele of newPageMap[page]) {
// 							if (ele.id === newElement.id) {
// 								newPageMap[page][i] = newElement;
// 								return;
// 							}

// 							i++;
// 						}

// 						newAmount++;
// 						newPageMap[page].push(newElement);
// 					});

// 					return {
// 						...oldState,
// 						list: {
// 							...list,
// 							amount: newAmount,
// 							elements: [...elements, ...list],
// 							pagination: {
// 								...pagination,
// 								page,
// 								pageMap: newPageMap
// 							}
// 						}
// 					};
// 				}
// 				case SET_ACTION: {
// 					if (typeof payload !== 'string') {
// 						return oldState;
// 					}

// 					const { form } = oldState;

// 					return {
// 						...oldState,
// 						form: {
// 							...form,
// 							action: payload
// 						}
// 					};
// 				}
// 				case TOGGLE_FORM_VISION: {
// 					const { form } = oldState;

// 					return {
// 						...oldState,
// 						form: {
// 							...form,
// 							visible: !form.visible
// 						}
// 					};
// 				}
// 				default: return oldState;
// 			}
// 		}, { ...CATEGORY_STORE }
// 	);
// 	const [productStore, dispatchProductStore] = useReducer(
// 		(oldState, { type = null, payload = null } = {}) => {
// 			const dispatcher = productDispatchers[type];

// 			return dispatcher == null ? oldState : dispatcher(payload, oldState);
// 		}, { ...PRODUCT_STORE }
// 	);

// 	return <Context.Provider value={{
// 		layoutStore, dispatchLayoutStore,
// 		categoryStore, dispatchCategoryStore,
// 		productStore, dispatchProductStore
// 	}}>
// 		{ children }
// 	</Context.Provider>;
// }

// export default function ProductBoard() {
// 	return (
// 		<ContextProvider>
// 			<div className="uk-position-relative">
// 				<Navbar
// 					centerElement={ <Nav /> }
// 				/>
// 				<Body />
// 			</div>
// 		</ContextProvider>
// 	);
// }

// function Body() {
// 	const { layoutStore } = useGlobalContext();

// 	return layoutStore.view;
// }

// const LAYOUT_STORE = {
// 	view: <ProductView />
// };

// function Nav() {
// 	const { dispatchLayoutStore } = useGlobalContext();
// 	const navigateCategoryView = () => {
// 		dispatchLayoutStore({
// 			type: SET_VIEW,
// 			payload: <CategoryView />
// 		});
// 	};
// 	const navigateProductView = () => {
// 		dispatchLayoutStore({
// 			type: SET_VIEW,
// 			payload: <ProductView />
// 		});
// 	};

// 	return (
// 		<div className="uk-grid-collapse uk-child-width-1-2@m uk-height-1-1" uk-grid="">
// 			<div
// 				onClick={navigateCategoryView}
// 				className="uk-height-1-1 uk-position-relative pointer noselect"
// 			>
// 				<div className="colors uk-position-center">Category</div>
// 			</div>
// 			<div
// 				onClick={navigateProductView}
// 				className="uk-height-1-1 uk-position-relative pointer noselect"
// 			>
// 				<div className="colors uk-position-center">Product</div>
// 			</div>
// 		</div>
// 	);
// }

// const CATEGORY_STORE = {
// 	isInitialized: false,
// 	list: {
// 		elements: [],
// 		amount: 0,
// 		pagination: {
// 			page: 0,
// 			total: 0,
// 			size: 10,
// 			fetchStatus: [],
// 			pageMap: {
// 				0: []
// 			}
// 		}
// 	},
// 	form: {
// 		model: {
// 			name: "",
// 			description: ""
// 		},
// 		error: "",
// 		visible: false,
// 		action: CREATE
// 	},
// 	individualView: {
// 		visible: false,
// 		target: null
// 	}
// };
// const FETCHED_CATEGORY_COLUMNS = ["id", "name", "description", "active"];
// const PUSH_INTO_CATEGORY_SELECT = "PUSH_INTO_CATEGORY_SELECT";

// function CategoryView() {
// 	const {
// 		categoryStore: { form: { visible: formVisible } },
// 		dispatchCategoryStore
// 	} = useGlobalContext();
// 	const promptCreateForm = () => {
// 		dispatchCategoryStore({ type: TOGGLE_FORM_VISION });
// 		dispatchCategoryStore({ type: SET_ACTION, payload: CREATE });
// 	};

// 	return (
// 		<section className="uk-padding">
// 			<CategoryList />
// 			{
// 				formVisible ? (
// 					<CategoryForm />
// 				) : null
// 			}
// 			<div
// 				onClick={promptCreateForm}
// 				className="uk-position-fixed uk-border-circle uk-box-shadow-large backgroundf pointer"
// 				uk-tooltip="Create a new Category"
// 				style={{
// 					width: '50px',
// 					height: '50px',
// 					bottom: '25px',
// 					right: '50px'
// 				}}
// 			>
// 				<span
// 					uk-icon="icon: plus; ratio: 1.5"
// 					className="uk-position-center"
// 				></span>
// 			</div>
// 		</section>	
// 	);
// }

// function CategoryList() {
// 	const {
// 		categoryStore: {
// 			list: {
// 				pagination: { page, pageMap, total, fetchStatus, size }
// 			},
// 			isInitialized,
// 			individualView
// 		},
// 		dispatchCategoryStore,
// 		dispatchProductStore
// 	} = useGlobalContext();

// 	useEffect(() => {
// 		const doFetch = async () => {
// 			if (isInitialized) {
// 				return;
// 			}

// 			const [count, countErr] = await fetchCategoryCount();

// 			if (countErr) {
// 				console.error(countErr);
// 				return;
// 			}

// 			const [listRes, listFetchErr] = await fetchCategoryList({ columns: FETCHED_CATEGORY_COLUMNS });

// 			if (listFetchErr) {
// 				console.error(listFetchErr);
// 				return;
// 			}

// 			const size = 10, page = 0;
// 			const totalPages = Math.ceil(count / size);
// 			const pageMap = { 0: listRes };
// 			let fetchStatus = spread(totalPages, false);

// 			fetchStatus[0] = true;

// 			dispatchCategoryStore({
// 				type: SET_LIST,
// 				payload: {
// 					elements: listRes,
// 					amount: count,
// 					pagination: {
// 						page, size, total: totalPages,
// 						fetchStatus, pageMap
// 					}
// 				}
// 			});
// 			dispatchProductStore({
// 				type: PUSH_INTO_CATEGORY_SELECT,
// 				payload: listRes
// 			});
// 		};
// 		doFetch();
// 	}, [isInitialized, dispatchCategoryStore, dispatchProductStore]);

// 	const requestPage = async (pageNumber) => {
// 		const actualPage = pageNumber - 1;

// 		if (fetchStatus[actualPage] === true) {
// 			dispatchCategoryStore({
// 				type: SET_PAGE,
// 				payload: actualPage
// 			});

// 			return;
// 		}

// 		const [ list, err ] = await fetchCategoryList({
// 			page: actualPage, size, columns: FETCHED_CATEGORY_COLUMNS
// 		});

// 		if (err) {
// 			console.error(err);
// 			return;
// 		}

// 		dispatchCategoryStore({
// 			type: FULFILL_PAGE,
// 			payload: { page: actualPage, list }
// 		});
// 		dispatchProductStore({
// 			type: PUSH_INTO_CATEGORY_SELECT,
// 			payload: list
// 		});
// 	};
// 	const promptIndividualView = (categoryId) => {
// 		dispatchCategoryStore({ type: TOGGLE_INDIVIDUAL_VIEW_VISION });
// 		dispatchCategoryStore({
// 			type: SET_INDIVIDUAL_VIEW_TARGET,
// 			payload: categoryId
// 		});
// 	};
// 	const promptEditForm = (targetModel) => {
// 		dispatchCategoryStore({
// 			type: SET_ACTION,
// 			payload: EDIT
// 		});
// 		dispatchCategoryStore({
// 			type: SET_MODEL,
// 			payload: targetModel
// 		});
// 		dispatchCategoryStore({ type: TOGGLE_FORM_VISION });
// 	};

// 	return (
// 		<Fragment>
// 			<table className="uk-table uk-table-justify uk-table-divider">
// 				<thead>
// 					<tr>
// 						<th className="uk-width-small">ID</th>
// 						<th>Name</th>
// 						<th>Description</th>
// 						<th>Status</th>
// 						<th></th>
// 					</tr>
// 				</thead>
// 				<tbody>
// 				{
// 					pageMap[page].map(category => (
// 						<tr key={category.id}>
// 							<td
// 								className="pointer"
// 								onClick={() => promptIndividualView(category.id)}
// 							>
// 								<span className="uk-text-bold colors">
// 									{ category.id }
// 								</span>
// 							</td>
// 							<td>
// 								{ category.name }
// 							</td>
// 							<td>
// 								{ category.description }
// 							</td>
// 							<td>
// 							{
// 								category.active ?
// 								<span className="uk-text-success">ACTIVE</span> :
// 								<span className="uk-text-muted">INACTIVE</span> 
// 							}
// 							</td>
// 							<td
// 								className="pointer"
// 								onClick={() => promptIndividualView(category.id)}
// 							>
// 								<span
// 									uk-icon="icon: info"
// 									className="uk-icon-button"
// 								></span>
// 							</td>
// 						</tr>
// 					))
// 				}
// 				</tbody>
// 			</table>
// 			<Paging
// 				amount={total}
// 				amountPerChunk={5}
// 				selected={page + 1}
// 				onPageSelect={requestPage}
// 			/>
// 			{
// 				individualView.visible ? (() => {
// 					let target;

// 					for (let ele of pageMap[page]) {
// 						if (ele.id === individualView.target) {
// 							target = ele;
// 							break;
// 						}
// 					}

// 					if (target == null) return null;

// 					return (
// 						<div
// 							uk-modal=""
// 							className="uk-open uk-display-block"
// 						>
// 							<div className="uk-modal-dialog">
// 								<div className="uk-modal-header">
// 									<h2 className="uk-modal-title">{target.name}</h2>
// 								</div>
// 								<div className="uk-modal-body" uk-overflow-auto="">
// 									<div
// 										className="uk-grid-medium uk-child-width-1-2 uk-grid-match"
// 										uk-grid=""
// 									>
// 										<div>
// 											<div className="uk-card uk-card-default uk-card-body">
// 												<h3 className="uk-card-title uk-text-center">Description</h3>
// 												{
// 													target.description == null || target.description.length === 0 ?
// 													<p className="uk-text-muted uk-text-small">There's no description yet</p> :
// 													<p>{target.description}</p>
// 												}
// 											</div>
// 										</div>
// 										<div>
// 											<div className="uk-card uk-card-default uk-card-body uk-text-center">
// 												<h3 className="uk-card-title">Status</h3>
// 												{
// 													target.active ?
// 													<span className="uk-text-success">ACTIVE</span> :
// 													<span className="uk-text-muted">INACTIVE</span> 
// 												}
// 											</div>
// 										</div>
// 									</div>
// 								</div>
// 								<div className="uk-modal-footer uk-text-right">
// 									<button
// 										className="uk-button backgroundf uk-margin-right"
// 										onClick={() => promptEditForm(target)}
// 									>Edit</button>
// 									<button
// 										className="uk-button uk-button-default"
// 										onClick={() => dispatchCategoryStore({ type: TOGGLE_INDIVIDUAL_VIEW_VISION })}
// 									>Close</button>
// 								</div>
// 							</div>
// 						</div>
// 					);
// 				})() : null
// 			}
// 		</Fragment>
// 	);
// }

// function CategoryForm() {
// 	const {
// 		categoryStore: {
// 			list: { elements: list, pagination: { page } },
// 			form: { model, error, action }
// 		},
// 		dispatchCategoryStore
// 	} = useGlobalContext();
// 	const validate = (props = []) => {
// 		let [, err] = [null, null];

// 		for (let key of props) {
// 			[, err] = Category.validator[key](model[key]);

// 			if (err) {
// 				return err;
// 			}
// 		}

// 		const { name } = model;

// 		if (name.length === 0) {
// 			return null;
// 		}

// 		for (let category of list) {
// 			if (name.trim() === category.name) {
// 				return "Name was taken";
// 			}
// 		}

// 		return null;
// 	};
// 	const onSubmit = async (event) => {
// 		event.preventDefault();

// 		const error = validate(["name", "description"]);

// 		if (error) {
// 			dispatchCategoryStore({
// 				type: SET_ERROR,
// 				payload: error
// 			});
// 			return;
// 		}

// 		dispatchCategoryStore({
// 			type: SET_ERROR,
// 			payload: ""
// 		});

// 		switch(action) {
// 			case CREATE: {
// 				const [creationRes, creationErr] = await createCategory({ model });

// 				if (creationErr) {
// 					dispatchCategoryStore({
// 						type: SET_ERROR,
// 						payload: creationErr["name"]
// 					});
// 					return;
// 				}

// 				dispatchCategoryStore({ type: TOGGLE_FORM_VISION });
// 				dispatchCategoryStore({
// 					type: PUSH_LIST,
// 					payload: {
// 						page,
// 						list: [creationRes]
// 					}
// 				});
// 				return;
// 			}
// 			case EDIT: {
// 				const [editRes, editErr] = await updateCategory({ model });

// 				if (editErr) {
// 					dispatchCategoryStore({
// 						type: SET_ERROR,
// 						payload: editErr["name"]
// 					});
// 					return;
// 				}

// 				dispatchCategoryStore({ type: TOGGLE_FORM_VISION });
// 				dispatchCategoryStore({
// 					type: PUSH_LIST,
// 					payload: {
// 						page,
// 						list: [editRes]
// 					}
// 				});
// 				return;
// 			}
// 			default: return;
// 		}
// 	};
// 	const onInputChange = (event) => {
// 		dispatchCategoryStore({
// 			type: MODIFY_MODEL,
// 			payload: { name: event.target.name, value: event.target.value }
// 		});
// 	};
// 	const updateActivationState = async (state) => {
// 		const [, err] = await updateCategoryActivationState(model.id, state);

// 		if (err) {
// 			console.error(err);
// 			return;
// 		}

// 		const newCategoryState = {
// 			...model,
// 			active: state
// 		};

// 		dispatchCategoryStore({
// 			type: PUSH_LIST,
// 			payload: {
// 				page,
// 				list: [newCategoryState]
// 			}
// 		});
// 		dispatchCategoryStore({
// 			type: TOGGLE_FORM_VISION
// 		});
// 	}

// 	return (
// 		<div id="model-form" className="uk-flex-top uk-open uk-display-block" uk-modal="">
// 			<div className="uk-modal-dialog uk-modal-body uk-margin-auto-vertical">
// 				<h2>
// 				{
// 					action === CREATE ? "Create a new Category" : `Edit Category`
// 				}
// 				</h2>
// 				<form onSubmit={onSubmit} style={{zIndex: "20"}}>
// 					<div className="uk-margin">
// 						<label className="uk-label backgrounds">Name</label>
// 						<input
// 							className="uk-input"
// 							type="text"
// 							value={model.name}
// 							placeholder="Category name"
// 							name="name"
// 							onChange={onInputChange}
// 						/>
// 					</div>
// 					<div className="uk-margin">
// 						<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
// 							<div>
// 								<label className="uk-label backgrounds">Description</label>
// 							</div>
// 							<div className="uk-text-right">
// 								<span>{model.description && `${255 - model.description.length} left`}</span>
// 							</div>
// 						</div>
// 						<textarea
// 							className="uk-input uk-height-small"
// 							type="text"
// 							value={model.description}
// 							placeholder="Description"
// 							name="description"
// 							onChange={onInputChange}
// 							rows={3}
// 						></textarea>
// 						<span className="uk-text-danger">{error}</span>
// 					</div>
// 					{
// 						action === EDIT ? (
// 							<Fragment>
// 								<hr className="uk-divider-icon" />
// 								<div className="uk-text-center">
// 								{
// 									model.active ? <button
// 										className="uk-button uk-button-danger"
// 										onClick={() => updateActivationState(false)}
// 										type="button"
// 									>
// 										Deactivate
// 									</button> :
// 									<button
// 										className="uk-button backgroundf"
// 										onClick={() => updateActivationState(true)}
// 										type="button"
// 									>
// 										Activate
// 									</button>
// 								}
// 								</div>
// 							</Fragment>
// 						) : null
// 					}
// 					<div className="uk-margin">
// 						<button className="uk-button uk-margin-right backgroundf" type="submit">Submit</button>
// 						<button
// 							className="uk-button uk-button-default"
// 							type="button"
// 							onClick={() => dispatchCategoryStore({ type: TOGGLE_FORM_VISION })}
// 						>Cancel</button>
// 					</div>
// 				</form>
// 			</div>
// 		</div>
// 	);
// }

// const PRODUCT_STORE = {
// 	list: {
// 		isInitialized: false,
// 		elements: {},
// 		visible: false,
// 		view: {
// 			elements: [],
// 			subview: null
// 		},
// 		filter: {
// 			categoryId: "SHIRT"
// 		},
// 		pagination: {
// 			size: 18,
// 			fetchStatus: {},
// 			pageStatus: {}
// 		}
// 	},
// 	form: {
// 		action: CREATE,
// 		categorySelect: {
// 			visible: false,
// 			listView: null,
// 			list: [],
// 			amount: 0
// 		},
// 		model: {
// 			name: "",
// 			category: null,
// 			active: true,
// 			description: "",
// 			images: []
// 		},
// 		errors: {
// 			name: "",
// 			category: "",
// 			active: "",
// 			description: "",
// 			images: ""
// 		},
// 		callbackOnClose: () => null
// 	}
// };
// const TOGGLE_CATEGORY_SELECT_VISION = "TOGGLE_CATEGORY_SELECT_VISION";
// const SET_CATEGORY_SELECT_LIST = "SET_CATEGORY_SELECT_LIST";
// const SET_CATEGORY_SELECT_LIST_VIEW = "SET_CATEGORY_SELECT_LIST_VIEW";
// const SET_LIST_VIEW_ELEMENTS = "SET_LIST_VIEW_ELEMENTS";
// const SET_CALLBACK_ON_CLOSE = "SET_CALLBACK_ON_CLOSE";
// const FETCHED_PRODUCT_COLUMNS = ["id", "name", "rating", "images", "description", "active"];
// const SET_SUBVIEW_ELEMENTS = "SET_SUBVIEW_ELEMENTS";

// function ProductView() {
// 	const {
// 		productStore: {
// 			list: {
// 				visible: listVisible
// 			},
// 			form: { visible: formVisible }
// 		},
// 		dispatchProductStore
// 	} = useGlobalContext();
// 	const promptCreateForm = () => {
// 		dispatchProductStore({
// 			type: SET_MODEL,
// 			payload: {...PRODUCT_STORE.form.model}
// 		});
// 		dispatchProductStore({ type: TOGGLE_FORM_VISION });
// 		dispatchProductStore({
// 			type: SET_ACTION,
// 			payload: CREATE
// 		});
// 	};
// 	const promptListView = () => {
// 		dispatchProductStore({
// 			type: SET_LIST_VIEW_ELEMENTS,
// 			payload: []
// 		});
// 		dispatchProductStore({
// 			type: SET_SUBVIEW_ELEMENTS,
// 			payload: null
// 		});
// 		dispatchProductStore({ type: TOGGLE_LIST_VISION });
// 	};

// 	return (
// 		<section className="uk-padding-small">
// 			<div className="uk-margin">
// 				<h3 className="uk-heading-line"><span>Actions</span></h3>
// 				<nav
// 					className="uk-grid-small uk-padding-small uk-grid-1-6"
// 					uk-grid=""
// 				>
// 					<div>
// 						<div
// 							style={{ width: "80px", height: "80px" }}
// 							onClick={promptCreateForm}
// 							className="uk-position-relative pointer noselect uk-inline-clip uk-transition-toggle uk-background-muted uk-border-circle"
// 						>
// 							<span
// 								className="uk-position-center"
// 								uk-icon="icon: plus; ratio: 1.2"
// 							></span>
// 							<div
// 								className="uk-transition-slide-bottom uk-position-bottom uk-overlay uk-width-1-1 uk-height-1-1 uk-text-center uk-padding-remove uk-text-small uk-background-muted uk-position-relative"
// 							><span className="uk-position-center">New Product</span></div>
// 						</div>
// 					</div>
// 					<div>
// 						<div
// 							style={{ width: "80px", height: "80px" }}
// 							onClick={promptListView}
// 							className="uk-position-relative pointer noselect uk-inline-clip uk-transition-toggle uk-background-muted uk-border-circle"
// 						>
// 							<span
// 								className="uk-position-center"
// 								uk-icon="icon: list; ratio: 1.2"
// 							></span>
// 							<div
// 								className="uk-transition-slide-bottom uk-position-bottom uk-overlay uk-width-1-1 uk-height-1-1 uk-text-center uk-padding-remove uk-text-small uk-background-muted uk-position-relative"
// 							><span className="uk-position-center">View products</span></div>
// 						</div>
// 					</div>
// 				</nav>
// 			</div>
// 			<div>
// 			{
// 				formVisible ? (
// 					<ProductForm />
// 				) : null
// 			}
// 			{
// 				listVisible ? (
// 					<ProductListView />
// 				) : null
// 			}
// 			</div>
// 		</section>
// 	);
// }

// function ProductListView() {
// 	const [containerClassName, setContainerClassName] = useState('fade-in');
// 	const {
// 		productStore: {
// 			form: {
// 				categorySelect: { list: categoryList }
// 			},
// 			list: {
// 				elements: listElements,
// 				view: {
// 					elements: viewElements,
// 					subview
// 				},
// 				pagination
// 			}
// 		},
// 		categoryStore: {
// 			list: { amount: totalCategoryAmount }
// 		},
// 		dispatchProductStore
// 	} = useGlobalContext();
// 	const [filterState, setFilterState] = useState({
// 		category: null
// 	});
// 	const close = () => {
// 		dispatchProductStore({ type: SET_CALLBACK_ON_CLOSE, payload: () => null });
// 		setContainerClassName('fade-out uk-modal');
// 		setTimeout(() => dispatchProductStore({ type: TOGGLE_LIST_VISION }), 180);
// 	};
// 	const onFilterCategoryChange = async (categoryId, index) => {
// 		if (filterState.category && filterState.category.id === categoryId) {
// 			return;
// 		}
		
// 		const { fetchStatus } = pagination;

// 		if (fetchStatus[index] === true) {
// 			dispatchProductStore({
// 				type: SET_LIST_VIEW_ELEMENTS,
// 				payload: listElements[categoryId]
// 			});
// 			setFilterState({
// 				...filterState,
// 				category: categoryList[index]
// 			});
// 			return;
// 		}

// 		const [fetchRes, fetchErr] = await getProductListByCategory({
// 			columns: FETCHED_PRODUCT_COLUMNS,
// 			identifier: categoryId,
// 			internal: true
// 		});

// 		if (fetchErr) {
// 			console.error(fetchErr);
// 			return;
// 		}

// 		dispatchProductStore({
// 			type: SET_LIST_VIEW_ELEMENTS,
// 			payload: fetchRes
// 		});
// 		dispatchProductStore({
// 			type: SET_LIST,
// 			payload: {
// 				categoryId,
// 				elements: fetchRes
// 			}
// 		});
// 		dispatchProductStore({
// 			type: MODIFY_PAGINATION_STATE,
// 			payload: {
// 				...pagination,
// 				fetchStatus: fetchStatus.map((ele, i) => index !== i ? ele : true)
// 			}
// 		});
// 		setFilterState({
// 			...filterState,
// 			category: categoryList[index]
// 		});
// 	};
// 	const onItemClick = (product) => {
// 		setContainerClassName('fade-out uk-modal');
// 		setTimeout(() => setContainerClassName('uk-invisible uk-modal'), 180);
// 		dispatchProductStore({ type: TOGGLE_FORM_VISION });
// 		dispatchProductStore({
// 			type: SET_MODEL,
// 			payload: {
// 				...product,
// 				category: filterState.category
// 			}
// 		});
// 		dispatchProductStore({ type: SET_ACTION, payload: EDIT });
// 		dispatchProductStore({
// 			type: SET_CALLBACK_ON_CLOSE,
// 			payload: () => setContainerClassName('fade-in uk-modal')
// 		});
// 	};
// 	useEffect(() => {
// 		const doFetch = async () => {
// 			const currentCategoryAmount = categoryList.length;

// 			if (currentCategoryAmount === 0 || currentCategoryAmount.length < totalCategoryAmount) {
// 				const [fetchRes, fetchErr] = await getAllCategories();

// 				if (fetchErr) {
// 					console.error(fetchErr);
// 					return;
// 				}

// 				const fetchedAmount = fetchRes.length;

// 				dispatchProductStore({
// 					type: PUSH_INTO_CATEGORY_SELECT,
// 					payload: fetchRes
// 				});
// 				dispatchProductStore({
// 					type: MODIFY_PAGINATION_STATE,
// 					payload: {
// 						fetchStatus: spread(fetchedAmount, false),
// 						pageStatus: spread(fetchedAmount, 0)
// 					}
// 				});
// 				return;
// 			}

// 			dispatchProductStore({
// 				type: MODIFY_PAGINATION_STATE,
// 				payload: {
// 					fetchStatus: spread(totalCategoryAmount, false),
// 					pageStatus: spread(totalCategoryAmount, 0)
// 				}
// 			});
// 			return;
// 		};

// 		doFetch();
// 	}, [categoryList.length, totalCategoryAmount, dispatchProductStore]);
// 	const onSearchInputEntered = (value) => {
// 		if (value.length === 0) {
// 			return;
// 		}

// 		dispatchProductStore({
// 			type: SET_SUBVIEW_ELEMENTS,
// 			payload: viewElements.filter(product => product.name.toLowerCase().includes(value.toLowerCase()))
// 		});
// 	};
// 	const onSearchInputEmptied = () => {
// 		dispatchProductStore({
// 			type: SET_SUBVIEW_ELEMENTS,
// 			payload: null
// 		});
// 	};
// 	const { category: selectedCategory } = filterState;

// 	return (
// 		<div
// 			className={`uk-modal-full uk-open uk-display-block ${containerClassName}`}
// 			uk-modal="" style={{ maxHeight: "100vh", overflow: "hidden" }}
// 		>
// 			<div className="uk-modal-dialog">
// 				<div style={{ maxHeight: "100vh", overflow: "auto" }}>
// 					<div
// 						uk-grid=""
// 						className="uk-grid-collapse uk-grid-match uk-height-1-1"
// 					>
// 						<div className="uk-width-1-5 uk-padding-small uk-background-muted" uk-height-viewport="">
// 							<div className="uk-margin">
// 								<div className="noselect">
// 									<ul className="uk-nav-default uk-nav-parent-icon" uk-nav="">
// 										<li className="uk-parent">
// 											<a href="#category-list">Category</a>
// 											<ul className="uk-nav-sub">
// 											{
// 												categoryList.map((ele, index) => (
// 													<li
// 														key={ele.id}
// 														onClick={() => onFilterCategoryChange(ele.id, index)}
// 													>
// 														<a href="#category-list">{ele.name}</a>
// 													</li>
// 												))
// 											}
// 											</ul>
// 										</li>
// 									</ul>
// 								</div>
// 							</div>
// 						</div>
// 						<div className="uk-width-4-5 uk-position-relative">
// 							<div>
// 								<Navbar
// 									searchInputEntered={onSearchInputEntered}
// 									outerRightElement={
// 										<div
// 											className="uk-position-relative uk-height-1-1" style={{width: "100px"}}
// 											onClick={close}
// 										>
// 											<button
// 												className="uk-button uk-position-center uk-position-z-index"
// 												type="button" uk-icon="icon: close; ratio: 1.25;"												
// 											></button>
// 										</div>
// 									}
// 									searchInputEmptied={onSearchInputEmptied}
// 								/>	
// 							</div>
// 							<div className="uk-padding">
// 								<ProductList
// 									onItemClick={onItemClick}
// 									list={subview == null ? viewElements : subview}
// 									messageIfEmpty={selectedCategory == null ? "Select a Category" : `${selectedCategory.name} is empty`}
// 									header={ selectedCategory && selectedCategory.name }
// 								/>
// 							</div>
// 						</div>
// 					</div>
// 				</div>
// 			</div>
// 		</div>
// 	);
// }

// const MAX_IMAGES_AMOUNT = 20;

// function ProductForm() {
// 	const [containerClassName, setContainerClassName] = useState('fade-in');
// 	const {
// 		productStore: {
// 			/*list: {
// 				view: {
// 					elements: viewElements,
// 					subview: subviewElements
// 				}
// 			},*/
// 			form: {
// 				model, errors, categorySelect, action,
// 				callbackOnClose
// 			}
// 		},
// 		categoryStore: {
// 			list: {
// 				amount: categoryAmount
// 			}
// 		},
// 		dispatchProductStore
// 	} = useGlobalContext();
// 	const onChange = (event) => {
// 		dispatchProductStore({
// 			type: MODIFY_MODEL,
// 			payload: { name: event.target.name, value: event.target.value }
// 		});
// 	};
// 	const promptSelectCategory = async () => {
// 		const currentAmount = categorySelect.list.length;

// 		if (currentAmount === 0 || currentAmount < categoryAmount) {
// 			const [res, err] = await getAllCategories();

// 			if (err) {
// 				console.error(err);
// 				return;
// 			}

// 			dispatchProductStore({
// 				type: SET_CATEGORY_SELECT_LIST,
// 				payload: res
// 			});
// 		}

// 		dispatchProductStore({
// 			type: SET_CATEGORY_SELECT_LIST_VIEW,
// 			payload: null
// 		});
// 		dispatchProductStore({ type: TOGGLE_CATEGORY_SELECT_VISION });
// 	};
// 	const categorySelectKeyDown = (event) => {
// 		if (event.keyCode === 27) {
// 			dispatchProductStore({ type: TOGGLE_CATEGORY_SELECT_VISION });
// 		}
// 	};
// 	const categorySearchChange = (event) => {
		// const { target: { value } } = event;
		// const { list } = categorySelect;

		// if (value.length === 0) {
		// 	dispatchProductStore({
		// 		type: SET_CATEGORY_SELECT_LIST_VIEW,
		// 		payload: null
		// 	});
		// 	return;
		// }

		// dispatchProductStore({
		// 	type: SET_CATEGORY_SELECT_LIST_VIEW,
		// 	payload: list.filter(ele => ele.name.toLowerCase().includes(value.toLowerCase()))
		// });
		// return;
// 	};
// 	const selectCategory = (category) => {
// 		dispatchProductStore({
// 			type: MODIFY_MODEL,
// 			payload: {
// 				name: 'category',
// 				value: category
// 			}
// 		});
// 		dispatchProductStore({
// 			type: TOGGLE_CATEGORY_SELECT_VISION
// 		});
// 	};
// 	const onGalleryAddImage = (images) => {
// 		dispatchProductStore({
// 			type: MODIFY_MODEL,
// 			payload: {
// 				name: "images",
// 				value: [...model.images, ...images.map(file => ({
// 					file,
// 					src: URL.createObjectURL(file)
// 				}))]
// 			}
// 		});
// 	};
// 	const onGalleryImageRemove = (index) => {
// 		dispatchProductStore({
// 			type: MODIFY_MODEL,
// 			payload: {
// 				name: "images",
// 				value: model.images.filter((image, i) => index !== i)
// 			}
// 		});
// 	};
// 	const validateModel = (props) => Object.fromEntries(props.map(prop => [prop, Product.validator[prop](model[prop])[1]]).filter(err => err[1] != null));
// 	const onSubmit = async (event) => {
// 		event.preventDefault();
// 		event.stopPropagation();

// 		const errorSet = validateModel(["name", "description", "category", "active", "images"]);
		
// 		if (!isEmpty(errorSet)) {
// 			dispatchProductStore({
// 				type: SET_ERROR,
// 				payload: errorSet
// 			});
// 			return;
// 		}

// 		dispatchProductStore({
// 			type: SET_ERROR,
// 			payload: PRODUCT_STORE.form.errors
// 		});

// 		switch(action) {
// 			case CREATE: {
// 				const [newModel, err] = await createProduct({
// 					...model,
// 					images: model.images.map(image => image.file)
// 				});

// 				if (err) {
// 					if (typeof err === 'object') {
// 						dispatchProductStore({
// 							type: SET_ERROR,
// 							payload: err
// 						});
// 						return;
// 					}

// 					console.error(err);
// 					return;
// 				}

// 				dispatchProductStore({
// 					type: PUSH_LIST,
// 					payload: {
// 						categoryId: newModel.category.id,
// 						model: newModel
// 					}
// 				});
// 				close();
// 				return;
// 			}
// 			case EDIT: {
// 				const [, err] = await updateProduct({
// 					...model,
// 					images: model.images.map(image => typeof image === 'string' ? image : image.file)
// 				});

// 				if (err) {
// 					if (typeof err === 'object') {
// 						dispatchProductStore({
// 							type: SET_ERROR,
// 							payload: err
// 						});
// 						return;
// 					}

// 					console.error(err);
// 					return;
// 				}
				
// 				window.location.reload(false);
// 				// this is too buggy, let's reload
// 				// the page for now
// 				// dispatchProductStore({
// 				// 	type: SET_LIST_VIEW_ELEMENTS,
// 				// 	payload: viewElements.map((element, index) => element.id !== updatedModel.id ? element : updatedModel)
// 				// });

// 				// if (subviewElements != null) {
// 				// 	dispatchProductStore({
// 				// 		type: SET_LIST_VIEW_ELEMENTS,
// 				// 		payload: subviewElements.map((element, index) => element.id !== updatedModel.id ? element : updatedModel)
// 				// 	});
// 				// }

// 				// dispatchProductStore({
// 				// 	type: SET_LIST_ELEMENT,
// 				// 	payload: {
// 				// 		categoryId: updatedModel.category.id,
// 				// 		productId: updatedModel.id,
// 				// 		model: updatedModel
// 				// 	}
// 				// });
// 				// close();
// 				return;
// 			}
// 			default: return;
// 		}
// 	};
// 	const close = () => {
// 		setContainerClassName('fade-out uk-modal');
// 		setTimeout(() => dispatchProductStore({
// 			type: TOGGLE_FORM_VISION
// 		}), 180);
// 		callbackOnClose();
// 	};

// 	return (
// 		<div
// 			className={`uk-modal-full uk-open uk-display-block ${containerClassName}`}
// 			uk-modal="" style={{ maxHeight: "100vh", overflow: "hidden", zIndex: 2 }}
// 		>
// 			<div className="uk-modal-dialog">
// 				<button
// 					className="uk-button uk-position-top-right uk-position-z-index"
// 					type="button" uk-icon="icon: close; ratio: 1.25;"
// 					onClick={close}
// 				></button>
// 				<div
// 					className="uk-padding-small" uk-height-viewport=""
// 					style={{ maxHeight: "100vh", overflow: "auto" }}
// 				>
// 					<div
// 						className="uk-grid-small uk-child-width-1-2"
// 						uk-grid=""
// 					>
// 						<div
// 							className="uk-padding-small"
// 						>
// 							<form onSubmit={onSubmit}>
// 								<div className="uk-margin">
// 									<label className="uk-label backgrounds">Name</label>
// 									<input
// 										name="name"
// 										type="text"
// 										placeholder="Name"
// 										className="uk-input"
// 										onChange={onChange}
// 										value={model.name}
// 									/>
// 									<p className="uk-text-danger">{errors.name}</p>
// 								</div>
// 								<div className="uk-margin">
// 									<label className="uk-label backgrounds">Category</label>
// 									<div
// 										className="uk-grid-small uk-child-width-1-2 uk-padding-small uk-height-small"
// 										uk-grid=""
// 									>
// 										<div>
// 										{
// 											model.category != null ?
// 											<div className="uk-card uk-card-default uk-card-body uk-text-center">
// 												<h4 className="uk-card-title">{model.category.name}</h4>
// 											</div> :
// 											<div className="uk-card uk-card-default uk-card-body uk-text-center">
// 												<h4 className="uk-text-muted">Not selected yet</h4>
// 											</div>
// 										}
// 										</div>
// 										<div
// 											className="uk-position-relative pointer"
// 											onClick={promptSelectCategory}
// 										>
// 											<div className="colors uk-position-center noselect">
// 												<span uk-icon="menu"></span>Select one
// 											</div>
// 										</div>
// 									</div>
// 									<p className="uk-text-danger">{errors.category}</p>
// 									{
// 										categorySelect.visible ? 
// 										<div
// 											className="uk-flex-top uk-open uk-display-block uk-position-fixed uk-width-1-1"
// 											uk-modal=""
// 											onKeyDown={categorySelectKeyDown}
// 											tabIndex={10}
// 										>
// 											<div
// 												className="uk-modal-dialog uk-modal-body uk-margin-auto-vertical"
// 												uk-overflow-auto=""
// 											>
// 												<div className="uk-grid-small uk-margin-bottom" uk-grid="">
// 													<div className="uk-width-auto">
// 														<h2 className="colors">Select Category</h2>
// 													</div>
// 													<div className="uk-width-expand">
// 														<input
// 															type="text"
// 															placeholder="Search..."
// 															className="uk-input"
// 															onChange={categorySearchChange}
// 														/>
// 													</div>
// 													<div
// 														className="uk-width-auto pointer"
// 														onClick={() => dispatchProductStore({ type: TOGGLE_CATEGORY_SELECT_VISION })}
// 													>
// 														<div style={{
// 															width: "50px",
// 															height: "100%"
// 														}} className="uk-position-relative">
// 															<span
// 																uk-icon="icon: close; ratio: 1.5"
// 																className="uk-position-center"
// 															></span>
// 														</div>
// 													</div>
// 												</div>
// 												<div className="uk-padding-small">
// 													<div className="uk-child-width-1-4 uk-grid-match" uk-grid="">
// 													{
// 														categorySelect.listView == null ? 
// 														categorySelect.list.map(ele => (
// 															<div key={ele.id}>
// 																<div
// 																	className="uk-card uk-card-default uk-card-body uk-text-center pointer"
// 																	onClick={() => selectCategory(ele)}
// 																>
// 																	<h4 className="uk-card-title color-inherit">{ele.name}</h4>
// 																</div>
// 															</div>
// 														)) : 
// 														categorySelect.listView.map(ele => (
// 															<div key={ele.id}>
// 																<div
// 																	className="uk-card uk-card-default uk-card-body uk-text-center pointer"
// 																	onClick={() => selectCategory(ele)}
// 																>
// 																	<h4 className="uk-card-title">{ele.name}</h4>
// 																</div>
// 															</div>
// 														))
// 													}
// 													</div>
// 												</div>
// 											</div>
// 										</div> : null
// 									}
// 								</div>
// 								{/*<div className="uk-margin">
// 									<label className="uk-label backgrounds">Status</label>
// 									<div className="uk-grid-small uk-child-width-1-2 uk-padding-small" uk-grid="">
// 										<div
// 											onClick={() => dispatchProductStore({
// 												type: MODIFY_MODEL,
// 												payload: { name: 'active', value: true }
// 											})}
// 											className="pointer"
// 										>
// 											<div className={`uk-card uk-card-default uk-card-body uk-text-center noselect ${model.active ? "backgroundf" : ""}`}>
// 												<h4
// 													className="uk-card-title color-inherit"
// 												>Active</h4>
// 											</div>
// 										</div>
// 										<div
// 											onClick={() => dispatchProductStore({
// 												type: MODIFY_MODEL,
// 												payload: { name: 'active', value: false }
// 											})}
// 											className="pointer"
// 										>
// 											<div className={`uk-card uk-card-default uk-card-body uk-text-center noselect ${!model.active ? "backgroundf" : ""}`}>
// 												<h4
// 													className="uk-card-title color-inherit"
// 												>Inactive</h4>
// 											</div>
// 										</div>
// 									</div>
// 									<p className="uk-text-danger">{errors.active}</p>
// 								</div>*/}
// 								<div className="uk-margin">
// 									<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
// 										<div>
// 											<label className="uk-label backgrounds">Description</label>
// 										</div>
// 										<div className="uk-text-right">
// 											<span className="uk-text-muted">
// 											{
// 												model.description == null ? "3000 character(s) left" :
// 												`${3000 - model.description.length} character(s) left`	
// 											}
// 											</span>
// 										</div>
// 									</div>
// 									<textarea
// 										className="uk-textarea"
// 										name="description"
// 										value={model.description}
// 										placeholder="Description"
// 										rows="5"
// 										onChange={onChange}
// 										maxLength="3000"
// 									>
// 									</textarea>
// 									<p className="uk-text-danger">{errors.description}</p>
// 								</div>
// 								<div className="uk-margin">
// 									<button type="submit" className="uk-button backgroundf uk-margin-right">Submit</button>
// 									<button
// 										type="button" className="uk-button uk-button-default"
// 										onClick={close}
// 									>Cancel</button>
// 								</div>
// 							</form>
// 						</div>
// 						<div>
// 							<PureGallery
// 								max={MAX_IMAGES_AMOUNT}
// 								url="/product/image"
// 								elements={model.images}
// 								message={errors.images}
// 								onAdd={onGalleryAddImage}
// 								onDomainImageRemove={onGalleryImageRemove}
// 								onClientImageRemove={onGalleryImageRemove}
// 							/>
// 						</div>
// 					</div>
// 				</div>
// 			</div>
// 		</div>
// 	);
// }



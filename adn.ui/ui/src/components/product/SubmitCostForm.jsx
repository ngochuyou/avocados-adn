import { createContext, useContext, useState, useEffect } from 'react';
import { useProduct } from '../../hooks/product-hooks';
import { useProvider } from '../../hooks/provider-hooks';

import { getProductList, searchProduct } from '../../actions/product';
import { fetchProviderList, searchProvider } from '../../actions/provider';

import { SET_ERROR, MODIFY_MODEL } from '../../actions/common';

import { CenterModal } from '../utils/Modal';
import { SearchInput } from '../utils/Input';
import { DomainImage } from '../utils/Gallery';
import { AlertBox } from '../utils/Alert';

import { hasLength, asIf, isObj } from '../../utils';

import { useDispatch } from '../../hooks/hooks';

import { server } from '../../config/default';

import ProductCost from '../../models/ProductCost';

const FormContext = createContext();
const useFormContext = () => useContext(FormContext);

const FORM_STORE = {
	model: {
		product: null,
		provider: null,
		price: 100000
	},
	errors: {}
};

const formDispatchers = {
	MODIFY_MODEL: (payload, oldState) => {
		const { name, value } = payload;

		if (!hasLength(name)) {
			return oldState;
		}

		const { model } = oldState;

		return {
			...oldState,
			model: {
				...model,
				[name]: value
			}
		};
	},
	SET_ERROR: (payload, oldState) => {
		if (!isObj(payload)) {
			return oldState;
		}

		return {
			...oldState,
			errors: payload
		};
	}
};

function FormContextProvider({ children }) {
	const [store, dispatch] = useDispatch(FORM_STORE, formDispatchers);

	return (
		<FormContext.Provider value={{
			store, dispatch
		}}>
			{ children }
		</FormContext.Provider>
	);
}

const FETCHED_PRODUCT_COLUMNS = [
	"id", "code", "name", "images"
];
const FETCHED_PROVIDER_COLUMNS = [
	"id", "name", "email",
	"representatorName"
];

export default function SubmitCostForm() {
	return (
		<div>
			<FormContextProvider>
				<h2 className="uk-heading-line uk-text-left">
					<span>Submit Product Cost</span>
				</h2>
				<div className="uk-grid-small" uk-grid="">
					<div className="uk-width-2-3">
						<LeftPanel />
					</div>
					<div className="uk-width-1-3">
						<RightPanel />
					</div>
				</div>
			</FormContextProvider>
		</div>
	);
}

function LeftPanel() {
	const {
		store: { model, errors },
		dispatch: dispatchFormStore
	} = useFormContext();
	const [alert, setAlert] = useState(null);
	const {
		store: { elements: products } 
	} = useProduct();
	const {
		store: { elements: { map: providers } } 
	} = useProvider();
	const [productPickerVison, setProductPickerVision] = useState(false);
	const [providerPickerVison, setProviderPickerVision] = useState(false);
	const onSelectProduct = (productId) => {
		dispatchFormStore({
			type: MODIFY_MODEL,
			payload: {
				name: "product",
				value: products[productId]
			}
		});
		setProductPickerVision(false);
	};
	const onSelectProvider = (providerId) => {
		dispatchFormStore({
			type: MODIFY_MODEL,
			payload: {
				name: "provider",
				value: providers[providerId]
			}
		});
		setProviderPickerVision(false);
	};
	const onInputChange = (event) => {
		const { name, value } = event.target;

		dispatchFormStore({
			type: MODIFY_MODEL,
			payload: { name, value }
		});
	};
	const validate = (columns) => {
		let result = {}, success = true;

		for (let column of columns) {
			const [ok, err] = ProductCost.validators[column](model[column]);

			success = success && ok;

			if (!ok) {
				result[column] = err;
			}
		}

		dispatchFormStore({
			type: SET_ERROR,
			payload: result
		});

		return success;
	};
	const onSubmit = async () => {
		const success = validate(["product", "provider", "price"]);

		if (!success) {
			return;
		}

		// const [, err] = await createProductDetail(model);

		// if (err) {
		// 	console.log(err);

		// 	const { result } = err;

		// 	setAlert(result);
		// 	return;
		// }

		// setAlert("Submitted for approval");
	};

	return (
		<div>
		{
			asIf(alert != null)
			.then(() => (
				<AlertBox
					onClose={() => setAlert(null)}
					message={alert}
					className="uk-alert-primary"
				/>
			))
			.else(() => null)
		}
			<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
				<div className="pointer noselect" >
					<div className="uk-text-right">
						<label className="uk-label backgroundf">Product</label>
					</div>
				{
					asIf(model.product == null).then(() => (
						<div
							className="uk-card uk-card-default uk-card-body"
							onClick={() => setProductPickerVision(true)}
						>
							<div className="uk-text-center uk-text-muted">Pick a Product</div>
						</div>	
					)).else(() => {
						const { product } = model;

						return (
							<div
								className="uk-card uk-card-default uk-card-body uk-padding-remove uk-overflow-hidden"
								onClick={() => setProductPickerVision(true)}
								style={{ borderRadius: "10px" }}
							>
								<div className="uk-grid-collapse" uk-grid="">
									<div className="uk-width-1-3">
										<div>
											<div style={{
												width: "100%",
												height: "150px"
											}}>
											{
												asIf(hasLength(product.images))
													.then(() => (
														<DomainImage
															url={`${server.images.product}/${product.images[0]}`}
															fit="cover"
														/>
													))
													.else(() => (
														<span className="uk-position-center">No images</span>	
													))
											}
											</div>
										</div>
									</div>
									<div className="uk-width-2-3">
										<div className="uk-position-relative uk-height-1-1">
											<div className="uk-position-center">
												<div className="uk-text-lead">{product.id}</div>
												<div>{product.name}</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						);
					})
				}
					<div className="uk-text-danger">{errors.product}</div>
				</div>
				<div
					className="pointer noselect"
					onClick={() => setProviderPickerVision(true)}
				>
					<div className="uk-text-right">
						<label className="uk-label backgroundf">Provider</label>
					</div>
					<div className="uk-card uk-card-default uk-card-body">
					{
						asIf(model.provider == null)
						.then(() => <div className="uk-text-center uk-text-muted">Pick a Provider</div>)
						.else(() => {
							const { provider } = model;

							return (
								<div className="uk-height-1-1 uk-position-relative">
									<div className="uk-text-lead">{provider.name}</div>
									<div>{provider.email}</div>
									<div>{provider.representatorName}</div>
								</div>
							);
						})
					}
					</div>
					<div className="uk-text-danger">{errors.provider}</div>
				</div>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Cost</label>
				<input
					className="uk-input"
					value={model.price}
					name="price"
					placeholder="Cost"
					onChange={onInputChange}
					type="number"
					min="0"
				/>
				<div className="uk-text-danger">{errors.price}</div>
			</div>
			<div className="uk-margin">
				<button
					className="uk-button backgroundf"
					onClick={onSubmit}
				>Submit Cost</button>
			</div>
			<ProductPicker
				visible={productPickerVison}
				close={() => setProductPickerVision(false)}
				onSelect={onSelectProduct}
			/>
			<ProviderPicker
				visible={providerPickerVison}
				close={() => setProviderPickerVision(false)}
				onSelect={onSelectProvider}
			/>
		</div>
	);
}

function ProductPicker({
	visible, close = () => null,
	onSelect = () => null
}) {
	const {
		store: { product: { elements: products } },
		setProducts
	} = useProduct();
	const [view, setView] = useState(null);
	const [searchDisabled, setSearchDisabled] = useState(false);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await getProductList({
				columns: FETCHED_PRODUCT_COLUMNS,
				page: 0, size: 20, internal: true
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);
		};

		doFetch();
	}, [setProducts]);

	if (!visible) {
		return null;
	}

	const onClose = () => {
		close();

		if (view != null) {
			setView(null);	
		}
	};
	const selectProduct = (productId) => {
		onSelect(productId);

		if (view != null) {
			setView(null);
		}
	};
	const lookUpCurrentStore = (keyword) => Object.values(products).filter(product => product.name.toLowerCase().includes(keyword));
	const onSearchInputEntered = async (keyword) => {
		if (keyword.length === 0) {
			return;
		}

		setSearchDisabled(true);

		const lowerCasedKeyWord = keyword.toLowerCase();
		const [res, err] = await searchProduct({
			productName: lowerCasedKeyWord,
			columns: FETCHED_PRODUCT_COLUMNS
		});

		if (err) {
			console.error(err);
			setView(lookUpCurrentStore(lowerCasedKeyWord));
			setSearchDisabled(false);
			return;
		}

		const view = [];

		for (let product of Object.values(products)) {
			if (product.name.toLowerCase().includes(lowerCasedKeyWord)) {
				view.push(product);
			}
		}

		res.forEach(product => {
			const { id } = product;

			if (products[id] == null) {
				view.push(product);
			}
		});

		setView(view);
		setProducts(res);
		setTimeout(() => setSearchDisabled(false), 500);
	};
	const onSearchInputChange = (keyword) => {
		if (keyword.length === 0) {
			setView(null);
			return;
		}

		setView(lookUpCurrentStore(keyword.toLowerCase()));
	};
	const renderedElements = view == null ? Object.values(products) : view;

	return (
		<CenterModal
			close={onClose}
			width="100vw"
		>
			<div className="uk-width-1-1">
				<SearchInput
					placeholder="Search for a product"
					onChange={onSearchInputChange}
					onEntered={onSearchInputEntered}
					onSearchBtnClick={onSearchInputEntered}
					dd="Press Enter after finishing your keyword"
					disabled={searchDisabled}
				/>
			</div>
			<div className="uk-margin uk-grid-small uk-child-width-1-3" uk-grid="">
			{
				renderedElements.map(product => (
					<div
						key={product.id}
						onClick={() => selectProduct(product.id)}
					>
						<div className="uk-card uk-card-default uk-card-body uk-card-hover uk-padding-small pointer">
							<div className="uk-grid-small" uk-grid="">
								<div className="uk-width-1-3">
									<div>
										<div style={{
											width: "70px",
											height: "70px"
										}}>
										{
											asIf(hasLength(product.images))
												.then(() => (
													<DomainImage
														url={`${server.images.product}/${product.images[0]}`}
														fit="contain"
													/>
												))
												.else(() => (
													<span className="uk-position-center">No images</span>	
												))
										}
										</div>
									</div>
								</div>
								<div className="uk-width-2-3">
									<div className="uk-text-lead">{product.id}</div>
									<div className="">{product.name}</div>
								</div>
							</div>	
						</div>		
					</div>
				))
			}
			</div>
		</CenterModal>
	);
}

function ProviderPicker({
	visible, close = () => null,
	onSelect = () => null
}) {
	const {
		store: {
			elements: {
				map: providers,
				wasInit: wasStoreInit
			}
		},
		push
	} = useProvider();
	const [view, setView] = useState(null);
	const [searchDisabled, setSearchDisabled] = useState(false);

	useEffect(() => {
		const doFetch = async () => {
			if (wasStoreInit) {
				return;
			}

			const [res, err] = await fetchProviderList({
				columns: FETCHED_PROVIDER_COLUMNS,
				page: 0, size: 20
			});

			if (err) {
				console.error(err);
				return;
			}

			push(res);
		};

		doFetch();
	}, [wasStoreInit, push]);

	if (!visible) {
		return null;
	}

	const onClose = () => {
		close();

		if (view != null) {
			setView(null);	
		}
	};
	const selectProvider = (providerId) => {
		onSelect(providerId);

		if (view != null) {
			setView(null);	
		}
	};
	const lookUpCurrentStore = (keyword) => Object.values(providers).filter(provider => provider.name.toLowerCase().includes(keyword));
	const onSearchInputEntered = async (keyword) => {
		if (keyword.length === 0) {
			return;
		}

		setSearchDisabled(true);

		const lowerCasedKeyWord = keyword.toLowerCase();
		const [res, err] = await searchProvider({
			name: lowerCasedKeyWord,
			columns: FETCHED_PROVIDER_COLUMNS
		});

		if (err) {
			console.error();
			setView(lookUpCurrentStore(lowerCasedKeyWord));
			setSearchDisabled(false);
			return;
		}

		const view = [];

		for (let provider of Object.values(providers)) {
			if (provider.name.toLowerCase().includes(lowerCasedKeyWord)) {
				view.push(provider);
			}
		}

		res.forEach(provider => {
			const { id } = provider;

			if (providers[id] == null) {
				view.push(provider);
			}
		});

		setView(view);
		push(res);
		setTimeout(() => setSearchDisabled(false), 500);
	};
	const onSearchInputChange = (keyword) => {
		if (keyword.length === 0) {
			setView(null);
			return;
		}

		setView(lookUpCurrentStore(keyword.toLowerCase()));
	};
	const renderedElements = view == null ? Object.values(providers) : view;

	return (
		<CenterModal
			close={onClose}
			width="100vw"
		>
			<div className="uk-width-1-1">
				<SearchInput
					placeholder="Search for a Provider"
					onChange={onSearchInputChange}
					onEntered={onSearchInputEntered}
					onSearchBtnClick={onSearchInputEntered}
					dd="Press Enter when finishing your keyword"
					disabled={searchDisabled}
				/>
			</div>
			<div className="uk-margin uk-grid-small uk-child-width-1-4 uk-grid-match" uk-grid="">
			{
				renderedElements.map(provider => (
					<div
						key={provider.id}
						onClick={() => selectProvider(provider.id)}
					>
						<div className="uk-card uk-card-default uk-card-body uk-card-hover uk-padding-small pointer">
							<div className="uk-text-lead">{provider.name}</div>
							<div>{provider.email}</div>
							<div>{provider.representatorName}</div>
						</div>
					</div>
				))
			}
			</div>
		</CenterModal>
	);
}

function RightPanel() {
	return (
		<div>
			
		</div>
	);
}
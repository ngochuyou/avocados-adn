import {
	createContext, useContext,
	useReducer, Fragment,
	useState, useEffect
} from 'react';

import { server } from '../../config/default';

import {
	SET_VIEW, PUSH_LIST, MODIFY_MODEL, POP_LIST, TOGGLE_MODAL_VISION,
	CLEAR_LIST, SET_LIST
} from '../../actions/common';
import { searchProduct, createStockDetails } from '../../actions/product';
import { searchProvider } from '../../actions/provider';

import { DomainImage } from '../utils/Gallery';
import { FixedAddButton } from '../utils/Button';
import { ColorInput } from '../utils/Input';
import { ConfirmModal } from '../utils/ConfirmModal';

import { spread } from '../../utils';

import StockDetail from '../../models/StockDetail';

import { useSidebarContext } from '../../pages/Dashboard';

const Context = createContext();
const useGlobalContext = () => useContext(Context);

const LAYOUT_STORE = {
	view: <ImportBoard />
};

const { images: { product: IMAGE_URL } } = server;
const IMPORT_STORE = {
	form: {
		model: {
			product: null,
			size: StockDetail.NamedSize[0],
			numericSize: StockDetail.MINIMUM_NUMERIC_SIZE,
			color: "#000000",
			material: "",
			provider: null,
			status: StockDetail.Status[0],
			active: true,
			description: "",
			quantity: 1
		},
		elements: [],
		elementsAmount: 0,
		pickers: {
			product: {
				visible: false,
				elements: {},
				view: [],
				index: 0
			},
			provider: {
				visible: false,
				elements: {},
				view: []
			}
		}
	}
};

function ContextProvider({ children }) {
	const [layoutStore, dispatchLayoutStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = layoutDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...LAYOUT_STORE }
	);
	const [importStore, dispatchImportStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = importDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...IMPORT_STORE }
	);

	return <Context.Provider value={{
		layoutStore, dispatchLayoutStore,
		importStore, dispatchImportStore
	}}>
		{ children }
	</Context.Provider>;
}

const layoutDispatchers = {
	SET_VIEW: (payload, oldState) => {
		if (payload == null || typeof payload !== 'object') {
			return oldState;
		}

		return {
			...oldState,
			view: payload
		};
	}
};
const importDispatchers = {
	MODIFY_MODEL: (payload, oldState) => {
		const { index, name, value } = payload;

		if (typeof index !== 'number' || typeof name !== 'string') {
			return oldState;
		}

		const { form, form: { elements, elementsAmount } } = oldState;
		const newElements = [...elements].map((model, i) => i !== index ? model : {
			...model,
			[name]: isNaN(value) ? value : Number(value) // no negative
		});

		return {
			...oldState,
			form: {
				...form,
				elements: newElements,
				elementsAmount: name !== 'quantity' ? elementsAmount : newElements.reduce((total, item) => !isNaN(item.quantity) ? total + parseInt(item.quantity) : total, 0)
			}
		};
	},
	CLEAR_LIST: (payload, oldState) => {
		const { form } = oldState;

		return {
			...oldState,
			form: {
				...form,
				elements: []
			}
		};
	},
	PUSH_LIST: (payload, oldState) => {
		const { form, form: { elements } } = oldState;

		return {
			...oldState,
			form: {
				...form,
				elements: [...elements, IMPORT_STORE.form.model]
			}
		};
	},
	POP_LIST: (payload, oldState) => {
		if (typeof payload !== 'number') {
			return oldState;
		}

		const { form, form: { elements } } = oldState;

		return {
			...oldState,
			form: {
				...form,
				elements: [...elements].filter((model, index) => index !== payload)
			}
		};
	},
	TOGGLE_MODAL_VISION: (payload, oldState) => {
		const { index, name, value } = payload;

		if (typeof index !== 'number' || typeof name !== 'string' || typeof value !== 'boolean') {
			return oldState;
		}

		const { form, form: { pickers } } = oldState;
		const target = pickers[name];

		if (target == null) {
			return oldState;
		}

		return {
			...oldState,
			form: {
				...form,
				pickers: {
					...pickers,
					[name]: { ...target, visible: value, index }
				}
			}
		};
	},
	PUSH_PICKER_ELEMENTS: (payload, oldState) => {
		const { name, list } = payload;

		if (typeof name !== 'string' || !Array.isArray(list)) {
			return oldState;
		}

		const { form, form: { pickers } } = oldState;
		const target = pickers[name];
		
		if (target == null) {
			return oldState;
		}

		return {
			...oldState,
			form: {
				...form,
				pickers: {
					...pickers,
					[name]: {
						...target,
						elements: {
							...target.elements,
							...Object.fromEntries(list.map(ele => [ele.id, ele]))
						}
					}
				}
			}
		};
	},
	SET_PICKER_VIEW: (payload, oldState) => {
		const { name, list } = payload;

		if (typeof name !== 'string' || !Array.isArray(list)) {
			return oldState;
		}

		const { form, form: { pickers } } = oldState;
		const target = pickers[name];
		
		if (target == null) {
			return oldState;
		}

		return {
			...oldState,
			form: {
				...form,
				pickers: {
					...pickers,
					[name]: { ...target, view: list }
				}
			}
		};
	},
	SET_LIST: (payload, oldState) => {
		if (!Array.isArray(payload)) {
			return oldState;
		}

		const { form } = oldState;

		return {
			...oldState,
			form: {
				...form,
				elements: payload,
				elementsAmount: payload.reduce((total, item) => !isNaN(item.quantity) ? total + parseInt(item.quantity) : total, 0)
			}
		};
	}
};

export default function StockBoard() {
	return (
		<ContextProvider>
			<Body />
		</ContextProvider>
	);
}

function Body() {
	const { layoutStore, dispatchImportStore } = useGlobalContext();
	useEffect(() => {
		const elements = JSON.parse(localStorage[FORM_ELEMENTS_STORAGE_NAME]);

		dispatchImportStore({
			type: SET_LIST,
			payload: elements
		});
	}, [dispatchImportStore]);

	return (
		<Fragment>
			<Nav />
			{ layoutStore.view }
		</Fragment>
	);
}

const PRODUCT_PICKER = "product";
const PROVIDER_PICKER = "provider";

function ImportBoard() {
	const {
		importStore: {
			form: {
				elements: formElements,
				elementsAmount: formElementsAmount,
				pickers: {
					product: { visible: productPickerVisible },
					provider: { visible: providerPickerVisible }
				}
			},
		},
		dispatchImportStore
	} = useGlobalContext();
	const { setOverlay } = useSidebarContext();
	const [noti, setNoti] = useState();
	const [submitButtonVision, setSubmitButtonVision] = useState(true);
	const [confirmModalVision, setConfirmModalVision] = useState(false);
	const onModelChange = (index, event) => {
		dispatchImportStore({
			type: MODIFY_MODEL,
			payload: {
				index,
				name: event.target.name,
				value: event.target.value
			}
		});
	};
	const onRemoveModel = (index) => {
		dispatchImportStore({
			type: POP_LIST,
			payload: index
		});
	};
	const openProductPicker = (index) => {
		dispatchImportStore({
			type: TOGGLE_MODAL_VISION,
			payload: { name: PRODUCT_PICKER, value: true, index }
		});
	};
	const openProviderPicker = (index) => {
		dispatchImportStore({
			type: TOGGLE_MODAL_VISION,
			payload: { name: PROVIDER_PICKER, value: true, index }
		});
	};
	const clearBatch = () => {
		dispatchImportStore({ type: CLEAR_LIST });
	};
	const submitNoti = (message) => {
		setNoti(message);
		setTimeout(() => setNoti(null), 1500);
	};
	const saveBatch = () => {
		localStorage[FORM_ELEMENTS_STORAGE_NAME] = JSON.stringify(formElements);
		submitNoti("Batch saved");
	};
	const validateBatch = (columns) => formElements.map((item, index) => [index, columns.map(col => StockDetail.validator[col](item[col])[1]).filter(err => err != null)]).filter(set => set[1].length !== 0);
	const submitBatch = () => {
		setSubmitButtonVision(false);

		const errors = validateBatch(VALIDATED_STOCKDETAIL_COLUMNS);

		if (errors.length !== 0) {
			setOverlay(
				<ul className="uk-list uk-list-disc">
				{
					errors.map((set, index) => (
						<li key={index}>
							<div
								className="uk-text-primary"
							>
								<a
									href={`#${STOCKDETAIL_ITEM_ANCHOR_PREFIX}${set[0]}`}
									className="uk-link-reset"
								>{`Item #${set[0] + 1}`}</a>
							</div>
						{
							<ol className="uk-list">
							{
								set[1].map((err, index) => (
									<li key={index} className="uk-text-danger">
										<a
											href={`#${STOCKDETAIL_ITEM_ANCHOR_PREFIX}${set[0]}`}
											className="uk-link-reset"
										>{err}</a>
									</li>
								))
							}
							</ol>
						}
						</li>
					))
				}
				</ul>
			);
			setSubmitButtonVision(true);
			return;
		}

		setSubmitButtonVision(true);
		setOverlay(null);
		setConfirmModalVision(true);
	};
	const doSubmitBatch = async () => {
		const elements = formElements.flatMap(element => spread(element.quantity, element));
		const [, err] = await createStockDetails(elements);

		if (err) {
			console.error(err);
			return;
		}

		setConfirmModalVision(false);
		submitNoti("Batch created successfully");
	};

	return (
		<main className="uk-padding-small">
		{
			noti != null ? (
				<div className="uk-alert-primary uk-position-fixed uk-position-top-center uk-width-2xlarge" uk-alert="">
					<p>{noti}</p>
				</div>
			) : null
		}
		{
			confirmModalVision ? (
				<ConfirmModal
					message="Are you sure you want to submit this batch?"
					onYes={() => doSubmitBatch()}
					onNo={() => setConfirmModalVision(false)}
				/>
			) : null
		}
			<header>
				<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
					<div>
						<h3 className="uk-heading-line colors">
							<span>Import Batch</span>
						</h3>
					</div>
					<div className="uk-text-right">
						<button
							className="uk-button backgroundf uk-margin-small-right"
							onClick={saveBatch}
						>Save batch</button>
						<button
							className="uk-button uk-button-default"
							onClick={clearBatch}
						>Clear batch</button>
					</div>
				</div>
			</header>
			<section className="uk-background-muted">
			{
				formElements.map((model, index) => (
					<div
						key={index} className="uk-background-default uk-padding-small uk-box-shadow-hover-small uk-margin uk-margin-left uk-margin-right"
						id={`${STOCKDETAIL_ITEM_ANCHOR_PREFIX}${index}`}
					>
						<div className="uk-grid-small" uk-grid="">
							<div className="uk-width-auto">
								<div className="uk-text-lead colors uk-text-center">{`#${index + 1}`}</div>
								<div
									style={{
										width: "100px", maxWidth: "100px",
										height: "100px", maxHeight: "100px"
									}}
									uk-tooltip="Product"
									onClick={() => openProductPicker(index)}
									className="pointer"
								>
								{
									model.product == null ? (
										<div className="uk-width-1-1 uk-height-1-1 uk-position-relative noselect">
											<span className="uk-position-center uk-text-primary uk-text-small">Pick a product</span>
										</div>
									) : (
										<DomainImage
											url={`${IMAGE_URL}/${model.product.images[0]}`} name={model.product.images[0]}
											className="uk-border-circle"
										/>
									)
								}
								</div>
							</div>
							<div className="uk-width-expand">
								<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
									<div>
									{
										model.product == null ? (
											<div
												className="pointer"
												onClick={() => openProductPicker(index)}
											>
												<span className="uk-text-primary">Pick a product</span>
											</div>
										) : (
											<div className="colors">
												<span className="uk-text-large">{model.product.id}</span>
												<span className="uk-margin-left uk-text-muted">{model.product.name}</span>
											</div>
										)
									}
									</div>
									<div
										className="uk-position-relative pointer uk-text-right"
										onClick={() => openProviderPicker(index)}
										uk-tooltip="Choose a Provider"
									>
										<div>
										{
											model.provider != null ? (
												<div className="uk-text-muted">
													<span>{model.provider.email}</span>
													<span className="colors uk-margin-small-left uk-text-large">{model.provider.name}</span>
												</div>
											) : <span className="uk-text-primary uk-position-bottom-left">Pick a provider</span>
										}
										</div>
									</div>
								</div>
								<div className="uk-width-expand uk-margin-small uk-grid-small" uk-grid="">
									<div className="uk-width-expand">
										<div className="uk-margin-small">
											<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
												<div>
													<ColorInput
														uk-tooltip="Color" name="color" hex={model.color}
														className="uk-input" placeholder="Color"
														onChange={(event) => onModelChange(index, event)}
													/>
												</div>
												<div>
													<input
														uk-tooltip="Color" name="color"
														onChange={(event) => onModelChange(index, event)}
														className="uk-input" placeholder="Color" value={model.color}
														type="text" maxLength={StockDetail.MAXIMUM_COLOR_LENGTH}
													/>
												</div>
											</div>
										</div>
										<div className="uk-margin-small">
											<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
												<div>
													<select
														className="uk-select" value={model.size}
														uk-tooltip="Size" name="size"
														onChange={(event) => onModelChange(index, event)}
													>
													{
														StockDetail.NamedSize.map(ele => (
															<option key={ele} value={ele}>{ele}</option>
														))
													}
													</select>
												</div>
												<div>
													<input
														type="number" className="uk-input" placeholder="Size"
														min={StockDetail.MINIMUM_NUMERIC_SIZE} value={model.numericSize}
														max={StockDetail.MAXIMUM_NUMERIC_SIZE} uk-tooltip="Numeric Size"
														name="numericSize" onChange={(event) => onModelChange(index, event)}
													/>
												</div>
											</div>
										</div>
									</div>
									<div className="uk-width-expand">
										<div className="uk-margin-small">
											<input
												uk-tooltip="Description"
												type="text" className="uk-input" placeholder="Description"
												maxLength={StockDetail.MAXIMUM_DESCRIPTION_LENGTH}
												name="description" onChange={(event) => onModelChange(index, event)}
											/>
										</div>
										<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
											<div>
												<select
													className="uk-select" value={model.active}
													uk-tooltip="Active" name="active"
													onChange={(event) => onModelChange(index, event)}
												>
													<option key={true} value={true} className="uk-text-success">Active</option>
													<option key={false} value={false} className="uk-text-muted">Inactive</option>
												</select>
											</div>
											<div>
												<select
													className="uk-select" value={model.status}
													uk-tooltip="Status" name="status"
													onChange={(event) => onModelChange(index, event)}
												>
												{
													StockDetail.Status.map(ele => (
														<option key={ele} value={ele}>{ele}</option>
													))
												}
												</select>
											</div>
										</div>
									</div>
									<div style={{width: "75px"}}>
										<div className="uk-margin-small">
											<input
												uk-tooltip="Quantity"
												className="uk-input" placeholder="Quantity" type="number"
												min={0} max={StockDetail.MAXIMUM_ITEM_QUANTITY}
												value={model.quantity} name="quantity"
												onChange={(event) => onModelChange(index, event)}
											/>
										</div>
										<div className="uk-margin-small uk-text-center">
											<p
												onClick={() => onRemoveModel(index)}
												style={{width: "40px", height: "40px"}}
												uk-icon="icon: trash"
												className="uk-icon-button pointer"
											></p>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				))
			}
				<FixedAddButton
					onClick={() => dispatchImportStore({ type: PUSH_LIST })}
				/>
			</section>
			{
				submitButtonVision && formElements.length > 0 ? (
					<div className="uk-margin">
						<button
							className="uk-button backgroundf"
							onClick={submitBatch}
						>Submit batch</button>
						<span className="uk-margin-left">{`${formElementsAmount} item(s)`}</span>
					</div>
				) : null
			}
			{
				productPickerVisible ? 
				<ProductPicker /> : null
			}
			{
				providerPickerVisible ? 
				<ProviderPicker /> : null
			}
		</main>
	);
}

const SEARCHED_PRODUCT_COLUMNS = ["id", "name", "images", "active"];
const SEARCHED_PRODUCT_AMOUNT = 100;
const SEARCHED_PROVIDER_COLUMNS = ["id", "name", "email", "representatorName", "active"];
const SEARCHED_PROVIDER_AMOUNT = 100;
const SET_PICKER_VIEW = "SET_PICKER_VIEW";
const PUSH_PICKER_ELEMENTS = "PUSH_PICKER_ELEMENTS";
const VALIDATED_STOCKDETAIL_COLUMNS = ["product", "provider", "size", "status", "numericSize", "quantity"];
const FORM_ELEMENTS_STORAGE_NAME = "importBatchElements";
const STOCKDETAIL_ITEM_ANCHOR_PREFIX = "stockdetail-item-";

function ProductPicker() {
	const {
		importStore: {
			form: {
				pickers: { product: { elements, view, index } }
			}
		},
		dispatchImportStore
	} = useGlobalContext();
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const [isFetching, toggleFetching] = useReducer((isFetching) => !isFetching, false);
	const closeProductPicker = () => {
		setContainerClassName('fade-out uk-modal');
		setTimeout(() => {
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PRODUCT_PICKER,
					list: Object.values(elements)
				}
			});
			dispatchImportStore({
				type: TOGGLE_MODAL_VISION,
				payload: { name: PRODUCT_PICKER, value: false, index }
			});
		}, 180);
	};
	const onSearchInputChange = (event) => {
		const value = event.target.value.toLowerCase();

		if (value.length === 0) {
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PRODUCT_PICKER,
					list: Object.values(elements)
				}
			});
			return;
		}

		dispatchImportStore({
			type: SET_PICKER_VIEW,
			payload: {
				name: PRODUCT_PICKER,
				list: Object.values(elements).filter(product => product.name.toLowerCase().includes(value) || product.id.toLowerCase().includes(value))
			}
		});
	};
	const onSearchInputKeyDown = async (event) => {
		if (event.keyCode === 13) {
			if (isFetching) {
				return;
			}

			toggleFetching();

			const { target: { value } } = event;
			const [res, err] = await searchProduct({
				productId: value, productName: value,
				columns: SEARCHED_PRODUCT_COLUMNS,
				size: SEARCHED_PRODUCT_AMOUNT
			});

			if (err) {
				console.error(err);
				toggleFetching();
				return;
			}

			dispatchImportStore({
				type: PUSH_PICKER_ELEMENTS,
				payload: {
					name: PRODUCT_PICKER,
					list: res
				}
			});
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PRODUCT_PICKER,
					list: res
				}
			});
			toggleFetching();
			return;
		}
	};
	const onModalKeyDown = (event) => {
		if (event.keyCode === 27) {
			closeProductPicker();
		}
	};
	const onPick = (product) => {
		dispatchImportStore({
			type: MODIFY_MODEL,
			payload: { name: "product", value: product, index }
		});
		closeProductPicker();
	};

	return (
		<div
			className={`uk-modal-center uk-open uk-display-block ${containerClassName}`}
			uk-modal="" style={{ maxHeight: "100vh", overflow: "hidden", zIndex: 2 }}
			tabIndex={10} onKeyDown={onModalKeyDown}
		>
			<div className="uk-modal-dialog uk-width-2xlarge uk-padding-small">
				<button
					className="uk-button uk-position-top-right uk-position-z-index"
					type="button" uk-icon="icon: close; ratio: 1.25;"
					onClick={closeProductPicker}
				></button>
				<header>
					<h3 className="uk-heading colors">Pick a product</h3>
				</header>
				<main>
					<div className="uk-margin-small">
						<input
							type="search" placeholder="Product code or Product name"
							className="uk-input uk-border-pill" onChange={onSearchInputChange}
							onKeyDown={onSearchInputKeyDown}
						/>
					</div>
					<section
						className="uk-height-max-medium uk-position-relative uk-grid-collapse uk-child-width-1-2 uk-overflow-auto"
						uk-grid=""
					>
					{
						view.length === 0 ?
						<div className="uk-height-medium">
							<span className="uk-text-muted uk-position-center">Nothing found</span>
						</div> : 
						view.map(product => (
							<div
								key={product.id}
								className="uk-box-shadow-hover-small uk-padding-small pointer"
								onClick={() => onPick(product)}
							>
								<div
									uk-grid=""
									className="uk-grid-small uk-padding-small"
								>
									<div className="uk-width-auto">
										<div style={{width: "100px", height: "100px"}}>
											<DomainImage
												url={`${IMAGE_URL}/${product.images[0]}`} name={product.images[0]}
												className="uk-border-circle"
											/>
										</div>
									</div>
									<div className="uk-width-expand">
										<div className="uk-text-lead">{product.id}</div>
										<div className="uk-text-muted">{product.name}</div>
										{
											product.active ?
											<div className="uk-text-success">ACTIVE</div> :
											<div className="uk-text-muted">INACTIVE</div>
										}
									</div>
								</div>
							</div>
						))
					}
					</section>
				</main>
			</div>
		</div>
	);
}

function ProviderPicker() {
	const {
		importStore: {
			form: {
				pickers: { provider: { elements, view, index } }
			}
		},
		dispatchImportStore
	} = useGlobalContext();
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const [isFetching, toggleFetching] = useReducer((isFetching) => !isFetching, false);
	const closeProviderPicker = () => {
		setContainerClassName('fade-out uk-modal');
		setTimeout(() => {
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PROVIDER_PICKER,
					list: Object.values(elements)
				}
			});
			dispatchImportStore({
				type: TOGGLE_MODAL_VISION,
				payload: { name: PROVIDER_PICKER, value: false, index }
			});
		}, 180);
	};
	const onSearchInputChange = (event) => {
		const value = event.target.value.toLowerCase();

		if (value.length === 0) {
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PROVIDER_PICKER,
					list: Object.values(elements)
				}
			});
			return;
		}

		dispatchImportStore({
			type: SET_PICKER_VIEW,
			payload: {
				name: PROVIDER_PICKER,
				list: Object.values(elements).filter(provider => provider.name.toLowerCase().includes(value))
			}
		});
	};
	const onSearchInputKeyDown = async (event) => {
		if (event.keyCode === 13) {
			if (isFetching) {
				return;
			}

			toggleFetching();

			const { target: { value } } = event;
			const [res, err] = await searchProvider({
				name: value,
				columns: SEARCHED_PROVIDER_COLUMNS,
				size: SEARCHED_PROVIDER_AMOUNT
			});

			if (err) {
				console.error(err);
				toggleFetching();
				return;
			}

			dispatchImportStore({
				type: PUSH_PICKER_ELEMENTS,
				payload: {
					name: PROVIDER_PICKER,
					list: res
				}
			});
			dispatchImportStore({
				type: SET_PICKER_VIEW,
				payload: {
					name: PROVIDER_PICKER,
					list: res
				}
			});
			toggleFetching();
			return;
		}
	};
	const onModalKeyDown = (event) => {
		if (event.keyCode === 27) {
			closeProviderPicker();
		}
	};
	const onPick = (provider) => {
		dispatchImportStore({
			type: MODIFY_MODEL,
			payload: { name: "provider", value: provider, index }
		});
		closeProviderPicker();
	};

	return (
		<div
			className={`uk-modal-center uk-open uk-display-block ${containerClassName}`}
			uk-modal="" style={{ maxHeight: "100vh", overflow: "hidden", zIndex: 2 }}
			tabIndex={10} onKeyDown={onModalKeyDown}
		>
			<div className="uk-modal-dialog uk-width-2xlarge uk-padding-small">
				<button
					className="uk-button uk-position-top-right uk-position-z-index"
					type="button" uk-icon="icon: close; ratio: 1.25;"
					onClick={closeProviderPicker}
				></button>
				<header>
					<h3 className="uk-heading colors">Pick a provider</h3>
				</header>
				<main>
					<div className="uk-margin-small">
						<input
							type="search" placeholder="Provider name"
							className="uk-input uk-border-pill" onChange={onSearchInputChange}
							onKeyDown={onSearchInputKeyDown}
						/>
					</div>
					<section
						className="uk-height-max-medium uk-position-relative uk-grid-collapse uk-child-width-1-2 uk-overflow-auto"
						uk-grid=""
					>
					{
						view.length === 0 ?
						<div className="uk-height-medium">
							<span className="uk-text-muted uk-position-center">Nothing found</span>
						</div> : 
						view.map(provider => (
							<div
								key={provider.id}
								className="uk-box-shadow-hover-small uk-padding-small pointer"
								onClick={() => onPick(provider)}
							>	
								<div
									uk-grid=""
									className="uk-grid-small uk-child-width-1-2"
								>
									<div className="uk-text-lead colors">{provider.name}</div>
									<div className="uk-text-small uk-position-relative">
									{
										provider.active ?
										<div className="uk-text-success uk-position-center">ACTIVE</div> :
										<div className="uk-text-muted uk-position-center">INACTIVE</div>
									}
									</div>
								</div>
								<div
									className="uk-text-truncate uk-margin-small-top"
									uk-tooltip={provider.email}
								>{provider.email}</div>
								<div>{provider.representatorName}</div>
							</div>
						))
					}
					</section>
				</main>
			</div>
		</div>
	);
}

function ViewBoard() {
	return (
		<section>
			view
		</section>
	);
}

function Nav() {
	const {
		layoutStore: { view },
		dispatchLayoutStore
	} = useGlobalContext();

	return (
		<div>
			<ul className="uk-box-shadow-small uk-child-width-expand" id="nav" uk-tab="" uk-height-match="">
				<li className={view.type === ViewBoard ? "uk-active" : ""}>
					<a
						href="#nav"
						style={{height: "30px"}}
						onClick={() => dispatchLayoutStore({
							type: SET_VIEW,
							payload: <ViewBoard />
						})}
					>VIEW</a>
				</li>
				<li className={view.type === ImportBoard ? "uk-active" : ""}>
					<a
						href="#nav"
						style={{height: "30px"}}
						onClick={() => dispatchLayoutStore({
							type: SET_VIEW,
							payload: <ImportBoard />
						})}
					>IMPORT</a>
				</li>
			</ul>
		</div>
	);
}
import { useReducer, useEffect, Fragment } from 'react';

import Navbar from './Navbar.jsx';
import Paging from '../utils/Paging.jsx';

import {
	fetchCategoryList, fetchCategoryCount,
	createCategory, updateCategory,
	updateCategoryActivationState
} from '../../actions/product';

import { spread, toMap } from '../../utils';

import { Category } from '../../models/Factor';
/*layout*/
const CATEGORY_VIEW = "CATEGORY";
const PRODUCT_VIEW = "PRODUCT";

const FETCH_CATEGORY_COLUMNS = ["id", "name", "description", "active"];

const LAYOUT_STATE = {
	view: CATEGORY_VIEW
}

const SET_LAYOUT_VIEW = "SET_LAYOUT_VIEW"; 
/*===================*/
/*category*/
const CATEGORY_STORE = {
	list: {},
	count: 0,
	pagination: {
		page: 0,
		totalPages: 0,
		size: 10,
		fetchStatus: [],
		pageMap: {
			0: []
		}
	}
}

const SET_STORE = "SET_STORE";
const SET_CATEGORY_LIST = "SET_CATEGORY_LIST";
const SET_CATEGORIES_COUNT = "SET_CATEGORIES_COUNT";
const FULFILL_CATEGORY_PAGE_REQUEST = "FULFILL_CATEGORY_PAGE_REQUEST";
const SWITCH_CATEGORY_PAGE = "SWITCH_CATEGORY_PAGE";
/*===================*/
const fetchStore = async (dispatchCategoryStore) => {
	const [ fetchedCategoryList, categoryListFetchErr ] = await fetchCategoryList({
		columns: FETCH_CATEGORY_COLUMNS
	});

	if (categoryListFetchErr) {
		console.error(categoryListFetchErr);
		return;
	}

	dispatchCategoryStore({
		type: SET_CATEGORY_LIST,
		payload: fetchedCategoryList
	});

	const [ fetchedCategoryCount, categoryCountFetchErr ] = await fetchCategoryCount();

	if (categoryCountFetchErr) {
		console.error(categoryCountFetchErr);
		return;
	}

	dispatchCategoryStore({
		type: SET_CATEGORIES_COUNT,
		payload: fetchedCategoryCount
	});
};

export default function ProductBoard() {
	const [ layoutState, dispatchLayoutState ] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch(type) {
				case SET_LAYOUT_VIEW: {
					if (payload !== CATEGORY_VIEW && payload !== PRODUCT_VIEW) {
						return oldState;
					}

					return { ...oldState, view: payload };
				}
				default: return oldState;
			}
		}, { ...LAYOUT_STATE }
	);
	const [ categoryStore, dispatchCategoryStore ] = useReducer(
		(oldState, { type = null, payload = null} = {}) => {
			switch(type) {
				case FULFILL_CATEGORY_PAGE_REQUEST: {
					const { page , list: categoryList } = payload;

					if (typeof page !== 'number' || !Array.isArray(categoryList)) {
						return oldState;
					}

					const { list, pagination, pagination: { pageMap, fetchStatus } } = oldState;
					
					return {
						...oldState,
						list: toMap(categoryList, { ...list }, "id"),
						pagination: {
							...pagination,
							page,
							pageMap: {
								...pageMap,
								[page]: categoryList
							},
							fetchStatus: fetchStatus.map((ele, index) => index !== page ? ele : true)
						}
					};
				}
				case SWITCH_CATEGORY_PAGE: {
					if (typeof payload !== 'number') {
						return oldState;
					}

					const { pagination } = oldState;

					return {
						...oldState,
						pagination: { ...pagination, page: payload }
					};
				}
				case SET_CATEGORY_LIST: {
					if (!Array.isArray(payload)) {
						return oldState;
					}

					const pageMap = { 0: payload };
					const { pagination } = oldState;

					return {
						...oldState,
						list: toMap(payload, {}, "id"),
						pagination: { ...pagination, pageMap }
					};
				}
				case SET_CATEGORIES_COUNT: {
					if (typeof payload !== 'number') {
						return oldState;
					}

					const { pagination, pagination: { size } } = oldState;
					const totalPages = Math.ceil(payload / size);
					let fetchStatus = spread(totalPages, false);

					fetchStatus[0] = true;

					return {
						...oldState,
						count: payload,
						pagination: { ...pagination, fetchStatus, totalPages }
					};
				}
				case SET_STORE: {
					return { ...oldState, ...payload };
				}
				default: return oldState;
			}
		}, { ...CATEGORY_STORE }
	);

	useEffect(() => fetchStore(dispatchCategoryStore), []);

	const { view: viewOnLayout } =  layoutState;
	const requestCategoryPage = async (pageNumber) => {
		const { pagination: { fetchStatus, size } } = categoryStore;
		const page = pageNumber - 1;

		if (fetchStatus[page] === true) {
			dispatchCategoryStore({
				type: SWITCH_CATEGORY_PAGE,
				payload: page
			});

			return;
		}

		const [ list, err ] = await fetchCategoryList({ page, size, columns: FETCH_CATEGORY_COLUMNS });

		if (err) {
			console.error(err);
			return;
		}

		dispatchCategoryStore({
			type: FULFILL_CATEGORY_PAGE_REQUEST,
			payload: { page, list }
		});
	};
	const pushCategory = (category) => {
		const { list } = categoryStore;

		for (let element of Object.values(list)) {
			if (element.id === category.id) {
				const { pagination, pagination: { page, pageMap } } = categoryStore;

				dispatchCategoryStore({
					type: SET_STORE,
					payload: {
						...categoryStore,
						list: { ...list, [category.id]: category },
						pagination: {
							...pagination,
							pageMap: {
								...pageMap,
								[page]: [...pageMap[page]].map(ele => ele.id === category.id ? category : ele)
							}
						}
					}
				});
				return;
			}
		}

		let { count, pagination, pagination: { page, totalPages, size, pageMap } } = categoryStore;
		const isFull = (count % size) === 0;

		dispatchCategoryStore({
			type: SET_STORE,
			payload: {
				...categoryStore,
				list: { ...list, [category.id]: category },
				count: count++,
				pagination: {
					...pagination,
					totalPages: isFull ? totalPages + 1 : totalPages,
					size,
					pageMap: {
						...pageMap,
						[page]: [...pageMap[page], category]
					}
				}
			}
		});
	};

	return (
		<div className="uk-position-relative">
			<Navbar
				centerElement={
					<Tab
						onCategoryNavClick={() => dispatchLayoutState({
							type: SET_LAYOUT_VIEW,
							payload: CATEGORY_VIEW
						})}
						onProductNavClick={() => dispatchLayoutState({
							type: SET_LAYOUT_VIEW,
							payload: PRODUCT_VIEW
						})}
					/>
				}
			/>
			<div className="uk-padding">
			{
				viewOnLayout === CATEGORY_VIEW ? (
					<CategoryView
						store={categoryStore}
						requestPage={requestCategoryPage}
						pushCategory={pushCategory}
					/>
				) : ( <ProductView /> )
			}
			</div>
		</div>
	);
}

function ProductView() {
	return (
		<section>
			Product
		</section>
	);
}

const ACTION_CREATE = "ACTION_CREATE";
// const ACTION_EDIT = "ACTION_EDIT";

const CATEGORY_FORM_STATE = {
	model: {
		name: "",
		description: ""
	},
	error: "",
	action: ACTION_CREATE,
	visible: false
};

const CATEGORY_INDIVIDUAL_VIEW_STATE = {
	target: {},
	visible: false,
	isEditing: false
};

const TOGGLE_VISION = "TOGGLE_VISION";
const MODIFY_MODEL = "MODIFY_MODEL";
const SET_ERROR = "SET_ERROR";
const SET_MODEL = "SET_MODEL";
const TOGGLE_EDIT = "TOGGLE_EDIT";

function CategoryView({
	store = CATEGORY_STORE,
	requestPage = () => null,
	pushCategory = () => null
}) {
	const [ formState, dispatchFormState ] = useReducer(
		(oldState, { type = null, payload = null} = {}) => {
			switch(type) {
				case MODIFY_MODEL: {
					const { name, value } = payload;

					if (typeof name !== 'string' || typeof value !== 'string') {
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
				}
				case SET_MODEL: {
					const { model, visible } = payload;

					if (model == null || typeof model !== 'object' || typeof visible !== 'boolean') {
						return oldState;
					}

					return {
						...oldState,
						model, visible
					};
				}
				case SET_ERROR: {
					if (typeof payload !== 'string') {
						return oldState;
					}

					return {
						...oldState,
						error: payload
					};
				}
				case TOGGLE_VISION: {
					const { visible } = oldState;

					return { ...oldState, visible: !visible };
				}
				default: return oldState;
			}
		}, { ...CATEGORY_FORM_STATE }
	);
	const [ indivViewState, dispatchIndivViewState ] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch(type) {
				case TOGGLE_EDIT: {
					return {
						...oldState,
						isEditing: !oldState.isEditing
					}
				}
				case TOGGLE_VISION: {
					return {
						...oldState,
						visible: !oldState.visible
					};
				}
				case MODIFY_MODEL: {
					const { model, visible } = payload;

					if (model == null || typeof model !== 'object') {
						return oldState;
					}

					if (typeof visible !== 'boolean') {
						return oldState;
					}

					return {
						...oldState,
						target: model,
						visible: visible
					};
				}
				default: return oldState;
			}
		}, { ...CATEGORY_INDIVIDUAL_VIEW_STATE }
	);
	const { pagination: {
		page,
		pageMap,
		totalPages
	} } = store;
	const validate = (props = []) => {
		const { model, model: { name: categoryName, description } } = formState;
		let [, err] = [null, null];

		for (let key of props) {
			[, err] = Category.validator[key](description);

			if (err) {
				dispatchFormState({
					type: SET_ERROR,
					payload: err
				});
				return null;
			}
		}

		if (categoryName.length === 0) {
			return null;
		}

		const { list } = store;

		for (let category of Object.values(list)) {
			if (categoryName.trim() === category.name) {

				dispatchFormState({
					type: SET_ERROR,
					payload: "Name was taken"
				});
				return null;
			}
		}

		return model;
	};
	const onCreate = async (event) => {
		event.preventDefault();
		dispatchFormState({
			type: SET_ERROR,
			payload: ""
		});

		const model = validate(["name", "description"]);

		if (model == null) {
			return;
		}

		const [creationRes, creationErr] = await createCategory({ model });

		if (creationErr) {
			dispatchFormState({
				type: SET_ERROR,
				payload: creationErr["name"]
			});
			return;
		}

		dispatchFormState({ type: TOGGLE_VISION });
		pushCategory(creationRes);
	};
	const onUpdate = async (event) => {
		event.preventDefault();
		dispatchFormState({
			type: SET_ERROR,
			payload: ""
		});

		const model = validate(["name", "description", "active"]);

		if (model == null) {
			return;
		}

		const [res, err] = await updateCategory({ model });

		if (err) {
			dispatchFormState({
				type: SET_ERROR,
				payload: err["name"]
			});
			return;
		}

		pushCategory(res);
		dispatchIndivViewState({
			type: MODIFY_MODEL,
			payload: {
				model: res,
				visible: true
			}
		});
		dispatchIndivViewState({ type: TOGGLE_EDIT });
	};
	const onUpdateActivationState = async (state) => {
		const [, err] = await updateCategoryActivationState(target.id, state);

		if (err) {
			console.error(err);
			return;
		}

		const newCategoryState = {
			...target,
			active: state
		};

		dispatchIndivViewState({
			type: MODIFY_MODEL,
			payload: {
				model: newCategoryState,
				visible: true
			}
		});
		pushCategory(newCategoryState);
	}
	const onFormInputChange = (event) => {
		dispatchFormState({
			type: MODIFY_MODEL,
			payload: { name: event.target.name, value: event.target.value }
		});
	};
	const { target } = indivViewState;

	return (
		<section>
			<table className="uk-table uk-table-justify uk-table-divider">
				<thead>
					<tr>
						<th className="uk-width-small">ID</th>
						<th>Name</th>
						<th>Description</th>
						<th>Status</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
				{
					pageMap[page].map(category => (
						<tr
							key={category.id} className="pointer"
							onClick={() => dispatchIndivViewState({
								type: MODIFY_MODEL,
								payload: {
									model: category,
									visible: true
								}
							})}
						>
							<td>
								<span className="uk-text-bold colors">
									{ category.id }
								</span>
							</td>
							<td>
								{ category.name }
							</td>
							<td>
								{ category.description }
							</td>
							<td>
							{
								category.active ?
								<span className="uk-text-success">ACTIVE</span> :
								<span className="uk-text-muted">INACTIVE</span> 
							}
							</td>
							<td>
								<span
									uk-icon="icon: file-edit"
									className="uk-icon-button"
								></span>
							</td>
						</tr>
					))
				}
				</tbody>
			</table>
			<Paging
				amount={totalPages}
				amountPerChunk={5}
				selected={page + 1}
				onPageSelect={requestPage}
			/>
			<div
				onClick={() => dispatchFormState({ type: TOGGLE_VISION })}
				className="uk-position-fixed uk-border-circle uk-box-shadow-large backgroundf pointer"
				uk-tooltip="Create a new Category"
				style={{
					width: '50px',
					height: '50px',
					bottom: '25px',
					right: '50px'
				}}
			>
				<span
					uk-icon="icon: plus; ratio: 1.5"
					className="uk-position-center"
				></span>
			</div>
			{
				formState.visible ? (
					<div id="model-form" className="uk-flex-top uk-open uk-display-block" uk-modal="">
						<div className="uk-modal-dialog uk-modal-body uk-margin-auto-vertical">
							<h2>Create new Category</h2>
							<CategoryForm
								model={formState.model}
								onSubmit={onCreate}
								error={formState.error}
								onInputChange={onFormInputChange}
								close={() => dispatchFormState({ type: TOGGLE_VISION })}
							/>
						</div>
					</div>
				) : null
			}
			{
				indivViewState.visible ? (
					<div
						uk-modal=""
						className="uk-open uk-display-block"
					>
    					<div className="uk-modal-dialog">
							<div className="uk-modal-header">
								<h2 className="uk-modal-title">{target.name}</h2>
							</div>
							<div className="uk-modal-body" uk-overflow-auto="">
								<div
									className="uk-grid-medium uk-child-width-1-2 uk-grid-match"
									uk-grid=""
								>
									<div>
										<div className="uk-card uk-card-default uk-card-body">
											<h3 className="uk-card-title uk-text-center">Description</h3>
											{
												target.description == null || target.description.length === 0 ?
												<p className="uk-text-muted uk-text-small">There's no description yet</p> :
												<p>{target.description}</p>
											}
										</div>
									</div>
									<div>
										<div className="uk-card uk-card-default uk-card-body uk-text-center">
											<h3 className="uk-card-title">Status</h3>
											{
												target.active ?
												<span className="uk-text-success">ACTIVE</span> :
												<span className="uk-text-muted">INACTIVE</span> 
											}
										</div>
									</div>
								</div>
								<div className="uk-margin">
								{
									!indivViewState.isEditing ? null :
									<Fragment>
										<CategoryForm
											onSubmit={onUpdate}
											error={formState.error}
											onInputChange={onFormInputChange}
											model={formState.model}
											close={() => dispatchIndivViewState({ type: TOGGLE_EDIT })}
										/>
										<hr className="uk-divider-icon" />
										<div className="uk-text-center">
										{
											target.active ? <button
												className="uk-button uk-button-danger"
												onClick={() => onUpdateActivationState(false)}
											>
												Deactivate
											</button> :
											<button
												className="uk-button backgroundf"
												onClick={() => onUpdateActivationState(true)}
											>
												Activate
											</button>
										}
										</div>
									</Fragment>
								}
								</div>
							</div>
							<div className="uk-modal-footer uk-text-right">
								<button
									className="uk-button backgroundf uk-margin-right"
									onClick={() => {
										dispatchFormState({
											type: SET_MODEL,
											payload: {
												model: target,
												visible: false
											}
										});
										dispatchIndivViewState({ type: TOGGLE_EDIT });
									}}
								>Edit</button>
								<button
									className="uk-button uk-button-default"
									onClick={() => {
										dispatchIndivViewState({
											type: MODIFY_MODEL,
											payload: {
												model: {},
												visible: false
											}
										});
										dispatchIndivViewState({ type: TOGGLE_EDIT });
										dispatchFormState({
											type: SET_MODEL,
											payload: {
												model: {
													name: "",
													description: ""
												},
												visible: false
											}
										});
									}}
								>Close</button>
							</div>
    					</div>
					</div>
				) : null
			}
		</section>
	);
}

function CategoryForm({
	model = {},
	onSubmit = (event) => event.preventDefault(),
	error = "",
	onInputChange = () => null,
	close = () => null
}) {
	return (
		<form onSubmit={onSubmit} style={{zIndex: "20"}}>
			<div className="uk-margin">
				<label className="uk-label backgrounds">Name</label>
				<input
					className="uk-input"
					type="text"
					value={model.name}
					placeholder="Category name"
					name="name"
					onChange={onInputChange}
				/>
			</div>
			<div className="uk-margin">
				<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
					<div>
						<label className="uk-label backgrounds">Description</label>
					</div>
					<div className="uk-text-right">
						<span>{`${255 - model.description.length} left`}</span>
					</div>
				</div>
				<textarea
					className="uk-input uk-height-small"
					type="text"
					value={model.description}
					placeholder="Description"
					name="description"
					onChange={onInputChange}
					rows={3}
				></textarea>
				<span className="uk-text-danger">{error}</span>
			</div>
			<div className="uk-margin">
				<button className="uk-button uk-margin-right backgroundf" type="submit">Submit</button>
				<button
					className="uk-button uk-button-default"
					type="button"
					onClick={close}
				>Cancel</button>
			</div>
		</form>
	);
}

function Tab({
	onCategoryNavClick = () => null,
	onProductNavClick = () => null
}) {
	return (
		<div className="uk-grid-collapse uk-child-width-1-2@m uk-height-1-1" uk-grid="">
			<div
				onClick={onCategoryNavClick}
				className="uk-height-1-1 uk-position-relative pointer"
			>
				<div className="colors uk-position-center">Category</div>
			</div>
			<div
				onClick={onProductNavClick}
				className="uk-height-1-1 uk-position-relative pointer"
			>
				<div className="colors uk-position-center">Product</div>
			</div>
		</div>
	);
}
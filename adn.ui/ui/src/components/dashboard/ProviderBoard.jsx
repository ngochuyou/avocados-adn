import {
	useContext, createContext, useReducer,
	useEffect, useState, useCallback, Fragment
} from 'react';
import { useParams } from 'react-router-dom';

import { routes } from '../../config/default';

import {
	SET_LIST, SET_VIEW, MODIFY_MODEL, SET_ERROR,
	FULFILL_PAGE, SWITCH_PAGE, TOGGLE_INDIVIDUAL_VIEW_VISION,
	SET_INDIVIDUAL_VIEW_TARGET, SET_LIST_ELEMENT,
	SET_FETCH_STATUS, SET_MODEL
} from '../../actions/common';
import {
	createProvider, fetchProviderList, fetchProviderCount,
	approveProvider, obtainProvider, updateProvider
} from '../../actions/provider';

import { useAuth } from '../../hooks/authentication-hooks';
import { useProvider } from '../../hooks/provider-hooks';

import Paging from '../utils/Paging.jsx';
import { ConfirmModal } from '../utils/ConfirmModal.jsx';
import { FullModal } from '../utils/Modal.jsx';
import { DomainImage } from '../utils/Gallery';
// import { NoFollow } from '../utils/Link';
import { PluralValueInput } from '../utils/PluralValueInput';
import SubmitCostForm from '../product/SubmitCostForm';
import CostList from '../product/CostList';

import Account from '../../models/Account';
import { Provider } from '../../models/Factor';

import {
	hasLength, isString, isObj, toMap, spread,
	isBool, formatDatetime, asIf
} from '../../utils';

import { server } from '../../config/default';

const ViewContext = createContext();
const useViewContext = () => useContext(ViewContext);

const VIEW_STORE = {
	currentView: <ProviderListView />
};

function ViewContextProvider({ children }) {
	const [viewStore, dispatchViewStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = viewDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...VIEW_STORE }
	);

	return (
		<ViewContext.Provider value={{
			viewStore, dispatchViewStore
		}}>
			{children}
		</ViewContext.Provider>
	);
}

const viewDispatchers = {
	SET_VIEW: (payload, oldState) => {
		if (payload == null) {
			return oldState;
		}

		return {
			...oldState,
			currentView: payload
		};
	}
};

const FormContext = createContext();
const useFormContext = () => useContext(FormContext);

const FORM_CONTEXT = {
	model: {
		name: "",
		email: "",
		address: "",
		phoneNumbers: [],
		representatorName: "",
		website: ""
	},
	errors: {}
}

function FormContextProvider({ children }) {
	const [formContext, dispatchFormContext] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = formDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...FORM_CONTEXT }
	);

	return (
		<FormContext.Provider value={{
			formContext, dispatchFormContext
		}}>
		{ children }
		</FormContext.Provider>
	);
}

const formDispatchers = {
	MODIFY_MODEL: (payload, oldState) => {
		const { name, value } = payload;

		if (!isString(name)) {
			return oldState;
		}

		return {
			...oldState,
			model: {
				...oldState.model,
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
	},
	SET_MODEL: (payload, oldState) => {
		if (!isObj(payload)) {
			return oldState;
		}

		return {
			...oldState,
			model: payload,
			errors: {}
		};
	}
};

const ListContext = createContext();
const useListContext = () => useContext(ListContext);

const LIST_STORE = {
	providers: {},
	providerCount: 0,
	fetchStatus: {},
	pagination: {
		page: 0,
		totalPages: 0,
		size: 10,
		fetchStatus: [],
		pageMap: {}
	},
	individualview: {
		vision: false,
		target: null
	}
};

const FETCHED_COLUMNS = [
	"id", "name", "email", "approvedTimestamp",
	"phoneNumbers", "representatorName"
];
const OBTAINED_COLUMNS = [
	"createdBy", "createdTimestamp",
	"updatedBy", "updatedTimestamp",
	"approvedBy", "approvedTimestamp",
	"address"
];
const OBTAINED_PRODUCT_DETAILS_COLUMNS = [
	"product", "price",
	"approvedBy", "approvedTimestamp",
	"droppedTimestamp"
];
const SET_PROVIDER_COUNT = "SET_PROVIDER_COUNT";

function ListContextProvider({ children }) {
	const [listStore, dispatchListStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = listDispatchers[type];
			
			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...LIST_STORE }
	);
	const {
		store: {
			elements: {
				wasInit: wasStoreInit,
				map: providers,
				total: providerCount
			}
		},
		push, setTotal
	} = useProvider();

	useEffect(() => {
		const doFetch = async () => {
			if (!wasStoreInit) {
				const [providerList, fetchProviderListErr] = await fetchProviderList({
					columns: FETCHED_COLUMNS
				});

				if (fetchProviderListErr) {
					console.error(fetchProviderListErr);
					return;
				}

				push(providerList);

				const [providerCount, fetchProviderCountErr] = await fetchProviderCount();

				if (fetchProviderCountErr) {
					console.error(fetchProviderCountErr);
					return;
				}

				setTotal(providerCount);

				dispatchListStore({
					type: SET_LIST,
					payload: providerList
				});
				dispatchListStore({
					type: SET_PROVIDER_COUNT,
					payload: providerCount
				});
				return;
			}
		};

		doFetch();
	}, [push, providers, wasStoreInit, providerCount, setTotal]);
	
	return (
		<ListContext.Provider value={{
			listStore, dispatchListStore
		}}>
		{ children }
		</ListContext.Provider>
	);
}

const listDispatchers = {
	SET_LIST: (payload, oldState) => {
		if (!Array.isArray(payload)) {
			return oldState;
		}

		const pageMap = { 0: payload };
		const { pagination } = oldState;

		return {
			...oldState,
			providers: toMap({
				array: payload,
				map: {},
				key: "id"
			}),
			pagination: { ...pagination, pageMap },
			fetchStatus: toMap({
				array: payload,
				map: {},
				key: "id",
				value: false
			})
		};
	},
	SET_PROVIDER_COUNT: (payload, oldState) => {
		if (isNaN(payload)) {
			return oldState;
		}

		const { pagination, pagination: { size } } = oldState;
		const totalPages = Math.ceil(payload / size);
		let fetchStatus = spread(totalPages, false);

		fetchStatus[0] = true;

		return {
			...oldState,
			providerCount: payload,
			pagination: { ...pagination, fetchStatus, totalPages }
		};
	},
	FULFILL_PAGE: (payload, oldState) => {
		const { page , list } = payload;

		if (isNaN(page) || !Array.isArray(list)) {
			return oldState;
		}

		const { providers, pagination, pagination: { pageMap, fetchStatus } } = oldState;

		return {
			...oldState,
			providers: toMap({
				array: list,
				map: { ...providers },
				key: "id"
			}),
			pagination: {
				...pagination,
				page,
				pageMap: {
					...pageMap,
					[page]: list
				},
				fetchStatus: fetchStatus.map((ele, index) => index !== page ? ele : true)
			}
		};
	},
	SWITCH_PAGE: (payload, oldState) => {
		if (isNaN(payload)) {
			return oldState;
		}

		const { pagination } = oldState;

		return {
			...oldState,
			pagination: { ...pagination, page: payload }
		};
	},
	TOGGLE_INDIVIDUAL_VIEW_VISION: (payload, oldState) => {
		const { individualview } = oldState;

		return {
			...oldState,
			individualview: {
				...individualview,
				vision: !individualview.vision
			}
		};
	},
	SET_INDIVIDUAL_VIEW_TARGET: (payload, oldState) => {
		if (!isString(payload)) {
			return oldState;
		}

		const { individualview } = oldState;

		return {
			...oldState,
			individualview: {
				...individualview,
				target: payload
			}
		};
	},
	SET_LIST_ELEMENT: (payload, oldState) => {
		if (!isObj(payload)) {
			return oldState;
		}

		const { id } = payload;
		const {
			pagination,
			pagination: { page }
		} = oldState;
		const newProviders = {...oldState.providers};
		const newPageMap = {...pagination.pageMap};

		newProviders[id] = payload;
		newPageMap[page] = newPageMap[page].map(ele => ele.id !== id ? ele : payload);

		return {
			...oldState,
			providers: newProviders,
			pagination: {
				...pagination,
				pageMap: newPageMap
			}
		};
	},
	SET_FETCH_STATUS: (payload, oldState) => {
		const { id, status } = payload;

		if (!isString(id) || !isBool(status)) {
			return oldState;
		}

		const newFetchStatus = {...oldState.fetchStatus};

		newFetchStatus[id] = status;

		return {
			...oldState,
			fetchStatus: newFetchStatus
		};
	}
};

export default function ProviderBoard() {
	return (
		<ViewContextProvider>
			<FormContextProvider>
				<ListContextProvider>
					<Navbar />
					<Main />
				</ListContextProvider>
			</FormContextProvider>
		</ViewContextProvider>
	);
}

function Main() {
	const {
		viewStore: { currentView },
		dispatchViewStore
	} = useViewContext();
	const { view } = useParams();

	useEffect(() => {
		const {
			dashboard: {
				provider: {
					list: { name: listViewName },
					new: { name: submitProviderViewName },
					costSubmit: { name: submitCostViewName },
					costList: { name: costListViewName }
				}
			}
		} = routes;

		switch (view) {
			case listViewName: {
				dispatchViewStore({
					type: SET_VIEW,
					payload: <ProviderListView />
				});
				return;
			}
			case submitProviderViewName: {
				dispatchViewStore({
					type: SET_VIEW,
					payload: <NewProviderView />
				});
				return;
			}
			case submitCostViewName: {
				dispatchViewStore({
					type: SET_VIEW,
					payload: <SubmitCostForm />
				});
				return;
			}
			case costListViewName: {
				dispatchViewStore({
					type: SET_VIEW,
					payload: <ProductCostListView />
				});
				return;
			}
			default: {
				dispatchViewStore({
					type: SET_VIEW,
					payload: <NewProviderView />
				});
				return;
			}
		}
	}, [view, dispatchViewStore]);

	return (
		<div className="uk-padding-small">
		{
			currentView
		}
		</div>
	);
}

function Navbar() {
	// const { dispatchViewStore } = useViewContext();

	return (
		<div style={{
			position: "relative",
			zIndex: "10"
		}} uk-sticky="sel-target: .nav-container; cls-active: uk-navbar-sticky; bottom: #transparent-sticky-navbar">
			<div className="nav-container uk-background-default">
				{/*<ul className="uk-box-shadow-small uk-child-width-expand" id="nav" uk-tab="" uk-height-match="">
					<li>
						<NoFollow
							href="#nav"
							style={{height: "30px"}}
							onClick={() => dispatchViewStore({
								type: SET_VIEW,
								payload: <ProviderListView />
							})}
						>Provider List</NoFollow>
					</li>
					<li>
						<NoFollow
							href="#nav"
							style={{height: "30px"}}
							onClick={(event) => dispatchViewStore({
								type: SET_VIEW,
								payload: <NewProviderView />
							})}
						>New Provider</NoFollow>
					</li>
					<li>
						<NoFollow
							href="#nav"
							style={{height: "30px"}}
							onClick={(event) => dispatchViewStore({
								type: SET_VIEW,
								payload: <SubmitCostForm />
							})}
						>Submit Cost</NoFollow>
					</li>
					<li>
						<NoFollow
							href="#nav"
							style={{height: "30px"}}
							onClick={(event) => dispatchViewStore({
								type: SET_VIEW,
								payload: <ProductCostListView />
							})}
						>Product Costs</NoFollow>
					</li>
				</ul>*/}
			</div>
		</div>
	);
}

function NewProviderView() {
	const { dispatchFormContext } = useFormContext();
	const onSubmit = async (model) => {
		const [res, err] = await createProvider(model);

		if (err) {
			dispatchFormContext({
				type: SET_ERROR,
				payload: err
			});
			return;
		}

		console.log(res);
	};

	return (
		<section>
			<h2 className="uk-heading-line colors"><span>New Provider</span></h2>
			<div className="uk-grid-small" uk-grid="">
				<div className="uk-width-3-4">
					<ProviderForm
						submit={onSubmit}
					/>
				</div>
				<div className="uk-width-1-4">
					
				</div>
			</div>
		</section>
	);
}

function ProviderForm({
	submit = () => null
}) {
	const {
		formContext: {
			model,
			errors
		},
		dispatchFormContext
	} = useFormContext();
	const onInputChange = (event) => {
		const { target } = event;

		dispatchFormContext({
			type: MODIFY_MODEL,
			payload: {
				name: target.name,
				value: target.value
			}
		});
	}
	const onAddPhoneNumber = useCallback((phoneNumber) => {
		if (model.phoneNumbers.includes(phoneNumber)) {
			return;
		}

		dispatchFormContext({
			type: MODIFY_MODEL,
			payload: {
				name: "phoneNumbers",
				value: [...model.phoneNumbers, phoneNumber]
			}
		});
	}, [dispatchFormContext, model.phoneNumbers]);
	const onRemovePhoneNumber = useCallback((index) => {
		dispatchFormContext({
			type: MODIFY_MODEL,
			payload: {
				name: "phoneNumbers",
				value: model.phoneNumbers.filter((ele, i) => i !== index)
			}
		});
	}, [dispatchFormContext, model.phoneNumbers]);
	const validate = (names) => {
		let result = {};
		let success = true;

		for (let name of names) {
			const [ok, err] = Provider.validator[name](model[name]);

			success = success && ok;
			result[name] = err;
		}

		dispatchFormContext({
			type: SET_ERROR,
			payload: result
		});

		return [success, result];
	};
	const onSubmit = async (event) => {
		event.preventDefault();
		event.stopPropagation();

		const [ok, ] = validate(["name", "email", "phoneNumbers", "address", "representatorName", "website"]);

		if (!ok) {
			return;
		}

		submit(model);
	};

	return (
		<form autoComplete="off" onSubmit={onSubmit}>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Provider Name</label>
				<input
					className="uk-input"
					placeholder="Provider Name"
					name="name"
					value={model.name}
					onChange={onInputChange}
				/>
				<label className="uk-text-danger">{errors.name}</label>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Email</label>
				<input
					className="uk-input"
					placeholder="Email"
					name="email"
					value={model.email}
					onChange={onInputChange}
					type="email"
				/>
				<label className="uk-text-danger">{errors.email}</label>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Phone Numbers</label>
				<PluralValueInput
					values={model.phoneNumbers}
					add={onAddPhoneNumber}
					remove={onRemovePhoneNumber}
					placeholder="New phone number"
					type="tel"
				/>
				<label className="uk-text-danger">{errors.phoneNumbers}</label>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Address</label>
				<input
					className="uk-input"
					placeholder="Address"
					name="address"
					value={model.address}
					onChange={onInputChange}
				/>
				<label className="uk-text-danger">{errors.address}</label>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Website</label>
				<input
					className="uk-input"
					placeholder="Website"
					name="website"
					value={model.website}
					onChange={onInputChange}
				/>
				<label className="uk-text-danger">{errors.website}</label>
			</div>
			<div className="uk-margin">
				<label className="uk-label backgroundf">Representator Name</label>
				<input
					className="uk-input"
					placeholder="Representator Name"
					name="representatorName"
					value={model.representatorName}
					onChange={onInputChange}
				/>
				<label className="uk-text-danger">{errors.representatorName}</label>
			</div>
			<div className="uk-margin">
				<button
					className="uk-button backgroundf"
					type="submit"
				>
					Submit
				</button>
			</div>
		</form>
	);
}

function ProviderListView() {
	const {
		listStore: {
			pagination: {
				fetchStatus: pagesFetchStatus,
				pageMap,
				totalPages,
				page: currentPage,
				size: pageSize
			},
			individualview: {
				vision: providerIndividualViewVision
			}
		}, 
		dispatchListStore
	} = useListContext();
	const { push: pushProviderStore } = useProvider();
	const viewProvider = (providerId) => {
		dispatchListStore({ type: TOGGLE_INDIVIDUAL_VIEW_VISION });
		dispatchListStore({
			type: SET_INDIVIDUAL_VIEW_TARGET,
			payload: providerId
		});
	};
	const requestPage = async (pageNumber) => {
		const page = pageNumber - 1;

		if (pagesFetchStatus[page] === true) {
			dispatchListStore({
				type: SWITCH_PAGE,
				payload: page
			});

			return;
		}

		const [providerList, err] = await fetchProviderList({
			page,
			size: pageSize,
			columns: FETCHED_COLUMNS
		});

		if (err) {
			console.error(err);
			return;
		}

		dispatchListStore({
			type: FULFILL_PAGE,
			payload: { page, list: providerList }
		});
		pushProviderStore(providerList);
	};
	const currentList = pageMap[currentPage];

	return (
		<section>
			<table className="uk-table uk-table-justify uk-table-divider">
 				<thead>
 					<tr>
 						<th className="uk-width-small">Name</th>
 						<th>Email</th>
 						<th>Phone</th>
 						<th>Approval status</th>
 						<th></th>
 					</tr>
 				</thead>
 				<tbody>
 				{
 					currentList && currentList.map((provider, index) => (
 						<tr key={provider.id}>
 							<td>
 								<span className="uk-text-bold colors">
 									{ provider.name }
 								</span>
 							</td>
 							<td>
 							{ provider.email }
 							</td>
 							<td>
 								<ul className="uk-list">
 								{
 									(provider.phoneNumbers && provider.phoneNumbers.map((phone, index) => (
 										<li key={index}>{phone}</li>
 									)))
 								}
 								</ul>
 							</td>
 							<td>
 							{
 								provider.approvedTimestamp == null ? (
 									<span className="uk-text-danger uk-text-bold">Pending</span>
 								) : (
 									<span className="uk-text-success uk-text-bold">Approved</span>
 								)
 							}
 							</td>
 							<td>
 								<div
 									className="pointer uk-icon-button"
 									uk-icon="info"
 									onClick={() => viewProvider(provider.id)}
 								></div>
 							</td>
 						</tr>
 					))
 				}
 				</tbody>
 			</table>
			<Paging
				amount={totalPages}
				amountPerChunk={5}
				selected={currentPage}
				onPageSelect={requestPage}
			/>
			{
				providerIndividualViewVision ? (
					<ProviderIndividualView />
				) : null
			}
		</section>
	);
}

function ProviderIndividualView() {
	const { principal } = useAuth();
	const {
		listStore: {
			fetchStatus,
			providers,
			individualview: {
				target
			}
		},
		dispatchListStore
	} = useListContext();
	const {
		formContext: {
			model: formModel
		},
		dispatchFormContext
	} = useFormContext();
	const model = providers[target];

	useEffect(() => {
		const doFetch = async () => {
			const { id } = model;

			if (fetchStatus[id] === true) {
				return;
			}

			const [res, err] = await obtainProvider({
				id,
				columns: OBTAINED_COLUMNS,
				productDetailsColumns: OBTAINED_PRODUCT_DETAILS_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			dispatchListStore({
				type: SET_FETCH_STATUS,
				payload: { id, status: true }
			});
			dispatchListStore({
				type: SET_LIST_ELEMENT,
				payload: { ...model, ...res }
			});
		};

		doFetch();
	}, [model, fetchStatus, dispatchListStore]);

	const [isEditing, setEditing] = useState(false);
	const [confirmModalVision, setConfirmModalVision] = useState(false);

	if (model == null) {
		return null;
	}

	const close = () => dispatchListStore({ type: TOGGLE_INDIVIDUAL_VIEW_VISION });
	const approve = async () => {
		const [, err] = await approveProvider(model.id);

		if (err) {
			console.error(err);
			return;
		}

		dispatchListStore({
			type: SET_LIST_ELEMENT,
			payload: {
				...model,
				approved: true,
				approvedBy: principal,
				approvedTimestamp: new Date().toString()
			}
		});
		setConfirmModalVision(false);
	};
	const onEdit = () => {
		dispatchFormContext({
			type: SET_MODEL,
			payload: {...model}
		});
		setEditing(true);
	};
	const onSubmit = async (model) => {
		setEditing(false);

		const [res, err] = await updateProvider(model);

		if (err) {
			dispatchFormContext({
				type: SET_ERROR,
				payload: err
			});
			setEditing(true);
			return;
		}

		dispatchListStore({
			type: SET_LIST_ELEMENT,
			payload: {...formModel, ...res}
		});
	};

	return (
		<FullModal close={close}>
			{
				confirmModalVision ? (
					<ConfirmModal
						message={
							<span>
								Are you sure you want to approve
								<span className="uk-text-danger uk-margin-small-left">
									{`${model.name}`}
								</span>
								?
							</span>
						}
						onNo={() => setConfirmModalVision(false)}
						onYes={approve}
					/>
				) : null
			}
			<div className="uk-grid-small" uk-grid="">
				<div className="uk-width-2-5">
				{
					!isEditing ? (
						<Fragment>
							{
								model.approvedTimestamp == null ? (
									<label className="uk-label uk-label-danger">
										Pending approval
									</label>
								) : (
									<label className="uk-label uk-label-success">
										Approved
									</label>
								)
							}
							<h1 className="uk-heading uk-margin-remove-top">
								{model.name}
							</h1>
							<div className="uk-margin">
								<label
									className="uk-label backgroundf"
								>Email</label>
								<div
									className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
								>{model.email}</div>
							</div>
							<div className="uk-margin">
								<label
									className="uk-label backgroundf"
								>Phone Numbers</label>
								<div
									className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
								>
									<ul className="uk-list uk-list-decimal uk-list-divider">
									{
										model.phoneNumbers.map((phoneNumber, index) => (
											<li key={index}>{phoneNumber}</li>
										))
									}
									</ul>
								</div>
							</div>
							<div className="uk-margin">
								<label
									className="uk-label backgroundf"
								>Address</label>
								<div
									className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
								>{model.address}</div>
							</div>
							<div className="uk-margin">
								<label
									className="uk-label backgroundf"
								>Website</label>
								{
									!hasLength(model.website) ? (
										<div className="uk-text-muted uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom">Unknown</div>
									) : (
										<div
											className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
										>{model.website}</div>
									)
								}
							</div>
							<div className="uk-margin">
								<label
									className="uk-label backgroundf"
								>Representator Name</label>
								{
									!hasLength(model.representatorName) ? (
										<div className="uk-text-muted uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom">Unknown</div>
									) : (
										<div
											className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
										>{model.representatorName}</div>
									)
								}
							</div>
							<hr className="uk-divider-icon" />
							<div className="uk-margin uk-grid-small uk-child-width-1-2" uk-grid="">
								<div>
									<label className="uk-label backgroundf">Created by</label>
									<p>
									{
										asIf(model.createdBy != null)
										.then(() => `${model.createdBy.lastName} ${model.createdBy.firstName}`)
										.else(() => null)
									}
									</p>
								</div>
								<div>
									<label className="uk-label backgroundf">Created Timestamp</label>
									<p>{formatDatetime(model.createdTimestamp)}</p>
								</div>
							</div>
							<div className="uk-margin uk-grid-small uk-child-width-1-2" uk-grid="">
								<div>
									<label className="uk-label backgroundf">Last updated by</label>
									<p>
									{
										asIf(model.updatedBy != null)
										.then(() => `${model.updatedBy.lastName} ${model.updatedBy.firstName}`)
										.else(() => null)
									}
									</p>
								</div>
								<div>
									<label className="uk-label backgroundf">Last Updated Timestamp</label>
									<p>{formatDatetime(model.updatedTimestamp)}</p>
								</div>
							</div>
							<div className="uk-margin uk-grid-small uk-child-width-1-2" uk-grid="">
								<div>
									<label className="uk-label backgroundf">Approved by</label>
									<p>
									{
										asIf(model.approvedBy != null)
										.then(() => `${model.approvedBy.lastName} ${model.approvedBy.firstName}`)
										.else(() => null)
									}
									</p>
								</div>
								<div>
									<label className="uk-label backgroundf">Approved Timestamp</label>
									<p>{formatDatetime(model.approvedTimestamp)}</p>
								</div>
							</div>
							<div className="uk-margin">
							{
								principal.role === Account.Role.HEAD ? (
									model.approvedTimestamp == null ? (
										<button
											className="uk-button backgroundf uk-margin-small-right"
											onClick={() => setConfirmModalVision(true)}
										>Approve Provider</button>
									) : null
								) : null
							}
								<button
									className="uk-button backgroundf"
									onClick={onEdit}
								>Edit</button>
							</div>
						</Fragment>
					) : (
						<Fragment>
							<h2 className="uk-heading uk-heading-line">
								<span>{`Edit ${model.name}`}</span>
							</h2>
							<ProviderForm
								submit={onSubmit}
							/>
						</Fragment>
					)
				}
				</div>
				<div className="uk-width-3-5 uk-height-1-1 uk-overflow-auto">
					<h4 className="uk-heading uk-heading-line">
						<span>Products</span>
					</h4>
					<div className="uk-overflow-auto">
					{
						!hasLength(model.productDetails) ? (
							<p className="uk-text-muted">Currently not providing any products</p>
						) : (
							<table className="uk-table uk-table-hover uk-table-middle uk-table-divider">
								<thead>
									<tr>
										<th className="uk-table-shrink"></th>
										<th className="uk-table-expand">Product Code</th>
										<th className="uk-table-expand">Product Name</th>
										<th className="uk-table-expand">Cost</th>
										<th className="uk-table-expand">Product Price</th>
									</tr>
								</thead>
								<tbody>
								{
									model.productDetails.map((detail, index) => {
										const { product } = detail;

										return (
											<tr key={index}>
												<td>
													<div style={{width: "60px", height: "60px"}}>
														<DomainImage
															url={`${server.images.product}/${product.images[0]}`}
															fit="contain"
														/>
													</div>
												</td>
												<td className="uk-text-bold">{product.id}</td>
												<td>{product.name}</td>
												<td className="uk-text-large">{detail.price}</td>
												<td className="uk-text-large uk-text-primary">{product.price}</td>
											</tr>
										);
									})
								}
								</tbody>
							</table>
						)
					}
					</div>
				</div>
			</div>
		</FullModal>
	);
}

function ProductCostListView() {
	return (
		<section>
			<CostList />
		</section>
	);
}
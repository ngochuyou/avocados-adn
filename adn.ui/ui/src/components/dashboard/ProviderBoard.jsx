import { useReducer, useEffect, useState } from 'react';

import {
	fetchProviderList, fetchProviderCount,
	obtainProvider
} from '../../actions/provider';
import { TOGGLE_MODAL_VISION, SET_MODEL } from '../../actions/common';

import { spread, toMap, isBool, isString } from '../../utils';

import Paging from '../utils/Paging.jsx';
import Navbar from './Navbar.jsx';

const STORE = {
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
	view: {
		visible: false,
		model: null
	}
};

const SEARCH_STORE = {
	totalResults: 0,
	list: [],
	isSearching: false
};

const SET_SEARCH_RESULTS_AND_SEARCHING_STATE = "SET_SEARCH_RESULTS_AND_SEARCHING_STATE";

const NAVBAR_STORE = {
	searchInput: {
		disabled: false
	},
	backButton: {
		visible: false,
		callback: () => null
	}
};

const TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY = "TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY";
const SET_NAVBAR_BACKBUTTON_CALLBACK_AND_VISIBILITY = "SET_NAVBAR_BACKBUTTON_CALLBACK_AND_VISIBILITY";

const FETCHED_COLUMNS = [
	"id", "name", "email",
	"phoneNumbers", "representatorName"
];
const OBTAINED_COLUMNS = [
	"website", "address",
	"productDetails"
]

const SET_PROVIDER_LIST = "SET_PROVIDER_LIST";
const SET_PROVIDER_COUNT = "SET_PROVIDER_COUNT";
const FULFILL_PAGE_REQUEST = "FULFILL_PAGE_REQUEST";
const SWITCH_PAGE = "SWITCH_PAGE";

const fetchStore = async (dispatch) => {
	const [providerList, fetchProviderListErr] = await fetchProviderList({
		columns: FETCHED_COLUMNS
	});

	if (fetchProviderListErr) {
		console.error(fetchProviderListErr);
		return;
	}

	dispatch({
		type: SET_PROVIDER_LIST,
		payload: providerList
	});

	const [providerCount, fetchProviderCountErr] = await fetchProviderCount();

	if (fetchProviderCountErr) {
		console.error(fetchProviderCountErr);
		return;
	}

	dispatch({
		type: SET_PROVIDER_COUNT,
		payload: providerCount
	});
}

export default function ProviderBoard({
	registerNavbarSearchInputCallback = () => null,	
	dispatchBackButton = () => null
}) {
	const [ store, dispatchStore ] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch(type) {
				case FULFILL_PAGE_REQUEST: {
					const { page , list } = payload;

					if (typeof page !== 'number' || !Array.isArray(list)) {
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
					}
				}
				case SWITCH_PAGE: {
					if (typeof payload !== 'number') {
						return oldState;
					}

					const { pagination } = oldState;

					return {
						...oldState,
						pagination: { ...pagination, page: payload }
					};
				}
				case SET_PROVIDER_LIST: {
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
				}
				case SET_MODEL: {
					if (!isString(payload)) {
						return oldState;
					}

					const { view } = oldState;

					return {
						...oldState,
						view: { ...view, model: payload }
					};
				}
				case TOGGLE_MODAL_VISION: {
					if (!isBool(payload)) {
						return oldState;
					}

					const { view } = oldState;

					return {
						...oldState,
						view: { ...view, visible: payload }
					};
				}
				case SET_PROVIDER_COUNT: {
					if (typeof payload !== 'number') {
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
				}
				default: return oldState;
			}
		}, { ...STORE }
	);
	const [ navbarState, dispatchNavbarState ] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch(type) {
				case SET_NAVBAR_BACKBUTTON_CALLBACK_AND_VISIBILITY: {
					const { callback, visible } = payload;

					if (typeof callback !== 'function' || typeof visible !== 'boolean') {
						return oldState;
					}

					const { backButton } = oldState;

					return {
						...oldState,
						backButton: { ...backButton, callback, visible }
					};
				}
				case TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY: {
					const { searchInput } = oldState;

					return {
						...oldState,
						searchInput: {
							...searchInput,
							disabled: !searchInput.disabled
						}
					}
				}
				default: return oldState;
			}
		}, { ...NAVBAR_STORE }
	);
	const [ searchState, dispatchSearchState] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch(type) {
				case SET_SEARCH_RESULTS_AND_SEARCHING_STATE: {
					const { list, isSearching } = payload;

					if (!Array.isArray(list) || typeof isSearching !== 'boolean') {
						return oldState;
					}

					return {
						...oldState,
						totalResults: list.length,
						list: list,
						isSearching: isSearching
					};
				}
				default: return oldState;
			}
		}, { ...SEARCH_STORE }
	);
	const searchProvidersByName = (providerName) => {
		dispatchNavbarState({ type: TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY });
		
		if (providerName.length === 0) {
			clearSearchResults();
			dispatchNavbarState({ type: TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY });
			return;
		} 

		let list = Object.values(store.providers).filter(provider => provider.name.toLowerCase().includes(providerName.toLowerCase()));

		dispatchSearchState({
			type: SET_SEARCH_RESULTS_AND_SEARCHING_STATE,
			payload: { list, isSearching: true }
		});
		dispatchNavbarState({
			type: SET_NAVBAR_BACKBUTTON_CALLBACK_AND_VISIBILITY,
			payload: {
				visible: true,
				callback: () => clearSearchResults()
			}
		});
		dispatchNavbarState({ type: TOGGLE_NAVBAR_SEARCHINPUT_DISABILITY });
	};
	const clearSearchResults = () => {
		dispatchSearchState({
			type: SET_SEARCH_RESULTS_AND_SEARCHING_STATE,
			payload: { list: [], isSearching: false }
		});
		dispatchNavbarState({
			type: SET_NAVBAR_BACKBUTTON_CALLBACK_AND_VISIBILITY,
			payload: {
				visible: false,
				callback: () => null
			}
		});
	}

	useEffect(() => fetchStore(dispatchStore), []);

	const requestProviderListPage = async (pageNumber) => {
		const { pagination: { fetchStatus, size } } = store;
		const page = pageNumber - 1;

		if (fetchStatus[page] === true) {
			dispatchStore({
				type: SWITCH_PAGE,
				payload: page
			});

			return;
		}

		const [providerList, err] = await fetchProviderList({ page, size, columns: FETCHED_COLUMNS });

		if (err) {
			console.error(err);
			return;
		}

		dispatchStore({
			type: FULFILL_PAGE_REQUEST,
			payload: { page, list: providerList }
		});
	};
	const toggleView = (id, visible) => {
		dispatchStore({
			type: SET_MODEL,
			payload: id
		});
		dispatchStore({
			type: TOGGLE_MODAL_VISION,
			payload: visible
		});
	}
	const selectProvider = async (id) => {
		if (fetchStatus[id] == true) {
			toggleView(id, true);
			return;
		}

		const [provider, err] = await obtainProvider({ providerId: id, columns: OBTAINED_COLUMNS });

		if (err) {
			console.error(err);
			return;
		}

		console.log(provider);
		toggleView(id, true);
	};
	const {
		providers,
		providerCount,
		pagination: { page, pageMap, totalPages },
		fetchStatus,
		view: {
			visible: isViewVisible,
			model: viewedModelId
		}
	} = store;
	const {
		searchInput: {
			disabled: navbarSearchInputDisabled
		},
		backButton: {
			visible: navbarBackButtonVisible,
			callback: navbarBackButtonCallback
		}
	} = navbarState;
	const { isSearching, list: searchResults, totalResults } = searchState;

	return (
		<div>
			<Navbar
				backButtonVisible={navbarBackButtonVisible}
				backButtonClick={navbarBackButtonCallback}
				searchInputEmptied={clearSearchResults}
				searchInputEntered={searchProvidersByName}
				isSearchInputDisabled={navbarSearchInputDisabled}
			/>
			<div className="uk-padding uk-padding-remove-top">
			{
				!isViewVisible ?
					!isSearching ? 
						<ProviderList
							list={pageMap[page]}
							totalAmount={providerCount}
							currentPage={page + 1}
							requestPage={requestProviderListPage}
							totalPages={totalPages}
							onSelect={selectProvider}
						/> :
						<ProviderList
							list={searchResults}
							totalAmount={totalResults}
							currentPage={1}
							totalPages={1}
							onSelect={selectProvider}
						/> :
					<ProviderView
						providers={providers}
						providerId={viewedModelId}
						close={() => dispatchStore({
							type: TOGGLE_MODAL_VISION,
							payload: false
						})}
					/>
			}	
			</div>
		</div>
	);
}

function ProviderList({
	list = [], totalAmount = 0,
	currentPage = 0, requestPage = () => null,
	totalPages = 0, onSelect = () => null
}) {
	return (
		<div>
			<table className="uk-table uk-table-justify uk-table-divider">
				<thead>
					<tr>
						<th className="uk-width-small">Name</th>
						<th>Email</th>
						<th>Phone</th>
						<th>Representator</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
				{
					list.map((provider, index) => (
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
							{ provider.representatorName }
							</td>
							<td>
								<div
									className="pointer uk-icon-button"
									uk-icon="info"
									onClick={() => onSelect(provider.id)}
								></div>
							</td>
						</tr>
					))
				}
				</tbody>
			</table>
			<div
				className="uk-grid-collapse"
				uk-grid=""
			>
				<div className="uk-width-expand">
					<Paging
						amount={totalPages}
						amountPerChunk={5}
						selected={currentPage}
						onPageSelect={requestPage}
					/>
				</div>
				<div className="uk-width-auto">
					<div
						className="uk-position-relative uk-height-1-1"
						style={{ minWidth: "150px" }}
					>
						<label
							className="uk-position-center backgroundf uk-label"
						>{`${list.length}/${totalAmount} result(s)`}</label>	
					</div>
				</div>
			</div>
		</div>
	);
}

function ProviderView({
	providers = [], providerId = null,
	close = () => null
}) {
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const model = providers[providerId];

	if (model == null) {
		return null;
	}

	const onClose = () => {
		setContainerClassName('fade-out uk-modal');
		setTimeout(() => close(), 180);
	}

	return (
		<div className={`uk-modal-full uk-open uk-display-block ${containerClassName}`} uk-modal="">
			<div className="uk-modal-dialog uk-height-1-1 uk-padding">
				<button
					className="uk-position-top-right uk-close-large"
					type="button" uk-close="" style={{top: "25px", right: "25px"}}
					onClick={onClose}
				></button>
				<div className="uk-grid-small uk-height-1-1" uk-grid="">
					<div className="uk-width-1-3">
						<h1 className="uk-heading">{model.name}</h1>
						<div className="uk-margin">
							<label
								className="uk-label backgroundf"
							>Representator Name</label>
							<div
								className="uk-text-large uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom"
							>{model.representatorName}</div>
						</div>
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
								<ul class="uk-list uk-list-decimal uk-list-divider">
								{
									model.phoneNumbers.map((phoneNumber, index) => (
										<li key={index}>{phoneNumber}</li>
									))
								}
								</ul>
							</div>
						</div>
					</div>
					<div className="uk-width-2-3 uk-height-1-1 uk-overflow-auto">
						
					</div>
				</div>
			</div>
		</div>
	);
}
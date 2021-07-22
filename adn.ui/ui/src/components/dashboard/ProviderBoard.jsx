import { useReducer, useEffect } from 'react';

import { fetchProviderList, fetchProviderCount } from '../../actions/provider';
import { spread, toMap } from '../../utils';

import Paging from '../utils/Paging.jsx';
import Navbar from './Navbar.jsx';

const STORE = {
	providers: {},
	providerCount: 0,
	pagination: {
		page: 0,
		totalPages: 0,
		size: 10,
		fetchStatus: [],
		pageMap: {}
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
						providers: toMap(list, { ...providers }, "id"),
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
						providers: toMap(payload, {}, "id"),
						pagination: { ...pagination, pageMap }
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
	const { providerCount, pagination: { page, pageMap, totalPages } } = store;
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
				!isSearching ? 
					<ProviderList
						list={pageMap[page]}
						totalAmount={providerCount}
						currentPage={page + 1}
						requestPage={requestProviderListPage}
						totalPages={totalPages}
					/> :
					<ProviderList
						list={searchResults}
						totalAmount={totalResults}
						currentPage={1}
						totalPages={1}
					/>
			}	
			</div>
		</div>
	);
}

function ProviderList({
	list = [], totalAmount = 0,
	currentPage = 0, requestPage = () => null,
	totalPages = 0
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
					</tr>
				</thead>
				<tbody>
				{
					list.map(provider => (
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
import { useReducer, useEffect } from 'react';

import { fetchProviderList, fetchProviderCount } from '../../actions/provider';
import { spread, toMap } from '../../utils';

import Paging from '../utils/Paging.jsx';

const STORE = {
	providers: {},
	providerCount: 0,
	pagination: {
		page: 0,
		totalPage: 0,
		size: 10,
		fetchStatus: [],
		pageMap: {}
	}
};

const FETCH_COLUMNS = [
	"id", "name", "email",
	"phoneNumbers", "representatorName"
];

const SET_PROVIDER_LIST = "SET_PROVIDER_LIST";
const SET_PROVIDER_COUNT = "SET_PROVIDER_COUNT";
const FULFILL_PAGE_REQUEST = "FULFILL_PAGE_REQUEST";
const SWITCH_PAGE = "SWITCH_PAGE";

const fetchStore = async (dispatch) => {
	const [providerList, fetchProviderListErr] = await fetchProviderList({
		columns: FETCH_COLUMNS
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

		const [providerList, err] = await fetchProviderList({ page, size });

		if (err) {
			console.error(err);
			return;
		}

		dispatchStore({
			type: FULFILL_PAGE_REQUEST,
			payload: {
				page,
				list: providerList
			}
		});
	};
	const { providerCount, pagination: { page, pageMap, size, totalPages } } = store;
	console.log("render board");
	return (
		<div>
			<div className="uk-padding-small">
				<ProviderList
					list={pageMap[page]}
					totalAmount={providerCount}
					amountPerPage={size}
					currentPage={page + 1}
					requestPage={requestProviderListPage}
					totalPages={totalPages}
				/>
			</div>
		</div>
	);
}

function ProviderList({
	list = [], totalAmount = 0, amountPerPage = 1,
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
									provider.phoneNumbers.map((phone, index) => (
										<li key={index}>{phone}</li>
									))
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
			<Paging
				amount={totalPages}
				amountPerChunk={5}
				selected={currentPage}
				onPageSelect={requestPage}
			/>
		</div>
	);
}
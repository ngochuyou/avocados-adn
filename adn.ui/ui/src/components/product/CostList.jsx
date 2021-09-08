import {
	useState, useEffect, useContext, createContext, useCallback,
	useReducer
} from 'react';

import {
	getProductDetailList, getProductDetailsCount,
	getProductDetailsByProduct, approveProductDetail
} from '../../actions/provider';
import { TOGGLE_INDIVIDUAL_VIEW_VISION, SET_MODEL } from '../../actions/common';

import { DomainProductImage } from '../utils/Gallery';
import { SearchInput } from '../utils/Input';
import { FullModal } from '../utils/Modal';
import { ConfirmModal } from '../utils/ConfirmModal';
import { AlertBox } from '../utils/Alert';

import { useToggle, useDispatch } from '../../hooks/hooks';
import {
	useProductCost, resolveDetailId,
	extractProductIdFromDetailId
} from '../../hooks/provider-hooks';

import { optional, asIf, formatVND, hasLength, isString, isEmpty } from '../../utils';

const FETCHED_COLUMNS = [
	"id", "product", "provider", "price",
	"approvedBy", "approvedTimestamp"
];
const OBTAINED_COLUMNS = [
	"id", "provider", "price",
	"approvedTimestamp"
];

const FILTER_APPROVAL_STATUS_ANY = "ANY";
const FILTER_APPROVAL_STATUS_APPROVED = "APPROVED";
const FILTER_APPROVAL_STATUS_PENDING = "PENDING";

const IndividualViewContext = createContext();

const useIndividualViewContext = () => useContext(IndividualViewContext);

const INDIVIDUAL_VIEW_STORE = {
	target: null,
	isVisible: false
};

const individualViewDispatchers = {
	TOGGLE_INDIVIDUAL_VIEW_VISION: (payload, oldState) => {
		const { isVisible } = oldState;

		return {
			...oldState,
			isVisible: !isVisible
		};
	},
	SET_MODEL: (payload, oldState) => {
		return {
			...oldState,
			target: payload
		}
	}
}

function IndividualViewContextProvider({ children }) {
	const [store, dispatch] = useDispatch(INDIVIDUAL_VIEW_STORE, individualViewDispatchers);
	const toggleVision = useCallback(() => dispatch({ type: TOGGLE_INDIVIDUAL_VIEW_VISION }), [dispatch]);
	const setTarget = useCallback((id) => {
		if (!isString(id) || !hasLength(id)) {
			return;
		}

		dispatch({
			type: SET_MODEL,
			payload: id
		});
	}, [dispatch]);

	return (
		<IndividualViewContext.Provider value={{
			store, toggleVision, setTarget
		}}>
		{ children }
		</IndividualViewContext.Provider>
	);
}

export default function CostList() {
	return (
		<IndividualViewContextProvider>
			<Main></Main>
		</IndividualViewContextProvider>
	);
}

function Main() {
	const [isSearchInputDisabled, ] = useToggle();
	const [isFetching, toggleFetching] = useToggle();
	const [costsView, setCostsView] = useState(null);
	const [currentPage, setCurrentPage] = useState(0);
	const [filter, setFilter] = useState({
		approvalStatus: FILTER_APPROVAL_STATUS_ANY
	});
	const [isConfirmModalVisible, toggleConfirmModalVision] = useToggle();
	const [toBeApprovedModel, setToBeApprovedModel] = useState(null);
	const [noti, setNoti] = useState(null);
	const {
		store: {
			elements: {
				wasInit: wasStoreInit,
				map: costs,
				total: totalCosts
			}
		},
		push, setTotal, update
	} = useProductCost();
	const {
		store: { isVisible: isIndividualViewVisible },
		toggleVision: toggleIndividualViewVision,
		setTarget: setIndividualViewTarget
	} = useIndividualViewContext();
	const costLists = Object.values(costs);

	useEffect(() => {
		const doFetch = async () => {
			if (!wasStoreInit) {
				const [detailList, fetchListErr] = await getProductDetailList({
					columns: FETCHED_COLUMNS
				});

				if (fetchListErr) {
					console.error(fetchListErr);
					return;
				}

				push(detailList);

				const [detailsCount, fetchCountErr] = await getProductDetailsCount();

				if (fetchCountErr) {
					console.error(fetchCountErr);
					return;
				}

				setTotal(detailsCount);
			}
		};

		doFetch();
	}, [wasStoreInit, push, setTotal]);
	const requestPage = async () => {
		toggleFetching();

		const nextPage = currentPage + 1;
		const [res, err] = await getProductDetailList({
			columns: FETCHED_COLUMNS,
			page: nextPage
		});

		if (err) {
			console.error(err);
			return;
		}

		push(res);
		setCurrentPage(nextPage);
		toggleFetching();
	};
	const renderedCosts = costsView != null ? costsView : costLists;
	const onFilterChange = (event) => {
		const { name, value } = event.target;
		const newFilter = {
			...filter,
			[name]: value
		};

		setFilter(newFilter);
		// setCostsView(costLists.filter());
	};
	const lookUpCurrentStore = (keyword) => costLists.filter(cost => cost.product.id.toLowerCase().includes(keyword) || cost.provider.name.toLowerCase().includes(keyword))
	const onSearchInputChange = (keyword) => {
		if (keyword.length === 0) {
			setCostsView(null);
			return;
		}

		setCostsView(lookUpCurrentStore(keyword.toLowerCase()));
	};
	const onSearch = (value) => {
		console.log(value);
	};
	const viewIndividual = (cost) => {
		setIndividualViewTarget(resolveDetailId(cost));
		toggleIndividualViewVision();
	};
	const approve = async () => {
		const [res, err] = await approveProductDetail({
			productId: toBeApprovedModel.id.productId,
			providerId: toBeApprovedModel.id.providerId,
		});

		if (err) {
			console.error(err);
			setNoti(err.result);
			toggleConfirmModalVision();
			return;
		}

		setNoti("Successfully approved cost");
		toggleConfirmModalVision();
		update([{
			...toBeApprovedModel,
			approvedTimestamp: new Date()
		}]);
	};
	const onApprove = (cost) => {
		setToBeApprovedModel(cost);
		toggleConfirmModalVision();
	};
	const remainingCostsAmount = totalCosts - costLists.length;

	return (
		<div className="uk-grid-collapse" uk-grid="">
		{
			asIf(isConfirmModalVisible)
			.then(() => (
				<ConfirmModal
					message={`Are you sure you want to approve ${toBeApprovedModel.provider.name}'s cost for ${toBeApprovedModel.product.id}?`}
					onYes={approve}
					onNo={toggleConfirmModalVision}
				/>
			))
			.else(() => null)
		}
			<div className="uk-width-3-4">
				<AlertBox
					message={noti}
					onClose={() => setNoti(null)}
				/>
				<SearchInput
					disabled={isSearchInputDisabled}
					placeholder="Product code or Provider name"
					onChange={onSearchInputChange}
					onEntered={onSearch}
					onSearchBtnClick={onSearch}
				/>
				<h2 className="uk-heading-line uk-margin-small-top">
					<span>Product Costs</span>
				</h2>
				<table className="uk-table uk-table-hover uk-table-middle uk-table-divider">
					<thead>
						<tr>
							<th className="uk-table-shrink"></th>
							<th>Product code</th>
							<th>Provider name</th>
							<th className="uk-table-expand">Cost (VND)</th>
							<th>Approval Status</th>
							<th className="uk-table-shrink"></th>
						</tr>
					</thead>
					<tbody>
					{
						renderedCosts.map((cost, index) => {
							const { product } = cost;
							const isApproved = cost.approvedTimestamp != null;

							return (
								<tr key={index}>
									<td>
										<div style={{width: "70px"}}>
											<DomainProductImage
												name={product.images[0]}
											/>
										</div>
									</td>
									<td>
										<span>{cost.product.id}</span>
									</td>
									<td>
										<span>{cost.provider.name}</span>
									</td>
									<td>
										<span className="uk-text-lead">{formatVND(cost.price)}</span>
									</td>
									<td className="uk-text-bold">
									{
										asIf(isApproved)
										.then(() => <span className="uk-text-success">Approved</span>)
										.else(() => <span className="uk-text-danger">Pending</span>)
									}
									</td>
									<td>
									{
										asIf(isApproved)
										.then(() => null)
										.else(() => (
											<div className="uk-icon-button pointer" uk-icon="check"
												onClick={() => onApprove(cost)}
											></div>
										))
									}
										<div className="uk-icon-button pointer" uk-icon="info"
											onClick={() => viewIndividual(cost)}
										></div>
									</td>
								</tr>
							);
						})
					}	
					</tbody>
				</table>
				{
					asIf(isIndividualViewVisible)
					.then(() => <IndividualView />)
					.else(() => null)
				}
				{
					asIf(costsView == null && !isFetching && remainingCostsAmount > 0)
					.then(() => (
						<div className="uk-position-relative" style={{height: "50px"}}>
							<div
								className="uk-position-center pointer"
								onClick={() => requestPage()}
							>
								<div
									className="uk-icon-button"
									uk-icon="more"
									uk-tooltip={`Show ${remainingCostsAmount} more`}
								></div>
							</div>
						</div>
					))
					.else(() => <span className="uk-text-meta">End of this list</span>)
				}
			</div>
			<div className="uk-width-1-4 uk-padding-small uk-padding-remove-top">
				<h4 className="uk-heading-line"><span>Filter</span></h4>
				<div className="uk-margin">
					<label className="uk-label backgroundf">Approval status</label>
					<div className="uk-margin uk-grid-small uk-child-width-auto uk-grid">
						<label>
							<input
							className="uk-radio"
							type="radio" name="approvalStatus" 
							value={FILTER_APPROVAL_STATUS_APPROVED} onChange={onFilterChange}
							checked={filter.approvalStatus === FILTER_APPROVAL_STATUS_APPROVED}/> Approved
						</label>
						<label>
							<input
							className="uk-radio"
							type="radio" name="approvalStatus" 
							value={FILTER_APPROVAL_STATUS_PENDING} onChange={onFilterChange}
							checked={filter.approvalStatus === FILTER_APPROVAL_STATUS_PENDING}/> Pending
						</label>
						<label>
							<input
							className="uk-radio"
							type="radio" name="approvalStatus" 
							value={FILTER_APPROVAL_STATUS_ANY} onChange={onFilterChange}
							checked={filter.approvalStatus === FILTER_APPROVAL_STATUS_ANY}/> Any
						</label>
					</div>
				</div>
			</div>
		</div>
	);
}

function IndividualView() {
	const {
		store: {
			target
		},
		toggleVision
	} = useIndividualViewContext();
	const {
		store: {
			elements: { map: detailsInStore }
		}
	} = useProductCost();
	const [, setDetails] = useReducer((o, n) => {
		if (!Array.isArray(n)) {
			return o;
		}

		return n;
	}, []);
	const model = optional(detailsInStore[target]).else({});

	useEffect(() => {
		const productId = extractProductIdFromDetailId(target);

		if (productId == null){
			return;
		}

		const doFetch = async () => {
			const [res, err] = await getProductDetailsByProduct({
				productId,
				columns: OBTAINED_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			setDetails(res);
		};

		doFetch();
	}, [target, setDetails]);

	if (isEmpty(model)) {
		return null;
	}

	const { provider, product } = model;

	return (
		<FullModal
			close={() => toggleVision()}
		>
			<h2 className="uk-heading-line uk-margin-remove-top">
				<span>{`${provider.name}'s supply of Product code ${product.id}`}</span>
			</h2>
			<div className="uk-grid-small uk-child-width-1-3" uk-grid="">
				<div>
					<div className="uk-flex">
						<div>{provider.name}</div>
						<div>{model.price}</div>
						<div className="uk-text-bold">
						{
							asIf(model.approvedTimestamp != null)
							.then(() => <span className="uk-text-success">Approved</span>)
							.else(() => <span className="uk-text-danger">Pending</span>)
						}
						</div>
					</div>
				</div>
				<div>
					<h4 className="uk-text-center">Cost history</h4>
				</div>
				<div>
					<h4 className="uk-text-center">Other providers of this product</h4>
				</div>
			</div>
		</FullModal>
	);
}
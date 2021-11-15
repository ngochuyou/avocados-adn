import { useEffect, useState, useMemo } from 'react';
import { Route, useParams, useHistory, useLocation } from 'react-router-dom';

import Account from '../../models/Account';

import { getProductList } from '../../actions/product';
import {
	getProductCostsByProduct, createProductCost,
	approveProductCost, createProvider, fetchProviderList,
	obtainProvider, updateProvider
} from '../../actions/provider';

import ProductCost from '../../models/ProductCost';

import { routes } from '../../config/default';

import { useAuth } from '../../hooks/authentication-hooks';
import { useNavbar } from './Navbar';
import { useProduct } from '../../hooks/product-hooks';
import { useProvider } from '../../hooks/provider-hooks';
import { useInputSet, useStateWithMessage } from '../../hooks/hooks';

import { ProductTable } from '../product/ProductList';
import Navbar from './Navbar';
import { ConfirmModal } from '../utils/ConfirmModal';
import ProviderPicker from '../product/ProviderPicker';
import PagedComponent from '../utils/PagedComponent';
import OrderSelector from '../utils/Sort';
import { PluralValueInput } from '../utils/Input';
import {
	asIf, formatVND, formatDatetime,
	datetimeLocalString, now, ErrorTracker, hasLength,
	updateURLQuery
} from '../../utils';

const FETCHED_PRODUCT_COLUMNS = ["id", "code", "name", "images"];

export default function ProviderBoard() {
	const {
		dashboard: {
			provider: {
				costs: { mapping: productCostsMapping },
				creation: { mapping: providerCreationMapping },
				list: { mapping: providerListMapping }
			}
		}
	} = routes;

	return (
		<div>
			<Navbar />
			<Route
				path={productCostsMapping}
				render={props => <CostManagement { ...props } />}
			/>
			<Route
				path={providerCreationMapping}
				render={props => <ProviderCreator { ...props } />}
			/>
			<Route
				path={providerListMapping}
				render={props => <ProviderList { ...props } />}
			/>
		</div>
	);
}

function ProviderList() {
	const {
		store: { elements: { map: providersMap } },
		setProviders
	} = useProvider();
	const {
		dashboard: { provider: { list: { url: providerListUrl } } }
	} = routes;
	const { providerId } = useParams();
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);
	const { push } = useHistory();
	const { setOnEntered } = useNavbar();

	useEffect(() => {
		setOnEntered(key => push(`${providerListUrl}?${updateURLQuery(urlParams, "name", p => key)}`));
	}, [setOnEntered, push, providerListUrl, urlParams]);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await fetchProviderList({
				columns: ["id", "name", "email", "representatorName"],
				page: urlParams.get('page'),
				size: 12,
				name: urlParams.get('name')
			});

			if (err) {
				return console.error(err);
			}

			return setProviders(res);
		};

		doFetch();
	}, [urlParams, setProviders]);

	const onSelect = (provider) => push(`${providerListUrl}/${provider.id}`);
	const providers = Object.values(providersMap);

	return (
		<main className="uk-padding-small">
		{
			asIf(providerId == null)
			.then(() => (
				<>
					<h3 className="uk-heading-line">
						<span>Provider list</span>
					</h3>
					<div>
						<div
							className="uk-text-primary pointer noselect"
							onClick={() => push(`${providerListUrl}`)}
						>Clear filter</div>
						<PagedComponent
							pageCount={providers.length}
							onNextPageRequest={() => push(`${providerListUrl}?${updateURLQuery(urlParams, "page", p => (+p || 0) + 1)}`)}
							onPreviousPageRequest={() => push(`${providerListUrl}?${updateURLQuery(urlParams, "page", p => +p - 1)}`)}
							currentPage={urlParams.get('page')}
						>
							<div className="uk-margin uk-grid-small uk-child-width-1-4 uk-grid-match" uk-grid="">
							{
								providers.map(provider => (
									<div
										key={provider.id}
										onClick={() => onSelect(provider)}
									>
										<div className="uk-card uk-card-default uk-card-body uk-card-hover uk-padding-small pointer">
											<div className="uk-text-lead">{provider.name}</div>
											<div
												style={{wordBreak: "break-all"}}
											>{provider.email}</div>
											<div>{provider.representatorName}</div>
										</div>
									</div>
								))
							}
							</div>
						</PagedComponent>
					</div>
				</>
			)).else(() => <ProviderEditor />)
		}
		</main>
	);
}

function ProviderEditor() {
	const { setBackBtnState } = useNavbar();
	const [model, setModel] = useState(null);
	const [errors, setErrors] = useState({});
	const { providerId } = useParams();
	const {
		dashboard: { provider: { list: { url: providerListUrl } } }
	} = routes;
	const { push } = useHistory();
	
	useEffect(() => {
		setBackBtnState({
			visible: true,
			callback: () => {
				push(providerListUrl);
				setBackBtnState({
					visible: false,
					callback: () => null
				});
			}
		});
	}, [push, setBackBtnState, providerListUrl]);
	
	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await obtainProvider({
				id: providerId,
				columns: ["id", "name", "email", "representatorName", "address", "website", "phoneNumbers"]
			});

			if (err) {
				return console.error(err);
			}

			setModel(res);
		};

		doFetch();
	}, [providerId]);

	if (model == null) {
		return null;
	}

	const onModelChange = (name, value) => setModel({
		...model,
		[name]: value
	});
	const onSuccess = async (model) => {
		const [res, err] = await updateProvider(model);

		if (err) {
			console.error(err);
			return setErrors(err);
		}

		return setModel(res);
	};

	return (
		<div className="uk-grid-small" uk-grid="">
			<div className="uk-width-2-3">
				<ProviderFormLayout
					onModelChange={onModelChange}
					model={model}
					onSuccess={onSuccess}
					errors={errors}
				/>
			</div>
			<div></div>
		</div>
	);
}

function ProviderCreator() {
	const [errors, setErrors] = useState({});

	const onSuccess = async (model) => {
		const [res, err] = await createProvider(model);

		if (err) {
			console.error(err);
			return setErrors(err);
		}

		setErrors({});

		return console.log(res);
	};

	return (
		<main className="uk-padding-small">
			<h3 className="uk-heading-line">
				<span>Create a Provider</span>
			</h3>
			<div className="uk-grid-small" uk-grid="">
				<div className="uk-width-2-3">
					<InternalStateProviderForm
						onSuccess={onSuccess}
						errors={errors}
					/>
				</div>
				<div></div>
			</div>
		</main>
	);
}

function InternalStateProviderForm({
	onSuccess = () => console.log("on success"),
	errors = {}
}) {
	const [model, setModelState] = useState({
		name: "",
		email: "",
		phoneNumbers: [],
		address: "",
		website: "",
		representatorName: ""
	});

	const onModelChange = (name, value) => setModelState({
		...model,
		[name]: value
	});

	return (
		<ProviderFormLayout
			onModelChange={onModelChange}
			model={model}
			onSuccess={onSuccess}
			errors={errors}
		/>
	);
}

function ProviderFormLayout({
	model = {},
	onModelChange = (name, val) => console.log(name, val),
	onSuccess = () => console.log("on success"),
	errors = {}
}) {
	const onInputChange = (event) => {
		const { target: {name, value } } = event;

		return onModelChange(name, value);
	};
	const onAddPhoneNumber = (newPhonenumber) => {
		if (model.phoneNumbers.includes(newPhonenumber)) {
			return;
		}

		return onModelChange("phoneNumbers", [
			...model.phoneNumbers, newPhonenumber
		]);
	};
	const onRemovePhoneNumber = (index, phonenumber) => onModelChange("phoneNumbers", model.phoneNumbers.filter(number => number !== phonenumber));
	const onSubmit = (event) => {
		event.preventDefault();
		event.stopPropagation();

		return onSuccess(model);
	};

	return (
		<form onSubmit={onSubmit}>
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

function CostManagement() {
	const {
		store: {
			product: { elements: productMap }
		},
		setProducts, mergeCosts
	} = useProduct();
	const {
		dashboard: {
			provider: { costs: { url: productCostsUrl } }
		}
	} = routes;
	const products = Object.values(productMap);
	const { productId } = useParams();
	const { push } = useHistory();

	useEffect(() => {
		const doFetch = async () => {
			let [res, err] = await getProductList({
				internal: true,
		 		FETCHED_PRODUCT_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);
		};

		doFetch();
	}, [setProducts, mergeCosts]);

	return (
		<div className="uk-padding-small">
			<h4>Product Costs</h4>
			{
				asIf(productId == null)
				.then(() => <ProductTable
					list={Object.values(products)}
					columns={["images", "code", "name"]}
					onRowSelect={(p) => push(`${productCostsUrl}/${p.id}`)}
				/>)
				.else(() => <IndividualCost />)
			}
		</div>
	);
}

function IndividualCost() {
	const {
		dashboard: {
			provider: { costs: { url: productCostsUrl } }
		}
	} = routes;
	const { setBackBtnState, setOnEntered } = useNavbar();
	const { productId } = useParams();
	const { push } = useHistory();
	const { principal } = useAuth();
	const [costList, setCostList] = useState([]);
	const [confirmModal, setConfirmModal] = useState(null);
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);

	useEffect(() => {
		setBackBtnState({
			visible: true,
			callback: () => push(productCostsUrl)
		});
		setOnEntered((key) => push(`${productCostsUrl}/${productId}?provider=${key}`));

		return () => setBackBtnState();
	}, [setBackBtnState, push, productCostsUrl, setOnEntered, productId]);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await getProductCostsByProduct({
				productId,
				columns: [
					"appliedTimestamp", "droppedTimestamp",
					"cost", "approvedTimestamp", "provider"
				],
				providerName: urlParams.get("provider"),
				page: urlParams.get("page"),
				size: urlParams.get("size"),
				sort: urlParams.get("sort")
			});

			if (err) {
				console.error(err);
				return;
			}

			setCostList(res.map(ele => ({
				...ele,
				formattedAppliedTimestamp: formatDatetime(ele.appliedTimestamp),
				formattedDroppedTimestamp: formatDatetime(ele.droppedTimestamp),
				formattedApprovedTimestamp: formatDatetime(ele.approvedTimestamp),
				cost: formatVND(ele.cost)
			})));
		};

		doFetch();			
	}, [productId, urlParams]);

	const onApprove = (index) => {
		const ele = costList[index];

		setConfirmModal(
			<ConfirmModal
				background="uk-background-muted"
				message={`Are you sure you want to apply ${ele.cost} on ${ele.formattedAppliedTimestamp} and drop it on ${ele.formattedDroppedTimestamp}?`}
				onNo={() => setConfirmModal(null)}
				onYes={() => approve(ele, index)}
			/>
		);
	};

	const approve = async (ele, index) => {
		const { appliedTimestamp, droppedTimestamp } = ele;
		const [res, err] = await approveProductCost({
			productId,
			providerId: ele.provider.id,
			appliedTimestamp,
			droppedTimestamp
		});

		if (err) {
			console.error(err);
			return;
		}

		const { approvedTimestamp } = res;

		setCostList(costList.map((p, i) => asIf(i === index).then(() => ({
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
					{`Cost schedule for Product ID: `}
					<span className="uk-text-bold colors">{productId}</span>
				</span>
			</h5>
			<PagedComponent
				pageCount={costList.length}
				onNextPageRequest={() => push(`${productCostsUrl}/${productId}?${updateURLQuery(urlParams, "page", p => hasLength(p) ? +p + 1 : 1)}`)}
				onPreviousPageRequest={() => push(`${productCostsUrl}/${productId}?${updateURLQuery(urlParams, "page", p => +p - 1)}`)}
				currentPage={urlParams.get("page")}
			>
				<div className="uk-margin">
					<OrderSelector
						className="uk-width-auto"
						onAscRequested={() => push(`${productCostsUrl}/${productId}?${updateURLQuery(urlParams, "sort", sort => `appliedTimestamp,asc`)}`)}
						onDesRequested={() => push(`${productCostsUrl}/${productId}?${updateURLQuery(urlParams, "sort", sort => `appliedTimestamp,desc`)}`)}
						labels={["Applied timestamp ascending", "Applied timestamp descending"]}
					/>
				</div>
				<table className="uk-table uk-table-divider">
					<thead>
						<tr>
							<th>Provider</th>
							<th>Applied timestamp</th>
							<th>Dropped timestamp</th>
							<th>Cost</th>
							<th>Approved timestamp</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
					{
						costList.map((ele, index) => (
							<tr key={index}>
								<td>{ele.provider.name}</td>
								<td>{ele.formattedAppliedTimestamp}</td>
								<td>{ele.formattedDroppedTimestamp}</td>
								<td>
									<span className="colors">{ele.cost}</span>
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
			<IndividualCostCreator
				onSuccess={(newCost) => setCostList([
					...costList,
					{
						...newCost,
						formattedAppliedTimestamp: formatDatetime(newCost.appliedTimestamp),
						formattedDroppedTimestamp: formatDatetime(newCost.droppedTimestamp),
						cost: formatVND(newCost.cost)
					}
				])}
			/>
		</div>
	);
}

function IndividualCostCreator({
	onSuccess = (cost) => console.log(cost)
}) {
	const { productId } = useParams();
	const [costProps, , costErr, setCostErr] = useInputSet(100000);
	const [provider, setProvider, providerError, setProviderError] = useStateWithMessage(null);
	const [appliedProps, , appliedError, setAppliedError] = useInputSet(datetimeLocalString(now()));
	const [droppedProps, , droppedError, setDroppedError] = useInputSet(datetimeLocalString(now()));
	const [providerPickerVisible, setProviderPickerVisible] = useState(false);

	const onProviderPicked = (provider) => {
		setProvider(provider);
		setProviderPickerVisible(false);
	};

	const onSubmit = async (event) => {
		event.preventDefault();
		event.stopPropagation();

		let tracker = new ErrorTracker();
		const cost = costProps.value;
		const appliedTimestamp = appliedProps.value;
		const droppedTimestamp = droppedProps.value;

		setCostErr(tracker.add(ProductCost.validators.cost(cost)));
		setProviderError(tracker.add(ProductCost.validators.provider(provider)));
		setAppliedError(tracker.add(ProductCost.validators.appliedTimestamp(appliedTimestamp)));
		setDroppedError(tracker.add(ProductCost.validators.droppedTimestamp(droppedTimestamp)));

		if (tracker.foundError()) {
			return;
		}

		const [res, err] = await createProductCost({
			productId,
			providerId: provider.id,
			cost,
			appliedTimestamp, droppedTimestamp
		});

		if (err) {
			console.error(err);
			setAppliedError(err.appliedTimestamp);
			setDroppedError(err.droppedTimestamp);
			setCostErr(err.cost);
			setProviderError(err.provider);
			return;
		}

		onSuccess({ ...res, provider });
	};

	return (
		<form onSubmit={onSubmit}>
			<h5 className="uk-heading uk-heading-line colors uk-text-right">
				<span>
					{`Submit a new Cost for Product ID: `}
					<span className="uk-text-bold colors">{productId}</span>
				</span>
			</h5>
			<div className="uk-grid-small" uk-grid="">
				<div className="uk-width-2-3">
					<div
						className="uk-margin pointer noselect"
						onClick={() => setProviderPickerVisible(true)}
					>
						<label className="uk-label backgroundf">Provider</label>
						<div className="uk-card uk-card-default uk-card-body">
						{
							asIf(provider == null)
							.then(() => <div className="uk-text-center uk-text-muted">Pick a Provider</div>)
							.else(() => (
								<div className="uk-height-1-1 uk-position-relative">
									<div className="uk-text-lead">{provider.name}</div>
									<div>{provider.email}</div>
									<div>{provider.representatorName}</div>
								</div>
							))
						}
						</div>
						<div className="uk-text-danger">{providerError}</div>
					</div>
					<div className="uk-margin">
						<label className="uk-label backgroundf">Cost</label>
						<input
							{...costProps}
							className="uk-input"
							placeholder="Cost"
							type="number"
							min="0"
							step="0.0001"
						/>
						<div className="uk-text-danger">{costErr}</div>
					</div>
					<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
						<div>
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
						<div>
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
				</div>
				<div className="uk-width-1-3"></div>
			</div>
			{
				asIf(providerPickerVisible)
				.then(() => <ProviderPicker
					close={() => setProviderPickerVisible(false)}
					onPick={onProviderPicked}
				/>)
				.else()
			}
			<div className="uk-margin">
				<button className="uk-button backgroundf">Submit</button>
			</div>
		</form>
	);
}
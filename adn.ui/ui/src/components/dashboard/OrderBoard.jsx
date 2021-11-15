import { useEffect, useState, useMemo } from 'react';
import { Route, useParams, useHistory, useLocation } from 'react-router-dom';

import Account from '../../models/Account';
import { Order } from '../../models/Factor';

import { obtainOrder, getOrdersList, confirmPayment, changeOrderStatus } from '../../actions/order';
import { getItemsList } from '../../actions/product';

import Navbar, { useNavbar } from './Navbar';
import { OrderList, IndividualOrder, calculateExpiredTimestamp } from '../account/OrderView';
import { ConfirmModal } from '../utils/ConfirmModal';
import PagedComponent from '../utils/PagedComponent';

import { routes } from '../../config/default';

import {
	formatVND, groupCartItems, asIf, hasLength, atom, formatDatetime,
	updateURLQuery
} from '../../utils';

export default function OrderBoard() {
	const {
		dashboard: {
			order: { list: { mapping: listMapping } }
		}
	} = routes;

	return <div>
		<Navbar />
		<Route
			path={listMapping}
			render={props => <ListBoard {...props}/>}
		/>
	</div>;
}

function ListBoard() {
	const { orderCode } = useParams();

	if (!hasLength(orderCode)) {
		return <OrderListView />;
	}

	return <IndividualOrderView />;
}

function OrderListView() {
	const {
		dashboard: {
			order: { list: { url: listUrl } }
		}
	} = routes;
	const { push } = useHistory();
	const [orders, setOrders] = useState([]);
	const { setBackBtnState, setOnEntered } = useNavbar();
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);

	useEffect(() => {
		setOnEntered(key => push(`${listUrl}?${updateURLQuery(urlParams, "customer", () => key)}`));
	}, [setOnEntered, listUrl, push, urlParams]);

	useEffect(() => {
		const doFetch = async () => {
			const [ordersList, err] = await getOrdersList({
				internal: true,
				columns: [
					"id", "code", "createdTimestamp",
					"status", "customer"
				],
				page: urlParams.get('page'),
				size: urlParams.get('size'),
				customer: urlParams.get('customer'),
				status: isNaN(urlParams.get('status')) ? urlParams.get('status') : null
			});

			if (err) {
				console.error(err);
				return;
			}

			setOrders(ordersList);
		};

		doFetch();
	}, [urlParams]);

	return (
		<div className="uk-padding-small">
			<h3>Customers Orders</h3>
			<div className="uk-flex uk-flex-right">
				<div>
					<select
						className="uk-select uk-width-auto"
						value={urlParams.get('status') || -1}
						onChange={(event) => push(`${listUrl}?${updateURLQuery(urlParams, "status", () => event.target.value)}`)}
					>
						<option
							value={-1}
						>All status</option>
						<option
							value={Order.Status.PENDING_PAYMENT}
							className="colors"
						>Pending payment</option>
						<option
							value={Order.Status.PAID}
							className="uk-text-primary"
						>Paid</option>
						<option
							value={Order.Status.EXPIRED}
							className="uk-text-muted"
						>Expired</option>
						<option
							value={Order.Status.DELIVERING}
							className="uk-text-warning"
						>On the way</option>
						<option
							value={Order.Status.FINISHED}
							className="uk-text-success"
						>Finished</option>
					</select>
				</div>
				<div className="uk-margin-left">
					<button
						className="uk-button uk-button-default"
						onClick={() => push(listUrl)}
					>Clear filter</button>
				</div>
			</div>
			<div className="uk-position-relative" uk-height-viewport="offset-top: true">
				<PagedComponent
					pageCount={orders.length}
					onNextPageRequest={() => push(`${listUrl}?${updateURLQuery(urlParams, "page", p => (+p || 0) + 1)}`)}
					onPreviousPageRequest={() => push(`${listUrl}?${updateURLQuery(urlParams, "page", p => +p - 1)}`)}
					currentPage={urlParams.get('page')}
				>
					<OrderList
						list={orders}
						extras={[
							{
								header: <th key="customer">Customer</th>,
								row: (order) => {
									const { customer } = order;

									return <td key="customer">{`${customer.firstName} ${customer.lastName}`}</td>;
								}
							}
						]}
						onRowSelect={(order) => {
							push(`${listUrl}/${order.code}`);
							setBackBtnState({
								visible: true,
								callback: () => {
									setBackBtnState();
									push(listUrl);
								}
							});
						}}
					/>
				</PagedComponent>
			</div>
		</div>
	);
}

function IndividualOrderView() {
	const { orderCode } = useParams();
	const [order, setOrder] = useState(null);

	useEffect(() => {
		const doFetch = async () => {
			const [order, orderErr] = await obtainOrder({
				code: orderCode,
				orderColumns: [
					"id", "status", "createdTimestamp",
					"district", "address", "note", "details",
					"updatedTimestamp", "customer"
				],
				detailsColumns: ["itemId", "price"]
			});

			if (orderErr) {
				console.error(orderErr);
				return;
			}

			const [items, itemsErr] = await getItemsList({
				itemIds: order.details.map(detail => detail.itemId),
				columns: ["id", "color", "namedSize", "product", "status"]
			});

			if (itemsErr) {
				console.error(itemsErr);
				return;
			}

			const priceMap = atom(order.details, "itemId");

			setOrder({
				...order,
				code: orderCode,
				customer: new Account(order.customer),
				formattedCreatedTimestamp: formatDatetime(order.createdTimestamp),
				formattedUpdatedTimestamp: formatDatetime(order.updatedTimestamp),
				expiredTimestamp: asIf(order.status !== Order.Status.PENDING_PAYMENT)
					.then(() => undefined)
					.else(() => calculateExpiredTimestamp(order.createdTimestamp)),
				items: Object.values(groupCartItems(items, (current, existing) => {
					const { price } = priceMap[current.id];

					return {
						price: formatVND(price),
						total: existing == null ? formatVND(price) : formatVND(price * (existing.quantity + 1))
					}
				}))
			});
		};

		doFetch();
	}, [orderCode]);

	if (order == null) {
		return null;
	}

	const editStatus = (status) => setOrder({ ...order, status });

	return (
		<div className="uk-padding-small">
			<h3 className="uk-heading-line">Order {order.code} - {order.customer.fullname}</h3>
			<IndividualOrderActions
				order={order}
				onSuccessfulConfirm={() => editStatus(Order.Status.PAID)}
				onSuccessfulStatusChange={editStatus}
			/>
			<IndividualOrder
				order={order}
			/>
		</div>
	);
}

const ACTIONS_RESOLVERS = {
	[Order.Status.PENDING_PAYMENT]: ({
		onConfirmPayment = () => console.log("on confirm")
	}) => {
		return (
			<>
				<button
					className="uk-button backgroundf"
					onClick={onConfirmPayment}
				>Confirm payment</button>
			</>
		);
	},
	[Order.Status.PAID]: ({
		onStatusChange = (status) => console.log(status),
		order
	}) => {
		return (
			<div className="uk-flex uk-flex-right">
				<div className="uk-width-auto">
					<label className="uk-label backgroundf">Status</label>
					<select
						className="uk-select"
						onChange={(event) => onStatusChange(event.target.value)}
						value={order.status}
					>
						<option></option>
						<option
							className="uk-text-warning" value={Order.Status.DELIVERING}
						>Being delivered</option>
						<option className="uk-text-success" value={Order.Status.FINISHED}>Finished</option>
					</select>
				</div>
			</div>
		);
	},
	[Order.Status.EXPIRED]: () => null,
	[Order.Status.DELIVERING]: ({
		onStatusChange = (status) => console.log(status),
		order
	}) => {
		return (
			<div className="uk-flex uk-flex-right">
				<div className="uk-width-auto">
					<label className="uk-label backgroundf">Status</label>
					<select
						className="uk-select"
						onChange={(event) => onStatusChange(event.target.value)}
						value={order.status}
					>
						<option></option>
						<option className="uk-text-success" value={Order.Status.FINISHED}>Finished</option>
					</select>
				</div>
			</div>
		);
	},
	[Order.Status.FINISHED]: () => null
};

function IndividualOrderActions({
	order,
	onSuccessfulConfirm = () => console.log("payment confirmed"),
	onSuccessfulStatusChange = (status) => console.log(`status changed ${status}`)
}) {
	const [confirmPaymentModalVisible, setConfirmModalVisible] = useState(false);
	const [statusChangeConfirmModal, setStatusChangeModal] = useState();

	const onConfirmPayment = () => setConfirmModalVisible(true);
	const doConfirmPayment = async () => {
		const [, err] = await confirmPayment(order.id);

		if (err) {
			console.error(err);
			return;
		}

		onSuccessfulConfirm();
		setConfirmModalVisible(false);
	};
	const onStatusChange = async (status) => {
		if (!hasLength(status)) {
			return;
		}

		setStatusChangeModal(
			<ConfirmModal
				message={`Are you sure you want to change the Order status to ${status} ? `}
				onYes={() => doChangeOrderStatus(status)}
				onNo={() => setStatusChangeModal(null)}
			/>
		);
	};
	const doChangeOrderStatus = async (status) => {
		const [, err] = await changeOrderStatus(order.id, status);

		if (err) {
			console.error(err);
			return;
		}

		onSuccessfulStatusChange(status);
		setStatusChangeModal(null);
	}

	return (
		<div className="uk-margin uk-text-right">
		{
			asIf(confirmPaymentModalVisible === true)
			.then(() => (
				<ConfirmModal
					message={`Are you sure you want to confirm the payment for order ${order.code}? Payment total is ${formatVND(order.details.reduce((total, current) => total + current.price, 0))}`}
					onYes={doConfirmPayment}
					onNo={() => setConfirmModalVisible(false)}
				/>
			)).else()
		}
		{ statusChangeConfirmModal }
		<h5 className="uk-heading-line">
			<span>Actions</span>
		</h5>
		{
			ACTIONS_RESOLVERS[order.status]({
				onConfirmPayment, onStatusChange, order
			})
		}
		</div>
	);
}
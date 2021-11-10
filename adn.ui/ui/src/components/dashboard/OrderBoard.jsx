import { useEffect, useState } from 'react';
import { Route, useParams, useHistory } from 'react-router-dom';

import Account from '../../models/Account';
import { Order } from '../../models/Factor';

import { obtainOrder, getOrdersList, confirmPayment, changeOrderStatus } from '../../actions/order';
import { getItemsList } from '../../actions/product';

import Navbar, { useNavbar } from './Navbar';
import { OrderList, IndividualOrder, calculateExpiredTimestamp } from '../account/OrderView';
import { ConfirmModal } from '../utils/ConfirmModal';

import { routes } from '../../config/default';

import { formatVND, groupCartItems, asIf, hasLength, atom, formatDatetime } from '../../utils';

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
	const { setBackBtnState } = useNavbar();

	useEffect(() => {
		const doFetch = async () => {
			const [ordersList, err] = await getOrdersList({
				internal: true,
				columns: [
					"id", "code", "createdTimestamp",
					"status", "customer"
				]
			});

			if (err) {
				console.error(err);
				return;
			}

			setOrders(ordersList);
		};

		doFetch();
	}, []);

	return (
		<div className="uk-padding-small">
			<h3>Customers Orders</h3>
			<div className="uk-position-relative" uk-height-viewport="offset-top: true">
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
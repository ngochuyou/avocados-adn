import { useEffect, useState, useMemo } from 'react';
import { useParams, useHistory, useLocation } from 'react-router-dom';

import Account from '../../models/Account';
import { Order } from '../../models/Factor';

import { useAuth } from '../../hooks/authentication-hooks';

import { obtainOrder, getOrdersList } from '../../actions/order';
import { getItemsList } from '../../actions/product';

import { routes } from '../../config/default';

import Navbar from '../utils/Navbar';
import AccessDenied from '../../pages/AccessDenied';
import { DomainProductImage } from '../utils/Gallery';
import PagedComponent from '../utils/PagedComponent';

import {
	atom, hasLength, asIf, formatDatetime, formatVND,
	groupCartItems, updateURLQuery
} from '../../utils';

export default function OrderView() {
	const { code: orderCode } = useParams();
	const { principal } = useAuth();

	if (principal == null || principal.role !== Account.Role.CUSTOMER) {
		return <AccessDenied />;
	}

	return (
		<div>
			<Navbar />
			<main className="uk-padding uk-padding-remove-top">
			{
				asIf(!hasLength(orderCode))
				.then(() => <OrderListView />)
				.else(() => <IndividualOrderView />)
			}
			</main>
		</div>
	);
}

function OrderListView() {
	const [orders, setOrders] = useState([]);
	const { push } = useHistory();
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);

	useEffect(() => {
		const doFetch = async () => {
			const [ordersList, err] = await getOrdersList({
				columns: ["id", "code", "createdTimestamp", "status"],
				page: urlParams.get('page'),
				size: urlParams.get('size')
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
		<main className="uk-padding uk-padding-remove-top">
			<h3 className="uk-heading-line">
				<span>Your orders</span>
			</h3>
			<div>
				<PagedComponent
					pageCount={orders.length}
					onNextPageRequest={() => push(`${routes.order.url}?${updateURLQuery(urlParams, "page", p => (+p || 0) + 1)}`)}
					onPreviousPageRequest={() => push(`${routes.order.url}?${updateURLQuery(urlParams, "page", p => +p - 1)}`)}
					currentPage={urlParams.get('page')}
				>
					<OrderList
						list={orders}
						onRowSelect={(order) => push(`${routes.order.url}/${order.code}`)}
					/>
				</PagedComponent>
			</div>
		</main>
	);
}

export function OrderList({
	list = [],
	onRowSelect = (order) => console.log(order),
	messageIfEmpty = "Nothing found in this list",
	extras = []
}) {
	if (!hasLength(list)) {
		return (
			<h5 className="uk-position-center uk-text-center">
				<span
					uk-icon="icon: list"
					className="uk-margin-small-right"
				></span>
				{messageIfEmpty}
			</h5>
		);
	}

	return <table className="uk-table uk-table-middle">
		<thead>
			<tr>
				<th>Order code</th>
				<th>Status</th>
				<th>Order date</th>
				<th>Expires on</th>
				{ extras.map(extra => extra.header) }
			</tr>
		</thead>
		<tbody>
		{
			list.map(order => (
				<tr
					key={order.code}
					className={`uk-box-shadow-hover-medium pointer ${order.status === Order.Status.EXPIRED ? "uk-background-muted" : ""}`}
					onClick={() => onRowSelect(order)}
				>
					<td><p className="uk-text-lead colors">{order.code}</p></td>
					<td><p>{STATUS_ELEMENT_RESOLVERS[order.status]()}</p></td>
					<td><p>{formatDatetime(order.createdTimestamp)}</p></td>
					<td><p>{order.status === Order.Status.PENDING_PAYMENT ? calculateExpiredTimestamp(order.createdTimestamp) : null}</p></td>
					{ extras.map(extra => extra.row(order)) }
				</tr>
			))
		}
		</tbody>
	</table>;
}

const STATUS_ELEMENT_RESOLVERS = {
	[Order.Status.PENDING_PAYMENT]: () => <label className="uk-label backgroundf">Pending Payment</label>,
	[Order.Status.PAID]: () => <label className="uk-label uk-label-primary">Payment confirmed</label>,
	[Order.Status.EXPIRED]: () => <label className="uk-label backgrounds">Expired</label>,
	[Order.Status.DELIVERING]: () => <label className="uk-label uk-label-warning">On the way</label>,
	[Order.Status.FINISHED]: () => <label className="uk-label uk-label-success">Finished</label>
};

export const calculateExpiredTimestamp = (timestamp) => {
	const createdTimestamp = new Date(timestamp);

	return formatDatetime(new Date(createdTimestamp.setTime(createdTimestamp.getTime() + (24 * 60 * 60 * 1000))));
};

function IndividualOrderView() {
	const { code: orderCode } = useParams();
	const [order, setOrder] = useState();
	const { principal } = useAuth();

	useEffect(() => {
		const doFetch = async () => {
			const [order, orderErr] = await obtainOrder({
				code: orderCode,
				orderColumns: [
					"id", "status", "createdTimestamp",
					"district", "address", "note", "details",
					"updatedTimestamp"
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

	return <IndividualOrder order={{
		...order,
		customer: principal
	}}/>;
}

export function IndividualOrder({
	order = null
}) {
	if (order == null) {
		return null;
	}

	return (
		<div>
			<div className="uk-margin-top uk-box-shadow-medium uk-border-rounded uk-overflow-hidden">
				<table className="uk-table">
					<thead></thead>
					<tbody>
						<tr>
							<td className="uk-width-1-2">
								<table className="uk-table uk-table-middle">
									<thead>
										<tr>
											<th className="uk-table-shrink uk-padding-remove"></th>
											<th className="uk-padding-remove"></th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><label className="uk-label backgrounds">Order Code</label></td>
											<td>
												<p className="uk-text-lead">{order.code}</p>
											</td>
										</tr>
										<tr>
											<td><label className="uk-label backgrounds">Delivery to</label></td>
											<td>
												<p className="uk-text-medium">
													{`${order.address} ${order.district.name} ${order.district.province.name} - ${order.customer.fullname}`}
												</p>
											</td>
										</tr>
										<tr>
											<td><label className="uk-label backgrounds">Contact</label></td>
											<td>
												<p className="uk-text-medium">
													{`${order.customer.phone}`}
												</p>
											</td>
										</tr>
									</tbody>
								</table>
							</td>
							<td className="uk-width-1-2">
								<table className="uk-table uk-table-middle">
									<thead>
										<tr>
											<th className="uk-table-shrink uk-padding-remove"></th>
											<th className="uk-padding-remove"></th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><label className="uk-label backgrounds">Order Status</label>
											</td>
											<td>
												<p className="uk-text-lead">{STATUS_ELEMENT_RESOLVERS[order.status]()}</p>
											</td>
										</tr>
									{
										asIf(order.status === Order.Status.PENDING_PAYMENT)
										.then(() => <tr>
											<td><label className="uk-label backgrounds">Expires on</label></td>
											<td>
												<p className="uk-text-medium">{order.expiredTimestamp}</p>
											</td>
										</tr>).else()
									}
										<tr>
											<td><label className="uk-label backgrounds">Total items</label></td>
											<td>
												<p className="uk-text-medium colors">{order.details.length}</p>
											</td>
										</tr>
										<tr>
											<td><label className="uk-label backgrounds">Total</label></td>
											<td>
												<p className="uk-text-medium uk-text-bold colors">{formatVND(order.details.reduce((total, item) => total + item.price, 0))}</p>
											</td>
										</tr>
										<tr>
											<td colSpan="2">
												<p className="uk-text-medium uk-text-italic uk-text-right">Bank transfer</p>
											</td>
										</tr>
										<tr>
											<td colSpan="2" className="uk-text-right">
												<div>{`Order made on ${order.formattedCreatedTimestamp}.`}</div>
												<div>{`Last updated on ${order.formattedUpdatedTimestamp}.`}</div>
											</td>
										</tr>
									</tbody>
								</table>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div className="uk-margin">
				<table className="uk-table uk-table-middle uk-text-center">
					<thead>
						<tr>
							<th className="uk-table-shrink"></th>
							<th className="uk-table-shrink uk-text-center">Color</th>
							<th className="uk-table-shrink uk-text-center">Size</th>
							<th className="uk-table-expand uk-text-center">Price</th>
							<th className="uk-table-expand uk-text-center">Quantity</th>
							<th className="uk-table-expand uk-text-center">Total</th>
						</tr>
					</thead>
					<tbody>
					{
						order.items.map(item => {
							const { product } = item;

							return <tr key={item.id} className="uk-box-shadow-hover-medium">
								<td className="noselect">
									<div
										style={{width: "100px", height: "100px"}}
										className="uk-border-circle uk-overflow-hidden"
									>
										<DomainProductImage name={product.images[0]} />
									</div>
								</td>
								<td>
									<div
										style={{height: "40px", width: "40px"}}
										className="uk-box-shadow-large uk-border-circle uk-overflow-hidden"
									>
										<div
											style={{backgroundColor: item.color}}
											className="uk-height-1-1"
										></div>
									</div>
								</td>
								<td>
									<p>{item.namedSize}</p>
								</td>
								<td>
									<p className="colors">{item.price}</p>
								</td>
								<td>
									<p>{item.quantity}</p>
								</td>
								<td>
									<p className="colors">{item.total}</p>
								</td>
							</tr>;
						})
					}
					</tbody>
				</table>
				<div className="uk-divider-icon"></div>
				<p>
					{!hasLength(order.note) ? <span className="uk-text-muted uk-text-italic">Notes weren't provided</span> : order.note}
				</p>
			</div>
		</div>
	);
}
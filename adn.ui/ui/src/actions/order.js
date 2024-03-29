import { fjson } from '../fetch';

import { hasLength, join } from '../utils';

export function placeOrder({
	districtId = "",
	address = "",
	note = ""
}) {
	if (!hasLength(districtId)) {
		return [null, "District information must not be empty"];
	}

	if (!hasLength(address)) {
		return [null, "Address information must not be empty"];
	}

	return fjson(`/rest/order`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			districtId,
			address,
			note
		})
	});
}

export function obtainOrder({
	code = "",
	orderColumns = [],
	detailsColumns = []
}) {
	if (!hasLength(code)) {
		return [null, "Order code was empty"];
	}

	return fjson(`/rest/order/${code}?orderColumns=${join(orderColumns)}&detailsColumns=${join(detailsColumns)}`);
}

export function getOrdersList({
	internal = false,
	customer = "",
	columns = [],
	page = 0, size = 10,
	status = ""
}) {
	const params = `${hasLength(customer) ? `customer=${customer}` : ""}${hasLength(status) ? `&status=${status}` : ""}&columns=${join(columns)}&size=${size || 10}&page=${page || 0}`;

	if (!internal) {
		return fjson(`/rest/order?${params}`);
	}

	return fjson(`/rest/order/internal?${params}`);
}

export function confirmPayment(orderId = "") {
	if (!hasLength(orderId)) {
		return [null, "Order ID was empty"];
	}

	return fjson(`/rest/order/confirm/${orderId}`, {
		method: 'PATCH'
	});
}

export function changeOrderStatus(orderId = "", status = "") {
	if (!hasLength(orderId)) {
		return [null, "Order ID was empty"];
	}

	if (!hasLength(status)) {
		return [null, "Status was empty"];
	}

	return fjson(`/rest/order/status/${orderId}/${status}`, {
		method: 'PATCH'
	});
}

export function rate({
	orderId = "", itemIds = [],
	rating = ""
}) {
	if (!hasLength(orderId) || !hasLength(itemIds) || !hasLength(rating)) {
		return [null, "Invalid params"];
	}

	return fjson(`/rest/order/rating/${orderId}?itemIds=${join(itemIds)}&rating=${rating}`, {
		method: 'PATCH'
	});
}
import { $fetch, fjson, asBlob } from '../fetch';

import { hasLength, join } from '../utils';

export const fetchAccount = (username = null, columns = []) => {
	if (username == null) {
		return [null, "Username was empty"];
	}

	return fjson(`/rest/user/${username}?columns=${columns.join(',')}`, {
		method: 'GET'
	});
}

export const getPersonnelDepartmentId = ({ username = "" }) => {
	if (username.length === 0) {
		return [null, "Username was empty"];
	}

	return fjson(`/rest/department/id/${username}`);
}

export async function lockAccount(username = "") {
	if (username.length === 0) {
		return [null, "Username was null"];
	}

	const [res, err] = await $fetch(`/rest/user/deactivate/${username}`, {
		method: 'PATCH',
		headers: {
			'Accept': 'text/plain'
		}
	});

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		return [await res.text(), null];
	}

	return [null, await res.text()];
}

export async function createUser(user = null) {
	if (user == null) {
		return [null, "User was empty"];
	}

	const form = new FormData();

	form.append('model', asBlob(user));

	const [res, err] = await $fetch(`/user`, {
		method: 'POST',
		headers: {
			'Accept': 'application/json'
		},
		body: form
	}, false);

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		return [await res.json(), null];
	}

	return [null, await res.json()];
}

export function signOut() {
	return $fetch(`/auth/logout`, {
		method: 'POST'
	});
}

export function updateCart(items) {
	if (!Array.isArray(items)) {
		return [null, "Items must be a collection"];
	}

	return fjson(`/rest/customer/cart`, {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(items)
	});
}

export function addCart({
	productId = null,
	color = null,
	namedSize = "",
	quantity = null
}) {
	if (isNaN(productId)) {
		return [null, "Product ID was null"];
	}

	if (isNaN(quantity)) {
		return [null, "Invalid quantity"];
	}

	return fjson(`/rest/customer/cart/add?productId=${productId}&color=${color == null ? "" : encodeURIComponent(color)}&namedSize=${namedSize}&quantity=${quantity}`, {
		method: 'PUT',
		encode: false
	});
}

export function subtractCart(itemIds = []) {
	if (!hasLength(itemIds)) {
		return [[], null];
	}

	if (!Array.isArray(itemIds)) {
		return [null, "Invalid item IDs"];
	}

	return fjson(`/rest/customer/cart/remove?items=${join(itemIds)}`, {
		method: 'PUT'
	});
}

export function emptyCart() {
	return fjson(`/rest/customer/cart/empty`, { method: 'PUT' });
}

export function getCart({
	productColumns = [],
	itemColumns = []
}) {
	return fjson(`/rest/customer/cart?productColumns=${productColumns}&itemColumns=${itemColumns}`);
}
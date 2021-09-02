import { isString, isEmpty, join } from '../utils';
import { fjson } from '../fetch';

export function fetchProviderList({ page = 0, size = 10, columns = [] }) {
	return fjson(`/rest/provider?page=${page}&size=${size}&columns=${join(columns)}`);
}

export function fetchProviderCount() {
	return fjson(`/rest/provider/count`);
}

export function obtainProvider({ id = null, columns = [], productDetailsColumns = [] }) {
	if (id == null) {
		return [null, "Provider ID was null"];
	}

	return fjson(`/rest/provider/${id}?columns=${join(columns)}&productDetails.columns=${join(productDetailsColumns)}`);
}

export function searchProvider({ name = "", columns = [], size = 500 }) {
	return fjson(`/rest/provider/search?name.like=${name}&columns=${join(columns)}&size=${size}`);
}

export function createProvider(model) {
	return fjson(`/rest/provider`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(model)
	});
}

export function approveProvider(providerId) {
	if (!isString(providerId)) {
		return [null, "Invalid Provider ID"];
	}

	return fjson(`/rest/provider/approve/${providerId}`, {
		method: 'PATCH'
	});
}

export function updateProvider(model) {
	if (isEmpty(model)) {
		return [null, "Invalid model"];
	}

	return fjson(`/rest/provider`, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(model)
	});
}
import { isString, hasLength, isEmpty, join, result } from '../utils';
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

export function getProductDetailList({ columns = "", page = 0, size = 10 }) {
	return fjson(`/rest/provider/product-detail?columns=${join(columns)}&page=${page}&size=${size}`);
}

export function getProductDetailsCount() {
	return fjson(`/rest/provider/product-detail/count`);
}

export function createProductDetail(model) {
	if (model == null) {
		return [null, result("Model was null")];
	}

	return fjson(`/rest/provider/product-detail`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(model)
	});
}

export function getProductDetailsByProduct({
	productId = null, columns = [],
	page = 0, size = 10
}) {
	if (!isString(productId) || !hasLength(productId)) {
		return [null, result("Product ID was empty")];
	}

	return fjson(`/rest/provider/product-detail/${productId}?columns=${join(columns)}&page=${page}&size=${size}`);
}

export function approveProductDetail({
	productId = null, providerId = null
}) {
	if (!isString(productId) || !hasLength(productId)) {
		return [null, result("Product code was empty")];
	}

	if (!isString(providerId) || !hasLength(providerId)) {
		return [null, result("Provider ID was empty")];
	}

	return fjson(`/rest/provider/product-detail/approve?productId=${productId}&providerId=${providerId}`, {
		method: 'PATCH'
	});
}

export function getProvidersOfProduct({
	productId = null, columns = [],
	page = 0, size = 1000
}) {
	if (!isString(productId) || !hasLength(productId)) {
		return [null, result("Product code was empty")];
	}

	return fjson(`/rest/provider/current/${productId}?columns=${join(columns)}&page=${page}&size=${size}`);
}
import {
	isString, /*hasLength*/ isEmpty, join, result,
	formatServerDatetime, hasLength
} from '../utils';
import { fjson } from '../fetch';

export function fetchProviderList({
	page = 0, size = 10,
	columns = [], name = "",
	sort = ""
}) {
	return fjson(`/rest/provider?page=${page || 0}&size=${size || 10}&columns=${join(columns)}${hasLength(name) ? `&name.like=${name}` : ""}${hasLength(sort) ? `&sort=${sort}` : ""}`);
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

// export function getProductCosts({ productIds = [] }) {
// 	if (!hasLength(productIds)) {
// 		return [[], null];
// 	}

// 	return fjson(`/rest/provider/cost?products=${join(productIds)}`);
// }

// export function getProductCostsCount(productId) {
// 	if (productId == null) {
// 		return [null, "Product ID was empty"];
// 	}

// 	return fjson(`/rest/provider/cost/count?product=${productId}`);
// }

export function getProductCostsByProduct({
	productId = null,
	columns = [],
	page = 0, size = 10,
	providerName = null,
	sort = null
}) {
	if (productId == null) {
		return [null, "Product ID was null"];
	}

	return fjson(`/rest/provider/cost/${productId}?columns=${join(columns)}&provider=${providerName || ""}&sort=${sort || ""}&page=${page || 0}&size=${size || 10}`, {}, false);
}

// export function getProductDetailList({ columns = "", page = 0, size = 10 }) {
// 	return fjson(`/rest/provider/cost?columns=${join(columns)}&page=${page}&size=${size}`);
// }

// export function getProductDetailsCount() {
// 	return fjson(`/rest/provider/cost/count`);
// }

export function createProductCost(model) {
	if (model == null) {
		return [null, result("Model was null")];
	}

	return fjson(`/rest/provider/cost`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			...model,
			appliedTimestamp: formatServerDatetime(model.appliedTimestamp),
			droppedTimestamp: formatServerDatetime(model.droppedTimestamp)
		})
	});
}

// export function getProductDetailsByProduct({
// 	productId = null, columns = [],
// 	page = 0, size = 10
// }) {
// 	if (!isString(productId) || !hasLength(productId)) {
// 		return [null, result("Product ID was empty")];
// 	}

// 	return fjson(`/rest/provider/cost/${productId}?columns=${join(columns)}&page=${page}&size=${size}`);
// }

export function approveProductCost({
	productId = null,
	providerId = null,
	appliedTimestamp = null,
	droppedTimestamp = null
}) {
	if (productId == null) {
		return [null, "Product ID was empty"];
	}

	if (providerId == null) {
		return [null, "Provider ID was empty"];
	}

	if (appliedTimestamp == null) {
		return [null, "Applied timestamp was empty"];
	}

	if (droppedTimestamp == null) {
		return [null, "Dropped timestamp was empty"];
	}

	return fjson(`/rest/provider/cost/approve?product=${productId}&provider=${providerId}&applied=${appliedTimestamp}&dropped=${droppedTimestamp}`, {
		method: 'PATCH'
	});
}

export function getProvidersByProduct({
	productId = null,
	page = 0, size = 1000
}) {
	if (!hasLength(productId)) {
		return [null, result("Product ID was empty")];
	}

	return fjson(`/rest/provider/cost/providersbyproduct/${productId}?page=${page}&size=${size}`);
}
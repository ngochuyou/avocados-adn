import { fjson } from '../fetch';
import { hasLength, join } from '../utils';

export function getProvidersCountByCategories(categoriesIds = []) {
	if (!Array.isArray(categoriesIds) || !hasLength(categoriesIds)) {
		return [[], null];
	}

	return fjson(`/rest/stats/cost/percategory?categories=${join(categoriesIds)}`);
}

export function getAvgProductCostsByProviders({
	productId = null,
	providerIds = [],
	year = null, month = null
}) {
	if (!hasLength(productId)) {
		return [[], null];
	}

	if (!Array.isArray(providerIds) || !hasLength(providerIds)) {
		return [[], null];
	}

	year = !hasLength(year) ? "" : year;
	month = !hasLength(month) ? "" : month;

	return fjson(`/rest/stats/cost/perprovider/${productId}?providers=${join(providerIds)}&year=${year}&month=${month}`);
}

export function getSoldProductAmount({
	overall = true,
	year = null, month = null,
	sort = "ASC"
} = {}) {
	if (overall === true) {
		return fjson(`/rest/stats/product/total?overall=true`);
	}

	return fjson(`/rest/stats/product/total?overall=false&year=${year || ""}&month=${month || ""}&sort=${sort}`);
}

export function getSoldProductAmountPerCategory({
	overall = true,
	categoryIds = [],
	productIds = [],
	year = null, month = null,
	sort = "ASC"
} = {}) {
	if (!Array.isArray(categoryIds) || !Array.isArray(productIds)) {
		return [[], null];
	}

	const commonQuery = `categories=${join(categoryIds)}&products=${join(productIds)}`;

	if (overall === true) {
		return fjson(`/rest/stats/product/total?overall=true&${commonQuery}`);
	}

	return fjson(`/rest/stats/product/total?overall=false&${commonQuery}&year=${year || ""}&month=${month || ""}&sort=${sort}`);
}
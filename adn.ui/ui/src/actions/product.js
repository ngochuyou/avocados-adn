import { $fetch, fjson, asBlob } from '../fetch';
import { hasLength, normalize, join, formatServerDatetime } from '../utils';

export function fetchCategoryList({ page = 0, size = 10, columns = [] }) {
	return fjson(`/rest/product/category/list?page=${page}&size=${size}&columns=${join(columns)}`);
}

export function fetchCategoryCount() {
	return fjson(`/rest/product/category/count`);
}

async function doModifyCategory(method, model = null) {
	if (model == null) {
		return [null, "Model was null"];
	}

	return await fjson('/rest/product/category', {
		method,
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(model)
	});
}

export function createCategory({ model = null }) {
	return doModifyCategory('POST', model);
}

export function updateCategory({ model = null }) {
	return doModifyCategory('PUT', model);
}

export async function updateCategoryActivationState(categoryId = null, state = null) {
	if (categoryId == null || state == null || typeof state != 'boolean') {
		return [null, "Invalid parameter"];
	}

	const [res, err] = await $fetch(`/rest/product/category/activation?id=${categoryId}&active=${state}`, {
		method: 'PATCH',
		headers: {
			'Accept' : 'text/plain'
		}
	});

	if (err) {
		return [null, err];
	}

	try {
		if (res.ok) {
			return [await res.text(), null];
		}

		return [null, await res.text()];
	} catch(exception) {
		return [null, exception];
	}
}

export function getAllCategories() {
	return fjson('/rest/product/category/all');
}

export function getProductCount() {
	return fjson('/rest/product/count');
}

export async function createProduct(model = null) {
	if (model == null) {
		return [null, "Model was null"];
	}

	const productModel = {...model};
	const form = new FormData();

	for (let file of model.images) {
		if (!(file instanceof Blob)) {
			return [null, "One of the files was not Blob"];
		}

		form.append("images", file);
	}

	delete productModel.images;

	form.append("model", asBlob(productModel));

	const [res, err] = await $fetch('/rest/product', {
		method: 'POST',
		headers: {
			'Accept': 'application/json'
		},
		body: form
	});

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		try {
			return [await res.json(), null];
		} catch (exception) {
			return [null, exception];
		}
	}

	return [null, await res.json()];
}

export async function updateProduct(model = null) {
	if (model == null) {
		return [null, "Model was null"];
	}

	const productModel = {...model};
	const form = new FormData();
	const images = [...productModel.images];
	const removedIndicies = [];

	for (let i in images) {
		if (images[i] instanceof Blob) {
			form.append("images", images[i]);
			removedIndicies.push(parseInt(i));
			continue;
		}

		if (typeof images[i] !== 'string') {
			return [null, `Unsupported file type: ${typeof images[i]}`];
		}
	}

	productModel.images = images.filter((file, index) => !removedIndicies.includes(index));

	form.append("model", asBlob(productModel));

	const [res, err] = await $fetch('/product', {
		method: 'PUT',
		headers: {
			'Accept': 'application/json'
		},
		body: form
	});

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		try {
			return [await res.json(), null];
		} catch (exception) {
			return [null, exception];
		}
	}

	return [null, await res.json()];
}

export function getProductListByCategory({
	columns = [], identifier = null, identifierName = "",
	page = 0, size = 18, internal = false
}) {
	if (identifier == null || identifier.length === 0) {
		return [null, "Category identifier was empty"];
	}

	if (internal) {
		return fjson(`/rest/product/internal?category=${identifier}&by=${identifierName}&columns=${join(columns)}`);	
	}

	return fjson(`/rest/product?category=${identifier}&by=${identifierName}&columns=${join(columns)}`);
}

export function getProductList({
	ids = [], columns = [],
	internal = false,
	page = 0, size = 18,
	name = "",
	sort = ""
}) {
	page = page == null ? "" : page;
	size = size == null ? "" : size;
	sort = sort == null ? "" : sort;

	const commonQuery = `columns=${join(columns)}&page=${page}&size=${size}&sort=${sort}&${hasLength(name) ? `name.like=${name}` : ""}`;

	if (internal) {
		return fjson(`/rest/product/internal?${commonQuery}`);
	}

	return fjson(`/rest/product?ids=${join(ids)}&${commonQuery}`);
}

export function searchProduct({
	productName = "", columns = [], size = 1000,
	internal = false
}) {
	if (!hasLength(productName)) {
		return [[], null];
	}
	
	const query = `name.like=${normalize(productName)}&columns=${join(columns)}&size=${size}`;

	if (internal) {
		return fjson(`/rest/product/search/internal?${query}`);
	}

	return fjson(`/rest/product/search?${query}`);
}

export function submitItemsBatch(batch = []) {
	if (!Array.isArray(batch) || batch.length === 0) {
		return [null, "Invalid batch"];
	}

	return fjson(`/rest/product/items`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			items: batch
		})
	});
}

export function obtainProduct({ id = null, columns = [] }) {
	if (!hasLength(id)) {
		return [null, "Product ID was null"];
	}

	return fjson(`/rest/product/${id}?columns=${join(columns)}`);
}

export function getProductPrices(ids = []) {
	if (!Array.isArray(ids)) {
		return [null, "Invalid ids"];
	}

	return fjson(`/rest/product/price?ids=${join(ids)}`);
}

export function getProductPrice({
	productId = null, columns = [],
	from = null, to = null,
	sort = "appliedTimestamp,asc",
	page = 0, size = 10
}) {
	if (isNaN(productId)) {
		return [null, "Invalid Product ID"];
	}
	console.log(from, to);
	sort = sort == null ? "" : sort;
	from = formatServerDatetime(from, null);
	to = formatServerDatetime(to, null);
	page = page == null ? 0 : page;
	size = size == null ? 10 : size;

	return fjson(`/rest/product/price/${productId}?columns=${join(columns)}&from=${from || ""}&to=${to || ""}&sort=${sort || ""}&page=${page || 0}&size=${size || 10}`, {
		encode: false
	}, false);
}

export function approveProductPrice({
	productId = null,
	appliedTimestamp = null,
	droppedTimestamp = null
}) {
	if (productId == null) {
		return [null, "Product ID was empty"];
	}

	if (appliedTimestamp == null) {
		return [null, "Applied timestamp was empty"];
	}

	if (droppedTimestamp == null) {
		return [null, "Dropped timestamp was empty"];
	}

	return fjson(`/rest/product/price/approve?product=${productId}&applied=${appliedTimestamp}&dropped=${droppedTimestamp}`, {
		method: 'PATCH'
	});
}

export function submitProductPrice(model) {
	if (model == null) {
		return [null, "Model was null"];
	}

	return fjson(`/rest/product/price`, {
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

export function getItemsListByProduct({
	productId = null,
	columns = []
}) {
	if (!hasLength(productId)) {
		return [null, "Product ID was empty"];
	}

	if (!hasLength(columns)) {
		return [null, "Requested columns were empty"];
	}

	return fjson(`/rest/product/items/${productId}?columns=${join(columns)}`);
}

export function getItemsList({
	itemIds = [],
	columns = []
}) {
	if (!hasLength(itemIds)) {
		return [[], null];
	}

	return fjson(`/rest/product/items?ids=${itemIds}&columns=${columns}`);
}
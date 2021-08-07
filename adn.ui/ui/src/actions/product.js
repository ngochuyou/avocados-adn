import { $fetch, fjson, asBlob } from '../fetch';
import { normalize } from '../utils';

export function fetchCategoryList({ page = 0, size = 10, columns = [] }) {
	return fjson(`/rest/product/category/list?page=${page}&size=${size}&columns=${columns.join(',')}`);
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
	columns = [], categoryId = null,
	page = 0, size = 18
}) {
	if (categoryId == null || categoryId.length === 0) {
		return [null, "Category id was empty"];
	}

	return fjson(`/rest/product?category=${categoryId}&columns=${columns.join(',')}`);
}

export function searchProduct({ productId = "", productName = "", columns = [], size = 10 }) {
	if (productId.length === 0 && productName.length === 0) {
		return [[], null];
	}
	
	return fjson(`/rest/product/search?id.like=${normalize(productId)}&name.like=${normalize(productName)}&columns=${columns.join(',')}&size=${size}`);
}

export function createStockDetails(batch = []) {
	if (!Array.isArray(batch) || batch.length === 0) {
		return [null, "Invalid batch"];
	}

	return fjson(`/rest/product/stockdetail`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			details: batch
		})
	});
}
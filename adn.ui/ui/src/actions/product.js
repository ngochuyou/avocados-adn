import { $fetch, fjson } from '../fetch';

export async function fetchCategoryList({ page = 0, size = 10, columns = [] }) {
	return await fjson(`/rest/category/list?page=${page}&size=${size}&columns=${columns.join(',')}`);
}

export async function fetchCategoryCount() {
	return await fjson(`/rest/category/count`);
}

async function doModifyCategory(method, model = null) {
	if (model == null) {
		return [null, "Model was null"];
	}

	return await fjson('/rest/category', {
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

	const [res, err] = await $fetch(`/rest/category/activation?id=${categoryId}&active=${state}`, {
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
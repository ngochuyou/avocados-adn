import { $fetch } from '../fetch';

export async function fetchProviderList({ page = 0, size = 10, columns = [] }) {
	const [res, err] = await $fetch(`/rest/provider?page=${page}&size=${size}&columns=${columns.join(',')}`);

	if (err) {
		return [null, err];
	}
	try {
		if (res.ok) {
			return [await res.json(), null];
		}

		return [null, await res.text()];
	} catch (exception) {
		return [null, exception];
	}
}

export async function fetchProviderCount() {
	const [res, err] = await $fetch(`/rest/provider/count`);

	if (err) {
		return [null, err];
	}
	try {
		if (res.ok) {
			return [await res.json(), null];
		}

		return [null, await res.text()];
	} catch (exception) {
		return [null, exception];
	}
}
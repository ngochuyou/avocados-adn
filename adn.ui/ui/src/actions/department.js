import { $fetch } from '../fetch';

export async function fetchDepartments(columns = []) {
	const [res, err] = await $fetch(`/rest/department?columns=${columns.join(',')}`, {
		method: 'GET',
		headers: {
			'Accept' : 'application/json'
		}
	});

	if (!err) {
		if (res.ok) {
			return await res.json();
		}
	}

	return null;
};

export const fetchChiefs = async (departmentIds = [], columns = []) => {
	if (departmentIds.length === 0) {
		return [];
	}

	const [res, err] = await $fetch(`/rest/department/chiefs?ids=${departmentIds.join(',')}&columns=${columns.join(',')}`, {
		method: 'GET',
		headers: {
			'Accept' : 'application/json'
		}
	});

	if (!err) {
		if (res.ok) {
			return await res.json();
		}
	}

	return null;
}

export const fetchPersonnelCounts = async (departmentIds = []) => {
	if (departmentIds.length === 0) {
		return [];
	}

	const [res, err] = await $fetch(`/rest/department/count?ids=${departmentIds.join(',')}`, {
		method: 'GET',
		headers: {
			'Accept' : 'application/json'
		}
	});

	if (!err) {
		if (res.ok) {
			return await res.json();
		}
	}

	return null;
}

export const fetchPersonnelListByDepartment = async (departmentId = null, columns = [], page = 0, size = 5) => {
	if (departmentId == null) {
		return [null, "Department id was null"];
	}

	const [res, err] = await $fetch(`/rest/department/personnel-list/${departmentId}?columns=${columns.join(',')}&page=${page}&size=${size}`, {
		method: 'GET',
		headers: {
			'Accept': 'application/json'
		}
	});

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		return [await res.json(), null];
	}

	return [null, await res.text()];
}
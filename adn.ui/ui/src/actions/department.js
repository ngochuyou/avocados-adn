import { $fetch } from '../fetch';

export async function fetchDepartments(columns = []) {
	const [res, err] = await $fetch(`/list/department?columns=${columns.join(',')}`, {
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
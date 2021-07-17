import { $fetch } from '../fetch';

export const fetchAccount = async (username = null, columns = []) => {
	if (username == null) {
		return [null, "Username was empty"];
	}

	const [res, err] = await $fetch(`/rest/account/${username}?columns=${columns.join(',')}`, {
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

export async function lockAccount(username = "") {
	if (username.length === 0) {
		return [null, "Username was null"];
	}

	const [res, err] = await $fetch(`/rest/account/deact/${username}`, {
		method: 'GET',
		headers: {
			'Accept': 'text/plain'
		}
	});

	if (err) {
		return [null, err];
	}

	if (res.ok) {
		return [await res.text(), null];
	}

	return [null, await res.text()];
}
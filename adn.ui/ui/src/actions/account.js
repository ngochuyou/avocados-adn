import { $fetch, fjson } from '../fetch';

export const fetchAccount = async (username = null, columns = []) => {
	if (username == null) {
		return [null, "Username was empty"];
	}

	return await fjson(`/rest/account/${username}?columns=${columns.join(',')}`, {
		method: 'GET'
	});
}

export async function lockAccount(username = "") {
	if (username.length === 0) {
		return [null, "Username was null"];
	}

	const [res, err] = await $fetch(`/rest/account/deactivate/${username}`, {
		method: 'PATCH',
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
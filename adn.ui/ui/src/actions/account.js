import { $fetch } from '../fetch';

export const fetchAccount = async (username = null, columns = []) => {
	if (username == null) {
		return [null, "Username was empty"];
	}

	const [res, err] = await $fetch(`/rest/account?username=${username}&columns=${columns.join(',')}`, {
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
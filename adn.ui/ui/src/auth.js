import { $fetch } from './fetch.js';
import { server } from './config/default.json';

export async function fetchPrincipal(...columns) {
	const [res, err] = await $fetch(`/rest/user?columns=${columns.join(',')}`, {
		method: 'GET',
		headers: {
			'Accept' : 'application/json',
			'Content-Type': 'application/json'
		}
	});

	if (err) {
		console.error(err);
		return [null, err];
	}

	if (res.ok) {
		return [await res.json(), null];
	}

	return [null, await res.json()];
}

export async function fetchToken({ username, password}) {
	let formData = new FormData();

	formData.append("username", username);
	formData.append("password", password);

	const [res, err] = await $fetch(server.auth.token_url, {
		method: "POST",
		body: formData
	});

	if (err) {
		console.error(err);
		return [null, err];
	}

	if (res.ok) {
		return [await res.text(), null];
	}

	return [null, await res.text()];
}
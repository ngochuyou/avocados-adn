import { $fetch } from './fetch.js';
import { server } from './config/default.json';

export async function fetchPrincipal(...columns) {
	const [res, err] = await $fetch(`/rest/account?columns=${columns.join(',')}`, {
		method: 'GET',
		headers: {
			'Accept' : 'application/json',
			'Content-Type': 'application/json'
		}
	});

	if (err) {
		return null;
	}

	if (res.ok) {
		return await res.json();
	}

	return null;
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
		return null;
	}

	return res;
}
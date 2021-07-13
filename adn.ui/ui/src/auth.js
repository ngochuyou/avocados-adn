import { $fetch } from './fetch.js';
import { server } from './config/default.json';

export async function fetchPrincipal() {
	const [res, err] = await $fetch('/rest/account', {
		method: 'GET',
		headers: {
			'Authorization': 'JWTBearer',
			'Accept' : 'application/json',
			'Content-Type': 'application/json'
		},
		credentials: 'include'
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
		body: formData,
		credentials: 'include'
	});

	if (err) {
		return null;
	}

	return res;
}
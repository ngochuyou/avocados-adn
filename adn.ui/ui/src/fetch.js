import { server } from "./config/default.json";

const url = server.url;

export async function $fetch(endpoint, options) {
	try {
		const res = await fetch(`${url}${encodeURI(endpoint)}`, {
			...options,
			headers: {
				...options.headers,
				'Authorization': 'JWTBearer',
			},
			credentials: 'include'
		});

		return [res, null];
	} catch(error) {
		return [null, error];
	}
}

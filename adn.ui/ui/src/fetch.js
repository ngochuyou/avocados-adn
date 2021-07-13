import { server } from "./config/default.json";

const url = server.url;

export async function $fetch(endpoint, options) {
	try {
		const res = await fetch(`${url}${endpoint}`, options);

		return [res, null];
	} catch(error) {
		return [null, error];
	}
}

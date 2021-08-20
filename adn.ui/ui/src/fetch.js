import { server } from "./config/default.json";

const url = server.url;

export async function $fetch(endpoint = null, options = {}) {
	if (endpoint == null) {
		return [null, "Endpoint was null"];
	}
	
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

export async function fjson(endpoint = null, options = {}) {
	const { headers } = options;
	const [res, err] = await $fetch(endpoint, {
		...options,
		headers: headers != null ? {
			...headers,
			'Accept': 'application/json'
		} : {
			'Accept': 'application/json'
		}
	});

	if (err) {
		return [null, err];
	}
	
	try {
		if (res.ok) {
			return [await res.json(), null];
		}

		return [null, await res.json()];
	} catch (exception) {
		return [null, exception];
	}
}

export function asBlob(model = null) {
	return model == null ? [null, "Model was null"] : new Blob([JSON.stringify(model)], { type: "application/json" });
}
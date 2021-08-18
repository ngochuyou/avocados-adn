import { fjson } from '../fetch';

export function fetchProviderList({ page = 0, size = 10, columns = [] }) {
	return fjson(`/rest/provider?page=${page}&size=${size}&columns=${columns.join(',')}`);
}

export function fetchProviderCount() {
	return fjson(`/rest/provider/count`);
}

export function obtainProvider({
	providerId = null, columns = []
}) {
	if (providerId == null) {
		return [null, "Provider ID was null"];
	}

	return fjson(`/rest/provider/${providerId}?columns=${columns.join(',')}`);
}

export function searchProvider({ name = "", columns = [], size = 500 }) {
	return fjson(`/rest/provider/search?name.like=${name}&columns=${columns.join(',')}&size=${size}`);
}
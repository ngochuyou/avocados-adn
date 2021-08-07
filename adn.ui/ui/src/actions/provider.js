import { fjson } from '../fetch';

export function fetchProviderList({ page = 0, size = 10, columns = [] }) {
	return fjson(`/rest/provider?page=${page}&size=${size}&columns=${columns.join(',')}`);
}

export function fetchProviderCount() {
	return fjson(`/rest/provider/count`);
}

export function searchProvider({ name = "", columns = [], size = 10 }) {
	return fjson(`/rest/provider/search?name.like=${name}&columns=${columns.join(',')}&size=${size}`);
}
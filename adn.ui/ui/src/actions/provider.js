import { fjson } from '../fetch';

export async function fetchProviderList({ page = 0, size = 10, columns = [] }) {
	return await fjson(`/rest/provider?page=${page}&size=${size}&columns=${columns.join(',')}`);
}

export async function fetchProviderCount() {
	return await fjson(`/rest/provider/count`);
}
import { fjson } from '../fetch';

import { hasLength, join } from '../utils';

export function getProvincesList({
	page = 0, size = 100, columns = []
}) {
	return fjson(`/rest/admindivision/province?page=${page}&size=${size}&columns=${join(columns)}`);
}

export function getDistrictsList({
	page = 0, size = 100,
	columns = [], province = null
}) {
	if (!hasLength(province)) {
		return [null, "Province ID was empty"];
	}

	return fjson(`/rest/admindivision/district/${province}?page=${page}&size=${size}&columns=${join(columns)}`);
}
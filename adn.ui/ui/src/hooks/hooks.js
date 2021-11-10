import { useEffect, useState, useReducer, useCallback } from 'react';
import { intRange, MONTH_NAMES, DATE_NAMES } from '../utils';

export const useInput = initValue => {
	const [value, setValue] = useState(initValue);
	const onValueChange = useCallback((value) => setValue(value), []);

	return [
		{ value, onChange: (event) => setValue(event.target.value) },
		onValueChange
	];
}

export const useInputSet = (initValue, initError = null) => {
	const [value, setValue] = useState(initValue);
	const [err, setError] = useState(initError);
	const onValueChange = useCallback((value) => setValue(value), []);

	return [
		{ value, onChange: (event) => setValue(event.target.value) },
		onValueChange,
		err,
		setError
	];
}

export const useStateWithMessage = (initValue, initMessage = null) => {
	const [state, setState] = useState(initValue);
	const [message, setMessage] = useState(initMessage);

	return [
		state, setState,
		message, setMessage
	];
}

export const useDispatch = (initStore, dispatchers) => useReducer(
	(oldState, { type = null, payload = null } = {}) => {
		const dispatcher = dispatchers[type];

		return dispatcher != null ? dispatcher(payload, oldState) : oldState;
	}, {...initStore}
);

export const useToggle = (initState = false) => useReducer((oldState) => !oldState, initState);

const getYearsFromOffset = (offset) => {
	const offsetDate = new Date(`01-01-${offset}`);

	return [
		...intRange({min: 1, max: 11}).map(index => offsetDate.getFullYear() - index),
		...intRange({min: 1, max: 11}).map(index => offsetDate.getFullYear() + index),
		offset
	].sort();
}

const getMonthsSet = () => {
	return MONTH_NAMES.map((name, index) => ({
		value: index + 1,
		name
	}));
}

export const useTemporal = ({
	initYears = null,
	initSelectedYear = undefined,
	initMonths = null,
	initSelectedMonth = undefined,
	initDays = null,
	initSelectedDay = undefined
} = {}) => {
	const current = new Date();
	const defaultYear = current.getFullYear();
	const [years, setYears] = useState(initYears == null ? getYearsFromOffset(defaultYear) : initYears);
	const [selectedYear, setSelectedYear] = useReducer((current, next) => parseInt(next), initSelectedYear || defaultYear);
	const [months, setMonths] = useState(initMonths == null ? getMonthsSet() : initMonths);
	const [selectedMonth, setSelectedMonth] = useReducer((current, next) => parseInt(next), initSelectedMonth || current.getMonth() + 1);
	const [days, setDays] = useState(initDays == null ? [] : initDays);
	const [selectedDay, setSelectedDay] = useReducer((current, next) => parseInt(next), initSelectedDay || current.getDate());

	useEffect(() => {
		setDays(intRange({min: 0, max: new Date(selectedYear, selectedMonth, 0).getDate()}).map(index => ({
			value: index + 1,
			name: DATE_NAMES[index]
		})));
	}, [selectedYear, selectedMonth]);

	return [
		years, setYears,
		selectedYear, setSelectedYear,
		months, setMonths,
		selectedMonth, setSelectedMonth,
		days, setDays,
		selectedDay, setSelectedDay
	];
};
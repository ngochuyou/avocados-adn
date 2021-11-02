import { useState, useReducer } from 'react';

export const useInput = initValue => {
	const [value, setValue] = useState(initValue);

	return [
		{ value, onChange: (event) => setValue(event.target.value) },
		() => setValue(initValue)
	];
}

export const useInputSet = (initValue, initError = null) => {
	const [value, setValue] = useState(initValue);
	const [err, setError] = useState(initError);

	return [
		{ value, onChange: (event) => setValue(event.target.value) },
		() => setValue(initValue),
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
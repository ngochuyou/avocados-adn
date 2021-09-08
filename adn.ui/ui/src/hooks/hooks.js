import { useState, useReducer } from 'react';

export const useInput = initValue => {
	const [value, setValue] = useState(initValue);

	return [
		{ value, onChange: (event) => setValue(event.target.value)},
		() => setValue(initValue)
	];
}

export const useDispatch = (initStore, dispatchers) => {
	const [store, dispatch] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = dispatchers[type];

			return dispatcher != null ? dispatcher(payload, oldState) : oldState;
		}, {...initStore}
	);

	return [store, dispatch];
}

export const useToggle = (initState = false) => {
	return useReducer((oldState) => !oldState, initState);
}
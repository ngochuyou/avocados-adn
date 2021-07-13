import { useState } from 'react';

export const useInput = initValue => {
	const [value, setValue] = useState(initValue);

	return [
		{ value, onChange: (event) => setValue(event.target.value)},
		() => setValue(initValue)
	];
}
import { memo } from 'react';
import { HEX_PATTERN, hex3tohex6 } from '../../utils';

export const ColorInput = memo(_ColorInput, (oldState, newState) => oldState.hex === newState.hex);

function _ColorInput(props) {
	let { hex: value } = props;

	if (!HEX_PATTERN.test(value)) {
		return (
			<input
				{...props}
				value="#000000"
				type="color"
			/>
		);
	}

	if (value.length === 4) {
		const [hex6, err] = hex3tohex6(value);

		return (
			<input
				{ ...props }
				value={err ? '#000000' : hex6}
				type="color"
			/>
		);
	}

	return (
		<input
			{ ...props }
			value={value}
			type="color"
		/>
	);
}
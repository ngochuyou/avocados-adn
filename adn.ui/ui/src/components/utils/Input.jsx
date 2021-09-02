import { useState, memo } from 'react';
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

export function SearchInput({
	value = "",
	placeholder = "Search",
	onEntered = () => null,
	onSearchBtnClick = () => null,
	onChange= () => null,
	dd = "",
	disabled = false,
	disableAll = false
}) {
	const [keyword, setKeyWord] = useState(value);
	const onKeywordChange = (event) => {
		const { value } = event.target;

		onChange(value, event);
		setKeyWord(value, event);
	};
	const onKeyDown = (event) => {
		if (event.keyCode === 13 && !disabled) {
			onEntered(keyword, event);
		}
	};
	const onSubmitBtnClick = (event) => {
		if (!disabled) {
			onSearchBtnClick(keyword, event);
		}
	}

	return (
		<div className="uk-flex uk-width-1-1">
			<div className="uk-width-expand">
				<input
					value={keyword}
					onChange={onKeywordChange}
					className="uk-input"
					placeholder={placeholder}
					onKeyDown={onKeyDown}
					disabled={disabled && disableAll}
				/>
				<dd className="uk-text-meta">{dd}</dd>
			</div>
			<div>
				<button
					className="uk-button backgroundf"
					onClick={onSubmitBtnClick}
					disabled={disabled}
				>Search</button>
			</div>
		</div>
	);
}
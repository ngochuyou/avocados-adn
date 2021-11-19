import { useState, useRef, memo } from 'react';
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
	const [keyword, setKeyWord] = useState(value || "");
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

export const PluralValueInput = memo(PurePluralValueInput, (o, n) => {
	return o.values === n.values && o.add === n.add
		&& o.remove === n.remove && o.validate === n.validate
		&& o.validateAll === n.validateAll;
});

function PurePluralValueInput({
	values = [],
	add = () => null,
	remove = () => null,
	placeholder = "New",
	className = "uk-width-large",
	type = "text",
	validate = () => true,
	validationError = "Invalid value"
}) {
	const inputRef = useRef(null);
	const errRef = useRef(null);
	const onAdd = () => {
		const { value } = inputRef.current;

		if (value.length === 0) {
			return;
		}

		if (validate(value)) {
			add(inputRef.current.value);
			errRef.current.innerText = "";
			inputRef.current.value = "";
			return;
		}

		errRef.current.innerText = validationError;
	};
	const onInputKeyDown = (event) => {
		if (event.keyCode === 13) {
			onAdd();
		}
	};

	return (
		<div className={`uk-padding-small ${className}`}>
			<ul className="uk-list uk-list-divider">
			{
				values.map((ele, index) => (
					<li key={index}>
						<div className="uk-flex">
							<div>
								<div
									className="uk-icon-button pointer" uk-icon="trash"
									onClick={() => remove(index, ele)}
								></div>
							</div>
							<div className="uk-margin-small-left uk-position-relative uk-width-medium">
								<div className="uk-position-center-left">{ele}</div>
							</div>
						</div>
					</li>
				))
			}
			</ul>
			<div className="uk-flex">
				<div className="uk-width-auto">
					<input
						ref={inputRef}
						placeholder={placeholder}
						className="uk-input"
						type={type}
						onKeyDown={onInputKeyDown}
					/>
					<div ref={errRef} className="uk-text-danger"></div>
				</div>
				<div>
					<div
						className="uk-icon-button pointer" uk-icon="plus"
						style={{width: "40px", height: "40px", marginLeft: "15px"}}
						onClick={onAdd}
					></div>
				</div>
			</div>
		</div>
	)
}
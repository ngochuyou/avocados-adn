import { useRef, memo } from 'react';

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
import { useReducer } from 'react';

const SET_SEARCH_INPUT_VALUE = "SET_SEARCH_INPUT_VALUE";

export default function Navbar({
	backButtonVisible = false,
	isSearchInputDisabled = false,
	backButtonClick = () => null,
	searchInputEntered = () => null,
	searchInputEmptied = () => null
}) {
	const [searchInputState, dispatchSearchInputState] = useReducer(
		(oldState, { type = null, payload = null} = {}) => {
			switch(type) {
				case SET_SEARCH_INPUT_VALUE: {
					return { ...oldState, value: payload };
				}
				default: return oldState;
			}
		}, {
			value: ""
		}
	);
	const onSearchInputChanged = async (event) => {
		if (isSearchInputDisabled) {
			return;
		}

		dispatchSearchInputState({
			type: SET_SEARCH_INPUT_VALUE,
			payload: event.target.value
		});

		if (event.target.value.length === 0) {
			searchInputEmptied();
		}
	};
	const onSearchButtonClick = () => {
		searchInputEntered(searchInputState.value);
	};
	const onSearchInputKeyUp = (event) => {
		if (event.keyCode === 13) {
			if (isSearchInputDisabled) {
				return;
			}

			searchInputEntered(searchInputState.value);
		}
	};
	const onBackButtonClick = () => {
		const result = backButtonClick();

		if (typeof result === 'function') {
			result();
		}
	};
	const onSubmit = (event) => {
		event.preventDefault();
	};
	const { value: searchInputValue } = searchInputState;
	
	return (
		<nav className="uk-navbar-container" uk-nav="">
			<div className="uk-grid-collapse" uk-grid="">
				<div className="uk-width-auto">
					<div
						style={{
							maxWidth: backButtonVisible ? "80px" : "0px",
							width: "80px"
						}}
						onClick={onBackButtonClick}
						className="uk-height-1-1 uk-position-relative transition-maxwidth-fast">
						<span
							className="uk-position-center uk-icon-button icon-size-small iconf"
							uk-icon="icon: arrow-left; ratio: 1.5"></span>
					</div>
				</div>
				<div className="uk-width-xlarge">
					<div className="uk-navbar-item">
						<form className="uk-width-1-1" onSubmit={onSubmit}>
							<input
								value={searchInputValue}
								onKeyUp={onSearchInputKeyUp}
								onChange={onSearchInputChanged}
								className="uk-input uk-width-3-4"
								type="text"
								placeholder="Search..."
								disabled={isSearchInputDisabled ? "disabled" : ""}
							/>
							<button
								disabled={isSearchInputDisabled ? "disabled" : ""}
								onClick={onSearchButtonClick}
								className="uk-button uk-button-default uk-width-1-4 backgroundf colorf"
							>Search</button>
						</form>
					</div>
				</div>
			</div>
		</nav>
	);
}
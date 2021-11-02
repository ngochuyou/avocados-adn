import {
	createContext, useContext,
	useState, useCallback
} from 'react';

import { useDispatch } from '../../hooks/hooks';

import { formatDate } from '../../utils';

const Context = createContext();

export const useNavbar = () => useContext(Context);

const SET_BACK_BTN_STATE = "SET_BACK_BTN_STATE";

const STORE = {
	className: "",
	backButtonVisible: false,
	backButtonClick: () => null,
	isSearchInputDisabled: false,
	searchInputEntered: () => null,
	searchInputEmptied: () => null,
	centerElement: null,
	outerRightElement: null
};

const dispatchers = {
	SET_BACK_BTN_STATE: (payload, oldState) => {
		const { visible, callback } = payload;
		
		return {
			...oldState,
			backButtonVisible: visible,
			backButtonClick: callback
		};
	}
};

export function ContextProvider({ children }) {
	const [store, dispatch] = useDispatch(STORE, dispatchers);

	const setBackBtnState = useCallback((nextState = {}) => {
		const { visible = false, callback = () => null } = nextState;
		if (typeof visible !== 'boolean' || typeof callback !== 'function') {
			return;
		}

		dispatch({
			type: SET_BACK_BTN_STATE,
			payload: nextState
		});
	}, [dispatch]);

	return (
		<Context.Provider value={{
			store, setBackBtnState
		}}>
			{ children }
		</Context.Provider>
	);
}

export default function Navbar() {
	const {
		store: {
			className,
			backButtonVisible,
			isSearchInputDisabled,
			backButtonClick,
			searchInputEntered,
			searchInputEmptied,
			centerElement,
			outerRightElement
		}
	} = useNavbar();
	const [searchInputVal, setSearchInputVal] = useState("");
	const onSearchInputChanged = async (event) => {
		if (isSearchInputDisabled) {
			return;
		}

		setSearchInputVal(event.target.value);

		if (event.target.value.length === 0) {
			searchInputEmptied();
		}
	};
	const onSearchButtonClick = () => {
		searchInputEntered(searchInputVal);
	};
	const onSearchInputKeyUp = (event) => {
		if (event.keyCode === 13) {
			if (isSearchInputDisabled) {
				return;
			}

			searchInputEntered(searchInputVal);
		}
	};
	const onBackButtonClick = () => {
		const result = backButtonClick();

		if (typeof result === 'function') {
			result();
		}
	};

	return (
		<nav className={`uk-navbar-container ${className}`} uk-nav="">
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
				<div className="uk-width-expand">
					{ centerElement }
				</div>
				<div className="uk-width-xlarge">
					<div className="uk-navbar-item">
						<div className="uk-width-1-1">
							<input
								value={searchInputVal}
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
						</div>
					</div>
				</div>
				<div className="uk-width-auto">
					{outerRightElement}
				</div>
				<div className="uk-width-auto">
					<div className="uk-navbar-item">
						{ formatDate(new Date()) }
					</div>
				</div>
			</div>
		</nav>
	);
};
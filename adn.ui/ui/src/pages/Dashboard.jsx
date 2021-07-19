import { useReducer, useContext, createContext } from 'react';
import { Link, Route } from 'react-router-dom';

import { useAuth } from '../hooks/authentication-hooks';

import Account from '../models/Account';
import AccessDenied from './AccessDenied.jsx';

import DepartmentBoard from '../components/dashboard/DepartmentBoard.jsx';
import ProviderBoard from '../components/dashboard/ProviderBoard.jsx';

const NavbarContext = createContext();
export const useNavbar = () => useContext(NavbarContext);

const SET_BACK_BUTTON_CALLBACK_AND_TOGGLE = "SET_BACK_BUTTON_CALLBACK_AND_TOGGLE";
const SET_SEARCH_INPUT_ONCHANGE = "SET_SEARCH_INPUT_ONCHANGE";
const SET_SEARCH_INPUT_VALUE = "SET_SEARCH_INPUT_VALUE";

const navbarStore = {
	backButtonState: {
		visible: false,
		callback: () => null
	},
	searchInputState: {
		value: "",
		onChange: () => null
	}
};

function NavbarContextProvider({ children }) {
	const [store, dispatch] = useReducer(
		(oldState, { type = null, payload = null} = {}) => {
			switch(type) {
				case SET_SEARCH_INPUT_VALUE: {
					const  { searchInputState } = oldState;

					return {
						...oldState,
						searchInputState: {
							...searchInputState,
							value: payload
						}
					}
				}
				case SET_BACK_BUTTON_CALLBACK_AND_TOGGLE: {
					const { backButtonState } = oldState;

					return {
						...oldState,
						backButtonState: {
							...backButtonState,
							visible: !backButtonState.visible,
							callback: payload
						}
					};
				}
				case SET_SEARCH_INPUT_ONCHANGE: {
					const { searchInputState } = oldState;

					return {
						...oldState,
						searchInputState: {
							...searchInputState,
							callback: payload
						}
					};
				}
				default: return oldState;
			}
		}, { ...navbarStore }
	);
	const setBackButtonCallbackAndToggle = (callback) => {
		if (typeof callback !== 'function') {
			return;
		}

		dispatch({
			type: SET_BACK_BUTTON_CALLBACK_AND_TOGGLE,
			payload: callback
		});
	};
	const setSearchInputOnChange = (onChange) => {
		if (typeof onChange !== 'function') {
			return;
		}

		dispatch({
			type: SET_SEARCH_INPUT_ONCHANGE,
			payload: onChange
		});
	};
	const changeSearchInputValue = (value) => {
		dispatch({
			type: SET_SEARCH_INPUT_VALUE,
			payload: value.toString()
		});
	};

	return <NavbarContext.Provider value={{
		store, setBackButtonCallbackAndToggle,
		setSearchInputOnChange, changeSearchInputValue
	}}>
		{ children }
	</NavbarContext.Provider>;
}

export default function Dashboard() {
	const { principal } = useAuth();

	if (principal && principal.role === Account.Role.ADMIN) {
		return (
			<NavbarContextProvider>
				<div className="uk-grid-collapse" uk-grid="">
					<div className="uk-width-1-5 backgroundf" uk-height-viewport="expand: true">
						<header className="uk-padding-small">
							<h3 className="colorf">
								<a className="uk-link-reset" href="/">Avocados</a>
							</h3>
							<ul className="uk-list uk-list-large uk-list-divider">
								<li>
									<Link to="/dashboard/provider" className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1">Provider</Link>
								</li>
								<li>
									<Link to="/dashboard/department" className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1">Department</Link>
								</li>
							</ul>
						</header>
					</div>
					<div className="uk-width-4-5 max-height-view uk-overflow-auto">
						<div>
							<Navbar />
							<div className="uk-padding-small">
								<Route
									path="/dashboard/department"
									render={props => (
										<DepartmentBoard
											{ ...props }
										/>
									)}
								/>
								<Route
									path="/dashboard/provider"
									render={props => (
										<ProviderBoard
											{ ...props }
										/>
									)}
								/>
							</div>
						</div>
					</div>
				</div>
			</NavbarContextProvider>
		);
	}

	return <AccessDenied message="Unauthorized role" />
}

function Navbar() {
	const { store: { backButtonState, searchInputState }, changeSearchInputValue } = useNavbar();
	const searchInputChanged = (event) => {
		changeSearchInputValue(event.target.value);
		searchInputState.onChange(searchInputState.value);
	};
	const onBackButtonClick = () => {
		const result = backButtonState.callback();

		if (typeof result === 'function') {
			result();
		}
	};

	return (
		<nav className="uk-navbar-container" uk-nav="">
			<div className="uk-grid-collapse" uk-grid="">
				<div className="uk-width-auto">
					<div
						style={{
							maxWidth: backButtonState.visible ? "80px" : "0px",
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
						<form className="uk-width-1-1">
							<input
								onKeyDown={searchInputChanged}
								className="uk-input uk-width-3-4"
								type="text"
								placeholder="Search..." />
							<button
								className="uk-button uk-button-default uk-width-1-4 backgroundf colorf">
								Search</button>
						</form>
					</div>
				</div>
			</div>
		</nav>
	);
}
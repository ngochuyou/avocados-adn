import { useReducer } from 'react';
import { Link, Route } from 'react-router-dom';

import { useAuth } from '../hooks/authentication-hooks';

import Account from '../models/Account';
import AccessDenied from './AccessDenied.jsx';

import DepartmentBoard from '../components/dashboard/DepartmentBoard.jsx';

const TOGGLE_BACK_BUTTON = "TOGGLE_BACK_BUTTON";
const SET_BACK_BUTTON_CALLBACK = "SET_BACK_BUTTON_CALLBACK";
const SET_STATE_AND_TOGGLE = "SET_STATE_AND_TOGGLE";

const BACK_BUTTON_INIT_STATE = {
	visible: false,
	callback: () => null
}

export default function Dashboard() {
	const { principal } = useAuth();
	const [ backButton, dispatchBackButton ] = useReducer(
		(oldState, { type = null, payload = null} = {}) => {
			switch (type) {
				case TOGGLE_BACK_BUTTON: {
					return {
						...oldState,
						visible: !oldState.visible
					}
				}
				case SET_BACK_BUTTON_CALLBACK: {
					if (typeof payload === 'function') {
						return {
							...oldState,
							callback: payload
						}
					}

					return oldState;
				}
				case SET_STATE_AND_TOGGLE: {
					const { callback } = payload;

					return {
						...oldState,
						visible: !oldState.visible,
						callback
					}
				}
				default: {
					return oldState;
				}
			}
		}, BACK_BUTTON_INIT_STATE);
	const backButtonClick = () => {
		let result = backButton.callback();

		while (typeof result === 'function') {
			result = result();
		}
	}

	if (principal && principal.role === Account.Role.ADMIN) {
		return (
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
						<nav className="uk-navbar-container" uk-nav="">
							<div className="uk-grid-collapse" uk-grid="">
								<div className="uk-width-auto">
									<div
										style={{
											maxWidth: backButton.visible ? "80px" : "0px",
											width: "80px"
										}}
										onClick={backButtonClick}
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
						<div className="uk-padding-small">
							<Route
								path="/dashboard/department"
								render={props => (
									<DepartmentBoard
										dispatchBackButton={dispatchBackButton}
										actions={{ SET_STATE_AND_TOGGLE }}
										{ ...props }
									/>
								)}
							/>
						</div>
					</div>
				</div>
			</div>
		);
	}

	return <AccessDenied message="Unauthorized role" />
}
import { Link, Route } from 'react-router-dom';

import { useAuth } from '../hooks/authentication-hooks';

import Account from '../models/Account';
import AccessDenied from './AccessDenied.jsx';

import DepartmentBoard from '../components/dashboard/DepartmentBoard.jsx';

export default function Dashboard() {
	const { principal } = useAuth();

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
								<div className="uk-width-1-2">
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
								<div className="uk-width-1-2">
									
								</div>
							</div>
						</nav>
						<div className="uk-padding-small">
							<Route path="/dashboard/department" render={props => <DepartmentBoard { ...props } />} />
						</div>
					</div>
				</div>
			</div>
		);
	}

	return <AccessDenied message="Unauthorized role" />
}
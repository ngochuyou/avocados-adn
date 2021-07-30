import { Link, Route } from 'react-router-dom';

import { useAuth } from '../hooks/authentication-hooks';

import Account from '../models/Account';
import AccessDenied from './AccessDenied.jsx';

import DepartmentBoard from '../components/dashboard/DepartmentBoard.jsx';
import ProviderBoard from '../components/dashboard/ProviderBoard.jsx';
import ProductBoard from '../components/dashboard/ProductBoard';

const dev = true;

export default function Dashboard() {
	const { principal } = useAuth();
	
	if (dev || (principal && (principal.role === Account.Role.ADMIN || principal.role === Account.Role.PERSONNEL))) {
		return (
			<div className="uk-grid-collapse" uk-grid="">
				<div className="uk-width-1-5 backgroundf" uk-height-viewport="expand: true">
					<header className="uk-padding-small">
						<h3 className="colorf">
							<a className="uk-link-reset" href="/">Avocados</a>
						</h3>
						<ul className="uk-list uk-list-large uk-list-divider">
							<li>
								<Link
									to="/dashboard/product"
									className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1"
								>Product</Link>
							</li>
							<li>
								<Link
									to="/dashboard/provider"
									className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1"
								>Provider</Link>
							</li>
							<li>
								<Link
									to="/dashboard/department"
									className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1"
								>Department</Link>
							</li>
						</ul>
					</header>
				</div>
				<div className="uk-width-4-5 max-height-view uk-overflow-auto">
					<div>
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
						<Route
							path="/dashboard/product"
							render={props => (
								<ProductBoard
									{ ...props }
								/>
							)}
						/>
					</div>
				</div>
			</div>
		);
	}

	return <AccessDenied message="Unauthorized role" />
}


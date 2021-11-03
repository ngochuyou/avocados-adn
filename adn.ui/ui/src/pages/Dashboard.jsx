import { useContext, createContext, useState } from 'react';
import { Link, Route } from 'react-router-dom';

import { ContextProvider as NavbarContextProvider } from '../components/dashboard/Navbar';
import { useAuth } from '../hooks/authentication-hooks';

import { profile, routes } from '../config/default';

import Account from '../models/Account';

import AccessDenied from './AccessDenied';

import { StockScope, SaleScope, PersonnelScope } from '../components/security/DepartmentScope';
import DepartmentBoard from '../components/dashboard/DepartmentBoard.jsx';
import ProviderBoard from '../components/dashboard/ProviderBoard.jsx';
import ProductBoard from '../components/dashboard/ProductBoard';
import StockBoard from '../components/dashboard/StockBoard';

const SidebarContext = createContext();
export const useSidebarContext = () => useContext(SidebarContext);

function SidebarContextProvider({ children }) {
	const [overlay, setOverlay] = useState(null);

	return (
		<SidebarContext.Provider value={{
			overlay, setOverlay
		}}>
			{ children }
		</SidebarContext.Provider>
	);
}

export default function Dashboard() {
	const { principal } = useAuth();
	const {
		dashboard: {
			provider: { mapping: providerMapping },
			product: { mapping: productMapping }
		}
	} = routes;

	if (profile.mode === 'DEV' || (principal && (principal.role === Account.Role.HEAD || principal.role === Account.Role.PERSONNEL))) {
		return (
			<NavbarContextProvider>
				<SidebarContextProvider>
					<div className="uk-grid-collapse" uk-grid="">
						<div className="uk-width-1-5 backgroundf uk-position-relative" uk-height-viewport="expand: true">
							<Sidebar />
						</div>
						<div className="uk-width-4-5 max-height-view uk-overflow-auto">
							<div>
								<StockScope>
									<Route
										path="/dashboard/stock"
										render={props => (
											<StockBoard
												{ ...props }
											/>
										)}
									/>
								</StockScope>
								<SaleScope>
									<Route
										path={`${providerMapping}`}
										render={props => (
											<ProviderBoard
												{ ...props }
											/>
										)}
									/>
									<Route
										path={`${productMapping}`}
										render={props => (
											<ProductBoard
												{ ...props }
											/>
										)}
									/>
								</SaleScope>
								<PersonnelScope>
									<Route
										path="/dashboard/department"
										render={props => (
											<DepartmentBoard
												{ ...props }
											/>
										)}
									/>
								</PersonnelScope>
							</div>
						</div>
					</div>
				</SidebarContextProvider>
			</NavbarContextProvider>
		);
	}

	return <AccessDenied message="Unauthorized role" />;
}

function Sidebar() {
	const { overlay, setOverlay } = useSidebarContext();
	const {
		dashboard: {
			provider: {
				mapping: providerBoardMapping,
				costs: { url: productCostsUrl }
			},
			product: {
				mapping: productBoardMapping,
				prices: { url: productPricesUrl },
				creation: { url: productCreationUrl }
			}
		}
	} = routes;

	return (
		<header className="uk-padding-small uk-height-1-1">
			<h3 className="colorf">
				<a className="uk-link-reset" href="/">Avocados</a>
			</h3>
			<ul className="uk-nav-default uk-nav-parent-icon" uk-nav="">
				<StockScope>
					<li>
						<Link
							to="/dashboard/stock"
							className="uk-link-reset"
						>Stock</Link>
					</li>
				</StockScope>
				<SaleScope>
					<li className="uk-parent">
						<Link
							to={productBoardMapping}
							className="uk-link-reset uk-parent"
						>Product</Link>
						<ul className="uk-nav-sub">
							<li>
								<Link
									to={productCreationUrl}
									className="uk-link-reset uk-parent"
								>Create Product</Link>
							</li>
							<li>
								<Link
									to={productPricesUrl}
									className="uk-link-reset uk-parent"
								>Prices</Link>
							</li>
						</ul>
					</li>
					<li className="uk-parent">
						<Link
							to={providerBoardMapping}
							className="uk-link-reset uk-parent"
						>Provider</Link>
						<ul className="uk-nav-sub">
							<li>
								<Link
									to={productCostsUrl}
									className="uk-link-reset uk-parent"
								>Costs</Link>
							</li>
						</ul>
					</li>
				</SaleScope>
				<PersonnelScope>
					<li>
						<Link
							to="/dashboard/department"
							className="uk-link-reset"
						>Department</Link>
					</li>
				</PersonnelScope>
			</ul>
		{
			overlay != null ? (
				<div className="uk-padding-small uk-position-top uk-width-1-1 uk-background-muted uk-height-1-1 uk-overflow-auto">
					<button
						type="button" uk-close=""
						onClick={() => setOverlay(null)}
					></button>
					{ overlay }
				</div>
			) : null
		}
		</header>
	);
}
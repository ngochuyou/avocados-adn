import { withRouter, Route } from 'react-router-dom';

import './App.css';

import { useAuth } from './hooks/authentication-hooks';

import AuthenticatedComponent from './components/security/AuthenticatedComponent.jsx';
import UnauthenticatedComponent from './components/security/UnauthenticatedComponent.jsx';

import LoginPage from './pages/LoginPage.jsx';
import AccessDenied from './pages/AccessDenied.jsx';
import Dashboard from './pages/Dashboard.jsx';
import HomePage from './pages/HomePage.jsx';
import ShoppingPage from './pages/ShoppingPage.jsx';
import ProductPage from './pages/ProductPage.jsx';
import NotFound from './pages/NotFound.jsx';

import { routes } from './config/default';

import ShoppingContextProvider from './hooks/shopping-hooks';

function App() {
	const { principal } = useAuth();

	return (
		<div>
			<AuthenticatedComponent principal={principal}>
				<Route path='/dashboard' render={props => <Dashboard { ...props } /> }/>
			</AuthenticatedComponent>
			<UnauthenticatedComponent principal={principal}>
				<Route path="/login" render={(props) => <LoginPage {...props}/> } />
			</UnauthenticatedComponent>
			<ShoppingContextProvider>
				<Route path="/" render={props => <HomePage />} exact/>
				<Route path='/access_denied' render={props => <AccessDenied { ...props } /> } exact />
				<Route path={`${routes.shopping.mapping}`} render={(props) => <ShoppingPage {...props}/> } />
				<Route path={`${routes.productView.mapping}`} render={(props) => <ProductPage {...props}/> } />
			</ShoppingContextProvider>
			<Route path="/notfound" component={NotFound} />
		</div>
	);
}

export default withRouter(App);

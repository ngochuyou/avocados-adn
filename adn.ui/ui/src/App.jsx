import { withRouter, Route, Switch } from 'react-router-dom';

import './App.css';

import { useAuth } from './hooks/authentication-hooks';

import AuthenticatedComponent from './components/security/AuthenticatedComponent.jsx';
import UnauthenticatedComponent from './components/security/UnauthenticatedComponent.jsx';

import LoginPage from './pages/LoginPage.jsx';
import AccessDenied from './pages/AccessDenied.jsx';
import Dashboard from './pages/Dashboard.jsx';
import HomePage from './pages/HomePage.jsx';
import ShoppingPage from './pages/ShoppingPage.jsx';
import NotFound from './pages/NotFound.jsx';

function App() {
	const { principal } = useAuth();

	return (
		<div>
			<AuthenticatedComponent principal={principal}>
				<Route path='/dashboard' render={props => <Dashboard { ...props } /> }/>
			</AuthenticatedComponent>
			<UnauthenticatedComponent principal={principal}>
				<Switch>
					<Route path="/login" render={(props) => <LoginPage {...props}/> } />
				</Switch>
			</UnauthenticatedComponent>
			<Switch>
				<Route path='/access_denied' render={props => <AccessDenied { ...props } /> } exact />
				<Route path="/" render={props => <HomePage />} exact/>
				<Route path="/shop/:categoryName?" render={(props) => <ShoppingPage {...props}/> } />
				<Route component={NotFound} />
			</Switch>
		</div>
	);
}

export default withRouter(App);

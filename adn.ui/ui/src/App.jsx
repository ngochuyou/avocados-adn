import { withRouter, Route, Switch } from 'react-router-dom';

import './App.css';

import { useAuth } from './hooks/authentication-hooks';

import AuthenticatedComponent from './components/security/AuthenticatedComponent.jsx';
import UnauthenticatedComponent from './components/security/UnauthenticatedComponent.jsx';

import LoginPage from './pages/LoginPage.jsx';
import AccessDenied from './pages/AccessDenied.jsx';
import Dashboard from './pages/Dashboard.jsx';
import HomePage from './pages/HomePage.jsx';

function App() {
	const { principal } = useAuth();

	return (
		<div>
			<Route path='/access_denied' render={props => <AccessDenied { ...props } /> } exact />
			<AuthenticatedComponent principal={principal}>
				<Route path='/dashboard' render={props => <Dashboard { ...props } /> }/>
			</AuthenticatedComponent>
			<UnauthenticatedComponent principal={principal}>
				<Switch>
					<Route path="/" render={props => <HomePage />} exact/>
					<Route path="/login" render={(props) => <LoginPage {...props}/> } />
				</Switch>
			</UnauthenticatedComponent>
		</div>
	);
}

export default withRouter(App);

import { useAuth } from './hooks/authentication-hooks';
import AuthenticatedComponent from './components/security/AuthenticatedComponent.jsx';
import UnauthenticatedComponent from './components/security/UnauthenticatedComponent.jsx';
import './App.css';
import { withRouter, Route, Switch } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';

function App() {
	const { principal } = useAuth();

	return (
		<div className="uk-container">
			<AuthenticatedComponent principal={principal}>
				<h1>Authenticated</h1>
			</AuthenticatedComponent>
			<UnauthenticatedComponent principal={principal}>
				<Switch>
					<Route path="/login" render={ (props) => <LoginPage {...props} /> } />
				</Switch>
			</UnauthenticatedComponent>
		</div>
	);
}

export default withRouter(App);

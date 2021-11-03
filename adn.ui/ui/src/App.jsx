import { withRouter, Route, Switch } from 'react-router-dom';

import './App.css';

// import { useAuth } from './hooks/authentication-hooks';

// import AuthenticatedComponent from './components/security/AuthenticatedComponent.jsx';
// import UnauthenticatedComponent from './components/security/UnauthenticatedComponent.jsx';
import GlobalContexts from './components/context/GlobalContexts';

// import LoginPage from './pages/LoginPage.jsx';
import AccessDenied from './pages/AccessDenied.jsx';
import Dashboard from './pages/Dashboard.jsx';
import HomePage from './pages/HomePage.jsx';
import ShoppingPage from './pages/ShoppingPage.jsx';
import NotFound from './pages/NotFound.jsx';
import Favorites from './pages/Favorites.jsx';

import ProductView from './components/product/ProductView';
import CartView from './components/product/CartView';

import { routes } from './config/default';

// import ShoppingContextProvider from './hooks/shopping-hooks';

function App() {
	// const { principal } = useAuth();

	return (
		<GlobalContexts>
			<Switch>
				<Route path='/dashboard' render={props => <Dashboard { ...props } /> }/>
				{/*<UnauthenticatedComponent principal={principal}>
					<Route path="/login" render={(props) => <LoginPage {...props}/> } />
				</UnauthenticatedComponent>*/}
				<Route path={`${routes.cart.mapping}`} render={props => <CartView />}/>
				<Route path={`${routes.productView.mapping}`} render={props => <ProductView />}/>
				<Route path={`${routes.favorites.mapping}`} render={props => <Favorites />} />
				<Route path={`${routes.home.mapping}`} render={props => <HomePage />} />
				<Route path='/access_denied' render={props => <AccessDenied { ...props } /> } exact />
				<Route path={`${routes.shopping.mapping}`} render={(props) => <ShoppingPage {...props}/> } />
				<Route path="/notfound" component={NotFound} />
			</Switch>
		</GlobalContexts>
	);
}

export default withRouter(App);

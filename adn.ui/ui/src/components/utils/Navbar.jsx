import { Link } from 'react-router-dom';

import { signOut as doSignOut } from '../../actions/account';

import Account from '../../models/Account';

import { useAuth } from '../../hooks/authentication-hooks';
import { useToggle } from '../../hooks/hooks';

import { asIf } from '../../utils';

import { NoFollow } from './Link';
import { CenterModal } from './Modal';
import LoginForm from '../security/LoginForm';
import RegisterForm from '../account/RegisterForm';
import { DomainImage } from './Gallery';

import { routes } from '../../config/default';

const linkStyle = {
	minHeight: "100%"
};

export default function Navbar({
	className=""
}) {
	const { principal } = useAuth();

	return (
		<div
			uk-sticky="animation: uk-animation-slide-top; sel-target: .uk-navbar-container; cls-active: uk-navbar-sticky; cls-inactive: uk-background-default; top: 200"
		>
			<nav
				className={`uk-navbar-container uk-position-relative uk-padding-small ${className}`}
				style={{height: "50px"}}
				uk-navbar="mode: click"
			>
				<div className="uk-navbar-left uk-padding-small uk-padding-remove-top uk-padding-remove-right uk-padding-remove-bottom" uk-height-match="">
					<ul className="uk-navbar-nav">
						<li>
						{
							asIf(principal != null && principal.role !== Account.Role.CUSTOMER)
							.then(() => <Link to={routes.dashboard.url} style={linkStyle}>Dashboard</Link>)
							.else()
						}
						</li>
					</ul>
				</div>
				<div className="uk-navbar-center">
					<div
						className="uk-navbar-item uk-logo colors noselect pointer"
						style={{fontSize: "2rem"}}
					>
						<Link
							className="uk-link-reset"
							to={routes.home.url}
						>Avocados</Link>
					</div>
				</div>
				<div className="uk-navbar-right">
					<ul className="uk-navbar-nav">
					{
						asIf(principal == null)
						.then(() => <UnauthenticatedRightNavbar />)
						.else(() => <AuthenticatedRightNavbar />)
					}
					</ul>
				</div>
			</nav>
		</div>
	);
}

function UnauthenticatedRightNavbar({
	className=""
}) {
	return (
		<>
			<li><FavoriteProductsNav /></li>
			<li><SignUpSignIn /></li>
		</>
	);
}

function SignUpSignIn() {
	const [authModalVisible, toggleAuthModalVision] = useToggle();
	const onSignUpSignInClick = () => toggleAuthModalVision();
	
	return (
		<>
			<NoFollow
				style={linkStyle}
				onClick={onSignUpSignInClick}
			>Sign up & Sign in</NoFollow>
			{
				asIf(authModalVisible)
				.then(() => (
					<CenterModal
						close={toggleAuthModalVision}
						className="uk-border-rounded"
					>
						<div className="uk-grid-medium uk-child-width-1-2" uk-grid="">
							<div className="uk-position-relative">
								<div className="uk-position-center">
									<LoginForm />
								</div>
							</div>
							<div>
								<RegisterForm
									onSuccess={() => window.location.reload(true)}
								/>
							</div>
						</div>
					</CenterModal>
				))
				.else()
			}
		</>
	);
}

function AuthenticatedRightNavbar({
	className=""
}) {
	return (
		<>
			<li>
				<Link
					style={linkStyle}
					to={routes.cart.url}
				>Cart</Link>
			</li>
			<li><FavoriteProductsNav /></li>
			<li><RightDropDownNav /></li>
		</>
	);
}

const USER_PHOTO_STYLE = {
	width: "50px",
	height: "50px"
}

function RightDropDownNav() {
	const { principal, evictPrincipal } = useAuth();

	const signOut = async () => {
		const [, err] = await doSignOut();

		if (err) {
			console.error(err);
			return;
		}

		evictPrincipal();
	};

	return (
		<>
			<NoFollow
				style={linkStyle}
			>
				<div
					style={USER_PHOTO_STYLE}
					className="uk-border-circle uk-overflow-hidden"
				>
					<DomainImage
						url={`/user/photo?filename=${principal.photo}`}
						name={principal.photo}
						fit="contained"
					/>
				</div>
			</NoFollow>
				<div className="uk-navbar-dropdown">
				<ul className="uk-nav uk-navbar-dropdown-nav noselect">
					<li>
						<NoFollow
							onClick={signOut}
						>Sign out</NoFollow>
					</li>
				</ul>
			</div>
		</>
	);
}

function FavoriteProductsNav() {
	return <Link
		style={linkStyle}
		to={routes.favorites.url}
	>Favorites</Link>;
}
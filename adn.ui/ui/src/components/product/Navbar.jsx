import { useHistory } from 'react-router-dom';

import { useShopping } from '../../hooks/shopping-hooks';

import { routes } from '../../config/default';

export default function Navbar({
	background = "uk-background-default"
}) {
	const {
		categoryStore: { elements, view: { target } }
	} = useShopping();
	const { push } = useHistory();
	const selectCategory = (category) => {
		if (target != null && category.id === target.id) {
			return;
		}

		push(`${routes.shopping.url}/${category.name}`);
	};

	return (
		<div uk-sticky="animation: uk-animation-slide-top; sel-target: .uk-navbar-container; cls-active: uk-navbar-sticky; top: 200">
			<nav className={`uk-navbar-container uk-navbar-transparent uk-position-relative uk-padding-small ${background}`} uk-nav="">
				<div className="uk-navbar-left" uk-height-match="">
					<ul
						className="uk-navbar-nav uk-margin-large-left"
						style={{height: "50px"}}
					>
						<li className="uk-padding-small-left uk-padding-small-right uk-position-relative">
							<div className="uk-position-center pointer noselect colors">Categories</div>
							<div className="uk-padding-remove" uk-dropdown="">
								<ul className="uk-nav uk-dropdown-nav">
								{
									elements.map((category) => (
										<li
											key={category.id}
											className="uk-padding-small pointer noselect uk-box-shadow-hover-small"
											onClick={() => selectCategory(category)}
										>{category.name}</li>
									))
								}
								</ul>
							</div>
						</li>
					</ul>
				</div>
				<div className="uk-navbar-center">
					<div className="uk-navbar-item uk-logo colors">Avocados</div>
				</div>
				<div className="uk-navbar-right">
				</div>
			</nav>
		</div>
	);
}
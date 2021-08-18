import { createContext, useContext, useReducer, useEffect } from 'react';
import { Link } from 'react-router-dom';

import { getAllCategories } from '../actions/product';
import { SET_LIST } from '../actions/common';

import { SourcedImage } from '../components/utils/Gallery'

const CATEGORY_STORE = {
	elements: []
}

const PRODUCT_STORE = {
	elements: [
		{
			id: "abc",
			name: "Plain White Shirt",
			images: ["alexi-romano-CCx6Fz_CmOI-unsplash.jpg"]
		},
		{
			id: "def",
			name: "Plain White Shirt",
			images: ["atikh-bana-_KaMTEmJnxY-unsplash.jpg"]
		},
		{
			id: "ghi",
			name: "Plain White Shirt",
			images: ["cesar-la-rosa-HbAddptme1Q-unsplash.jpg"]
		},
		{
			id: "jkl",
			name: "Plain White Shirt",
			images: ["freestocks-_3Q3tsJ01nc-unsplash.jpg"]
		}
	]
}

const Context = createContext();

const useGlobalContext = () => useContext(Context);

function ContextProvider({ children }) {
	const [categoryStore, dispatchCategoryStore] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			const dispatcher = cactegoryDispatchers[type];

			return dispatcher == null ? oldState : dispatcher(payload, oldState);
		}, { ...CATEGORY_STORE }
	);
	useEffect(() => {
		const doFetch = async () => {
			const [categories, fetchCategoriesErr] = await getAllCategories();

			if (fetchCategoriesErr) {
				console.error(fetchCategoriesErr);
				return;
			}

			dispatchCategoryStore({
				type: SET_LIST,
				payload: categories
			});
		};

		doFetch();
	}, []);

	return (
		<Context.Provider value={{
			categoryStore, dispatchCategoryStore
		}}>
			{ children }
		</Context.Provider>
	);
}

const cactegoryDispatchers = {
	SET_LIST: (payload, oldState) => {
		if (!Array.isArray(payload)) {
			return oldState;
		}

		return {
			...oldState,
			elements: payload
		};
	}
}

export default function HomePage() {
	return (
		<ContextProvider>
			<Main />
		</ContextProvider>
	);
}

function Main() {
	const {
		categoryStore: { elements }
	} = useGlobalContext();

	return (
		<div>
			{/*<div uk-slideshow="autoplay: false; autoplay-interval: 2000; pause-on-hover: false">*/}
			<div uk-slideshow="animation: scale; autoplay: true; autoplay-interval: 2000; pause-on-hover: false">
				<ul className="uk-slideshow-items" uk-height-viewport="min-height: 300">
				{
					PRODUCT_STORE.elements.map(model => (
						<li key={model.id}>
							<SourcedImage
								src={`/img/${model.images[0]}`}
								name={`/img/${model.images[0]}`}
							/>
						</li>
					))
				}
				</ul>
				<button
					className="uk-position-center-left uk-position-small uk-hidden-hover"
					uk-slidenav-previous="" uk-slideshow-item="previous"
				></button>
				<button
					className="uk-position-center-right uk-position-small uk-hidden-hover"
					uk-slidenav-next="" uk-slideshow-item="next"
				></button>
			</div>
			<div className="uk-position-top">
				<nav className="uk-navbar-container uk-navbar-transparent" uk-navbar="">
					<div className="uk-navbar-left">
						<ul className="uk-navbar-nav">
							<li className="uk-active uk-padding-small uk-margin-large-left pointer uk-text-center">
								<div
									style={{color: "white"}}
									className="uk-width-small"
									uk-toggle="target: #offcanvas-menu" uk-icon="icon: menu; ratio: 2"
								></div>
							</li>
						</ul>
					</div>
					<div className="uk-navbar-right">
						<ul className="uk-navbar-nav">
							<li className="uk-active uk-padding-small uk-margin-large-right pointer uk-text-center">
								<div
									style={{color: "white"}}
									className="uk-width-small"
									uk-toggle="target: #offcanvas-dashboard" uk-icon="icon: grid; ratio: 2"
								></div>
							</li>
						</ul>
					</div>
				</nav>
			</div>
			<div>
				<div id="offcanvas-menu" uk-offcanvas="mode: reveal; overlay: true">
					<div className="uk-offcanvas-bar uk-flex uk-flex-column">
						<ul className="uk-nav uk-nav-primary uk-nav-center uk-margin-auto-vertical">
							<li className="uk-nav-header">Categories</li>
							{
								elements.map(category => (
									<li
										key={category.id} className="uk-margin pointer noselect"
									>
										<div className="uk-transition-toggle">
											<Link to={`/shop/${category.name}`} className="uk-link-reset">
												<div
													className="uk-transition-scale-up"
													style={{ opacity: "1" }}
												>{category.name}</div>
											</Link>
										</div>
									</li>
								))
							}
							<li className="uk-nav-divider"></li>
						</ul>

					</div>
				</div>
			</div>
			<div>
				<div id="offcanvas-dashboard" uk-offcanvas="flip: true; mode: reveal; overlay: true">
					<div className="uk-offcanvas-bar uk-flex uk-flex-column">

						<ul className="uk-nav uk-nav-primary uk-nav-center uk-margin-auto-vertical">
							{/*<li className="uk-active"><a href="#">Active</a></li>
							<li className="uk-parent">
								<a href="#">Parent</a>
								<ul className="uk-nav-sub">
									<li><a href="#">Sub item</a></li>
									<li><a href="#">Sub item</a></li>
								</ul>
							</li>
							<li className="uk-nav-header">Header</li>
							<li><a href="#"><span className="uk-margin-small-right" uk-icon="icon: table"></span> Item</a></li>
							<li><a href="#"><span className="uk-margin-small-right" uk-icon="icon: thumbnails"></span> Item</a></li>
							<li className="uk-nav-divider"></li>
							<li><a href="#"><span className="uk-margin-small-right" uk-icon="icon: trash"></span> Item</a></li>*/}
						</ul>

					</div>
				</div>
			</div>
		</div>
	);
}
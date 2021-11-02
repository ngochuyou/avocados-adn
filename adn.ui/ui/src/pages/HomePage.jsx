import { useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';

import { getAllCategories } from '../actions/product';
import { getProductListByCategory, getProductPrices } from '../actions/product';

import { SourcedImage } from '../components/utils/Gallery'
import ProductList from '../components/product/ProductList'
import Navbar from '../components/utils/Navbar';

import { useProduct } from '../hooks/product-hooks';

import { routes } from '../config/default';

import { hasLength } from '../utils';

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

export default function HomePage() {
	return (
		<div>
			<Navbar
				className="uk-background-default"
			/>
			<Head />
			<Body />
		</div>
	);
}

function Head() {
	return (
		<header>
			{/*<div uk-slideshow="autoplay: false; autoplay-interval: 2000; pause-on-hover: false">*/}
			<div uk-slideshow="animation: scale; autoplay: false; autoplay-interval: 2000; pause-on-hover: false">
				<ul className="uk-slideshow-items" uk-height-viewport="offset-top: true; min-height: 300">
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
			{/*<div className="uk-position-top">
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
			</div>*/}
			{/*<div>
				<div id="offcanvas-dashboard" uk-offcanvas="flip: true; mode: reveal; overlay: true">
					<div className="uk-offcanvas-bar uk-flex uk-flex-column">
						<ul className="uk-nav uk-nav-primary uk-nav-center uk-margin-auto-vertical">*/}
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
						{/*</ul>
					</div>
				</div>
			</div>*/}
		</header>
	);
}

function Body() {
	return (
		<main className="uk-position-relative">
			<div
				className="uk-grid-collapse uk-grid-match"
				uk-grid=""
			>
				<div className="uk-width-1-4 uk-padding-small">
					<SideNav />
				</div>
				<div className="uk-width-3-4 uk-position-relative" uk-height-viewport="" uk-overflow-auto="">
					<ProductListView />
				</div>
			</div>
		</main>
	);
};

export const FETCHED_PRODUCT_COLUMNS = ["id", "name", "rating", "images"]

function SideNav() {
	const {
		store: {
			category: { elements: categories }
		},
		setCategories, setProducts,
		mergePrices
	} = useProduct();
	const { category } = useParams();
	const { push } = useHistory();

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await getAllCategories();

			if (err) {
				console.error(err);
				return;
			}

			setCategories(res);
		};

		doFetch();
	}, [setCategories]);

	useEffect(() => {
		if (!hasLength(category)) {
			if (!hasLength(categories)) {
				return;
			}

			push(`${routes.home.url}/${categories[0].name}`);
			return;
		}

		const doFetch = async () => {
			let [res, err] = await getProductListByCategory({
				identifierName: "name",
				identifier: category,
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);

			[res, err] = await getProductPrices(res.map(product => product.id));

			if (err) {
				console.error(err);
				return;
			}

			mergePrices(res);
		};

		doFetch();
	}, [category, categories, setProducts, mergePrices, push]);

	const onSelectCategory = (category) => push(`${routes.home.url}/${category.name}`);
	
	return (
		<div>
			<ul className="uk-nav uk-nav-center uk-nav-primary uk-margin-auto-vertical">
				<li className="uk-nav-header uk-text-bold">Categories</li>
				{
					categories.map(category => (
						<li
							key={category.id} className="uk-margin pointer noselect"
							onClick={() => onSelectCategory(category)}
						>
							<div className="uk-transition-toggle">
								<div
									className="uk-transition-scale-up"
									style={{ opacity: "1" }}
								>{category.name}</div>
							</div>
						</li>
					))
				}
				<li className="uk-nav-divider"></li>
			</ul>
		</div>
	);
}

function ProductListView() {
	const {
		store: {
			product: { elements: products }
		}
	} = useProduct();
	const { push } = useHistory();
	const { productView: { url: productViewUrl } } = routes;

	return (
		<div className="uk-padding-small">
			<ProductList
				onItemClick={(p) => push(`${productViewUrl}/${p.id}`)}
				list={Object.values(products)}
			/>
		</div>
	);
}
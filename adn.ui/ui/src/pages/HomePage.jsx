import { useEffect, useMemo } from 'react';
import { useParams, useHistory, useLocation } from 'react-router-dom';

import { addCart } from '../actions/account';
import { getAllCategories, getProductListByCategory, getProductPrices } from '../actions/product';

import { SourcedImage } from '../components/utils/Gallery'
import ProductList from '../components/product/ProductList'
import Navbar from '../components/utils/Navbar';
import { NoFollow } from '../components/utils/Link';
import { SearchInput } from '../components/utils/Input';
import PagedComponent from '../components/utils/PagedComponent';

import { useProduct } from '../hooks/product-hooks';
import { useInput } from '../hooks/hooks';

import { routes } from '../config/default';

import { hasLength, updateURLQuery } from '../utils';

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
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);
	const [startPriceProps, setStartPrice] = useInput(urlParams.get('from') || "");
	const [endPriceProps, setEndPrice] = useInput(urlParams.get('to') || "");

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
				columns: FETCHED_PRODUCT_COLUMNS,
				from: urlParams.get("from"),
				to: urlParams.get("to"),
				name: urlParams.get("name"),
				sort: urlParams.get("sort"),
				page: urlParams.get("page"),
				size: urlParams.get("size")
			});

			if (err) {
				return console.error(err);
			}

			setProducts(res);

			if (!hasLength(res)) {
				return;
			}

			[res, err] = await getProductPrices(res.map(product => product.id));

			if (err) {
				console.error(err);
				return;
			}

			mergePrices(res);
		};

		doFetch();
	}, [category, categories, setProducts, mergePrices, push, urlParams]);

	const onSelectCategory = (category) => push(`${routes.home.url}/${category.name}?${urlParams.toString()}`);
	const onApply = () => push(`${routes.home.url}/${category}?${[{name: "from", value: startPriceProps.value}, {name: "to", value: endPriceProps.value}]
		.reduce((params, current) => {
			const { name, value } = current;

			if (!hasLength(value)) {
				return params;
			}

			return new URLSearchParams(updateURLQuery(params, name, () => value));
		}, urlParams)}`);
	const onClear = () => {
		setStartPrice("");
		setEndPrice("");
		push(`${routes.home.url}/${category}`);
	};
	const onPriceSortChange = (event) => {
		const { target: { value } } = event;

		push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "sort", () => `price,${value === "-1" ? "" : value}`)}`);
	};

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
				<li className="uk-nav-header uk-text-bold">Filter</li>
				<li>
					<div>
						<div className="uk-flex uk-flex-center">
							<div className="uk-margin-small-right">
								<input
									type="number" min="50000"
									className="uk-input"
									placeholder="Start price"
									{...startPriceProps}
								/>
							</div>
							<div>
								<input
									type="number" min="50000"
									className="uk-input"
									placeholder="End price"
									{...endPriceProps}
								/>
							</div>
						</div>
						<div className="uk-flex uk-margin-small">
							<select
								className="uk-select"
								onChange={onPriceSortChange}
							>
								<option value="-1">None</option>
								<option value="asc">Ascending</option>
								<option value="desc">Descending</option>
							</select>
						</div>
					</div>
					<div className="uk-margin uk-flex uk-flex-center">
						<ul className="uk-subnav uk-subnav-pill" uk-margin="">
							<li
								onClick={onApply}
							><NoFollow>Apply</NoFollow></li>
							<li
								onClick={onClear}
							><NoFollow>Clear</NoFollow></li>
						</ul>
					</div>
				</li>
			</ul>
		</div>
	);
}

function ProductListView() {
	const {
		store: {
			product: { elements: productsMap }
		}
	} = useProduct();
	const { push } = useHistory();
	const { category } = useParams();
	const { productView: { url: productViewUrl } } = routes;
	const { search: query } = useLocation();
	const urlParams = useMemo(() => new URLSearchParams(query), [query]);

	const onCartClick = async (p) => {
		const [res, err] = await addCart({
			productId: p.id,
			quantity: 1
		});

		if (err) {
			console.error(err);
			return;
		}

		console.log(res);
	};
	const sort = urlParams.get("sort");
	let products = Object.values(productsMap);
	
	if (!hasLength(products) || products[0].price == null) {
		return (
			<div className="uk-padding-small">
				<SearchInput
					value={urlParams.get("name")}
					onEntered={(key) => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "name", () => key)}`)}
				/>
				<PagedComponent
					pageCount={0}
					onNextPageRequest={() => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "page", (p) => (+p || 0) + 1)}`)}
					onPreviousPageRequest={() => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "page", (p) => +p - 1)}`)}
					currentPage={urlParams.get("page")}
				>
					<ProductList list={[]} />
				</PagedComponent>
			</div>
		);
	}
	
	if (hasLength(sort)) {
		products = products.sort((left, right) => {
			if (sort.includes("asc")) {
				return left.price < right.price ? -1 : 1;
			}

			return left.price > right.price ? -1 : 1;
		});
	}

	return (
		<div className="uk-padding-small">
			<SearchInput
				value={urlParams.get("name")}
				onEntered={(key) => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "name", () => key)}`)}
			/>
			<PagedComponent
				pageCount={products.length}
				onNextPageRequest={() => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "page", (p) => (+p || 0) + 1)}`)}
				onPreviousPageRequest={() => push(`${routes.home.url}/${category}?${updateURLQuery(urlParams, "page", (p) => +p - 1)}`)}
				currentPage={urlParams.get("page")}
			>
				<ProductList
					onItemClick={(p) => push(`${productViewUrl}/${p.id}`)}
					list={products}
					onCartClick={onCartClick}
				/>
			</PagedComponent>
		</div>
	);
}
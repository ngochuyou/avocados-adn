import { useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
// aaldredkq
// atidman64
import { getProductListByCategory } from '../actions/product';
import {
	SET_LIST, SET_INDIVIDUAL_VIEW_TARGET
} from '../actions/common';

// import Navbar from '../components/product/Navbar';
import ProductList from '../components/product/ProductList';
import {
	useShopping, FETCHED_PRODUCT_COLUMNS
} from '../hooks/shopping-hooks';

import { routes } from '../config/default';

import { asIf, linear } from '../utils';

const { log } = console;

export default function ShoppingPage() {
	return <Main />;
}

function Main() {
	const { categoryName } = useParams();
	const {
		wasInit, init,
		categoryStore: {
			elements: categories
		},
		productStore: {
			list: { fetchStatusByCategories: productFetchStatusByCategories }
		},
		dispatchProductStore, dispatchCategoryStore
	} = useShopping();

	useEffect(() => asIf(!wasInit).then(init).else(), [wasInit, init]);
	useEffect(() => {
		const doFetch = async () => {
			log(`category name: ${categoryName}`);

			if (categoryName == null || categories == null || categories.length === 0) {
				log(`skip on ${categoryName}, ${categories}`);
				return;
			}

			const category = linear(categories, "name", categoryName);
			const categoryId = category.id;

			if (productFetchStatusByCategories[categoryId] != null) {
				log(`skip on fetch status`);
				dispatchCategoryStore({
					type: SET_INDIVIDUAL_VIEW_TARGET,
					payload: category
				});
				return;
			}

			log("do fetch");
			const [products, fetchProductsErr] = await getProductListByCategory({
				identifier: categoryId,
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (fetchProductsErr) {
				console.error(fetchProductsErr);
				return;
			}

			dispatchProductStore({
				type: SET_LIST,
				payload: { categoryId, products }
			});
			dispatchCategoryStore({
				type: SET_INDIVIDUAL_VIEW_TARGET,
				payload: category
			});
		};

		doFetch();
	}, [categoryName, categories, productFetchStatusByCategories, dispatchCategoryStore, dispatchProductStore]);

	return (
		<div className="uk-background-muted">
			{/*<Navbar
				background="uk-background-default"
			/>*/}
			<Body />
		</div>
	);
}

function Body() {
	const {
		productStore: {
			list: {
				elements: productsListMap
			}
		},
		categoryStore: { view: { target: viewedCategory } }
	} = useShopping();
	const { push } = useHistory();
	const onSelectProduct = async (product) => {
		push({
			pathname: `${routes.productView.url}/${product.id}`,
			state: {
				...product,
				category: viewedCategory
			}
		});
	};
	const category = viewedCategory == null ? {} : viewedCategory;
	const productList = productsListMap[category.id];

	return (
		<main>
			<div
				className="uk-padding uk-position-relative"
				uk-height-viewport="offset-top: true"
			>
				<ProductList
					header={
						<span className="uk-text-lead">{category.name}</span>
					}
					onItemClick={onSelectProduct}
					list={productList}
				/>
			</div>
		</main>
	);
}
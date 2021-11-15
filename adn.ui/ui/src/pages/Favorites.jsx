import { useEffect } from 'react';
import { useHistory } from 'react-router-dom';

import { getProductList, getProductPrices } from '../actions/product';

import Navbar from '../components/utils/Navbar';
import ProductList from '../components/product/ProductList';

import { routes } from '../config/default';

import { useProduct } from '../hooks/product-hooks';

import { locateBookmarks, mtokeys } from '../utils';

export const FETCHED_PRODUCT_COLUMNS = ["id", "name", "rating", "images"]

export default function Favorites() {
	const {
		store: {
			product: { elements: products }
		},
		setProducts, mergePrices
	} = useProduct();
	const { productView: { url: productViewUrl } } = routes;
	const { push } = useHistory();

	useEffect(() => {
		const doFetch = async () => {
			const ids = mtokeys(locateBookmarks());

			if (ids.length === 0) {
				return;
			}

			let [res, err] = await getProductList({
				ids,
				columns: FETCHED_PRODUCT_COLUMNS
			});

			if (err) {
				console.error(err);
				return;
			}

			setProducts(res);

			[res, err] = await getProductPrices(ids);

			if (err) {
				console.error(err);
				return;
			}

			mergePrices(res);
		};

		doFetch();
	}, [setProducts, mergePrices]);

	return (
		<div>
			<Navbar />
			<main className="uk-padding uk-padding-remove-top">
				<h3 className="uk-heading-line uk-margin-small-top colors">
					Your favorite Products
				</h3>
				<div
					className="uk-grid-small"
					uk-grid=""
				>
					<div className="uk-width-2-6">
						
					</div>
					<div className="uk-width-4-6">
						<ProductList
							list={Object.values(products)}
							onItemClick={(p) => push(`${productViewUrl}/${p.id}`)}
						/>
					</div>
				</div>
			</main>
		</div>
	);
}
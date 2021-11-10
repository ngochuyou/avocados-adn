import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

import Account from '../../models/Account';

import { useProduct, useCart } from '../../hooks/product-hooks';
import { useAuth } from '../../hooks/authentication-hooks';

import { getProductPrices, obtainProduct, getItemsListByProduct } from '../../actions/product';
import { addCart } from '../../actions/account';

import Navbar from '../utils/Navbar';
import { DomainProductImage } from '../utils/Gallery'
import Rating from '../utils/Rating.jsx';

import { asIf, formatVND, hasLength } from '../../utils';

export default function ProductView() {
	const { productId } = useParams();
	const {
		store: {
			product: { elements: productMap }
		},
		mergeProducts, mergePrices
	} = useProduct();
	const { addItem } = useCart();
	const { principal } = useAuth();

	useEffect(() => {
		if (!hasLength(productId)) {
			return;
		}

		const doFetch = async () => {
			const [product, productFetchErr] = await obtainProduct({
				id: productId,
				columns: [
					"name", "description", "rating",
					"images"
				]
			});

			if (productFetchErr) {
				console.error(productFetchErr);
				return;
			}

			const [items, itemsFetchErr] = await getItemsListByProduct({
				productId,
				columns: ["color", "namedSize"]
			});

			if (itemsFetchErr) {
				console.error(itemsFetchErr);
				return;
			}

			mergeProducts({ [productId]: {
				...product,
				id: productId,
				items
			} });

			const [price, priceFetchErr] = await getProductPrices([productId]);

			if (priceFetchErr) {
				console.error(priceFetchErr);
				return;
			}

			mergePrices(price);
		};

		doFetch();
	}, [mergeProducts, mergePrices, productId]);

	const product = productMap[productId];

	if (product == null || product.items == null) {
		return (
			<div>
				<Navbar />
				<h3 className="uk-heading-line uk-text-center">
					<span>Loading...</span>
				</h3>
			</div>
		);
	}

	const addToCart = async (product, color, namedSize) => {
		const [addedItemIds, err] = await addCart({
			productId: product.id,
			color, namedSize,
			quantity: 1
		});

		if (err) {
			console.error(err);
			return;
		}
		// we're only adding one item
		addItem([{
			id: addedItemIds[0],
			product, color, namedSize
		}]);
	}

	return (
		<div>
			<Navbar />
			<div className="uk-padding uk-padding-remove-top">
				<div className="uk-grid-small" uk-grid="">
					<div className="uk-width-3-5 uk-padding-small">
						<div
							uk-slideshow="animation: slide"
							className="uk-position-relative uk-box-shadow-medium"
						>
							<ul className="uk-slideshow-items">
							{
								product.images.map(image => (
									<li key={image}>
										<div style={{height: "400px"}}>
											<DomainProductImage
												name={image}
												fit="contain"
											/>
										</div>
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
						<div className="uk-margin">
							<h4 className="uk-heading-line colors">
								<span>Description</span>
							</h4>
							<p>{ product.description }</p>
						</div>
					</div>
					<div className="uk-width-2-5 uk-padding-small">
						<div className="uk-flex">
							<div
								className="uk-text-large colors uk-width-4-5"
							>{ product.name }</div>
							<div className="uk-width-1-5 uk-height-match">
								<Rating value={product.rating} />
							</div>
						</div>
						<p className="colors uk-text-bold">
							<span uk-icon="tag" className="uk-margin-small-right"></span>
							<span>{formatVND(product.price)}</span>
						</p>
						<div className="uk-margin">
							<div className="uk-heading-line colors uk-text-right">
								<span>
									<span
										uk-icon="icon: cart"
										className="uk-margin-small-right"
									></span>
									Shop now
								</span>
							</div>
							<div className="uk-margin">
								<table className="uk-table uk-table-middle uk-text-center">
									<thead>
										<tr>
											<th>Color</th>
											<th className="uk-text-center">Size</th>
											<th className="uk-text-center">Quantity</th>
											<th></th>
										</tr>
									</thead>
									<tbody>
									{
										asIf(hasLength(product.items))
										.then(() => product.items.map((item, key) => (
											<tr key={key}>
												<td>
													<div
														style={{height: "40px", width: "40px"}}
														className="uk-box-shadow-large uk-border-circle uk-overflow-hidden"
													>
														<div
															style={{backgroundColor: item.color}}
															className="uk-height-1-1"
														></div>
													</div>
												</td>
												<td>
													<div className="uk-text-muted uk-text-bold">{item.namedSize}</div>
												</td>
												<td>
													<label className="uk-label backgroundf">{`${item.quantity} left`}</label>
												</td>
												<td>
												{
													asIf(principal != null && principal.role === Account.Role.CUSTOMER)
													.then(() => (
														<button
															onClick={() => addToCart(product, item.color, item.namedSize)}
															uk-icon="icon: cart"
															className="uk-icon-button"
															uk-tooltip="Add to your cart"
														></button>
													)).else()
												}
												</td>
											</tr>
										)))
										.else(() => <tr>
											<td className="uk-text-muted" colSpan="3">Currently out of stock</td>
										</tr>)
									}
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}
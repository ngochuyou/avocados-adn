import { useState, useEffect } from 'react';

import Account from '../../models/Account';

import { useAuth } from '../../hooks/authentication-hooks';

import { getCart, addCart, subtractCart } from '../../actions/account';
import { getProductPrices } from '../../actions/product';

import Navbar from '../utils/Navbar';
import { DomainProductImage } from '../utils/Gallery'

import AccessDenied from '../../pages/AccessDenied';

import { asIf, formatVND } from '../../utils';

const getSpec = (item) => `${item.product.id}-${item.color}-${item.namedSize}`;
const ADD = "ADD";
const SUBTRACT = "SUBTRACT";

export default function CartView() {
	const { principal } = useAuth();
	const [cart, setCart] = useState([]);
	const [visualCart, setVisualCart] = useState({});
	const [priceMap, setPriceMap] = useState({});
	const isCustomer = (principal != null && principal.role === Account.Role.CUSTOMER);

	useEffect(() => {
		if (!isCustomer) {
			return;
		}

		const doFetch = async () => {
			const [fetchedCart, err] = await getCart({
				productColumns: ["id", "name", "images"],
				itemColumns: ["id", "color", "namedSize"]
			});

			if (err) {
				console.error(err);
				return;
			}

			const [fetchedPriceMap, fetchPriceErr] = await getProductPrices(Object.keys(fetchedCart.reduce((productIds, current) => {
				productIds[current.product.id] = null;
				return productIds;
			}, {})));

			if (fetchPriceErr) {
				console.error(fetchPriceErr);
				return;
			}

			const fullyFetchedCart = fetchedCart.map(item => ({
				...item,
				price: fetchedPriceMap[item.product.id]
			}));

			setPriceMap(fetchedPriceMap);
			setCart(fullyFetchedCart);
			setVisualCart(fullyFetchedCart.reduce((groupedCart, current) => {
				const specification = getSpec(current);

				if (groupedCart[specification] == null) {
					return {
						...groupedCart,
						[specification]: {
							...current,
							quantity: 1,
							refs: [current.id]
						}
					};
				}

				return {
					...groupedCart,
					[specification]: {
						...groupedCart[specification],
						quantity: groupedCart[specification].quantity + 1,
						refs: [...groupedCart[specification].refs, current.id]
					}
				};
			}, {}));
		};

		doFetch();
	}, [isCustomer, setCart, setVisualCart]);

	if (!isCustomer) {
		return <AccessDenied message="Unauthorized role" />;
	}

	const onModifyItem = (action, item) => {
		if (action === ADD) {
			return doAddItem(item);
		}

		return doRemoveItem(item);
	};

	const doAddItem = async (item) => {
		const specification = getSpec(item);
		const [newItemId, err] = await addCart({
			productId: item.product.id,
			color: item.color, namedSize: item.namedSize,
			quantity: 1
		});

		if (err) {
			console.error(err);
			return;
		}

		setCart([...cart, {
			...item,
			id: newItemId,
			price: priceMap[item.product.id]
		}]);
		setVisualCart({
			...visualCart,
			[specification]: {
				...visualCart[specification],
				quantity: visualCart[specification].quantity + 1,
				refs: [...visualCart[specification].refs, newItemId]
			}
		});
	};

	const doRemoveItem = async (item) => {
		const specification = getSpec(item);

		if (visualCart[specification] == null) {
			return;
		}

		const referencedItemIds = [...visualCart[specification].refs];
		const removedItemId = referencedItemIds[0];
		const [, err] = await subtractCart([removedItemId]);

		if (err) {
			console.error(err);
			return;
		}

		const currentQuantity = visualCart[specification].quantity;

		setCart(cart.filter(item => item.id !== removedItemId));

		if (currentQuantity === 1) {
			const newState = {...visualCart};

			delete newState[specification];

			return setVisualCart(newState);
		}

		setVisualCart({
			...visualCart,
			[specification]: {
				...visualCart[specification],
				quantity: currentQuantity - 1,
				refs: referencedItemIds.slice(1, referencedItemIds.length)
			}
		});
	};

	return (
		<div>
			<Navbar />
			<main className="uk-padding uk-padding-remove-top uk-padding-small-top">
				<h3 className="uk-heading-line">
					<span>Your cart
						<span uk-icon="icon: cart" className="uk-margin-small-left"></span>
					</span>
				</h3>
				<div className="uk-grid-small" uk-grid="">
					<div className="uk-width-3-5 uk-padding-small">
						<table className="uk-table uk-table-middle">
							<thead>
								<tr>
									<th className="uk-table-shrink uk-padding-remove"></th>
									<th className="uk-padding-remove"></th>
									<th className="uk-padding-remove"></th>
								</tr>
							</thead>
							<tbody>
							{
								Object.values(visualCart).map(item => {
									const { product } = item;

									return <tr
										key={item.id}
										className="uk-box-shadow-hover-medium"
									>
										<td>
											<div
												style={{width: "100px", height: "100px"}}
												className="uk-border-circle uk-overflow-hidden"
											>
												<DomainProductImage name={product.images[0]} />
											</div>
										</td>
										<td>
											<div className="uk-text-large colors">{product.name}</div>
											<div className="uk-margin-small uk-flex">
												<div>
													<p>Size</p>
												</div>
												<div className="uk-margin-left">
													<label className="uk-label backgroundf">{item.namedSize}</label>
												</div>
											</div>
											<div className="uk-margin-small">
												<div
													style={{height: "40px", width: "40px"}}
													className="uk-box-shadow-large uk-border-circle uk-overflow-hidden"
												>
													<div
														style={{backgroundColor: item.color}}
														className="uk-height-1-1"
													></div>
												</div>
											</div>
										</td>
										<td>
											<div>{formatVND(item.price)}</div>
											<div className="uk-margin" uk-margin="">
											{
												asIf(item.quantity !== 1)
												.then(() => <span
													className="uk-icon-button"
													uk-icon="icon: minus"
													onClick={() => onModifyItem(SUBTRACT, item)}
												></span>)
												.else(() => <span
													className="uk-icon-button"
													uk-icon="icon: trash"
													onClick={() => onModifyItem(SUBTRACT, item)}
												></span>)
											}
												<span className="uk-margin-left uk-margin-right noselect">{item.quantity}</span>
												<span
													className="uk-icon-button"
													uk-icon="icon: plus"
													onClick={() => onModifyItem(ADD, item)}
												></span>
											</div>
											<div>{formatVND(item.price * item.quantity)}</div>
										</td>
									</tr>;
								})
							}
							</tbody>
						</table>
					</div>
					<div className="uk-width-2-5 uk-padding-small uk-box-shadow-large">
						<div className="uk-padding-small">
							<table className="uk-table uk-table-middle">
								<thead>
									<tr>
										<td className="uk-table-shrink uk-padding-remove"></td>
										<td className="uk-padding-remove"></td>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td><label className="uk-label backgrounds">Total items</label></td>
										<td><p className="uk-text-large">{cart.length}</p></td>
									</tr>
									<tr>
										<td><label className="uk-label backgrounds">Cuts</label></td>
										<td><p className="uk-text-large"></p></td>
									</tr>
									<tr>
										<td><label className="uk-label backgrounds">Fees</label></td>
										<td><p className="uk-text-large"></p></td>
									</tr>
									<tr>
										<td><label className="uk-label backgrounds">Cart total</label></td>
										<td><p className="uk-text-large">{formatVND(cart.reduce((total, current) => total + current.price, 0))}</p></td>
									</tr>
								</tbody>
							</table>
							<div className="uk-heading-line">
								<span>
									<span uk-icon="file-edit" className="uk-margin-small-right"></span>
									Delivery informations
								</span>
							</div>
						</div>
					</div>
				</div>
			</main>
		</div>
	);
}
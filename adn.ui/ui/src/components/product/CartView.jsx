import {
	useState, useEffect, createContext, useContext
} from 'react';
import { Link, useHistory } from 'react-router-dom';

import Account from '../../models/Account';

import { useAuth } from '../../hooks/authentication-hooks';
import { useInputSet } from '../../hooks/hooks';

import {
	getCart, addCart, subtractCart, emptyCart
} from '../../actions/account';
import { placeOrder } from '../../actions/order';
import { getProductPrices } from '../../actions/product';
import { getProvincesList, getDistrictsList } from '../../actions/utils';

import Navbar from '../utils/Navbar';
import { DomainProductImage } from '../utils/Gallery';
import { CenterModal } from '../utils/Modal';

import AccessDenied from '../../pages/AccessDenied';

import {
	getItemSpecification, ErrorTracker,
	asIf, formatVND, hasLength, groupCartItems
} from '../../utils';

import { routes } from '../../config/default';

const ADD = "ADD";
const SUBTRACT = "SUBTRACT";

const LocalContext = createContext();

const useLocalContext = () => useContext(LocalContext);

function LocalContextProvider({ children }) {
	const [cart, setCart] = useState([]);
	const [provinces, setProvinces] = useState([]);
	const [province, setProvince] = useState(null);
	const [districts, setDistricts] = useState([]);
	const [districtProps, setDistrict, districtError, setDistrictErr] = useInputSet("");
	const [addressProps, , addressError, setAddressErr] = useInputSet("441/80/15 Lê Văn Qưới P. Bình Trị Đông A. Q.Bình Tân");
	const [noteProps, , noteError, setNoteErr] = useInputSet("");

	return (
		<LocalContext.Provider value={{
			cart, setCart,
			provinces, setProvinces,
			province, setProvince,
			districts, setDistricts,
			districtProps, setDistrict, districtError, setDistrictErr,
			addressProps, addressError, setAddressErr,
			noteProps, noteError, setNoteErr
		}}>
			{ children }		
		</LocalContext.Provider>
	);
}

export default function CartView() {
	return (
		<LocalContextProvider>
			<Main></Main>
		</LocalContextProvider>
	);
}

function Main() {
	const { principal } = useAuth();
	const {cart, setCart} = useLocalContext();
	const [visualCart, setVisualCart] = useState({});
	const [priceMap, setPriceMap] = useState({});
	const [deleteCartConfirmVisible, setDeleteCartConfirmVisible] = useState(false);
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
			setVisualCart(groupCartItems(fullyFetchedCart, (current, existing) => {
				if (existing == null) {
					return { refs: [current.id] };
				}

				return { refs: [...existing.refs, current.id] }
			}));
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
		const specification = getItemSpecification(item);
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
		const specification = getItemSpecification(item);

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

	const onEmptyCart = async () => {
		setDeleteCartConfirmVisible(false);

		if (!hasLength(cart)) {
			return;
		}

		const [, err] = await emptyCart();

		if (err) {
			console.error(err);
			return;
		}

		setCart([]);
		setVisualCart({});
		setPriceMap({});
	};

	return (
		<div>
			<Navbar />
			<main className="uk-padding uk-padding-remove-top uk-padding-small-top">
				<h3 className="uk-heading-line uk-text-center">
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
										<td className="noselect">
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
										<td className="noselect">
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
					<div className="uk-width-2-5 uk-padding-small uk-box-shadow-large uk-border-rounded uk-border-hidden">
					{
						asIf(hasLength(cart))
						.then(() => <div className="uk-padding-small">
							<CartSummary></CartSummary>
							<div>
								<div className="uk-heading-line colors">
									<span>
										<span uk-icon="credit-card" className="uk-margin-small-right"></span>
										Payment method
									</span>
								</div>
								<div className="uk-margin">
									<select
										disabled="disabled"
										className="uk-select"
									>
										<option>Bank transfer</option>
									</select>
								</div>
							</div>
							<DeliveryInstructions />
							<table className="uk-table uk-table-middle noselect">
								<thead></thead>
								<tbody>
									<tr>
										<td className="uk-padding-remove">
											<span
												className="uk-text-danger pointer"
												onClick={() => setDeleteCartConfirmVisible(!deleteCartConfirmVisible)}
											>Delete cart</span>
										</td>
										{
											asIf(deleteCartConfirmVisible)
											.then(() => (
												<>
													<td
														onClick={() => setDeleteCartConfirmVisible(false)}
														className="uk-padding-remove"
													>
														<span className="colors pointer">Never mind</span>
													</td>
													<td
														onClick={onEmptyCart}
														className="uk-padding-remove"
													>
														<span className="uk-text-muted pointer">Delete my cart</span>
													</td>
												</>
											))
											.else()
										}
									</tr>
								</tbody>
							</table>
						</div>)
						.else(() => (
							<div className="uk-position-relative" uk-height-viewport="offset-top: true">
								<h4
									className="uk-heading-line uk-position-center"
								>
									<Link className="uk-link-reset" to={routes.home.url}>Let's start by adding some items into your cart</Link>
								</h4>
							</div>
						))
					}
					</div>
				</div>
			</main>
		</div>
	);
}

function CartSummary() {
	const { cart } = useLocalContext();

	return (
		<>
			<div className="uk-heading-line colors">
				<span>
					<span uk-icon="list" className="uk-margin-small-right"></span>
					Cart Summary
				</span>
			</div>
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
		</>
	);
}

const VIEW = "VIEW";
const EDIT = "EDIT";

function DeliveryInstructions({
	mode = EDIT
}) {
	if (mode === EDIT) {
		return <EditableDeliveryInstructions />
	}

	return <ReadonlyDeliveryInstructions />
}

function ReadonlyDeliveryInstructions() {
	const {
		districts, districtProps, addressProps,
		noteProps, province
	} = useLocalContext();
	const district = districts.filter(dis => dis.id === parseInt(districtProps.value))[0];

	return (
		<div>
			<div className="uk-heading-line colors">
				<span>
					<span uk-icon="file-edit" className="uk-margin-small-right"></span>
					Delivery Instructions
				</span>
			</div>
			<table className="uk-table uk-table-middle">
				<thead>
					{/*<tr>
						<td className="uk-table-shrink uk-padding-remove"></td>
						<td className="uk-padding-remove"></td>
					</tr>*/}
				</thead>
				<tbody>
					<tr>
						<td><label className="uk-label backgrounds">Province</label></td>
						<td><p className="uk-text-medium">{province.name}</p></td>
					</tr>
					<tr>
						<td><label className="uk-label backgrounds">District</label></td>
						<td><p className="uk-text-medium">{district.name}</p></td>
					</tr>
					<tr>
						<td><label className="uk-label backgrounds">Address</label></td>
						<td><p className="uk-text-medium">{addressProps.value}</p></td>
					</tr>
					<tr>
						<td><label className="uk-label backgrounds">Notes</label></td>
						<td>
						{
							asIf(hasLength(noteProps.value))
							.then(() => <p className="uk-text-medium">{noteProps.value}</p>)
							.else(() => <p className="uk-text-medium uk-text-muted uk-text-italic">None</p>)
						}
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	);
}

function EditableDeliveryInstructions() {
	const {provinces, setProvinces} = useLocalContext();
	const {setProvince} = useLocalContext();
	const {districts, setDistricts} = useLocalContext();
	const {districtProps, setDistrict, districtError, setDistrictErr} = useLocalContext();
	const {addressProps, addressError, setAddressErr} = useLocalContext();
	const {noteProps, noteError} = useLocalContext();
	const [confirmModalVisible, setConfirmModalVisible] = useState(false);

	useEffect(() => {
		const doFetch = async () => {
			let [res, err] = await getProvincesList({
				columns: ["id", "name"]
			});

			if (err) {
				console.error(err);
				return;
			}

			setProvinces(res);

			if (!hasLength(res)) {
				console.log("No provinces found");
				return;
			}

			const province = res[0];

			setProvince(province);

			[res, err] = await getDistrictsList({
				province: province.id,
				columns: ["id", "name"]
			});

			if (err) {
				console.error(err);
				return;
			}

			setDistricts(res);

			if (!hasLength(res)) {
				console.log("No districts found");
				return;
			}

			setDistrict(res[0].id);
		};

		doFetch();
	}, [setDistricts, setProvinces, setProvince, setDistrict]);

	const onProvinceChange = async (event) => {
		const { target: { value } } = event;
		let provinceId = parseInt(value);
		const [res, err] = await getDistrictsList({
			province: provinceId,
			columns: ["id", "name"]
		});

		if (err) {
			console.error(err);
			return;
		}

		setProvince(provinces.filter(prov => prov.id === provinceId)[0]);
		setDistricts(res);
		setDistrict(hasLength(res) ? res[0].id : "");
	};

	const onSubmit = (event) => {
		event.preventDefault();
		event.stopPropagation();

		const { value: districtId } = districtProps;
		const { value: address } = addressProps;

		const tracker = new ErrorTracker();

		setDistrictErr(tracker.add(!hasLength(districtId) ? "District information must be filled out." : null));
		setAddressErr(tracker.add(!hasLength(address) ? "Address must be filled out" : null));

		if (tracker.foundError()) {
			return;
		}

		setConfirmModalVisible(true);
	};

	return (
		<>
			{
				asIf(confirmModalVisible)
				.then(() => <ConfirmModal
						close={() => setConfirmModalVisible(false)}
					></ConfirmModal>)
				.else()
			}
			<form onSubmit={onSubmit}>
				<div className="uk-heading-line colors">
					<span>
						<span uk-icon="file-edit" className="uk-margin-small-right"></span>
						Delivery Instructions
					</span>
				</div>
				<div className="uk-margin">
					<select
						onChange={onProvinceChange}
						className="uk-select"
						placeholder="Province"
					>
					{
						provinces.map((prov, index) => <option
							key={index}
							value={prov.id}
						>{prov.name}</option>)
					}
					</select>
				</div>
				<div className="uk-margin">
					<select
						{...districtProps}
						className="uk-select"
						placeholder="District"
					>
					{
						districts.map((dis, index) => <option
							key={index}
							value={dis.id}
						>{dis.name}</option>)
					}
					</select>
					<p className="uk-text-danger">{districtError}</p>
				</div>
				<div className="uk-margin">
					<input
						{...addressProps}
						className="uk-input"
						placeholder="Your address"
						name="address"
					/>
					<p className="uk-text-danger">{addressError}</p>
				</div>
				<div className="uk-margin">
					<textarea
						{...noteProps}
						className="uk-textarea"
						rows="5"
						placeholder="Things that you want us to keep in mind"
					></textarea>
					<p className="uk-text-danger">{noteError}</p>
				</div>
				<div className="uk-margin">
					<button className="uk-button backgroundf">Place order</button>
				</div>
			</form>
		</>
	);
}

function ConfirmModal({ close = () => console.log("close") }) {
	// const {cart, setCart} = useLocalContext();
	const { noteProps, addressProps, districtProps } = useLocalContext();
	const { push } = useHistory();

	const onSubmit = async (event) => {
		event.preventDefault();
		event.stopPropagation();
		
		const [res, err] = await placeOrder({
			districtId: parseInt(districtProps.value),
			address: addressProps.value,
			note: noteProps.value
		});

		if (err) {
			console.log(err);
			return;
		}

		push(`${routes.order.url}/${res.code}`);
	};

	return (
		<CenterModal
			close={close}
			footerCloseBtn={false}
		>
			<form onSubmit={onSubmit}>
				<h1 className="uk-heading-line uk-text-center colors">
					<label className="uk-label backgroundf">Quick revision</label>
				</h1>
				<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
					<div>
						<CartSummary></CartSummary>
					</div>
					<div>
						<DeliveryInstructions mode={VIEW}></DeliveryInstructions>
					</div>
				</div>
				<div className="uk-divider-icon"></div>
				<div className="uk-margin">
					<p className="uk-text-muted uk-text-italic">
						Please take a good look of your Order again as you won't be able to make any changes once it's placed
					</p>
				</div>
				<div className="uk-margin uk-text-center">
					<button
						className="uk-button backgroundf uk-margin-right"
					>Confirm</button>
					<button
						className="uk-button uk-button-default"
						type="button"
						onClick={close}
					>Go back</button>
				</div>
			</form>
		</CenterModal>
	);
}
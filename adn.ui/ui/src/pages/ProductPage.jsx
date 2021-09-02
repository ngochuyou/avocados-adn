import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

import {
	useShopping, FETCHED_PRODUCT_COLUMNS,
	OBTAINED_PRODUCT_COLUMNS
} from '../hooks/shopping-hooks';

import { asIf, hasLength } from '../utils';

import {
	SET_INDIVIDUAL_VIEW_TARGET, SET_LIST_ELEMENT,
	SET_MODEL
} from '../actions/common';
import { obtainProduct } from '../actions/product';

import Rating from '../components/utils/Rating';
import { DomainImage } from '../components/utils/Gallery';
import Navbar from '../components/product/Navbar';

const FRESHLY_FETCHED_PRODUCT_COLUMNS = [...FETCHED_PRODUCT_COLUMNS, OBTAINED_PRODUCT_COLUMNS, "category"];
const IMAGE_VIEW_SRC_PROP_NAME = "imageViewSrc";
const IMAGE_VIEW_MODAL_ID = "modal-media-image";

const { log } = console;

export default function ProductPage() {
	const { id: productId } = useParams();
	const {
		wasInit, init,
		productStore: {
			list: { elements, fetchStatus },
			view: { target: model }
		},
		categoryStore: {
			view: { target: viewedCategory }
		},
		dispatchProductStore
	} = useShopping();

	useEffect(() => {
		return () => {
			const imageView = document.querySelector(`#${IMAGE_VIEW_MODAL_ID}`);

			if (imageView != null) {
				imageView.remove();
			}
		};
	}, []);
	useEffect(() => {
		asIf(!wasInit).then(init).else();
	}, [wasInit, init]);
	useEffect(() => {
		const doEffect = async () => {
			log(`effected ${productId}`);

			if (!hasLength(productId)) {
				log(`skip on product id: ${productId}`);
				return;
			}

			if (fetchStatus[productId] === true) {
				log('skip on fetch status');
				return;
			}

			const [fetchedProduct, fetchProductErr] = await obtainProduct({
				id: productId,
				columns: FRESHLY_FETCHED_PRODUCT_COLUMNS
			});

			log(`do fetch`);

			if (fetchProductErr) {
				console.error(fetchProductErr);
				return;
			}

			// this needs to be executed first of all
			dispatchProductStore({
				type: SET_INDIVIDUAL_VIEW_TARGET,
				payload: fetchedProduct
			});
			// ================
			dispatchProductStore({
				type: SET_LIST_ELEMENT,
				payload: {
					categoryId: fetchedProduct.category.id,
					model: fetchedProduct
				}
			});
		};

		doEffect();
	}, [fetchStatus, productId, dispatchProductStore]);
	useEffect(() => {
		log('2nd effected');

		if (!hasLength(productId)) {
			log(`2nd effected: skip on product id: ${productId}`);
			return;
		}

		if (fetchStatus[productId] !== true) {
			log('2nd effected: skip on fetch status');
			return;
		}

		if (productId === model.id) {
			log('2nd effected: skip on view state');
			return;
		}

		for (let product of elements[viewedCategory.id]) {
			if (product.id === productId) {
				dispatchProductStore({
					type: SET_INDIVIDUAL_VIEW_TARGET,
					payload: product
				});

				log('2nd effected: found');

				return;
			}
		}

		log(`2nd effected: skip on product nullness`);
	}, [elements, productId, viewedCategory, model, fetchStatus, dispatchProductStore])

	const viewImage = (imageIndex) => {
		if (model == null) {
			return;
		}

		if (model[IMAGE_VIEW_SRC_PROP_NAME] === model.images[imageIndex]) {
			return;
		}

		dispatchProductStore({
			type: SET_MODEL,
			payload: {
				name: IMAGE_VIEW_SRC_PROP_NAME,
				value: model.images[imageIndex]
			}
		});
	};

	if (model == null) {
		return null;
	}

	return (
		<div
			className={`uk-width-1-1 uk-background-default`}
			uk-height-viewport="offset-top: true"
		>
			<Navbar background="uk-background-muted" />
			<main className="uk-container uk-position-relative">
				<div
					className="uk-width-1-1 uk-position-relative"
					uk-height-viewport="offset-top: true"
				>
					<div className="uk-height-1-1 uk-position-center uk-margin-small-top">
						<div className="uk-width-2xlarge">
							<header>
								<div className="uk-grid-small" uk-grid="">
									<div className="uk-width-expand">
										<div className="uk-text-lead uk-text-bold">{model.name}</div>
										<div className="uk-text-muted">{model.id}</div>
									</div>
									<div className="uk-width-small uk-position-relative">
										<Rating value={model.rating} max={5} />	
									</div>
								</div>
								<hr className="uk-divider-icon" />
							</header>
							<section>
								<p>{model.description}</p>
							</section>
							<section>
								<div className="uk-grid-small uk-child-width-1-2@m uk-child-width-1-1@s" uk-grid="masonry: true">
								{
									model.images.map((img, index) => (
										<div key={index}>
											<div
												className="uk-card uk-card-default uk-flex uk-flex-center uk-flex-middle uk-padding-small uk-background-muted pointer"
												onClick={() => viewImage(index)}
												href={`#${IMAGE_VIEW_MODAL_ID}`}
												uk-toggle=""
											>
												<DomainImage
													url={`/product/image/${img}`}
													name={img}
													fit="contained"
												/>
											</div>
										</div>
									))
								}
								</div>
								<hr />
							</section>
							<footer className="uk-margin">
								<StockDetailList />
							</footer>
						</div>
					</div>
				</div>
				<div
					id={IMAGE_VIEW_MODAL_ID} className="uk-flex-top"
					uk-modal=""
				>
					<div className="uk-modal-dialog uk-width-auto uk-margin-auto-vertical">
						<button className="uk-modal-close-outside" type="button" uk-close=""></button>
						<DomainImage
							url={`/product/image/${model[IMAGE_VIEW_SRC_PROP_NAME]}`}
							name={`${model.id} preview`}
							fit="contained"
						/>
					</div>
				</div>
			</main>
		</div>
	);
}

function StockDetailList() {
	const {
		productStore: { view: { target: model } }
	} = useShopping();

	return (
		<ul className="uk-list uk-list-divider colors">
		{
			model.stockDetails.map(detail => (
				<li key={detail.id}>
					<div className="uk-grid-small" uk-grid="">
						<div className="uk-width-auto">
							<div style={{width: "30px", height: "30px", backgroundColor: detail.color}}></div>
						</div>
						<div className="uk-width-auto">
							<div>{detail.size}</div>
						</div>
						{
							detail.numericSize != null ? (
								<div className="uk-width-auto">
									<div>{detail.numericSize}</div>
								</div>
							) : null
						}
						{
							detail.material != null ? (
								<div className="uk-width-auto">
									<div>{detail.material}</div>
								</div>
							) : null
						}
					</div>
				</li>	
			))
		}
		</ul>	
	);
}
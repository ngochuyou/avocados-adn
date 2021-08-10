import { Fragment } from 'react';

import { DomainImage } from '../utils/Gallery.jsx';
import Rating from '../utils/Rating.jsx';

export default function ProductList({
	list = [],
	messageIfEmpty = "Nothing found in this list",
	onItemClick = () => null,
	header = null
}) {
	if (list.length === 0) {
		return (
			<h5 className="uk-position-center uk-text-center">
				<span
					uk-icon="icon: list"
					className="uk-margin-small-right"
				></span>
				{messageIfEmpty}
			</h5>
		);
	}
	
	return (
		<Fragment>
			<h5 className="uk-heading-line">
				<span>
					{header}
					<label className="uk-margin-small-left uk-label backgrounds">{`${list.length} item(s)`}</label>
				</span>
			</h5>
			<div
				uk-grid=""
				className="uk-grid-small uk-child-width-1-3 uk-grid-match"
			>
			{
				list.map(model => (
					<div
						key={model.id}
						className=""
					>
						<div
							className="uk-card uk-card-default pointer"
							onClick={() => onItemClick(model)}
						>
							<div className="uk-card-media-top uk-height-medium uk-position-relative">
							{
								model.images == null || model.images.length === 0 ?
								<span className="uk-position-center">No preview</span> :
								<DomainImage url={`/product/image/${model.images[0]}`} name={model.images[0]} />
							}
							</div>
							<div>
								<div className="uk-card-body uk-padding-small">
									<div
										uk-grid=""
										className="uk-grid-collapse"
									>
										<div className="uk-width-4-5">
											<p className="uk-card-title">{model.name}</p>
										</div>
										<div className="uk-width-1-5 uk-position-relative">
										<Rating value={model.rating} />
										</div>
									</div>
									<p className="colors uk-text-bold">
										<span uk-icon="tag" className="uk-margin-small-right"></span>
										<span>{model.price}</span>
									</p>
								</div>
							</div>
						</div>
					</div>
				))
			}
			</div>
		</Fragment>
	);
}
import { Fragment } from 'react';

import { useProduct } from '../../hooks/product-hooks';

import { DomainProductImage } from '../utils/Gallery.jsx';
import Rating from '../utils/Rating.jsx';

import { formatVND } from '../../utils';

const heartStyle = {
	top: "10px",
	right: "10px"
}

const inactiveHeartStyle = {
	...heartStyle,
	background: "white",
	color: "black"
}

const activeHeartStyle = {
	...heartStyle,
	background: "#f00",
	color: "white"
}

export default function ProductList({
	list = [],
	header = null,
	messageIfEmpty = "Nothing found in this list",
	onItemClick = (p) => console.log(`${p.id} image clicked`),
	onCartClick = (p) => console.log(`${p.id} cart clicked`)
} = {}) {
	const {
		store: { bookmarks },
		mergeBookmarks
	} = useProduct();

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
						<div className="uk-card uk-card-default pointer">
							<div className="uk-card-media-top uk-height-medium uk-position-relative">
							{
								model.images == null || model.images.length === 0 ?
								<span className="uk-position-center">No preview</span> :
								<DomainProductImage
									name={model.images[0]}
									onClick={() => onItemClick(model)}
								/>
							}
								<span
									className="uk-position-top-right uk-icon-button heart"
									uk-icon="icon: heart"
									style={bookmarks[model.id] === undefined ? inactiveHeartStyle : activeHeartStyle}
									onClick={() => mergeBookmarks(model.id)}></span>
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
									<div
										uk-grid=""
										className="uk-grid-collapse uk-margin"
									>
										<div className="uk-width-4-5">
											<p className="colors uk-text-bold">
												<span uk-icon="tag" className="uk-margin-small-right"></span>
												<span>{formatVND(model.price)}</span>
											</p>
										</div>
										<div
											className="uk-width-1-5 uk-position-relative uk-text-center"
											onClick={() => onCartClick(model)}
											uk-tooltip="Quick add-to-cart"
										>
											<div uk-icon="icon: cart; ratio: 1.5"></div>
										</div>
									</div>
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

const PRODUCT_TABLE_COLUMNS_MAP = {
	code: [
		<th key={0}>Code</th>,
		(p) => (
			<td key="code-td">
				<span>{p.code}</span>
			</td>
		)
	],
	images: [
		<th key={1} className="uk-table-shrink"></th>,
		(p) => (
			<td key="images-td">
				<div style={{width: "70px"}}>
					<DomainProductImage name={p.images[0]} />
				</div>
			</td>
		)
	],
	name: [
		<th key={2}>Name</th>,
		(p) => (
			<td key="name-td">
				<span>{p.name}</span>
			</td>
		)
	]
}

const getProductTableHeader = (columnName) => PRODUCT_TABLE_COLUMNS_MAP[columnName][0];
const getProductTableRow = (p, columnName) => PRODUCT_TABLE_COLUMNS_MAP[columnName][1](p);

export function ProductTable({
	list = [], columns = [],
	extras = [], onRowSelect = (p) => console.log(p)
}) {
	return (
		<table className="uk-table uk-table-hover uk-table-middle uk-table-divider">
			<thead>
				<tr>
					{ columns.map(columnName => getProductTableHeader(columnName)) }
					{ extras.map(extra => extra.header) }
				</tr>
			</thead>
			<tbody>
			{
				list.map(p => (
					<tr
						className="pointer"
						key={p.id}
						onClick={() => onRowSelect(p)}
					>
					{ columns.map(columnName => getProductTableRow(p, columnName)) }
					{ extras.map(extra => extra.row(p)) }
					</tr>
				))
			}
			</tbody>
		</table>
	);
}
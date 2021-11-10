import { server } from '../../config/default.json';

import { memo } from 'react';

import { isImage, hasLength } from '../../utils';

const DOMAIN = "DOMAIN";
const CLIENT = "CLIENT";

const resolve = (any, url) => {
	if (typeof any === 'string') {
		return [DOMAIN, {
			file: { name: any },
			src: `${url}/${any}`
		}];
	}

	if (any != null && typeof any === 'object') {
		if (any.file != null) {
			return [CLIENT, any];
		}
	}

	return [CLIENT, {
		file: { name: "udef" },
		src: null
	}];
};

export const PureGallery = memo(Gallery, (o, n) => {
	return o.elements === n.elements &&
			o.max === n.max && o.message === n.message &&
			o.name === n.name && o.url === n.url;
});

function Gallery({
	elements = [],
	max = 100,
	message = "",
	name = "Gallery",
	url = "",
	onDomainImageRemove = () => null,
	onClientImageRemove = () => null,
	onAdd = () => null,
	maxSize = 1.5
}) {
	const stopEvent = (event) => {
		event.preventDefault();
		event.stopPropagation();
	};
	const resolveImagesForAdd = (files) => {
		const images = Array.from(files).filter(file => isImage(file));
		const shouldAdd = images.length !== 0 && images.length + elements.length <= max;

		return [shouldAdd, images];
	};
	const onDrop = (event) => {
		stopEvent(event);

		const [shouldAdd, images] = resolveImagesForAdd(event.dataTransfer.files);

		if (!shouldAdd) {
			return;
		}

		onAdd(images);
	};
	const onFileInputChange = (event) => {
		const [shouldAdd, images] = resolveImagesForAdd(event.target.files);

		if (!shouldAdd) {
			return;
		}

		onAdd(images);
	};

	return (
		<div
			onDragOver={stopEvent}
			onDragLeave={stopEvent}
			onDrop={onDrop}
			className="uk-height-1-1"
		>
			<div className="uk-grid-small" uk-grid="">
				<div className="uk-width-auto">
					<h3 className="colors">{name}</h3>
				</div>
				<div className="uk-width-expand">
					<div className="uk-height-1-1 uk-position-relative">
						<label className="uk-label backgroundf uk-position-center-left">
							{`${elements.length}/${max}`}
						</label>
					</div>
				</div>
				<div className="uk-width-auto">
					<div className="uk-height-1-1 uk-position-relative">
						<span className="uk-text-danger">{message}</span>
					</div>
				</div>
			</div>
			<div className="uk-grid-small uk-child-width-1-2 uk-padding-small" uk-grid="masonry: true">
			{
				elements.map((element, index) => {
					const [type, model] = resolve(element, url);

					if (type === DOMAIN) {
						return <DomainImageCard
							url={model.src}
							onRemove={onDomainImageRemove}
							index={index}
							key={index}/
						>;
					}

					return <ClientImageCard
						src={model.src}
						onRemove={onClientImageRemove}
						index={index}
						key={index}
						message={model.file.size > maxSize * 1048576 ?
							<p className="uk-text-small uk-text-danger">
							{`File size too large, maximum ${maxSize} MB allowed`}
							</p> : null
						}
					/>;
				})
			}
			</div>
			<div
				className="uk-position-fixed uk-border-circle uk-box-shadow-large backgroundf"
				uk-tooltip="Add an image"
				style={{
					width: '50px',
					height: '50px',
					right: '25px',
					bottom: '50px',
				}}
			>
				<span
					uk-icon="icon: plus; ratio: 1.5"
					className="uk-position-center"
				></span>
				<input
					style={{opacity: "0"}}
					className="uk-position-center uk-width-1-1 uk-height-1-1 pointer"
					type="file"
					onChange={onFileInputChange}
					multiple="multiple"
				/>
			</div>
		</div>
	);
}

function DomainImageCard({
	index = -1, url = "",
	onRemove = () => null
}) {
	return (
		<div>
			<div className="uk-card uk-card-default uk-card-body uk-text-center noselect uk-padding-small">
				<div>
					<SourcedImage src={`${server.url}${url}`} name={url} />
				</div>
				<div className="uk-text-center uk-margin-small-top uk-margin-small-bottom">
					<span
						uk-icon="icon: trash" className="uk-icon-button pointer"
						onClick={() => onRemove(index)}
					></span>
				</div>
			</div>
		</div>
	);
}

function ClientImageCard({
	index = -1, src = null,
	onRemove = () => null,
	message = ''
}) {
	return (
		<div>
			<div className="uk-card uk-card-default uk-card-body uk-text-center noselect uk-padding-small uk-position-relative">
				<div>
					<SourcedImage src={src} name={index} />
				</div>
				<div className="uk-text-center uk-margin-small-top uk-margin-small-bottom">
					<span
						uk-icon="icon: trash" className="uk-icon-button pointer"
						onClick={() => onRemove(index)}
					></span>
					<div>{message}</div>
				</div>
			</div>
		</div>
	);
}

export function SourcedImage({ src = null, name = "udef", fit = "cover" }) {
	return <img
		className="uk-width-1-1 uk-height-1-1"
		src={src}
		alt={name}
		style={{
			objectFit: fit,
			maxHeight: "100%",
			maxWidth: "100%"
		}}
	/>;
}

export function DomainImage({
	url = "", name = "", className = "", fit = "cover",
	onClick = () => null
}) {
	return <img
		onClick={onClick}
		className={`uk-width-1-1 uk-height-1-1 ${className}`}
		src={`${server.url}${url}`}
		alt={name}
		style={{
			objectFit: fit,
			maxHeight: "100%",
			maxWidth: "100%"
		}}
	/>;
}

export const DomainProductImage = memo(_DomainProductImage, (o, n) => o.name === n.name);

function _DomainProductImage({
	name = "", className = "", fit = "cover",
	onClick = () => console.log("click")
}) {
	if (!hasLength(name)) {
		return <div>No preview</div>;
	}

	return <DomainImage
		onClick={onClick}
		url={`${server.images.product}/${name}`}
		className={className}
		fit={fit}
	/>;
}
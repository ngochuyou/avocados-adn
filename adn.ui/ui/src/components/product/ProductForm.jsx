import { useReducer, useState } from 'react';

import { hasLength, asIf } from '../../utils';

import CategoryPicker from '../product/CategoryPicker';
import { PureGallery } from '../utils/Gallery';

const MODEL = {
	name: "",
	category: null,
	description: "",
	images: [],
	material: "",
	locked: false
}

const MAX_IMAGES_AMOUNT = 20;

export default function ProductForm({
	onSuccess = (model) => console.log(model),
	errors = {}
}) {
	const [model, setModelState] = useReducer((oldState, nextState) => ({...oldState, ...nextState}), {...MODEL});
	const [categoryPickerVisible, setCategoryPickerVisible] = useState(false);

	const onInputChange = (event) => setModelState({ [event.target.name]: event.target.value });

	const onCategoryPick = (category) => {
		setModelState({ category });
		setCategoryPickerVisible(false);
	};

	const onGalleryAddImage = (images) => {
		setModelState({
			images: [...model.images, ...images.map(file => ({
				file,
				src: URL.createObjectURL(file)
			}))]
		});
	}

	const onGalleryImageRemove = (index) => {
		setModelState({
			images: model.images.filter((image, i) => index !== i)
		});
	};

	const onSubmit = (event) => {
		event.preventDefault();
		event.stopPropagation();

		onSuccess(model);
	};

	return (
		<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
			<div>
				<form onSubmit={onSubmit}>
					<div className="uk-margin">
						<label className="uk-label backgrounds">Name</label>
						<input
							name="name"
							type="text"
							placeholder="Name"
							className="uk-input"
							value={model.name}
							onChange={onInputChange}
						/>
						<p className="uk-text-danger">{errors.name}</p>
					</div>
					<div className="uk-margin">
						<label className="uk-label backgrounds">Category</label>
						<div
							className="uk-grid-small uk-child-width-1-2 uk-padding-small uk-height-small"
							uk-grid=""
						>
							<div>
							{
								model.category != null ?
								<div className="uk-card uk-card-default uk-card-body uk-text-center">
									<h4 className="uk-card-title">{model.category.name}</h4>
								</div> :
								<div className="uk-card uk-card-default uk-card-body uk-text-center">
									<p className="uk-text-muted">Not selected yet</p>
								</div>
							}
							</div>
							<div
								className="uk-position-relative pointer"
								onClick={() => setCategoryPickerVisible(true)}
							>
								<div className="colors uk-position-center noselect">
									<span uk-icon="menu"></span>Select one
								</div>
							</div>
						</div>
						<p className="uk-text-danger">{errors.category}</p>
						{
							asIf(categoryPickerVisible).then(() => (
								<CategoryPicker
									close={() => setCategoryPickerVisible(false)}
									onPick={onCategoryPick}
								/>
							)).else()
						}
					</div>
					<div className="uk-margin">
						<label className="uk-label backgrounds">Material</label>
						<input
							name="material"
							type="text"
							placeholder="Material"
							className="uk-input"
							value={model.material}
							onChange={onInputChange}
						/>
						<p className="uk-text-danger">{errors.material}</p>
					</div>
					<div className="uk-margin">
						<div className="uk-grid-small uk-child-width-1-2" uk-grid="">
							<div>
								<label className="uk-label backgrounds">Description</label>
							</div>
							<div className="uk-text-right">
								<span className="uk-text-muted">
								{
									asIf(!hasLength(model.description))
									.then(() => "3000 character(s) left")
									.else(() => `${3000 - model.description.length} character(s) left`)
								}
								</span>
							</div>
						</div>
						<textarea
							className="uk-textarea"
							name="description"
							placeholder="Description"
							rows="5"
							maxLength="3000"
							value={model.description}
							onChange={onInputChange}
						>
						</textarea>
						<p className="uk-text-danger">{errors.description}</p>
					</div>
					<div className="uk-margin">
						<button type="submit" className="uk-button backgroundf uk-margin-right">Submit</button>
					</div>
				</form>
			</div>
			<div>
				<PureGallery
					max={MAX_IMAGES_AMOUNT}
					url="/product/image"
					elements={model.images}
					onAdd={onGalleryAddImage}
					onDomainImageRemove={onGalleryImageRemove}
					onClientImageRemove={onGalleryImageRemove}
					message={errors.images || errors.message}
				/>
			</div>
		</div>
	);
}
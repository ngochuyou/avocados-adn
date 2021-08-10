import { VIETNAMESE_CHARACTERS } from '../utils';

const NAME_PATTERN = new RegExp(`^[\\s\\.,_\\-@"'\\*\\p{L}\\p{N}${VIETNAMESE_CHARACTERS}]+$`, "gu");
const MINIMUM_NAME_LENGTH = 1;
const MAXIMUM_NAME_LENGTH = 255;

class Factor {
	constructor({
		name = "",
		active = "",
	}) {
		this.name = name;
	}

	static validator = {
		name: (name) => {
			if (name.length < MINIMUM_NAME_LENGTH || name.length > MAXIMUM_NAME_LENGTH) {
				return [false, `Name length must vary between ${MINIMUM_NAME_LENGTH} and ${MAXIMUM_NAME_LENGTH}`];
			}
			// reset
			NAME_PATTERN.lastIndex = 0;
			// eslint-disable-next-line
			let ok = NAME_PATTERN.test(name);
			
			return [ok, ok ? null : "Name can only contain alphanumeric, spaces characters or '.', ',', '_', '-', '@', \", ' and *"];
		},
		active: (active) => {
			let ok = active != null && typeof active === 'boolean';

			return [ok, ok ? null : "Invalid active state"];
		}
	}
}

export class Category extends Factor {
	constructor(props) {
		super(props);

		const { id = "" } = props;

		this.id = id;	
	}

	static validator = {
		name: Factor.validator.name,
		active: Factor.validator.name,
		description: (description) => {
			let ok = description.length <= 255;

			return [ok, ok ? null : "Description is too long"];
		}
	}
}

export class Product extends Factor {
	constructor(props) {
		super(props);

		const {
			id = "",
			price = 0.0,
			category = {},
			images = [],
			description = ""
		} = props;

		this.id = id;
		this.price = price;
		this.category = category;
		this.images = images;
		this.description = description;
	}

	static validator = {
		name: Factor.validator.name,
		active: Factor.validator.active,
		price: (price) => {
			const ok = !isNaN(price) && price >= 0;

			return [ok, !ok ? "Price must start from 0" : null]
		},
		category: (category) => {
			const ok = category != null && typeof category === 'object';

			return [ok, !ok ? "Select a category" : null]	
		},
		images: (images) => {
			if (!Array.isArray(images)) {
				return [false, "Invalid images"];
			}

			const ok = images.length <= 20;

			return [ok, !ok ? "Cannot upload more than 20 images" : null];
		},
		description: (description) => {
			if (typeof description !== 'string') {
				return [false, "Invalid description"];
			}

			const ok = description.length <= 3000;

			return [ok, !ok ? "Description limit is 3000 words" : null]; 
		}
	}
}
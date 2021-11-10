import {
	isEmail, isAcceptablePhoneNumber,
	isAcceptablePhoneNumberErr, VIETNAMESE_CHARACTERS,
	hasLength
} from '../utils';

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
		...Factor.validator,
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
			category = {},
			images = [],
			description = ""
		} = props;

		this.id = id;
		this.category = category;
		this.images = images;
		this.description = description;
	}

	static validator = {
		...Factor.validator,
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

const PROVIDER_ADDRESS_PATTERN = new RegExp(`^[${VIETNAMESE_CHARACTERS}\\p{L}\\p{N}\\s/\\.,-_()*]{1,255}$`, "gu");
const PROVIDER_REPRESENTATOR_NAME = new RegExp(`^[${VIETNAMESE_CHARACTERS}\\p{L}\\p{N}\\s\\.,_-]+$`, "gu");

export class Provider extends Factor {
	constructor(props) {
		super(props);

		const {
			name = "",
			email = "",
			address = "",
			phoneNumbers = [],
			representatorName = "",
			website = ""
		} = props;

		this.name = name;
		this.email = email;
		this.address = address;
		this.phoneNumbers = phoneNumbers;
		this.representatorName = representatorName;
		this.website = website;
	}

	static validator = {
		...Factor.validator,
		email: (email) => {
			const ok = isEmail(email);

			return [ok, ok ? false : "Invalid email"];
		},
		address: (address) => {
			if (!hasLength(address)) {
				return [false, "Address must not be empty"];
			}

			PROVIDER_ADDRESS_PATTERN.lastIndex = 0;

			const ok = PROVIDER_ADDRESS_PATTERN.test(address);

			return [ok, ok ? null : "Address can only contain alphabetic characters, numbers, spaces, '.', ',', '-', '_', '(', ')'"];
		},
		phoneNumbers: (phoneNumbers) => {
			if (!Array.isArray(phoneNumbers)) {
				return [false, "Invalid phone numbers"];
			}

			if (phoneNumbers.length === 0) {
				return [false, "Must provide at least 1 phone number"];
			}

			if (phoneNumbers.reduce((total, current) => total + current.length, 0) > 250) {
				return [false, "Too many phone numbers"];
			}

			for (let phoneNumber of phoneNumbers) {
				if (!isAcceptablePhoneNumber(phoneNumber)) {
					return [false, isAcceptablePhoneNumberErr];
				}
			}

			return [true, null];
		},
		representatorName: (representatorName) => {
			if (representatorName.length === 0) {
				return [true, null];
			}

			PROVIDER_REPRESENTATOR_NAME.lastIndex = 0;

			const ok = PROVIDER_REPRESENTATOR_NAME.test(representatorName);

			return [ok, ok ? null : "Representator name can only contain alphabetic characters, numbers, spaces, '.', '_', '-'"];
		},
		website: (website) => {
			const ok = !hasLength(website) || website.length < 2000;

			return [ok, ok ? null : "Website address too long"];
		}
	}
}

export class Order extends Factor {
	static Status = {
		PENDING_PAYMENT: "PENDING_PAYMENT",
		PAID: "PAID",
		EXPIRED: "EXPIRED",
		DELIVERING: "DELIVERING",
		FINISHED: "FINISHED"
	}
}
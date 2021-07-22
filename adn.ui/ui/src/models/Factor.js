class Factor {
	constructor({
		name = "",
		active = "",
	}) {
		this.name = name;
	}
}

export class Category extends Factor {
	constructor(props) {
		super(props);

		const { id = "" } = props;

		this.id = id;	
	}

	static validator = {
		name: (name) => {
			// eslint-disable-next-line
			let ok = /^[A-Za-z\u00c0-\u00FF\u0100-\u0280\,\._\s]{0,255}$/.test(name);

			return [ok, ok ? null : "Category name can only contain alphanumeric characters, \".\" or \"_\" "];
		},
		description: (description) => {
			let ok = description.length <= 255;

			return [ok, ok ? null : "Description is too long"];
		},
		active: (active) => {
			let ok = active != null && typeof active === 'boolean';

			return [ok, ok ? null : "Invalid active state"];
		}
	}
}
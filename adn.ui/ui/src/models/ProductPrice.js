import { isPast } from '../utils';

export default class ProductPrice {
	constructor({
		appliedTimestamp = null,
		droppedTimestamp = null,
		price
	}) {
		this.appliedTimestamp = appliedTimestamp;
		this.droppedTimestamp = droppedTimestamp;
		this.price = price;
	}

	static validators = {
		appliedTimestamp: (timestamp) => {
			if (timestamp == null) {
				return "Applied timestamp cannot be empty";
			}

			return isPast(timestamp) ? "Applied timestamp cannot be in the past" : null;
		},
		droppedTimestamp: (timestamp) => {
			if (timestamp == null) {
				return "Dropped timestamp cannot be empty";
			}

			return isPast(timestamp) ? "Dropped timestamp cannot be in the past" : null;
		},
		price: (price) => price < 0 ? "Price cannot be negative" : null
	}
}
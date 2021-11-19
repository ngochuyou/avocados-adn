import { hasLength, isPast } from '../utils';

export default class ProductCost {
	static validators = {
		product: (product) => {
			if (product == null || !hasLength(product.id)) {
				return "Product information must not be empty";
			}

			return null;
		},
		provider: (provider) => {
			if (provider == null || !hasLength(provider.id)) {
				return "Provider information must not be empty";
			}

			return null;
		},
		cost: (cost) => {
			if (isNaN(cost) || cost < 0) {
				return "Cost must be a number and must not be negative";
			}

			return null;
		},
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
		}
	};
}
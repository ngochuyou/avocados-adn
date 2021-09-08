import { hasLength } from '../utils';

export default class ProductCost {
	static validators = {
		product: (product) => {
			if (product == null || !hasLength(product.id)) {
				return [false, "Product information must not be empty"];
			}

			return [true, null];
		},
		provider: (provider) => {
			if (provider == null || !hasLength(provider.id)) {
				return [false, "Provider information must not be empty"];
			}

			return [true, null];
		},
		price: (price) => {
			if (isNaN(price) || price < 0) {
				return [false, "Price must be number and must not be negative"];
			}

			return [true, null];
		}
	};
}
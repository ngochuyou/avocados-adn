export default class StockDetail {
	constructor({
		product = null,
		size = "",
		numericSize = "",
		color = "",
		material = "",
		provider = null,
		status = "",
		active = false,
		description = ""
	} = {}) {
		this.product = product;
		this.size = size;
		this.numericSize = numericSize;
		this.color = color;
		this.material = material;
		this.provider = provider;
		this.status = status;
		this.active = active;
		this.description = description;
	}

	static MINIMUM_NUMERIC_SIZE = 1;
	static MAXIMUM_NUMERIC_SIZE = 255;

	static NamedSize = ["XXL", "XL", "L", "M", "S", "XS", "XXS"];
	static Status = ["AVAILABLE", "UNAVAILABLE"];

	static MAXIMUM_COLOR_LENGTH = 255;
	static MAXIMUM_DESCRIPTION_LENGTH = 255;

	static MAXIMUM_ITEM_QUANTITY = 1000;
	static MAXIMUM_BATCH_SIZE = 1000;

	static validator = {
		product: (product) => [product != null, product != null ? null : "Product information is missing"],
		provider: (provider) => [provider != null, provider != null ? null : "Provider information is missing"],
		size: (size) => {
			const ok = size == null || StockDetail.NamedSize.includes(size);

			return [ok, ok ? null : `Invalid size ${size}`];
		},
		numericSize: (size) => {
			const ok = size >= StockDetail.MINIMUM_NUMERIC_SIZE && size <= StockDetail.MAXIMUM_NUMERIC_SIZE;

			return [ok, ok ? null : `Numeric size must vary between ${StockDetail.MINIMUM_NUMERIC_SIZE} and ${StockDetail.MAXIMUM_NUMERIC_SIZE}`];	
		},
		quantity: (qty) => {
			const ok = qty >= 0 && qty <= StockDetail.MAXIMUM_ITEM_QUANTITY;

			return [ok, ok ? null : `Quantity cannot exceed ${StockDetail.MAXIMUM_ITEM_QUANTITY}`];
		},
		status: (status) => {
			const ok = StockDetail.Status.includes(status);

			return [ok, ok ? null : `Invalid status ${status}`];	
		}
	}
}
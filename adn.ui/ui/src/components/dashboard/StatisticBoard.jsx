import { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';

import { useProduct } from '../../hooks/product-hooks';
import { useProvider } from '../../hooks/provider-hooks';

import { getAllCategories, searchProduct } from '../../actions/product';
import { getProvidersByProduct } from '../../actions/provider';
import {
	getProvidersCountByCategories, getAvgProductCostsByProviders,
	consumeSoldProduct, consumeSoldProductByAssociation
} from '../../actions/stats';

import { routes } from '../../config/default';

import { Route } from 'react-router-dom';
import Navbar from './Navbar';
import { SearchInput } from '../utils/Input';
import { DomainProductImage } from '../utils/Gallery';
import { TemporalPicker } from '../utils/Temporal';
// import { NoFollow } from '../utils/Link';

import {
	merge, atom, chartColors, hasLength, asIf,
	MONTH_NAMES, DATE_NAMES, formatVND
} from '../../utils';

export default function StatisticBoard() {
	const {
		dashboard: {
			stats: {
				cost: { mapping: statsCostMapping },
				product: { mapping: statsProductMapping },
				sales: { mapping: statsSalesMapping },
			}
		}
	} = routes;

	return (
		<div>
			<Navbar />
			<div className="uk-padding-small">
				<Route
					path={statsCostMapping}
					render={(props) => <ProductCostBoard {...props}/>}
				/>
				<Route
					path={statsProductMapping}
					render={(props) => <ProductBoard {...props}/>}
				/>
				<Route
					path={statsSalesMapping}
					render={(props) => <SalesBoard {...props}/>}
				/>
			</div>
		</div>
	);
}

const COUNT = "COUNT";
const SUM = "SUM";

function SalesBoard() {
	return (
		<main>
			<SoldProductCount
				heading={
					<h3 className="uk-heading-line colors">
						<span>
							Sales as per period
							<span className="uk-text-italic uk-margin-left">STAT-REV-01</span>
						</span>
					</h3>
				}
				action={SUM}
			/>
			<SoldProductCountPerAssociation
				heading={
					<h3 className="uk-heading-line colors">
						<span>
							Sales as per period and Category
							<span className="uk-text-italic uk-margin-left">STAT-REV-02</span>
						</span>
					</h3>
				}
				action={SUM}
			/>
		</main>
	);
}

function ProductBoard() {
	return (
		<main>
			<SoldProductCount
				heading={
					<h3 className="uk-heading-line colors">
						<span>
							Numbers of Products sold as per period
							<span className="uk-text-italic uk-margin-left">STAT-PD-01</span>
						</span>
					</h3>
				}
			/>
			<SoldProductCountPerAssociation
				heading={
					<h3 className="uk-heading-line colors">
						<span>
							Numbers of Products sold as per Period and Category
							<span className="uk-text-italic uk-margin-left">STAT-PD-02</span>
						</span>
					</h3>
				}
			/>
			<SoldProductCountPerAssociation
				association={KEY_PRODUCT}
				heading={
					<h3 className="uk-heading-line colors">
						<span>
							Numbers of Products sold as per Period and Product
							<span className="uk-text-italic uk-margin-left">STAT-PD-03</span>
						</span>
					</h3>
				}
			/>
		</main>
	);
}

const KEY_CATEGORY = "CATEGORY";
const KEY_PRODUCT = "PRODUCT";

function SoldProductCountPerAssociation({
	association = KEY_CATEGORY,
	action = COUNT,
	heading = <div></div>
}) {
	const [allTime, setAllTime] = useState(0);
	const [selectedYear, setSelectedYear] = useState(-1);
	const [selectedMonth, setSelectedMonth] = useState(-1);
	const [chartData, setChartData] = useState(null);
	const [selectedAssociations, setSelectedAssociations] = useState([]);
	const [renderedProducts, setRenderedProducts] = useState([]);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await consumeSoldProduct({ action });

			if (err) {
				console.error(err);
				return;
			}

			setAllTime(res.total);
		};

		doFetch();
	}, [action]);

	useEffect(() => {
		if (!hasLength(selectedAssociations)) {
			return setChartData(null);
		}

		const doFetch = async () => {
			const commonParams = {
				overall: false,
				year: selectedYear === -1 ? null : selectedYear,
				month: selectedMonth === -1 ? null : selectedMonth,
				action
			};
			const [dataSet, err] = await consumeSoldProductByAssociation(association === KEY_CATEGORY ? {
				...commonParams,
				categoryIds: selectedAssociations
			}: {
				...commonParams,
				productIds: Object.keys(selectedAssociations)
			});

			if (err) {
				return console.error(err);
			}

			const [groupKey, labelNames] = selectedYear === -1 ?
				[ YEAR, {} ] : selectedMonth === -1 ?
					[MONTH, MONTH_NAMES] :
						[DAY, DATE_NAMES];
			const commonAssociations = dataSet.reduce((result, data) => ({
				...result,
				[data.associationId]: data.associationName
			}), {});
			const commonAssociationsAsArray = Object.keys(commonAssociations).map(id => parseInt(id)).sort((left, right) => left - right);
			const grouped = dataSet.reduce((grouped, data) => ({
				...grouped,
				[data[groupKey]]: grouped[data[groupKey]] == null ? [data] : [...grouped[data[groupKey]], data]
			}), {});
			Object.entries(grouped).forEach(entry => {
				const group = entry[1];
				const groupAsMap = atom(group, "associationId");
				const missingAssociations = [];

				for (let associationId of commonAssociationsAsArray) {
					if (groupAsMap[associationId] == null) {
						missingAssociations.push(associationId);
					}
				}

				for (let associationId of missingAssociations) {
					grouped[entry[0]] = [...grouped[entry[0]], {
						[groupKey]: parseInt(entry[0]),
						associationId,
						associationName: commonAssociations[associationId],
						total: 0
					}];
				}
			});

			const finalGroup = Object.fromEntries(Object.entries(grouped).map(entry => [entry[0], entry[1].sort((left, right) => left.associationId - right.associationId)]));

			return setChartData({
				labels: Object.entries(commonAssociations).sort((left, right) => left[0] - right[0]).map(entry => entry[1]),
				datasets: Object.entries(finalGroup).map((entry, index) => {
					const group = entry[1];

					return {
						data: group.map(data => data.total),
						label: selectedYear === - 1 ? entry[0] : labelNames[entry[0] - 1],
						backgroundColor: chartColors
					};
				})
			});
		};

		doFetch();
	}, [action, selectedYear, selectedMonth, selectedAssociations, association]);

	const onCategoryChange = (event) => {
		const { target: { value, checked } } =  event;
		const castedValue = parseInt(value);

		if (checked === true) {
			return setSelectedAssociations([...selectedAssociations, castedValue]);
		}

		return setSelectedAssociations(selectedAssociations.filter(associationId => associationId !== castedValue));
	};

	const onProductPicked = (product) => {
		const { id } = product;

		if (selectedAssociations[id] != null) {
			return setSelectedAssociations(Object.fromEntries(Object.entries(selectedAssociations).filter(entry => parseInt(entry[0]) !== id)));
		}

		setRenderedProducts(merge(renderedProducts, [product], (product) => product.id));

		return setSelectedAssociations({
			...selectedAssociations,
			[id]: product
		});
	}

	return (
		<section style={{minHeight: "300px"}}>
			{ heading }
			<div className="uk-text-medium">
				{`Overall: ${action === SUM ? formatVND(allTime) : allTime}`}
			</div>
			<div className="uk-margin">
				<TemporalPicker
					selectedYear={selectedYear}
					onYearPicked={setSelectedYear}
					selectedMonth={selectedMonth}
					onMonthPicked={setSelectedMonth}
					allowWholeMonth={true}
				/>
				{
					asIf(chartData != null)
					.then(() => (
						<Bar
							data={chartData}
						/>
					)).else()
				}
				{
					asIf(association === KEY_CATEGORY)
					.then(() => <CategoryPicker
						onCategoryChange={onCategoryChange}
					/>)
					.else(() => (
						<div className="uk-margin-small">
							<ProductPicker
								onProductPicked={onProductPicked}
							/>
							<div className="uk-margin uk-grid-small uk-child-width-auto uk-grid noselect">
							{
								renderedProducts.map(product => (
									<label key={product.id}>
										<input
											className="uk-checkbox"
											type="checkbox"
											checked={selectedAssociations[product.id] != null}
											value={product.id}
											onChange={() => onProductPicked({ id: product.id })}
										/>
										{` ${product.name}`}
									</label>
								))
							}	
							</div>
						</div>
					))
				}
			</div>
		</section>
	);
}

function SoldProductCount({
	action = COUNT,
	heading = <div></div>
}) {
	const current = new Date();
	const [allTime, setAllTime] = useState(0);
	const [selectedYear, setSelectedYear] = useState(current.getFullYear());
	const [selectedMonth, setSelectedMonth] = useState(current.getMonth() + 1);
	const [chartData, setChartData] = useState(null);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await consumeSoldProduct({ action });

			if (err) {
				console.error(err);
				return;
			}

			setAllTime(res.total);
		};

		doFetch();
	}, [action]);

	useEffect(() => {
		const doFetch = async () => {
			const [dataSet, err] = await consumeSoldProduct({
				overall: false,
				year: selectedYear === -1 ? null : selectedYear,
				month: selectedMonth === -1 ? null : selectedMonth,
				action
			});

			if (err) {
				return console.error(err);
			}

			return setChartData({
				labels: dataSet.map((() => {
					if (selectedYear === -1) {
						return data => data.year;
					}

					if (selectedMonth === -1) {
						return data => MONTH_NAMES[data.month - 1];
					}

					return data => DATE_NAMES[data.day - 1];
				})()),
				datasets: [{
					data: dataSet.map(data => data.total),
					label: "Total",
					backgroundColor: chartColors
				}]
			});
		};

		doFetch();
	}, [selectedYear, selectedMonth, action]);

	return (
		<section style={{minHeight: "300px"}}>
			{ heading }
			<div className="uk-text-medium">
				{`Overall: ${action === SUM ? formatVND(allTime) : allTime}`}
			</div>
			<div className="uk-margin">
				<TemporalPicker
					selectedYear={selectedYear}
					onYearPicked={setSelectedYear}
					selectedMonth={selectedMonth}
					onMonthPicked={setSelectedMonth}
					allowWholeMonth={true}
				/>
				{
					asIf(chartData != null)
					.then(() => (
						<Bar
							data={chartData}
						/>
					)).else()
				}
			</div>
		</section>
	);
}

function ProductCostBoard() {
	return (
		<main>
			<ProviderCountPerCategory />
			<CostComparisonByProduct />
		</main>
	);
}

function CostComparisonByProduct() {
	const [selectedProduct, setSelectedProduct] = useState(null);

	return (
		<section>
			<div>
				<h3 className="uk-heading-line colors">
					<span>
						Cost comparsion between Providers as per Product
						<span className="uk-text-italic uk-margin-left">STAT-PV-02</span>
					</span>
				</h3>
				<ProductPicker
					onProductPicked={(product) => setSelectedProduct(product)}
				/>
			</div>
			<CostComparsionChart
				product={selectedProduct}
			/>
		</section>
	);
}

const YEAR = "year";
const MONTH = "month";
const DAY = "day";

function CostComparsionChart({
	product = null
}) {
	const {
		store: { elements: { map: providers } },
		setProviders
	} = useProvider();
	const [selectedProviders, setSelectedProviders] = useState([]);
	const current = new Date();
	const [selectedYear, setSelectedYear] = useState(current.getFullYear());
	const [selectedMonth, setSelectedMonth] = useState(current.getMonth() + 1);
	const [chartData, setChartData] = useState(null);

	useEffect(() => {
		if (product == null) {
			return;
		}

		const doFetch = async () => {
			const [res, err] = await getProvidersByProduct({
				productId: product.id,
				page: 0, size: 500
			});

			if (err) {
				return console.error(err);
			}

			return setProviders(res);
		};

		return doFetch();
	}, [product, setProviders]);

	useEffect(() => {
		if (!hasLength(selectedProviders) || product == null) {
			return setChartData(null);
		}

		const doFetch = async () => {
			const [dataSet, err] = await getAvgProductCostsByProviders({
				productId: product.id,
				providerIds: selectedProviders,
				year: selectedYear === -1 ? null : selectedYear,
				month: selectedMonth === -1 ? null : selectedMonth
			});

			if (err) {
				console.error(err);
				return;
			}

			setChartData({
				labels: dataSet.map(data => data.providerName),
				datasets: [{
					data: dataSet.map(data => data.avgCost),
					label: "Average Cost",
					backgroundColor: chartColors
				}]
			});
		};

		doFetch();
	}, [product, selectedProviders, selectedYear, selectedMonth]);

	if (product == null) {
		return (
			<div style={{minHeight: "300px"}}></div>
		);
	}

	const onProvidersChange = (event) => {
		const { target: { value: providerId, checked } } = event;

		if (checked === true) {
			return setSelectedProviders([...selectedProviders, providerId]);
		}

		return setSelectedProviders(selectedProviders.filter(id => id !== providerId));
	};
	const onTemporalPicked = (type = null, value = null) => {
		if (type == null || value == null) {
			return;
		}

		if (type === YEAR) {
			return setSelectedYear(value);
		}

		return setSelectedMonth(value);
	};

	return (
		<div style={{minHeight: "300px"}}>
			<div className="uk-flex">
				<table className="uk-table uk-table-middle">
					<thead>
						<tr>
							<th className="uk-table-shrink uk-padding-remove"></th>
							<th className="uk-table-expand uk-padding-remove"></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>
								<div className="uk-flex uk-left-right">
									<div className="uk-width-expand"></div>
									<div style={{height: "50px", width: "50px"}}>
										<DomainProductImage
											name={product.images[0]}
										/>
									</div>
								</div>
							</td>
							<td>
								<h4>{product.name}</h4>
							</td>
						</tr>
					</tbody>
				</table>
				<TemporalPicker
					onYearPicked={(value) => onTemporalPicked(YEAR, value)}
					onMonthPicked={(value) => onTemporalPicked(MONTH, value)}
					selectedYear={selectedYear}
					selectedMonth={selectedMonth}
				/>
			</div>
			{
				asIf(chartData != null)
				.then(() => (
					<Bar
						data={chartData}
					/>
				)).else()
			}
			<div className="uk-margin uk-grid-small uk-child-width-auto uk-grid noselect">
			{
				(() => {
					const providersAsArray = Object.values(providers);
					const checkedStates = selectedProviders.reduce((state, current) => ({
						...state,
						[current]: current
					}), {});

					return providersAsArray.map(provider => (
						<label key={provider.id}>
							<input
								className="uk-checkbox"
								type="checkbox"
								value={provider.id}
								onChange={onProvidersChange}
								checked={checkedStates[provider.id] != null}
							/>
							{` ${provider.name}`}
						</label>
					))
				})()
			}	
			</div>
			<div className="uk-flex uk-flex-center">
				<div
					className="uk-button uk-button-default"
					onClick={() => setSelectedProviders([])}
				>Exclude all</div>
				<div
					className="uk-button uk-button-default uk-margin-left uk-margin-right"
					onClick={() => setSelectedProviders(Object.values(providers).map(pv => pv.id))}
				>Include all</div>
			</div>
		</div>
	);
}

function ProductPicker({
	onProductPicked = (product) => console.log("product picked")
}) {
	const {
		store: {
			product: { elements: products }
		},
		setProducts
	} = useProduct();
	const [productPickerVisible, setProductPickerVisible] = useState(false);

	const onSearchInputEntered = async (keyword) => {
		keyword = keyword.toLowerCase();

		const [res, err] = await searchProduct({
			internal: true, columns: ["id", "name", "images"],
			productName: keyword
		});
		
		if (err) {
			console.error(err);
			return;
		}

		setProducts(res);
		setProductPickerVisible(true);
	};
	const onSelectProduct = async (product) => {
		onProductPicked(product);
		setProductPickerVisible(false);
	};

	return (
		<div>
			<SearchInput
				placeholder="Search using Product name"
				onEntered={onSearchInputEntered}
			/>
			{
				asIf(productPickerVisible === true)
				.then(() => (
					<div className="uk-grid-small" uk-grid="">
					{
						Object.values(products).map(product => (
							<div
								key={product.id}
								className="uk-box-shadow-hover-medium noselect pointer"
								onClick={() => onSelectProduct(product)}
							>
								<table className="uk-table uk-table-middle">
									<thead>
										<tr>
											<th className="uk-table-shrink uk-padding-remove"></th>
											<th className="uk-padding-remove"></th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td>
												<div style={{height: "50px", width: "50px"}}>
													<DomainProductImage
														name={product.images[0]}
													/>
												</div>
											</td>
											<td>
												<p className="uk-text-large">{product.name}</p>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
						))
					}
					</div>
				)).else()
			}
		</div>
	);
}

function ProviderCountPerCategory() {
	const [datas, setDatas] = useState(null);
	const [selectedCategories, setSelectedCategories] = useState([]);

	useEffect(() => {
		if (!hasLength(selectedCategories)) {
			return;
		}

		const doFetch = async () => {
			const [dataSet, err] = await getProvidersCountByCategories(selectedCategories);

			if (err) {
				console.error(err);
				return null;
			}

			setDatas({
				labels: dataSet.map(data => data.categoryName),
				datasets: [{
					data: dataSet.map(data => data.providersCount),
					label: "Number of Providers",
					backgroundColor: chartColors
				}]
			});
		};

		doFetch();
	}, [selectedCategories]);

	const onCategoryChange = (event) => {
		const { target: { value, checked } } = event;
		const castedValue = parseInt(value);

		if (checked === true) {
			return setSelectedCategories([...selectedCategories, castedValue]);
		}

		return setSelectedCategories(selectedCategories.filter(categoryId => categoryId !== castedValue));
	};

	return (
		<section style={{minHeight: "300px"}}>
			<h3 className="uk-heading-line colors">
				<span>
					Numbers of Providers as per Category
					<span className="uk-text-italic uk-margin-left">STAT-PV-01</span>
				</span>
			</h3>
			{
				asIf(datas != null)
				.then(() => (
					<Bar data={datas} />
				)).else()
			}
			<CategoryPicker
				onCategoryChange={onCategoryChange}
			/>
		</section>
	);
}

function CategoryPicker({
	onCategoryChange = (event) => console.log(`category ${event.target.value} picked`)
}) {
	const [categories, setCategories] = useState([]);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await getAllCategories();

			if (err) {
				console.error(err);
				return;
			}

			setCategories(res);
		};

		doFetch();
	}, []);

	return (
		<div className="uk-margin uk-grid-small uk-child-width-auto uk-grid noselect">
		{
			categories.map(category => (
				<label key={category.id}>
					<input
						className="uk-checkbox"
						type="checkbox"
						value={category.id}
						onChange={onCategoryChange}
					/>
					{` ${category.name}`}
				</label>
			))
		}	
		</div>
	);
}

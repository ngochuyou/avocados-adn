import { useTemporal } from '../../hooks/hooks';

import { asIf } from '../../utils';

export function TemporalPicker({
	onYearPicked = (year) => console.log(`the year ${year} picked`),
	onMonthPicked = (month) => console.log(`the month ${month} picked`),
	onDayPicked = (day) => console.log(`the day ${day} picked`),
	selectedYear = -1,
	selectedMonth = -1,
	selectedDay = -1,
	displayYear = true,
	displayMonth = true,
	displayDay = false,
	allowAllTime = true,
	allowWholeYear = true,
	allowWholeMonth = false
}) {
	const [
		years, /*setYear*/,
		/*selectedYear*/, setSelectedYear,
		months, /*setMonths*/,
		/*selectedMonth*/, setSelectedMonth,
		days
	] = useTemporal();

	const onMonthChanged = (event) => {
		const month = parseInt(event.target.value);

		onMonthPicked(month);
		setSelectedMonth(month);
	};

	const onYearChanged = (event) => {
		const year = parseInt(event.target.value);

		onYearPicked(year);
		setSelectedYear(year);
	}

	return (
		<div className="uk-margin uk-flex uk-flex-center">
			{
				asIf(displayYear === true)
				.then(() => (<div className="uk-width-small">
					<select
						className="uk-select"
						value={selectedYear == null ? -1 : selectedYear}
						onChange={onYearChanged}
					>
					{
						asIf(allowAllTime)
						.then(() => (
							<option key={"emptyYear"} value={-1}>All time</option>
						)).else()
					}
					{
						years.map(year => <option key={year} value={year}>{year}</option>)
					}
					</select>
				</div>)).else()
			}
			{
				asIf(displayMonth === true)
				.then(() => (<div className="uk-width-small uk-margin-left">
					<select
						className="uk-select"
						value={selectedMonth == null ? -1 : selectedMonth}
						onChange={onMonthChanged}
					>
					{
						asIf(allowWholeYear)
						.then(() => (
							<option key={"emptyMonth"} value={-1}>Whole year</option>
						)).else()
					}
					{
						months.map(month => <option key={month.value} value={month.value}>{month.name}</option>)
					}
					</select>
				</div>)).else()
			}
			{
				asIf(displayDay === true)
				.then(() => (<div className="uk-width-small uk-margin-left">
					<select
						className="uk-select"
						value={selectedDay == null ? -1 : selectedDay}
						onChange={(event) => onDayPicked(parseInt(event.target.value))}
					>
					{
						asIf(allowWholeMonth)
						.then(() => (
							<option key={"emptyDay"} value={-1}>Whole month</option>
						)).else()
					}
					{
						days.map(day => <option key={day.value} value={day.value}>{day.name}</option>)
					}
					</select>
				</div>)).else()
			}
		</div>
	);
}
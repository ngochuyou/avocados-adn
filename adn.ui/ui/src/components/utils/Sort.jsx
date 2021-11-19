const ASC = "ASC";
const DESC = "DESC";

export default function OrderSelector({
	onAscRequested = () => console.log("ASC requested"),
	onDesRequested = () => console.log("DESC requested"),
	labels = ["Ascending", "Descending"],
	className = ""
}) {
	return (
		<select
			className={`uk-select ${className}`}
			onChange={(event) => event.target.value === ASC ? onAscRequested() : onDesRequested()}
		>
			<option value={ASC}>{labels[0]}</option>
			<option value={DESC}>{labels[1]}</option>
		</select>
	);
}
export default function Rating({ value = null, max = null }) {
	return value == null ? (
		<label className="uk-label backgrounds uk-position-center">
			NR
		</label>
	) : (
		<label className="uk-label backgroundf uk-position-center">
			{max == null ? value : `${value}/${max}`}
		</label>
	);
}
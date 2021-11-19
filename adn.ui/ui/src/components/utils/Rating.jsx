export default function Rating({ value = null, max = null }) {
	return <div className="uk-position-relative uk-width-1-1 uk-height-1-1">
	{
		value == null ? (
			<label className="uk-label backgrounds uk-position-center">
				NR
			</label>
		) : (
			<label className="uk-label backgroundf uk-position-center">
				{max == null ? value : `${value}/${max}`}
			</label>
		)
	}
	</div>;
}
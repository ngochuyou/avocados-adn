export function ConfirmModal({
	message = "Are you sure?",
	onYes = () => null,
	onNo = () => null
}) {
	return (
		<div
			className="uk-position-fixed uk-position-center backgroundf uk-padding"
			style={{zIndex: "990"}}
		>
			<p className="uk-text-large uk-text-center">{message}</p>
			<div className="uk-text-center">
				<button
					onClick={onYes}
					className="uk-button uk-button-danger uk-margin-small-right"
				>Yes</button>
				<button
					onClick={onNo}
					className="uk-button backgrounds">
				No</button>
			</div>
		</div>
	);
}
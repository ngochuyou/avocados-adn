export function ConfirmModal({
	id = v4(),
	onYes = () => null,
	onNo = () => null
}) {
	return (
		<div
			id={id}
			className="uk-flex-top"
			uk-modal=""
		>
			<div className="uk-modal-dialog uk-modal-body uk-margin-auto-vertical backgroundf">
				<p className="uk-text-large">Are you sure you want to deactivate this account?</p>
				<div className="uk-text-center">
					<button
						onClick={() => console.log("asdasd")}
						className="uk-button uk-button-danger uk-margin-small-right"
					>Yes</button>
					<button
						className="uk-button uk-modal-close backgrounds">
					No</button>
				</div>
			</div>
		</div>
	);
}
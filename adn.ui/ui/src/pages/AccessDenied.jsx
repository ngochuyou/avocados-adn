export default function AccessDenied({ message = "Access to this page was denied", ...props }) {

	return (
		<div className="uk-position-relative uk-height-viewport">
			<div className="uk-position-center uk-text-center">
				<span
					uk-icon="icon: ban; ratio: 10"
					className="uk-text-danger"
				></span>
				<h1 className="uk-card-title uk-text-danger uk-text-bold">
					Access denied
				</h1>
				<p className="uk-text-lead">{ message }</p>
			</div>
		</div>
	);
}
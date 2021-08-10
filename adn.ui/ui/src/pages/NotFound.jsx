export default function NotFound({ message = "The page you're trying to access does not exist.", ...props }) {
	return (
		<div className="uk-position-relative uk-height-viewport">
			<div className="uk-position-center uk-text-center">
				<span
					uk-icon="icon: question; ratio: 10"
					className="uk-text-muted"
				></span>
				<h1 className="uk-card-title uk-text-primary uk-text-bold">
					Page not found
				</h1>
				<p className="uk-text-lead uk-text-muted">{ message }</p>
			</div>
		</div>
	);
}
import { NoFollow } from './Link'; 
import { asIf } from '../../utils';

export function AlertBox({ message, onClose, className }) {
	return asIf(message != null)
			.then(() => (
				<Alert
					message={message}
					onClose={onClose}
					className={className}
				/>
			)).else(() => null);
}

function Alert({
	message = "", className = "uk-alert-primary",
	onClose = () => null
}) {
	return (
		<div className={className} uk-alert="">
			<NoFollow
				href="#" className="uk-alert-close" uk-close=""
				onClick={onClose}
			></NoFollow>
			<p>{message}</p>
		</div>
	);	
}
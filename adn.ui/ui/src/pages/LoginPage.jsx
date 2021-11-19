import { useHistory } from 'react-router-dom';

export default function LoginPage() {
	const history = useHistory();

	return (
		<div className="uk-grid-collapse uk-grid-match uk-text-center" uk-grid="">
			<div className="uk-width-2-3">
				
			</div>
			<div className="uk-height-viewport uk-width-1-3 uk-position-relative">
				<div className="uk-card uk-card-default uk-card-body uk-position-center">
					<LoginForm onSuccess={() => console.log("success")} />
				</div>
			</div>
		</div>
	);
}
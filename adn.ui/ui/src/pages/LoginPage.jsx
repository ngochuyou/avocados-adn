import { useState } from 'react';
import { useInput } from '../hooks/hooks';
import { fetchToken } from '../auth.js';
import Account from '../models/Account';
import { useHistory } from 'react-router-dom';

export default function LoginPage() {
	const [usernameProps, setUsername] = useInput("ngochuy.ou");
	const [passwordProps, setPassword] = useInput("password");
	const [message, setMessage] = useState(null);
	const [isLoading, setLoading] = useState(false);
	const history = useHistory();

	const onSubmit = async (e) => {
		e.preventDefault();
		setMessage(null);
		setLoading(true);

		const { value: username } = usernameProps;
		let [, err] = Account.validator.username(username);

		if (err) {
			setMessage(err);
			setLoading(false);
			return;
		}

		const { value: password } = passwordProps;

		[, err] = Account.validator.password(password);

		if (err) {
			setMessage(err);
			setLoading(false);
			return;	
		}

		const res = await fetchToken({ username, password });

		setUsername("");
		setPassword("");

		if (res.ok) {
			history.push('/');
			return;
		}

		setLoading(false);
	};

	return (
		<div className="uk-grid-collapse uk-grid-match uk-text-center" uk-grid="">
			<div className="uk-width-2-3">
				
			</div>
			<div className="uk-height-viewport uk-width-1-3 uk-position-relative">
				<div className="uk-card uk-card-default uk-card-body uk-position-center">
					<h3 className="uk-card-title">Login</h3>
					<form onSubmit={onSubmit}>
						{ message && (<div uk-alert="">{ message }</div>)}
						<div className="uk-margin">
							<input
								className="uk-input"
								{ ...usernameProps }
								{ ...Account.inputProps.username }
							/>
						</div>
						<div className="uk-margin">
							<input
								className="uk-input"
								{ ...passwordProps }
								{ ...Account.inputProps.password }
							/>
						</div>
						<button
							className="uk-button uk-button-primary"
							type="submit"
							disabled={ isLoading ? "disabled" : ""}
						>Login</button>
					</form>
				</div>
			</div>
		</div>
	);
}
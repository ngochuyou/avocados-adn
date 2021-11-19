import { useState } from 'react';

import { fetchToken } from '../../auth.js';

import { useInput } from '../../hooks/hooks';
import { useAuth } from '../../hooks/authentication-hooks';

import Account from '../../models/Account';

const { inputProps } = Account;

export default function LoginForm({
	onSuccess = () => null	
}) {
	const [usernameProps, setUsername] = useInput("");
	const [passwordProps, setPassword] = useInput("");
	const [message, setMessage] = useState(null);
	const [isLoading, setLoading] = useState(false);
	const { fetchPrincipal } = useAuth();

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

		[, err] = await fetchToken({ username, password });

		setUsername("");
		setPassword("");

		if (err) {
			setMessage(err);
			setLoading(false);
			return;			
		}

		fetchPrincipal();
		setLoading(false);
	};

	return (
		<div>
			<h3 className="uk-card-title">Login</h3>
			<form onSubmit={onSubmit}>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={inputProps.username.id}>
						Username
					</label>
					<input
						className="uk-input"
						{ ...usernameProps }
						{ ...inputProps.username }
					/>
				</div>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={inputProps.username.id}>
						Password
					</label>
					<input
						className="uk-input"
						{ ...passwordProps }
						{ ...inputProps.password }
					/>
				</div>
				<p>{ message && (<span className="uk-text-danger">{ message }</span>)}</p>
				<button
					className="uk-button backgroundf"
					type="submit"
					disabled={ isLoading ? "disabled" : ""}
				>Go</button>
			</form>
		</div>
	);
}
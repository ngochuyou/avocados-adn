import { useState } from 'react';

import { createUser } from '../../actions/account';

import { useInput } from '../../hooks/hooks';

import Account from '../../models/Account';

const { inputProps } = Account;
const { usePrefix: useInputPrefix } = inputProps;

export default function RegisterForm({
	elementsPrefix = "alt",
	onSuccess = () => null
}) {
	const [usernameProps, ] = useInput("");
	const [phoneProps, ] = useInput("");
	const [emailProps, ] = useInput("");
	const [passwordProps, ] = useInput("");
	const [rePasswordProps, ] = useInput("");
	const [usernameErr, setUsernameErr] = useState(null);
	const [phoneErr, setPhoneErr] = useState(null);
	const [emailErr, setEmailErr] = useState(null);
	const [passwordErr, setPasswordErr] = useState(null);
	const [rePasswordErr, setRePasswordErr] = useState(null);
	
	const onSubmit = async (event) => {
		event.preventDefault();
		event.stopPropagation();

		const user = {
			username: usernameProps.value,
			phone: phoneProps.value,
			email: emailProps.value,
			password: passwordProps.value
		};
		let error = null;

		setUsernameErr(error = Account.validator.username(user.username)[1]);
		setPhoneErr(error = Account.validator.phone(user.phone)[1]);
		setEmailErr(error = Account.validator.email(user.email)[1]);
		setPasswordErr(error = Account.validator.password(user.password)[1]);
		setRePasswordErr(error = (
			passwordProps.value !== rePasswordProps.value ?
				"Password and re-password must match" : 
				null));

		if (error) {
			setUsernameErr(error.username);
			setPhoneErr(error.phone);
			setEmailErr(error.email);
			setPasswordErr(error.password);
			return ;
		}

		const [res, err] = await createUser({
			...user,
			role: Account.Role.CUSTOMER
		});

		if (err) {
			console.error(err);
			return;
		}

		onSuccess(res);
	};

	return (
		<div>
			<h3 className="uk-card-title">Register</h3>
			<form onSubmit={onSubmit}>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={`${elementsPrefix}-${inputProps.username.id}`}>
						Username
					</label>
					<input
						className="uk-input"
						{ ...usernameProps }
						{ ...useInputPrefix("username", elementsPrefix) }
					/>
					<span className="uk-text-danger">{usernameErr}</span>
				</div>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={`${elementsPrefix}-${inputProps.phone.id}`}>
						Phone number
					</label>
					<input
						className="uk-input"
						{ ...phoneProps }
						{ ...useInputPrefix("phone", elementsPrefix) }
					/>
					<span className="uk-text-danger">{phoneErr}</span>
				</div>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={`${elementsPrefix}-${inputProps.email.id}`}>
						Email
					</label>
					<input
						className="uk-input"
						{ ...emailProps }
						{ ...useInputPrefix("email", elementsPrefix) }
					/>
					<span className="uk-text-danger">{emailErr}</span>
				</div>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={`${elementsPrefix}-${inputProps.password.id}`}>
						Password
					</label>
					<input
						className="uk-input"
						{ ...passwordProps }
						{ ...useInputPrefix("password", elementsPrefix) }
					/>
					<span className="uk-text-danger">{passwordErr}</span>
				</div>
				<div className="uk-margin">
					<label
						className="uk-label backgroundf"
						htmlFor={`${elementsPrefix}-${inputProps.rePassword.id}`}>
						Re-password
					</label>
					<input
						className="uk-input"
						{ ...rePasswordProps }
						{ ...useInputPrefix("rePassword", elementsPrefix) }
					/>
					<span className="uk-text-danger">{rePasswordErr}</span>
				</div>
				<button
					className="uk-button backgroundf"
					type="submit"
					/*disabled={ isLoading ? "disabled" : ""}*/
				>Submit</button>
			</form>
		</div>
	);
}
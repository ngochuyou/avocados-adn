export default class Account {
	constructor({
		username = "",
		password = "",
		email = ""
	}) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	static validator = {
		username: (username) => {
			// eslint-disable-next-line
			let ok = /^[A-Za-z\u00c0-\u00FF\u0100-\u0280\._]{8,}$/.test(username);

			return [ok, ok ? null : "Invalid username"];
		},
		password: (password) => {
			let ok = password.length >= 8;

			return [ok, ok ? null : "Password must contains 8 or more characters"];
		},
		email: (email) => {
			// eslint-disable-next-line
			let ok = /^(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i.test(email);

			return [ok, ok ? null : "Invalid email"];
		}
	};

	static inputProps = {
		username: {
			type: "text",
			placeholder: "Username"
		},
		password: {
			type: "password",
			placeholder: "Password"
		},
		email: {
			type: "email",
			placeholder: "Email"
		}
	}
}
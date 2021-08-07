export default class Account {
	constructor({
		username = "",
		password = "",
		rePassword = "",
		email = "",
		phone = "",
		firstName = "",
		lastName = "",
		photo = "",
		role = Account.Role.ANONYMOUS,
		gender = Account.Gender.UNKNOWN,
		birthDate = null,
		active = false,
		deactivatedDate = null,
		createdDate = null,
		updatedDate = null
	} = {}) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.firstName = firstName;
		this.lastName = lastName;
		this.photo = photo;
		this.role = role;
		this.gender = gender;
		this.birthDate = birthDate;
		this.active = active;
		this.deactivatedDate = deactivatedDate;
		this.createdDate = createdDate;
		this.updatedDate = updatedDate;
	}

	static validator = {
		username: (username) => {
			// eslint-disable-next-line
			let ok = /^[0-9A-Za-z\u00c0-\u00FF\u0100-\u0280\._]{8,}$/.test(username);
			
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
	
	static Role = {
		ADMIN: 'ADMIN',
		PERSONNEL: 'PERSONNEL',
		MANAGER: 'MANAGER',
		EMPLOYEE: 'EMPLOYEE',
		ANONYMOUS: 'ANONYMOUS'
	}

	static Gender = {
		FEMALE: 'FEMALE',
		MALE: 'MALE',
		UNKNOWN: 'UNKNOWN'
	}
}

export class Personnel extends Account {
	constructor(props) {
		super(props);

		const { createdBy = "" } = props;

		this.createdBy = createdBy;
	}
}
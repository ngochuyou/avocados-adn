import { hasLength, isEmail, asIf } from '../utils';

const USERNAME_REG = new RegExp("^[0-9A-Za-z\u00c0-\u00FF\u0100-\u0280.\\-_@#$'!*&']{8,}$");
const PHONE_REG = RegExp("^[\\w\\d\\._\\(\\)\\+\\s\\-:]{4,}$");

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
		this.fullname = `${firstName} ${lastName}`
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
			USERNAME_REG.lastIndex = 0;
			// eslint-disable-next-line
			let ok = hasLength(username) && USERNAME_REG.test(username);
			
			return [ok, ok ? null : "Invalid username"];
		},
		password: (password) => {
			let ok = password.length >= 8;

			return [ok, ok ? null : "Password must contains 8 or more characters"];
		},
		email: (email) => hasLength(email) ?
			asIf(isEmail(email))
				.then(() => [true, null])
				.else(() => [null, "Invalid email"]) :
			true,
		phone: (phone) => {
			PHONE_REG.lastIndex = 0;

			let ok = PHONE_REG.test(phone);

			return [ok, ok ? null : "Invalid phone number"];
		}
	};

	static inputProps = {
		username: {
			type: "text",
			placeholder: "Username",
			id: "username"
		},
		phone: {
			type: "tel",
			placeholder: "Phone number",
			id: "phone"
		},
		password: {
			type: "password",
			placeholder: "Password",
			id: "password"
		},
		rePassword: {
			type: "password",
			placeholder: "Re-password",
			id: "re-password"
		},
		email: {
			type: "email",
			placeholder: "Email",
			id: "email"
		},
		usePrefix: (key, prefix = "alt") => ({
			...Account.inputProps[key],
			id: `${prefix}-${Account.inputProps[key].id}`
		})
	}
	
	static Role = {
		HEAD: 'HEAD',
		PERSONNEL: 'PERSONNEL',
		MANAGER: 'MANAGER',
		EMPLOYEE: 'EMPLOYEE',
		CUSTOMER: 'CUSTOMER',
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
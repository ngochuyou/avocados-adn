import React from 'react';

class App extends React.Component {

	async componentDidMount() {
		// let form = new FormData();

		// form.append('username', "ngochuy.ou");
		// form.append('password', "password");

		let res = await fetch(`http://localhost:8080/auth/logout`, {
			credentials: 'include',
			method: 'POST',
			// body: form
		});

		res = await res.text();
		console.log(res);

		// let res = await fetch(`http://localhost:8080/public/greet`, {
		// 	method: 'GET',
		// 	mode: 'cors',
		// 	credentials: 'include',
		// 	headers: {
		// 		"Authorization": "JWTBearer"
		// 	},
		// });
		//210
	}

	render() {
		return null;
	}
}

export default App;
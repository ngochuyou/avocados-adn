import React from 'react';

class App extends React.Component {

	async componentDidMount() {
		let res = await fetch(`http://localhost:8080/auth/token?username=${encodeURIComponent("ngochuy.ou")}&password=password`, {
			credentials: 'include',
			method: 'POST',
			headers: {
				"Content-Type": "application/x-www-form-urlencoded"
			},
			redirect: 'follow'
		});

		console.log(res);

		res = await fetch(`http://localhost:8080/t/greet`, {
			method: 'GET',
			mode: 'cors',
			credentials: 'include'
		});
	}

	render() {
		return null;
	}
}

export default App;
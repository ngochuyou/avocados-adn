import { useEffect, useState } from 'react';
import { Link, Route } from 'react-router-dom';
import { useAuth } from '../hooks/authentication-hooks';
import Account from '../models/Account';
import AccessDenied from './AccessDenied.jsx';
import { $fetch } from '../fetch';

export default function Dashboard() {
	const { principal } = useAuth();

	if (principal && principal.role === Account.Role.ADMIN) {
		return (
			<div className="uk-grid-collapse uk-grid-match" uk-grid="" uk-height-viewport="expand: true">
				<div className="uk-width-1-5">
					<header className="uk-padding-small backgroundf">
						<h3 className="colorf">
							<a className="uk-link-reset" href="/">Avocados</a>
						</h3>
						<ul className="uk-list uk-list-large uk-list-divider">
							<li>
								<Link to="/dashboard/provider" className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1">Provider</Link>
							</li>
							<li>
								<Link to="/dashboard/department" className="uk-link-reset uk-display-inline-block uk-height-1-1 uk-width-1-1">Department</Link>
							</li>
						</ul>
					</header>
				</div>
				<div className="uk-width-4-5 uk-padding-small">
					<Route path="/dashboard/department" render={props => <DepartmentBoard { ...props } />} />
				</div>
			</div>
		);
	}

	return <AccessDenied message="Unauthorized role" />
}


function DepartmentBoard() {
	const [ list, setList ] = useState([]);

	useEffect(() => {
		const doFetchList = async () => {
			const [res, err] = await $fetch('/list/department', {
				method: 'GET',
				headers: {
					'Accept' : 'application/json',
					'Content-Type': 'application/json'
				}
			});

			if (!err) {
				if (res.ok) {
					setList(await res.json());
				}
			}
		};

		doFetchList();
	}, []);

	return (
		<div className="">
			<h2>Departments</h2>
			<div className="uk-grid-small uk-child-width-1-1@s uk-child-width-1-2@m uk-child-width-1-3@l uk-flex-center uk-text-center" uk-grid="">
			{
				list.map(item => (
					<div key={item.id}>
						<div className="uk-card uk-card-small uk-card-hover uk-card-default uk-card-body">
							<h3>{item.name}</h3>
						</div>
					</div>
				))
			}	
			</div>
		</div>
	);
}
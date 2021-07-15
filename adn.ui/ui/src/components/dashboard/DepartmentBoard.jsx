import { useState, useEffect, useReducer } from 'react';

import { fetchDepartments, fetchChiefs, fetchPersonnelCounts } from '../../actions/department';
import { fetchAccount } from '../../actions/account';
import { server } from '../../config/default.json';
import { intRange } from '../../utils';

const SET_CHIEFS = "SET_CHIEFS";
const UPDATE_CHIEF = "UPDATE_CHIEF";

export default function DepartmentBoard() {
	const [ departments, setDepartments ] = useState([]);
	const [ chiefs, dispatchChiefStates ] = useReducer((oldChiefs, { type = null, payload = null } = {}) => {
		switch (type) {
			case SET_CHIEFS: {
				return payload;
			}
			case UPDATE_CHIEF: {
				const { index, chiefState } = payload;

				return oldChiefs.map((chief, i) => i !== index ? chief : {
					...chief, ...chiefState
				});
			}
			default: {
				return oldChiefs;
			}
		}
	}, []);
	const [ counts, setCounts ] = useState([]);
	const [ chiefViewState, toggleChiefViewState ] = useReducer((oldState, selectedIndex = 0) => {
		return {
			visible: !oldState.visible,
			selectedIndex: selectedIndex
		};
	}, { visible: false, selectedIndex: 0 });

	useEffect(() => {
		const doFetch = async () => {
			const departments = await fetchDepartments(["id", "name"]);

			if (departments == null) return;

			setDepartments(departments);

			const departmentIds = departments.map(ele => ele.id);
			const chiefs = await fetchChiefs(departmentIds, ["username", "firstName", "lastName"]);

			if (chiefs == null) return;

			dispatchChiefStates({
				type: SET_CHIEFS,
				payload: chiefs.map(chief => {
					return {
						...chief,
						isFetched: false
					}
				})
			});

			const counts = await fetchPersonnelCounts(departmentIds);

			if (counts === null) return;

			setCounts(counts);
		};

		doFetch();
	}, []);

	const onChiefClick = async (index) => {
		if (chiefs[index].isFetched === true) {
			toggleChiefViewState(index);
			return;
		}

		const targetedChief = chiefs[index];
		const columns = ["email", "phone", "role", "gender", "photo", "birthDate", "active"];
		const [res, err] = await fetchAccount(targetedChief.username, columns);

		if (err) {
			return;
		}

		dispatchChiefStates({
			type: UPDATE_CHIEF,
			payload: { 
				index,
				chiefState: {
					...res,
					isFetched: true
				}
			}
		});
		toggleChiefViewState(index);
	}

	const selectedChief = {
		...chiefs[chiefViewState.selectedIndex],
		department: departments[chiefViewState.selectedIndex]
	};

	return (
		<div>
			<div className="uk-grid-small uk-child-width-1-1@s uk-child-width-1-2@m uk-child-width-1-3@l uk-flex-center uk-text-center" uk-grid="">
			{
				intRange(departments.length).map(index => {
					let department = departments[index];
					let chief = chiefs[index];
					let count = counts[index];

					return (
						<div key={index}>
							<div className="uk-card uk-card-small uk-card-hover uk-card-default uk-card-body">
								<div className="uk-card-header">
									<div className="uk-grid-small uk-flex-middle" uk-grid="">
										<div className="uk-width-auto">
											
										</div>
										<div className="uk-width-expand">
											<h3 className="uk-card-title uk-margin-remove-bottom">{department.name}</h3>
										</div>
									</div>
								</div>
								<div className="uk-card-body">
									<a
										className="uk-link-muted"
										onClick={() => onChiefClick(index)}
										href="#chief-view"
									>
										<b className="uk-margin-right colors">Chief:</b>
										{chief !== undefined ? `${chief.firstName} ${chief.lastName}` : null}
									</a>
									<p>
										<b className="uk-margin-right colors">Personnel count:</b>
										{count !== undefined ? count : 0}
									</p>
								</div>
							</div>
						</div>
					);
				})
			}	
			</div>
			{
				chiefViewState.visible === false ? null : (
					<AccountViewModal
						account={ selectedChief }
						toggle={toggleChiefViewState}
					/>
				)
			}
		</div>
	);
}

function AccountViewModal({ account = null, toggle = () => null }) {
	if (account == null) {
		return (
			<div className="uk-open uk-display-block" uk-modal="">
				<div className="uk-modal-dialog">
					<button
						onClick={toggle}
						className="uk-modal-close-default"
						type="button" uk-close=""></button>
					<div className="uk-modal-header">
						<h1>Nothing found</h1>							
					</div>
					<div className="uk-modal-footer uk-text-right">
						<button
							onClick={toggle}
							className="uk-button uk-button-default uk-modal-close"
							type="button">Close</button>
					</div>
				</div>
			</div>
		);
	}

	return (
		<div className="uk-open uk-display-block" uk-modal="">
			<div className="uk-modal-dialog">
				<button
					onClick={toggle}
					className="uk-modal-close-outside"
					type="button" uk-close=""></button>
				<div className="uk-modal-header">
					<div className="uk-card-badge uk-label backgroundf colorf">{account.role}</div>
					<div className="uk-grid-small uk-flex-middle" uk-grid="">
						<div className="uk-width-auto">
							<img
								className="uk-border-circle"
								style={{ width: "50px", height: "50px" }}
								src={`${server.url}/account/photo?filename=${account.photo}`} />
						</div>
						<div className="uk-width-expand">
							<h2 className="uk-modal-title uk-margin-remove-bottom colors">
								{`${account.firstName} ${account.lastName}`}
							</h2>
							<p className="uk-text-meta uk-margin-remove-top">
							{
								account.department == null ? null : (
									`${account.department.name} Department Chief`
								)
							}
							</p>
						</div>
					</div>								
				</div>
				<div className="uk-modal-body">
					<div>
					{
						["email", "phone", "gender"].map((field, index) => (
							<div
								className="uk-grid-collapse uk-margin-small-top uk-margin-small-bottom"
								uk-grid=""
								key={index}
							>
								<div className="uk-width-1-3">
									<label className="uk-label backgroundf colorf">{field.toUpperCase()}</label>
								</div>
								<div className="uk-width-2-3">
									<p>{account[field]}</p>
								</div>
							</div>
						))
					}
						<div className="uk-grid-collapse uk-margin-small-top uk-margin-small-bottom" uk-grid="">
							<div className="uk-width-1-3">
								<label className="uk-label backgroundf colorf">BIRTHDATE</label>
							</div>
							<div className="uk-width-2-3">
								<p>{account.birthDate === null ? "UNKNOWN" : new Date(account.birthDate).toString()}</p>
							</div>
						</div>
						<div className="uk-grid-collapse uk-margin-small-top uk-margin-small-bottom" uk-grid="">
							<div className="uk-width-1-3">
								<label className="uk-label backgroundf colorf">ACCOUNT STATUS</label>
							</div>
							<div className="uk-width-2-3">
							{
								account.active === true ? (
									<span className="uk-text-success">ACTIVE</span>
								) : (
									<span className="uk-text-muted">ACTIVE</span>
								)
							}
							</div>
						</div>
					</div>
				</div>
				<div className="uk-modal-footer uk-text-right">
					<button
						onClick={toggle}
						className="uk-button uk-button-default uk-modal-close"
						type="button">Close</button>
				</div>
			</div>
		</div>
	);
}
import { useState, useEffect, useReducer, Fragment } from 'react';

import {
	fetchDepartments, fetchChiefs,
	fetchPersonnelCounts, fetchPersonnelListByDepartment
} from '../../actions/department';
import { fetchAccount, lockAccount } from '../../actions/account';

import { intRange, spread, range, isEmpty } from '../../utils';
import { server } from '../../config/default.json';

import AccountViewModal from '../account/AccountViewModal.jsx';
import Paging from '../utils/Paging.jsx';

const SET_CHIEFS = "SET_CHIEFS";
const UPDATE_CHIEF = "UPDATE_CHIEF";

const VIEW_DEPARTMENT_LIST = "VIEW_DEPARTMENT_LIST";
const VIEW_DEPARTMENT_PERSONNEL_LIST = "VIEW_DEPARTMENT_PERSONNEL_LIST";

const AMOUNT_PER_PAGE = 5;

const SET_PERSONNEL_LISTS = "SET_PERSONNEL_LISTS";
const PERSONNEL_LIST_FETCHED = "PERSONNEL_LIST_FETCHED";
const SET_SELECTED_PERSONNEL_LIST_INDEX = "SET_SELECTED_PERSONNEL_LIST_INDEX";
const SET_SELECTED_PERSONNEL_LIST_PAGE = "SET_SELECTED_PERSONNEL_LIST_PAGE";
const FULFILL_PERSONNEL_STATE = "FULFILL_PERSONNEL_STATE";
const SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET = "SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET";

export default function DepartmentBoard({ dispatchBackButton = () => null, actions = {} }) {
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
	const [ stage, setStage ] = useState(VIEW_DEPARTMENT_LIST);

	useEffect(() => {
		const doFetch = async () => {
			const departments = await fetchDepartments(["id", "name"]);

			if (departments == null) return;

			setDepartments(departments);

			const departmentIds = departments.map(ele => ele.id);
			const chiefs = await fetchChiefs(departmentIds, ["username", "photo", "firstName", "lastName"]);

			if (chiefs == null) return;

			dispatchChiefStates({
				type: SET_CHIEFS,
				payload: chiefs.map(chief => {
					return {
						...chief,
						isFetched: false
					};
				})
			});

			const counts = await fetchPersonnelCounts(departmentIds);

			if (counts === null) return;

			setCounts(counts);
			dispatchPersonnelState({
				type: SET_PERSONNEL_LISTS,
				payload: range(departments.length).map(ele => {
					return {};
				})
			});
		};

		doFetch();
	}, []);

	const onChiefClick = async (index) => {
		if (chiefs[index].isFetched === true) {
			toggleChiefViewState(index);
			return;
		}

		const targetedChief = chiefs[index];
		const columns = ["email", "phone", "role", "gender", "birthDate", "active"];
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
	const fetchPersonnelList = async (departmentId, page = 0, size = AMOUNT_PER_PAGE) => {
		return await fetchPersonnelListByDepartment(
			departmentId,
			["username", "photo",
				"firstName", "lastName",
				"active", "gender" ],
			page, size
		);
	};
	const onDepartmentClick = async (index) => {
		if (personnelState.fetchStatus[index] === false) {
			const department = departments[index];
			const [fetchedList, err] = await fetchPersonnelList(department.id);

			if (err) {
				setStage(VIEW_DEPARTMENT_LIST);
				return;
			}

			dispatchPersonnelState({
				type: PERSONNEL_LIST_FETCHED,
				payload: {
					index,
					list: fetchedList,
					page: 0
				}
			});
		} else {
			dispatchPersonnelState({
				type: SET_SELECTED_PERSONNEL_LIST_INDEX,
				payload: index
			});
		}

		setStage(VIEW_DEPARTMENT_PERSONNEL_LIST);
		dispatchBackButton({
			type: actions.SET_STATE_AND_TOGGLE,
			payload: {
				callback: () => () => {
					dispatchBackButton({
						type: actions.SET_STATE_AND_TOGGLE,
						payload: {
							callback: () => null
						}
					});
					setStage(VIEW_DEPARTMENT_LIST);
				}
			}
		});
	}

	const [ personnelState, dispatchPersonnelState ] = useReducer(
		(oldState, { type = null, payload = null } = {}) => {
			switch (type) {
				case SET_SELECTED_PERSONNEL_LIST_PAGE: {
					const { selectedIndex, paginations: oldPaginations, v } = oldState;

					return {
						...oldState,
						paginations: oldPaginations.map((ele, i) => i !== selectedIndex ? ele : {
							...ele,
							page: payload
						}),
						v: v + 1
					};
				}
				case SET_PERSONNEL_LISTS: {
					if (!Array.isArray(payload)) {
						return oldState;
					}

					return {
						...oldState,
						lists: payload,
						fetchStatus: spread(payload.length, false),
						paginations: spread(payload.length, {
							page: 0,
							size: AMOUNT_PER_PAGE
						}),
						v: 1
					}
				}
				case PERSONNEL_LIST_FETCHED: {
					const { index, list, page } = payload;
					const { lists: oldLists, fetchStatus: oldFetchStatus, paginations: oldPaginations, v } = oldState;

					return {
						...oldState,
						lists: oldLists.map((ele, i) => {
							if (index !== i) {
								return ele;
							}

							const personnelMap = ele[page] == null ? {} : ele[page];

							list.forEach(personnel => personnelMap[personnel.username] = {
								...personnel,
								fulfilled: false
							});

							return { ...ele, [page]: personnelMap };
						}),
						paginations: oldPaginations.map((ele, i) => index !== i ? ele : {
							...oldPaginations[index],
							page
						}),
						selectedIndex: index,
						fetchStatus: oldFetchStatus.map((ele, i) => index !== i ? ele : true),
						v: v + 1
					};
				}
				case SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET: {
					const { individualView, v } = oldState;

					return {
						...oldState,
						individualView: {
							...individualView,
							target: payload,
							visible: !individualView.visible
						},
						v: v + 1
					}
				}
				case FULFILL_PERSONNEL_STATE: {
					const { selectedIndex, paginations, v, lists } = oldState;
					const { username } = payload;

					return {
						...oldState,
						v: v + 1,
						lists: lists.map((ele, i) => {
							if (i !== selectedIndex) {
								return ele;
							}

							const personnelMap = {
								...ele[paginations[selectedIndex].page]
							};

							personnelMap[username] = {
								...personnelMap[username],
								...{
									...payload,
									fulfilled: true
								}
							};

							return {
								...ele,
								[paginations[selectedIndex].page]: personnelMap
							};
						})
					};
				}
				case SET_SELECTED_PERSONNEL_LIST_INDEX: {
					const { selectedIndex, v: oldV } = oldState;

					if (typeof payload !== 'number' || payload === selectedIndex) {
						return oldState;
					}

					return {
						...oldState,
						selectedIndex: payload,
						v: oldV + 1
					}
				}
				default: {
					return oldState;
				}
			}
		}, {
			lists: [],
			fetchStatus: [],
			selectedIndex: 0,
			version: 0,
			paginations: [],
			individualView: {
				visible: false,
				target: {}
			}
		}
	);
	const onPersonnelListPageRequest = async (pageNumber) => {
		const { selectedIndex, lists, paginations } = personnelState;

		if (!isEmpty(lists[selectedIndex][pageNumber])) {
			dispatchPersonnelState({
				type: SET_SELECTED_PERSONNEL_LIST_PAGE,
				payload: pageNumber
			});
			return;
		}

		const [fetchedList, err] = await fetchPersonnelList(departments[selectedIndex].id, pageNumber, paginations[selectedIndex].size);

		if (err) {
			return;
		}

		dispatchPersonnelState({
			type: PERSONNEL_LIST_FETCHED,
			payload: {
				list: fetchedList,
				page: pageNumber,
				index: selectedIndex
			}
		});
	};
	const viewIndividual = async (username) => {
		const { selectedIndex, lists, paginations, } = personnelState;

		if (lists[selectedIndex][paginations[selectedIndex].page][username].fulfilled === true) {
			dispatchPersonnelState({
				type: SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET,
				payload: username
			});
			return;
		}

		const [fetchedFields, err] = await fetchAccount(
			username,
			["email", "phone", "birthDate", "role"]
		);

		if (err) {
			return null;
		}

		dispatchPersonnelState({
			type: FULFILL_PERSONNEL_STATE,
			payload: { ...fetchedFields, username }
		});
		dispatchPersonnelState({
			type: SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET,
			payload: username
		});
	}
	const lockPersonnelAccount = async (username) => {
		const [res, ] = await lockAccount(username);

		if (res == null) {
			return null;
		}

		const { lists, selectedIndex, paginations } = personnelState;

		dispatchPersonnelState({
			type: FULFILL_PERSONNEL_STATE,
			payload: {
				...lists[selectedIndex][paginations[selectedIndex].page][username],
				active: false
			}
		});
	};

	return (
		<div>
			{
				stage !== VIEW_DEPARTMENT_LIST ? null :
				<Fragment>
					<div
						style={{
							maxHeight: stage === VIEW_DEPARTMENT_LIST ? "none" : "0px",
							height: "fit-content"
						}}
						className="uk-grid-small uk-child-width-1-1@s uk-child-width-1-2@m uk-child-width-1-3@l uk-flex-center uk-text-center"
						uk-grid="">
					{
						intRange({ max: departments.length }).map(index => {
							let department = departments[index];
							let chief = chiefs[index];
							let count = counts[index];

							return (
								<div key={index}>
									<div
										className="uk-card uk-card-small uk-card-hover uk-card-default uk-card-body">
										<div
											onClick={() => onDepartmentClick(index)}
											className="uk-card-header noselect pointer">
											<div className="uk-grid-small uk-flex-middle" uk-grid="">
												<div className="uk-width-auto">
													
												</div>
												<div className="uk-width-expand">
													<h3 className="uk-card-title uk-margin-remove-bottom">
														{department.name}
													</h3>
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
								account={chiefs[chiefViewState.selectedIndex]}
								toggle={toggleChiefViewState}
								dd={`${departments[chiefViewState.selectedIndex].name} Department Chief`}
								display="uk-open uk-display-block"
							/>
						)
					}
				</Fragment>
			}
			{
				stage !== VIEW_DEPARTMENT_PERSONNEL_LIST ? null :
				(() => {
					const {
						lists,
						selectedIndex: departmentIndex,
						paginations, individualView
					} = personnelState;
					const targetedList = lists[departmentIndex][paginations[departmentIndex].page];

					return (
						<Fragment>
							<PersonnelList
								list={targetedList}
								amountPerPage={paginations[departmentIndex].size}
								department={departments[departmentIndex]}
								chief={chiefs[departmentIndex]}
								count={counts[departmentIndex]}
								onRequestPage={onPersonnelListPageRequest}
								viewIndividual={viewIndividual}
							/>
							{
								!individualView.visible ? null : (
									<AccountViewModal
										display="uk-open uk-display-block"
										toggle={() => dispatchPersonnelState({
											type: SET_PERSONNEL_INDIVIDUAL_VIEW_TARGET,
											payload: {}
										})}
										account={targetedList[individualView.target]}
										lockAccount={lockPersonnelAccount}
									/>
								)
							}
						</Fragment>
					);
				})()
			}
		</div>
	);
}

function PersonnelList({
	list = {},
	department = {},
	chief = {},
	count = 0,
	amountPerPage = 5,
	onRequestPage = () => null,
	viewIndividual = () => null
}) {
	const [ selected, setSelected ] = useState(1);
	const onSelect = (pageNumber) => {
		if (selected === pageNumber) {
			return;
		}

		setSelected(pageNumber);
		onRequestPage(pageNumber - 1);
	};

	return (
		<div>
			<div className="uk-grid-collapse uk-child-width-1-2" uk-grid="">
				<div>
					<h2>{department.name}</h2>
				</div>
				<div className="uk-text-right uk-grid-collapse uk-grid-match" uk-grid="">
					<div className="uk-width-expand">
						<div className="uk-position-relative">
							<p className="uk-position-center-right uk-text-lead uk-margin-right colors">
								{`${chief.firstName} ${chief.lastName}`}
							</p>
						</div>
					</div>
					<div className="uk-width-auto">
						<div>
							<img
								alt={chief.username}
								className="uk-border-circle photo-small"
								src={`${server.url}/account/photo?filename=${chief.photo}`} />
						</div>
					</div>
				</div>
			</div>
			{
				isEmpty(list) ? (
					<div uk-alert="">Nothing found in this list</div>
				) : (
					<table className="uk-table uk-table-hover uk-table-middle uk-table-divider">
						<thead>
							<tr>
								<th className="uk-table-shrink"></th>
								<th>Fullname</th>
								<th>Gender</th>
								<th>Account Status</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody>
						{
							Object.keys(list).map(username => {
								const account = list[username];

								return (
									<tr key={username}>
										<td>
											<img
												className="uk-preserve-width uk-border-circle photo-small"
												src={`${server.url}/account/photo?filename=${account.photo}`}
												alt={account.username}
											/>
										</td>
										<td>{`${account.firstName} ${account.lastName}`}</td>
										<td>{account.gender}</td>
										<td>
										{
											account.active ? <span className="uk-text-success">ACTIVE</span> :
											<span className="uk-text-danger">LOCKED</span>
										}
										</td>
										<td>
											<div className="uk-button-group">
												<div className="uk-inline">
													<button className="uk-button uk-button-default" type="button">
														<span uk-icon="icon: chevron-down; ratio: 1"></span>
													</button>
													<div uk-dropdown="mode: hover; boundary: ! .uk-button-group; boundary-align: true;">
														<ul className="uk-nav uk-dropdown-nav">
															<li>
																<ul className="uk-list uk-list-divider">
																	<li
																		onClick={() => viewIndividual(username)}
																		className="pointer">
																		VIEW
																	</li>
																</ul>
															</li>
														</ul>
													</div>
												</div>
											</div>
										</td>
									</tr>
								)
							})
						}
						</tbody>
					</table>
				)
			}
			<Paging
				amount={Math.ceil(count / amountPerPage)}
				selected={selected}
				onPageSelect={onSelect}
				amountPerChunk={amountPerPage}
				usePageInput={true}
			/>
		</div>
	);
}
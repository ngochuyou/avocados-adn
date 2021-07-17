import { Fragment } from 'react';

import { server } from '../../config/default.json';
import { isEmpty } from '../../utils';
import { v4 } from 'uuid';

// import { Personnel } from '../../models/Account';

// const FIELDS = Object.getOwnPropertyNames(new Personnel({}));

// const FIELDS = [
// 	"username", "email", "phone", "firstName",
// 	"lastName", "gender", "active", "birthDate"
// ];

export default function AccountViewModal({
	id = v4(),
	account = null,
	toggle = () => null, dd = null,
	display = "",
	v = 0,
	lockAccount = () => null
}) {
	const lockAccountAndToggle = () => {
		lockAccount(account.username);
		toggle();
	};

	return (
		<div id={id} className={`uk-model-section ${display}`} uk-modal="">
			<div className="uk-modal-dialog">
				<button
					onClick={toggle}
					className="uk-modal-close-outside"
					type="button" uk-close=""></button>
				{
					account == null || isEmpty(account) ? (
						<div className="uk-modal-header">
							<h1>Nothing found</h1>	
						</div>
					) : (
						<Fragment>
							<div className="uk-modal-header">
								<div className="uk-card-badge uk-label backgroundf colorf">{account.role}</div>
								<div className="uk-grid-small uk-flex-middle" uk-grid="">
									<div className="uk-width-auto">
										<img
											alt="accountphoto"
											className="uk-border-circle photo-small"
											src={`${server.url}/account/photo?filename=${account.photo}`} />
									</div>
									<div className="uk-width-expand">
										<h2 className="uk-modal-title uk-margin-remove-bottom colors">
											{`${account.firstName} ${account.lastName}`}
										</h2>
										<p className="uk-text-meta uk-margin-remove-top">
										{ dd }
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
											<p>{account.birthDate == null ? "UNKNOWN" : new Date(account.birthDate).toString()}</p>
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
												<span className="uk-text-muted">LOCKED</span>
											)
										}
										</div>
									</div>
								</div>
							</div>
						</Fragment>
					)
				}
				<div className="uk-modal-footer uk-text-right">
					<button
						onClick={toggle}
						className="uk-button uk-button-default uk-modal-close uk-margin-small-right"
						type="button">Close</button>
					<button
						onClick={lockAccountAndToggle}
						className="uk-button uk-button-danger"
						type="button">Lock</button>
				</div>
			</div>
		</div>
	);
}
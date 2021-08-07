import { profile } from '../../config/default';

function AuthenticatedComponent({ principal, children }) {
	if (principal === null && profile.mode !== 'DEV') {
		return <></>;
	}

	return children;
}

export default AuthenticatedComponent;
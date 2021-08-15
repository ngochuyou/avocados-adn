import { profile } from '../../config/default';

export default function AuthenticatedComponent({ principal, children }) {
	if (principal === null && profile.mode !== 'DEV') {
		return <></>;
	}

	return children;
}
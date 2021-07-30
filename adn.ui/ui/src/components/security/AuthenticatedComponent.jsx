const dev = true;

function AuthenticatedComponent({ principal, children }) {
	if (principal === null && !dev) {
		return <></>;
	}

	return children;
}

export default AuthenticatedComponent;
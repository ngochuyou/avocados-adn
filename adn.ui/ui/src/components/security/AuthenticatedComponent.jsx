function AuthenticatedComponent({ principal, children }) {
	if (principal === null) {
		return <></>;
	}

	return children;
}

export default AuthenticatedComponent;
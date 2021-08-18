export default function UnauthenticatedComponent({ principal, children }) {
	if (principal != null) {
		return <></>;
	}

	return children;
}
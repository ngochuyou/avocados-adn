export function NoFollow(props) {
	const preventFollowClick = (event) => {
		event.preventDefault();

		const { onClick } = props;

		if (typeof onClick === 'function') {
			onClick(event);
		}

		return false;
	};

	return (
		<a
			{...props}
			onClick={preventFollowClick}
		>{props.children}</a>
	);
}
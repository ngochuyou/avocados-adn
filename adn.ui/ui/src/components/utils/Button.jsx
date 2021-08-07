export function FixedAddButton({ onClick = () => null, tooltip = "Add" }) {
	return (
		<div
			onClick={onClick}
			className="uk-position-fixed uk-border-circle uk-box-shadow-large backgroundf pointer"
			uk-tooltip={tooltip}
			style={{
				width: '50px',
				height: '50px',
				bottom: '25px',
				right: '50px'
			}}
		>
			<span
				uk-icon="icon: plus; ratio: 1.5"
				className="uk-position-center"
			></span>
		</div>
	);
}
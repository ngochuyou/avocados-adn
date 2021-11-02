import { useState } from 'react';

export function FullModal({
	children,
	close = () => null,
	footerCloseBtn = true
} = {}) {
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const onClose = () => {
		setContainerClassName('fade-out uk-modal');
		setTimeout(() => close(), 180);
	}

	return (
		<div className={`uk-modal-full uk-open uk-display-block ${containerClassName}`} uk-modal="">
			<div className="uk-modal-dialog uk-height-1-1 uk-padding-small" uk-height-viewport="" uk-overflow-auto="">
				<button
					className="uk-position-fixed uk-close-large"
					type="button" uk-close="" style={{top: "25px", right: "25px"}}
					onClick={onClose}
				></button>
				{children}
				<div className="uk-text-right">
				{
					footerCloseBtn ? (
						<button
							className="uk-button uk-button-default"
							onClick={onClose}
						>Close</button>
					) : null
				}
				</div>
			</div>
		</div>
	);
}

export function CenterModal({
	children,
	close = () => null,
	onKeyDown = () => null,
	footerCloseBtn = true,
	width,
	className = ""
}) {
	const [containerClassName, setContainerClassName] = useState('fade-in');
	const onClose = () => {
		setContainerClassName('fade-out uk-modal');
		setTimeout(() => close(), 180);
	};

	return (
		<div
			className={`uk-modal uk-open uk-display-block ${containerClassName}`}
			uk-modal="" style={{ maxHeight: "100vh", overflow: "hidden", zIndex: 999 }}
			tabIndex={10} onKeyDown={onKeyDown}
		>
			<button
				className="uk-button uk-position-top-right"
				type="button" uk-icon="icon: close; ratio: 2;"
				onClick={onClose} style={{color: "white"}}
			></button>
			<div
				className={`uk-modal-dialog uk-width-2xlarge uk-padding-small ${className}`}
				style={{
					maxHeight: "90vh",
					overflow: "auto",
					width: width
				}}
			>
				{ children }
				<div className="uk-text-right">
				{
					footerCloseBtn ? (
						<button
							className="uk-button uk-button-default"
							onClick={onClose}
						>Close</button>
					) : null
				}
				</div>
			</div>
		</div>
	);
}
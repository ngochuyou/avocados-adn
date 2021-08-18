import { intRange } from '../../utils';

export default function Paging({
	amount = 0,
	onPageSelect = () => null,
	selected = 0,
	amountPerChunk = 0,
	align = "",
	usePageInput = false
}) {
	if ((amount / amountPerChunk) < 3.4) {
		return (
			<SingleChunk
				amount={amount}
				onPageSelect={onPageSelect}
				selected={selected}
				align={align}
				usePageInput={usePageInput}
			/>
		);
	}

	return (
		<TripleChunk
			amount={amount}
			onPageSelect={onPageSelect}
			selected={selected}
			amountPerChunk={amountPerChunk}
			align={align}
			usePageInput={usePageInput}
		/>
	);
}

function TripleChunk({
	amount = 0,
	onPageSelect = () => null,
	selected = 0,
	amountPerChunk = 0,
	align = "",
	usePageInput = false
}) {
	const eachSidesOfSelected = Math.ceil(amountPerChunk / 2);
	const firstChunkRight = amountPerChunk;

	const lastChunkLeft = amount - amountPerChunk + 1;

	const selectedChunkLeft = selected - eachSidesOfSelected + 1;
	const selectedChunkRight = selected + eachSidesOfSelected -1;

	return (
		<ul className={`uk-pagination ${align}`}>
			<Pages
				min={1}
				max={firstChunkRight}
				selected={selected}
				onClick={onPageSelect}
			/>
			<li className="uk-disabled"><span>...</span></li>
			{
				(selectedChunkLeft <= firstChunkRight) ? 
					<Pages
						min={selectedChunkLeft + 2 + Math.abs(selectedChunkLeft - firstChunkRight + 1)}
						max={selectedChunkRight + 2 + Math.abs(selectedChunkLeft - firstChunkRight + 1)}
						selected={selected}
						onClick={onPageSelect}
					/> :
				(selectedChunkRight < lastChunkLeft) ?
					<Pages
						min={selectedChunkLeft}
						max={selectedChunkRight}
						selected={selected}
						onClick={onPageSelect}
					/> :
					<Pages
						min={selectedChunkLeft - Math.abs(selectedChunkRight - lastChunkLeft + 1)}
						max={selectedChunkRight - Math.abs(selectedChunkRight - lastChunkLeft + 1)}
						selected={selected}
						onClick={onPageSelect}
					/>
			}
			<li className="uk-disabled"><span>...</span></li>
			<Pages
				min={lastChunkLeft}
				max={amount}
				selected={selected}
				onClick={onPageSelect}
			/>
			{
				usePageInput ? (
					<PageInput onPageSelect={onPageSelect} />
				) : null
			}
		</ul>
	);
}

function SingleChunk({
	amount = 0,
	onPageSelect = () => null,
	selected = 0,
	align = "",
	usePageInput = false
}) {
	return (
		<ul className={`uk-pagination ${align}`}>
			<Pages
				min={1}
				max={amount}
				selected={selected}
				onClick={onPageSelect}
			/>
			{
				usePageInput ? (
					<PageInput onPageSelect={onPageSelect} />
				) : null
			}
		</ul>
	);
}

function Pages({ min = 1, max = 1, selected = -1, onClick = () => null }) {
	return intRange({ min, max }).map(index => (
		<li
			key={index}
			onClick={() => onClick(index)}
			className={selected === index ? "uk-active noselect" : "noselect" }
		>
			<span className="pointer">{index}</span>
		</li>
	));
}

function PageInput({ onPageSelect = () => null }) {
	const pageInputKeyDowned = (event) => {
		if (event.keyCode === 13) {
			onPageSelect(parseInt(event.target.value));
		}
	};

	return (
		<li>
			<div className="uk-width-auto uk-padding-remove">
				<input
					className="uk-input uk-width-small"
					type="number"
					placeholder="Page number"
					onKeyDown={pageInputKeyDowned}
				/>
			</div>
		</li>
	);
}
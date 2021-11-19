import { asIf } from '../../utils';

import { NoFollow } from './Link';

export default function PagedComponent({
	pageCount = 0,
	onNextPageRequest = () => console.log("next page requested"),
	onPreviousPageRequest = () => console.log("previous page requested"),
	currentPage = 0,
	children
}) {
	return (
		<>
			{ children }
			<div className="uk-text-center noselect uk-margin-small">
			{
				asIf(currentPage > 0)
				.then(() => (
					<NoFollow
						onClick={onPreviousPageRequest}
						className="uk-margin-right"
					>Previous page</NoFollow>
				)).else()
			}
			{
				asIf(pageCount > 0)
				.then(() => (
					<NoFollow onClick={onNextPageRequest}>Next page</NoFollow>
				)).else()
			}
			</div>
		</>
	);
}
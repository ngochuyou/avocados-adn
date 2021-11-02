import { useState, useEffect } from 'react';

import { fetchProviderList, searchProvider } from '../../actions/provider';

import { SearchInput } from '../utils/Input';
import { CenterModal } from '../utils/Modal';

import { useProvider } from '../../hooks/provider-hooks';

const FETCHED_PROVIDER_COLUMNS = [
	"id", "name", "email",
	"representatorName"
];

export default function ProviderPicker({
	close = () => console.log("close"),
	onPick = (p) => console.log(p)
}) {
	const {
		store: {
			elements: { map: providers }
		}, push
	} = useProvider();
	const [view, setView] = useState(null);
	const [searchDisabled, setSearchDisabled] = useState(false);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await fetchProviderList({
				columns: FETCHED_PROVIDER_COLUMNS,
				page: 0, size: 20
			});

			if (err) {
				console.error(err);
				return;
			}

			push(res);
		};

		doFetch();
	}, [push]);

	const lookUpCurrentStore = (keyword) => Object.values(providers).filter(provider => provider.name.toLowerCase().includes(keyword));
	const onSearchInputChange = (keyword) => {
		if (keyword.length === 0) {
			setView(null);
			return;
		}

		setView(lookUpCurrentStore(keyword.toLowerCase()));
	};
	const onSearchInputEntered = async (keyword) => {
		if (keyword.length === 0) {
			return;
		}

		setSearchDisabled(true);

		const lowerCasedKeyWord = keyword.toLowerCase();
		const [res, err] = await searchProvider({
			name: lowerCasedKeyWord,
			columns: FETCHED_PROVIDER_COLUMNS
		});

		if (err) {
			console.error(err);
			setView(lookUpCurrentStore(lowerCasedKeyWord));
			setSearchDisabled(false);
			return;
		}

		setView([
			...lookUpCurrentStore(lowerCasedKeyWord),
			...res.filter(p => providers[p.id] == null)
		]);
		push(res);
		setTimeout(() => setSearchDisabled(false), 500);
	};
	
	const renderedElements = view == null ? Object.values(providers) : view;

	return (
		<CenterModal
			close={close}
			width="100vw"
		>
			<div className="uk-width-1-1">
				<SearchInput
					placeholder="Search for Providers using a name"
					onChange={onSearchInputChange}
					onEntered={onSearchInputEntered}
					onSearchBtnClick={onSearchInputEntered}
					dd="Press Enter as you finish your keyword"
					disabled={searchDisabled}
				/>
			</div>
			<div className="uk-margin uk-grid-small uk-child-width-1-4 uk-grid-match" uk-grid="">
			{
				renderedElements.map(provider => (
					<div
						key={provider.id}
						onClick={() => onPick(provider)}
					>
						<div className="uk-card uk-card-default uk-card-body uk-card-hover uk-padding-small pointer">
							<div className="uk-text-lead">{provider.name}</div>
							<div>{provider.email}</div>
							<div>{provider.representatorName}</div>
						</div>
					</div>
				))
			}
			</div>
		</CenterModal>
	);
}
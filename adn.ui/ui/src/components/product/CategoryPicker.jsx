import { useEffect, useState } from 'react';

import { fetchCategoryList } from '../../actions/product';

import { useProduct } from '../../hooks/product-hooks';

export default function CategoryPicker({
	close = () => console.log("close"),
	onPick = (category) => console.log("category")
}) {
	const {
		store: {
			category: { elements: list }
		},
		setCategories
	} = useProduct();
	const [listView, setListView] = useState(null);

	useEffect(() => {
		const doFetch = async () => {
			const [res, err] = await fetchCategoryList({
				columns: ["id", "name"],
				size: 500
			});

			if (err) {
				console.error(err);
				return;
			}

			setCategories(res);
		};

		doFetch();
	}, [setCategories]);

	const onKeyDown = (event) => {
		if (event.keyCode === 27) {
			close();
		}
	};

	const onSearchChange = (event) => {
		const { target: { value } } = event;

		if (value.length === 0) {
			setListView(null);
			return;
		}

		setListView(list.filter(ele => ele.name.toLowerCase().includes(value.toLowerCase())));
		return;
	};

	return (
		<div
			className="uk-flex-top uk-open uk-display-block uk-position-fixed uk-width-1-1"
			uk-modal=""
			onKeyDown={onKeyDown}
			tabIndex={10}
		>
			<div
				className="uk-modal-dialog uk-modal-body uk-margin-auto-vertical uk-width-auto"
				uk-overflow-auto=""
			>
				<div className="uk-grid-small uk-margin-bottom" uk-grid="">
					<div className="uk-width-auto">
						<h2 className="colors">Select Category</h2>
					</div>
					<div className="uk-width-expand">
						<input
							type="text"
							placeholder="Search..."
							className="uk-input"
							onChange={onSearchChange}
						/>
					</div>
					<div
						className="uk-width-auto pointer"
						onClick={close}
					>
						<div style={{
							width: "50px",
							height: "100%"
						}} className="uk-position-relative">
							<span
								uk-icon="icon: close; ratio: 1.5"
								className="uk-position-center"
							></span>
						</div>
					</div>
				</div>
				<div className="uk-padding-small">
					<div className="uk-child-width-1-4 uk-grid-match" uk-grid="">
					{
						listView == null ? 
						list.map(ele => (
							<div key={ele.id}>
								<div
									className="uk-card uk-card-default uk-card-body uk-text-center pointer"
									onClick={() => onPick(ele)}
								>
									<h4 className="uk-card-title color-inherit">{ele.name}</h4>
								</div>
							</div>
						)) : 
						listView.map(ele => (
							<div key={ele.id}>
								<div
									className="uk-card uk-card-default uk-card-body uk-text-center pointer"
									onClick={() => onPick(ele)}
								>
									<h4 className="uk-card-title">{ele.name}</h4>
								</div>
							</div>
						))
					}
					</div>
				</div>
			</div>
		</div>
	);
}
import GlobalProductContextProvider from '../../hooks/product-hooks';
import GlobalProviderContextProvider from '../../hooks/provider-hooks';

export default function GlobalContexts({ children }) {
	return (
		<GlobalProviderContextProvider>
			<GlobalProductContextProvider>
			{ children }
			</GlobalProductContextProvider>
		</GlobalProviderContextProvider>
	);
}
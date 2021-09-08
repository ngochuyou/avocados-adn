import GlobalProductContextProvider from '../../hooks/product-hooks';
import GlobalProviderContextProvider, {
	GlobalProductCostContextProvider
} from '../../hooks/provider-hooks';

export default function GlobalContexts({ children }) {
	return (
		<GlobalProviderContextProvider>
			<GlobalProductContextProvider>
				<GlobalProductCostContextProvider>
					{ children }
				</GlobalProductCostContextProvider>
			</GlobalProductContextProvider>
		</GlobalProviderContextProvider>
	);
}
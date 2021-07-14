/**
 * 
 */
package adn.application.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;

import adn.application.Constants;

/**
 * @author Ngoc Huy
 *
 */
@Order(8)
public class AfterBuildMethodsInvoker implements ContextBuilder {

	private static final Logger logger = LoggerFactory.getLogger(AfterBuildMethodsInvoker.class);

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + String.format("Building %s", this.getClass().getName()));

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EffectivelyFinal.class));

		for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
			Class<? extends EffectivelyFinal> type = (Class<? extends EffectivelyFinal>) Class
					.forName(beanDef.getBeanClassName());
			EffectivelyFinal methods = ContextProvider.getBean(type);

			methods.getAccess().execute();
		}

		logger.info(getLoggingPrefix(this) + String.format("Finished building %s", this.getClass().getName()));
	}

}

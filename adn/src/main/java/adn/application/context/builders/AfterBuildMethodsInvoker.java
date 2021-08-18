/**
 * 
 */
package adn.application.context.builders;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.application.context.internal.EffectivelyFinal;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class AfterBuildMethodsInvoker implements ContextBuilder {

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(AfterBuildMethodsInvoker.class);

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EffectivelyFinal.class));

		for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
			Class<? extends EffectivelyFinal> type = (Class<? extends EffectivelyFinal>) Class
					.forName(beanDef.getBeanClassName());
			EffectivelyFinal method;

			_try: try {
				ConstructorBinding cAnno = type.getDeclaredAnnotation(ConstructorBinding.class);

				if (cAnno != null) {
					method = type.getConstructor().newInstance();

					break _try;
				}

				method = ContextProvider.getBean(type);
			} catch (NoSuchBeanDefinitionException nsbde) {
				if (IdentifierGenerator.class.isAssignableFrom(type)) {
					Generic anno;

					if ((anno = type.getDeclaredAnnotation(Generic.class)) == null) {
						throw nsbde;
					}

					Class<? extends Entity> entityClass = (Class<? extends Entity>) anno.entityGene();
					SessionFactoryImplementor sfi = ContextProvider.getBean(SessionFactoryImplementor.class);

					method = (EffectivelyFinal) sfi.getMetamodel().entityPersister(entityClass)
							.getIdentifierGenerator();
					break _try;
				}

				throw nsbde;
			} catch (Exception any) {
				throw any;
			}

			method.getAccess().execute();

			try {
				method.getAccess().close();
			} catch (IllegalAccessException iae) {
				logger.error(String.format("Access to [%s] was closed", method.getClass().getName()));
			}
		}
	}

}

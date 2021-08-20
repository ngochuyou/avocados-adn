/**
 * 
 */
package adn.application;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.application.context.builders.AfterBuildMethodsInvoker;
import adn.application.context.builders.ConfigurationContext;
import adn.application.context.builders.CredentialFactory;
import adn.application.context.builders.DatabaseInitializer;
import adn.application.context.builders.DefaultAuthenticationBasedModelProducerFactory;
import adn.application.context.builders.DefaultAuthenticationBasedModelPropertiesProducerFactory;
import adn.application.context.builders.DefaultDepartmentBasedModelPropertiesFactory;
import adn.application.context.builders.DefaultEntityExtractorProvider;
import adn.application.context.builders.DepartmentScopeContext;
import adn.application.context.builders.DynamicMapModelProducerFactoryImpl;
import adn.application.context.builders.EntityBuilderProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.application.context.builders.ResourceManagerFactoryBuilder;
import adn.application.context.builders.SpecificationFactory;
import adn.application.context.builders.TestRunner;
import adn.application.context.internal.ContextBuilder;

/**
 * @author Ngoc Huy
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@EnableCaching(proxyTargetClass = true)
public class BootEntry {

	public static void main(String[] args) {
		SpringApplication.run(BootEntry.class, args);
	}

	@SuppressWarnings("unchecked")
	@EventListener(ApplicationReadyEvent.class)
	private void doAfterStartup() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(ContextBuilder.class));
		
		List<Class<? extends ContextBuilder>> orderedBuilderClasses = Arrays.asList(
			ConfigurationContext.class,
			ModelContextProvider.class,
			DatabaseInitializer.class,
			DepartmentScopeContext.class,
			CredentialFactory.class,
			EntityBuilderProvider.class,
			SpecificationFactory.class,
			ResourceManagerFactoryBuilder.class,
			DefaultEntityExtractorProvider.class,
			DynamicMapModelProducerFactoryImpl.class,
			DefaultAuthenticationBasedModelProducerFactory.class,
			DefaultAuthenticationBasedModelPropertiesProducerFactory.class,
			DefaultDepartmentBasedModelPropertiesFactory.class,
			AfterBuildMethodsInvoker.class,
			TestRunner.class
		); 
		// @formatter:on
		List<Class<? extends ContextBuilder>> scannedBuilderClasses = scanner
				.findCandidateComponents(Constants.ROOT_PACKAGE).stream().map(beanDef -> {
					try {
						return (Class<? extends ContextBuilder>) Class.forName(beanDef.getBeanClassName());
					} catch (ClassNotFoundException cnfe) {
						cnfe.printStackTrace();
						return null;
					}
				}).filter(builderClass -> !Objects.isNull(builderClass)).collect(Collectors.toList());
		Queue<Class<? extends ContextBuilder>> finalBuilderClassess = new ArrayDeque<>();

		for (Class<? extends ContextBuilder> builderClass : orderedBuilderClasses) {
			if (builderClass.equals(DefaultAuthenticationBasedModelProducerFactory.class)
					|| builderClass.equals(DefaultAuthenticationBasedModelPropertiesProducerFactory.class)
					|| builderClass.equals(DefaultDepartmentBasedModelPropertiesFactory.class)) {
				scannedBuilderClasses.remove(builderClass);
				continue;
			}

			finalBuilderClassess.add(invokeBuilder(builderClass));
			scannedBuilderClasses.remove(builderClass);
		}

		Collections.sort(scannedBuilderClasses, (left, right) -> {
			try {
				Order leftOrder = left.getDeclaredAnnotation(Order.class);
				Order rightOrder = right.getDeclaredAnnotation(Order.class);

				if (leftOrder == null) {
					throw new Exception("Order annotation is required on " + left.getName());
				}

				if (rightOrder == null) {
					throw new Exception("Order annotation is required on " + right.getName());
				}

				return Integer.compare(leftOrder.value(), rightOrder.value());
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);
				exit();
				return 0;
			}
		});

		List<Class<? extends ContextBuilder>> secondBatchBuilders = new ArrayList<>();

		for (Class<? extends ContextBuilder> builderClass : scannedBuilderClasses) {
			finalBuilderClassess.add(invokeBuilder(builderClass));
			secondBatchBuilders.add(builderClass);
		}

		secondBatchBuilders.stream().forEach(builderClass -> scannedBuilderClasses.remove(builderClass));

		Assert.isTrue(scannedBuilderClasses.isEmpty(), String.format(
				"scannedBuilderClasses was supposed to be empty, %d builders remaining", scannedBuilderClasses.size()));
	}

	private Class<? extends ContextBuilder> invokeBuilder(Class<? extends ContextBuilder> builderClass) {
		try {
			ContextBuilder builder = ContextProvider.getBean(builderClass);

			builder.buildAfterStartUp();
		} catch (BeansException e) {
			try {
				builderClass.getConstructor().newInstance().buildAfterStartUp();
			} catch (Exception anyElse) {
				anyElse.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
				exit();
				return null;
			}
		} catch (Exception any) {
			any.printStackTrace();
			exit();
			return null;
		}

		return builderClass;
	}

	private void exit() {
		System.exit(-1);
	}

}
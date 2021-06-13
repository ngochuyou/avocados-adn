/**
 * 
 */
package adn.application;

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

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;

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
		scanner.findCandidateComponents(Constants.ROOT_PACKAGE)
			.stream()
			.map(bean -> {
				try {
					return (Class<? extends ContextBuilder>) Class.forName(bean.getBeanClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
					SpringApplication.exit(context);
					
					return null;
				}
			})
			.sorted((left, right) -> {
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
					
					return 0;
				}
			})
			.forEach(clazz -> {
				try {
					ContextBuilder manager = context.getBean(clazz);
					
					manager.buildAfterStartUp();
				} catch (BeansException be) {
					try {	
						clazz.getConstructor().newInstance().buildAfterStartUp();
					} catch (Exception e) {
						e.printStackTrace();
						SpringApplication.exit(context);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					SpringApplication.exit(context);
				}
			});
		// @formatter:on
	}

}
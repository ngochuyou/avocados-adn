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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.basePackage })
@SpringBootApplication
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class ADNApplication {

	public static void main(String[] args) {
		// TODO: Auto-generated stuff
		SpringApplication.run(ADNApplication.class, args);
	}

	@SuppressWarnings("unchecked")
	@EventListener(ApplicationReadyEvent.class)
	private void doAfterStartup() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(ContextBuilder.class));
		scanner.findCandidateComponents(Constants.basePackage)
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
						throw new Exception("Order annotation is required on " + left.getClass().getName());
					}
					
					if (rightOrder == null) {
						throw new Exception("Order annotation is required on " + right.getClass().getName());
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
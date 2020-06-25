/**
 * 
 */
package adn.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * @author Ngoc Huy
 *
 */
@ComponentScan(basePackages = { Constants.basePackage })
@SpringBootApplication
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class ADNApplication {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO: Auto-generated stuff
		SpringApplication.run(ADNApplication.class, args);

		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(ApplicationManager.class));
		scanner.findCandidateComponents(Constants.managerPackage)
			.stream()
			.map(bean -> {
				ApplicationManager manager = null;
				
				try {
					manager = context.getBean((Class<? extends ApplicationManager>) Class.forName(bean.getBeanClassName()));
				} catch (Exception ex) {
					ex.printStackTrace();
					SpringApplication.exit(context);
				}
				
				return manager;
			})
			.sorted((left, right) -> {
				try {
					Order leftOrder = left.getClass().getDeclaredAnnotation(Order.class);
					Order rightOrder = right.getClass().getDeclaredAnnotation(Order.class);
					
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
			.forEach(manager -> manager.initialize());
		// @formatter:on
	}

}

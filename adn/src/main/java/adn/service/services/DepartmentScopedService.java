/**
 * 
 */
package adn.service.services;

import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConstructorBinding;

import adn.application.context.ContextProvider;
import adn.application.context.EffectivelyFinal;
import adn.controller.exception.UnauthorisedDepartmentException;

/**
 * @author Ngoc Huy
 *
 */
@ConstructorBinding
public class DepartmentScopedService implements EffectivelyFinal {

	private static UUID STOCK;
	private static UUID SALE;
	private static UUID PERSONNEL;
	private static UUID FINANCE;

	private static Access access = new Access() {

		@Override
		public void close() {
			access = null;
		}

		public void execute() throws Exception {
			final Logger logger = LoggerFactory.getLogger(this.getClass());
			SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
			Session ss = sf.openSession();
			Query<Object[]> hql = ss.createQuery("SELECT d.id, d.name FROM Department d WHERE d.name IN (:names)",
					Object[].class);
			ss.setDefaultReadOnly(true);
			hql.setParameterList("names", new String[] { "Stock", "Sale", "Personnel", "Finance" });

			List<Object[]> rows = hql.getResultList();

			ss.clear();
			ss.close();

			STOCK = (UUID) rows.stream().filter(row -> row[1].equals("Stock")).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Stock department"))[0];

			SALE = (UUID) rows.stream().filter(row -> row[1].equals("Sale")).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Sale department"))[0];

			FINANCE = (UUID) rows.stream().filter(row -> row[1].equals("Finance")).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Finance department"))[0];

			PERSONNEL = (UUID) rows.stream().filter(row -> row[1].equals("Personnel")).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Personnel department"))[0];

			logger.info(String.format("Configured [%s]", DepartmentScopedService.class));
		};

	};

	@Override
	public Access getAccess() throws IllegalAccessException {
		return access;
	}

	public static void assertDepartment(UUID assertedTarget, UUID... criterias) throws UnauthorisedDepartmentException {
		if (assertedTarget != null) {
			for (UUID criteria : criterias) {
				if (criteria.equals(assertedTarget)) {
					return;
				}
			}
		}

		throw new UnauthorisedDepartmentException(String.format("Department of id [%s] was denied", assertedTarget));
	}

	public static final UUID stock() {
		return STOCK;
	}

	public static final UUID sale() {
		return SALE;
	}

	public static final UUID personnel() {
		return PERSONNEL;
	}

	public static final UUID finance() {
		return FINANCE;
	}

}

/**
 * 
 */
package adn.application.context.builders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.controller.exception.UnauthorisedDepartmentException;

/**
 * @author Ngoc Huy
 *
 */
public class DepartmentScopeContext implements ContextBuilder {

	private static UUID STOCK;
	private static UUID SALE;
	private static UUID PERSONNEL;
	private static UUID CUSTOMER_SERVICE;
	private static UUID UNKNOWN;

	public static final String STOCK_NAME = "Stock";
	public static final String SALE_NAME = "Sale";
	public static final String PERSONNEL_NAME = "Personnel";
	public static final String CUSTOMERSERVICE_NAME = "Customer Service";

	@Override
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
		Session ss = sf.openSession();
		Query<Object[]> hql = ss.createQuery("SELECT d.id, d.name FROM Department d WHERE d.name IN (:names)",
				Object[].class);
		ss.setDefaultReadOnly(true);
		hql.setParameterList("names", new String[] { STOCK_NAME, SALE_NAME, PERSONNEL_NAME, CUSTOMERSERVICE_NAME });

		List<Object[]> rows = hql.getResultList();

		ss.clear();
		ss.close();

		STOCK = (UUID) rows.stream().filter(row -> row[1].equals(STOCK_NAME)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate Stock department"))[0];

		SALE = (UUID) rows.stream().filter(row -> row[1].equals(SALE_NAME)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate Sale department"))[0];

		CUSTOMER_SERVICE = (UUID) rows.stream().filter(row -> row[1].equals(CUSTOMERSERVICE_NAME)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate Customer Service department"))[0];

		PERSONNEL = (UUID) rows.stream().filter(row -> row[1].equals(PERSONNEL_NAME)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate Personnel department"))[0];

		Set<UUID> ids = new HashSet<>(Arrays.asList(STOCK, SALE, CUSTOMER_SERVICE, PERSONNEL));

		do {
			UNKNOWN = UUID.randomUUID();
		} while (ids.contains(UNKNOWN));

		logger.info(String.format("Configured [%s]", DepartmentScopeContext.class));
	}

	public static final UUID unknown() {
		return UNKNOWN;
	}

	public static final UUID customerService() {
		return CUSTOMER_SERVICE;
	}

	public static final UUID personnel() {
		return PERSONNEL;
	}

	public static final UUID sale() {
		return SALE;
	}

	public static final UUID stock() {
		return STOCK;
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

}

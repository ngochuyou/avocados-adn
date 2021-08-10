/**
 * 
 */
package adn.service;

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
import org.springframework.boot.context.properties.ConstructorBinding;

import adn.application.context.ContextProvider;
import adn.application.context.EffectivelyFinal;
import adn.controller.exception.UnauthorisedDepartmentException;

/**
 * @author Ngoc Huy
 *
 */
@ConstructorBinding
public class DepartmentScoping implements EffectivelyFinal {

	private static UUID STOCK;
	private static UUID SALE;
	private static UUID PERSONNEL;
	private static UUID FINANCE;
	private static UUID UNKNOWN;

	private static Access access = new Access() {

		@Override
		public void close() {
			access = null;
		}

		public void execute() throws Exception {
			final Logger logger = LoggerFactory.getLogger(this.getClass());

			final String STOCK_NAME = "Stock";
			final String SALE_NAME = "Sale";
			final String PERSONNEL_NAME = "Personnel";
			final String FINANCE_NAME = "Finance";

			SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
			Session ss = sf.openSession();
			Query<Object[]> hql = ss.createQuery("SELECT d.id, d.name FROM Department d WHERE d.name IN (:names)",
					Object[].class);
			ss.setDefaultReadOnly(true);
			hql.setParameterList("names", new String[] { STOCK_NAME, SALE_NAME, PERSONNEL_NAME, FINANCE_NAME });

			List<Object[]> rows = hql.getResultList();

			ss.clear();
			ss.close();

			STOCK = (UUID) rows.stream().filter(row -> row[1].equals(STOCK_NAME)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Stock department"))[0];

			SALE = (UUID) rows.stream().filter(row -> row[1].equals(SALE_NAME)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Sale department"))[0];

			FINANCE = (UUID) rows.stream().filter(row -> row[1].equals(FINANCE_NAME)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Finance department"))[0];

			PERSONNEL = (UUID) rows.stream().filter(row -> row[1].equals(PERSONNEL_NAME)).findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to locate Personnel department"))[0];

			Set<UUID> ids = new HashSet<>(Arrays.asList(STOCK, SALE, FINANCE, PERSONNEL));

			do {
				UNKNOWN = UUID.randomUUID();
			} while (ids.contains(UNKNOWN));

			logger.info(String.format("Configured [%s]", DepartmentScoping.class));
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

	public static final UUID unknown() {
		return UNKNOWN;
	}

}

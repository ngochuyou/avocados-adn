/**
 * 
 */
package adn.application.context;

import java.util.List;
import java.util.UUID;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import adn.application.context.DefaultDepartmentBasedModelPropertiesFactory.DepartmentBasedModelPropertiesProducersBuilderContributor;
import adn.model.entities.Provider;
import adn.model.factory.property.production.department.DepartmentBasedModelPropertiesProducersBuilder;

/**
 * @author Ngoc Huy
 *
 */
public class DepartmentBasedModelPropertiesProducersContributorImplementor
		implements DepartmentBasedModelPropertiesProducersBuilderContributor {

	@Override
	public void contribute(DepartmentBasedModelPropertiesProducersBuilder builder) {
		SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
		Session ss = sf.openSession();

		ss.setDefaultReadOnly(true);
		ss.setHibernateFlushMode(FlushMode.MANUAL);

		Query<Object[]> hql = ss.createQuery("SELECT d.id, d.name FROM Department d WHERE d.name IN (:names)",
				Object[].class);

//		hql.setParameterList("names", new String[] { "Stock", "Sale", "Personnel", "Finance" });
		hql.setParameterList("names", new String[] { "Stock" });
		List<Object[]> departmentList = hql.getResultList();

		ss.clear();
		ss.close();

		UUID stock = getDepartmentId("Stock", departmentList);
		// @formatter:off
		builder
			.type(Provider.class)
				.department(stock, "Stock")
				.publish();
		// @formatter:on
	}

	private UUID getDepartmentId(String name, List<Object[]> rows) {
		for (Object[] row : rows) {
			if (row[1].equals(name)) {
				return (UUID) row[0];
			}
		}

		return null;
	}

}

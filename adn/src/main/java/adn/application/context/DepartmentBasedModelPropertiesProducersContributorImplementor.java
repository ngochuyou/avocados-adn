/**
 * 
 */
package adn.application.context;

import static adn.service.DepartmentScoping.PERSONNEL_NAME;
import static adn.service.DepartmentScoping.SALE_NAME;
import static adn.service.DepartmentScoping.STOCK_NAME;

import java.util.List;
import java.util.UUID;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import adn.application.context.DefaultDepartmentBasedModelPropertiesFactory.DepartmentBasedModelPropertiesProducersBuilderContributor;
import adn.helpers.Utils;
import adn.model.entities.Department;
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

		hql.setParameterList("names", new String[] { STOCK_NAME, SALE_NAME, PERSONNEL_NAME });
		List<Object[]> departmentList = hql.getResultList();

		ss.clear();
		ss.close();

		UUID stock = getDepartmentId(STOCK_NAME, departmentList);
		UUID sale = getDepartmentId(SALE_NAME, departmentList);
		UUID personnel = getDepartmentId(PERSONNEL_NAME, departmentList);
		// @formatter:off
		builder
			.type(Provider.class)
				.department("Stock, Sale", stock, sale).publish()
				.fields("deactivatedDate").use(Utils::formatLocalDateTime)
		.and()
			.type(Department.class)
				.department("Personnel", personnel).publish();
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

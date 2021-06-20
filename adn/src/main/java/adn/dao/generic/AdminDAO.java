package adn.dao.generic;

import java.util.Date;

import org.springframework.stereotype.Repository;

import adn.model.Genetized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Admin.class)
public class AdminDAO extends AccountDAO<Admin> {

	@Override
	public Admin insertionBuild(Admin model) {
		// TODO Auto-generated method stub
		model = super.insertionBuild(model);
		model.setContractDate(new Date());

		return model;
	}

	@Override
	public Admin updateBuild(Admin model) {
		// TODO Auto-generated method stub
		model = super.updateBuild(model);

		Admin persistence = sessionFactory.getCurrentSession().get(Admin.class, model.getId());

		if (persistence.getContractDate() == null) {
			persistence.setContractDate(model.getContractDate() != null ? model.getContractDate() : new Date());
		}

		return model;
	}

}

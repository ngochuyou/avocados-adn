package adn.dao.generic;

import java.util.Date;

import org.springframework.stereotype.Repository;

import adn.dao.BaseDAO;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Admin.class)
public class AdminDAO extends BaseDAO implements GenericDAO<Admin> {
	
	@Override
	public Admin insertionBuild(Admin model) {
		// TODO Auto-generated method stub
		model.setContractDate(new Date());
		
		return model;
	}
	
}

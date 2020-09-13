/**
 * 
 */
package adn.model.models;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.model.Genetized;
import adn.model.entities.Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Genetized(entityGene = Personnel.class)
public class PersonnelModel extends AccountModel {

	protected String createdBy;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}

/**
 * 
 */
package adn.model.models;

import adn.model.Genetized;
import adn.model.entities.Personnel;

/**
 * @author Ngoc Huy
 *
 */
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

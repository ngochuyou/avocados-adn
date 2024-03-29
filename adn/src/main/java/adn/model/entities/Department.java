/**
 * 
 */
package adn.model.entities;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.application.Common;
import adn.model.entities.metadata._Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "departments")
public class Department extends PermanentEntity implements NamedResource {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = Common.MYSQL_UUID_COLUMN_DEFINITION)
	protected UUID id;

	@Column(nullable = false, unique = true)
	protected String name;

	@JsonIgnore
	@OneToMany(mappedBy = _Personnel.department)
	private List<Personnel> personnels;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Personnel> getPersonnels() {
		return personnels;
	}

	public void setPersonnels(List<Personnel> personnels) {
		this.personnels = personnels;
	}

}

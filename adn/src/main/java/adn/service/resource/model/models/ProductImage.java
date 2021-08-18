/**
 * 
 */
package adn.service.resource.model.models;

import javax.persistence.Entity;

import org.hibernate.annotations.Persister;

import adn.service.resource.ResourcePersisterImpl;
import adn.service.resource.annotation.Constructor;
import adn.service.resource.annotation.Directory;
import adn.service.resource.annotation.LocalResource;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
@Directory(path = ProductImage.DIRECTORY)
@Constructor(columnNames = FileResource.ID_NAME, argumentTypes = String.class)
@Persister(impl = ResourcePersisterImpl.class)
@Entity
public class ProductImage extends ImageByBytes {

	public static final String DIRECTORY = "images\\product_image\\";

}

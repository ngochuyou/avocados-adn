/**
 * 
 */
package adn.model;

import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass // unused annotation
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractModel {

}

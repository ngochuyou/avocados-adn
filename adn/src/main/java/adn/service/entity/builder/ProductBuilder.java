/**
 * 
 */
package adn.service.entity.builder;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductBuilder extends FactorBuilder<Product> {

}

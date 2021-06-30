package adn.model.factory;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.models.Model;

@Component("modelEntityExtractor")
@Generic(entityGene = Entity.class)
public class ModelEntityExtractor<T extends Entity, M extends Model> implements EntityExtractor<T, M> {

}

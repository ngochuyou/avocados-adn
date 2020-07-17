/**
 * 
 */
package adn.service.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import adn.model.Model;
import adn.service.ApplicationService;
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Model.class)
public class ModelService implements ApplicationService<Model> {

	@Override
	public Model doDeactivationProcedure(Model model) {
		// TODO Auto-generated method stub
		model.setDeactivatedDate(new Date());

		return model;
	}

}

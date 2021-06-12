/**
 * 
 */
package adn.service.resource.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import adn.helpers.StringHelper;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class TemplateValidator implements Validator {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private TemplateValidator() {}

	@Override
	public boolean supports(Class<?> clazz) {
		return ResourceTemplate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ResourceTemplate template = (ResourceTemplate) target;

		logger.trace(String.format("Validating template: [%s]", template.getTemplateName()));

		Assert.isTrue(StringHelper.hasLength(template.getTemplateName()), "Template name must not be empty");
		Assert.isTrue(StringHelper.hasLength(template.getPathColumn()), "Unable to locate pathname column");
		Assert.notNull(template.getColumnNames(),
				String.format("[%s]: Resource column names must not be null", template.getTemplateName()));

		int span = template.getColumnNames().length;

		Assert.isTrue(span == template.getColumnTypes().length, "Column names span and column types span must match");

		for (int i = 1; i < span; i++) {
			Assert.isTrue(StringHelper.hasLength(template.getColumnNames()[i]),
					String.format("Column name must not be empty, found null at index [%d]", i));
			Assert.notNull(template.getColumnTypes()[i],
					String.format("Column type must not be null, found null at index [%d]", i));
		}
	}

}

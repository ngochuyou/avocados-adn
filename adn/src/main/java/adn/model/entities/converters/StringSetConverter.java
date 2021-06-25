/**
 * 
 */
package adn.model.entities.converters;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Ngoc Huy
 *
 */
@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

	private static final String DELIMETER = ",";

	@Override
	public String convertToDatabaseColumn(Set<String> attributes) {
		return attributes.stream().collect(Collectors.joining(DELIMETER));
	}

	@Override
	public Set<String> convertToEntityAttribute(String dbData) {
		return Set.of(dbData.split(DELIMETER));
	}

}

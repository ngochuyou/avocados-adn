/**
 * 
 */
package adn.model.entities.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Ngoc Huy
 *
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	private static final String DELIMETER = ",";

	@Override
	public String convertToDatabaseColumn(List<String> attributes) {
		return attributes == null ? "" : attributes.stream().collect(Collectors.joining(DELIMETER));
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		return dbData != null ? List.of(dbData.split(DELIMETER)) : new ArrayList<>();
	}

}

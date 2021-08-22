/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.SourceType;
import io.jsonwebtoken.lang.Collections;

/**
 * @author Ngoc Huy
 *
 */
public class SourceMetadataImpl<T extends DomainEntity> implements SourceMetadata<T> {

	private final Class<T> entityType;
	private final String[] columns;
	private final Class<?> representation;
	private final Set<Integer> associationIndicies;
	private final SourceType sourceType;
	private final Map<Integer, SourceMetadata<?>> associationMetadatas;

	// @formatter:off
	public SourceMetadataImpl(
			Class<T> entityType,
			Collection<String> requestedColumns,
			SourceType sourceType,
			Class<?> representation,
			DomainEntityMetadata<T> entityMetadata) {
		super();
		this.entityType = entityType;
		this.columns = requestedColumns.toArray(String[]::new);
		this.sourceType = sourceType;
		this.representation = resolveRepresentation(sourceType, representation);
		associationIndicies = null;
		associationMetadatas = null;
	}
	
	public SourceMetadataImpl(
			Class<T> entityType,
			Collection<String> requestedColumns,
			SourceType sourceType,
			Class<?> representation,
			DomainEntityMetadata<T> entityMetadata,
			SourceMetadata<?>... associationMetadatas) {
		super();
		this.entityType = entityType;
		this.columns = requestedColumns.toArray(String[]::new);
		this.sourceType = sourceType;
		this.representation = resolveRepresentation(sourceType, representation);
		// associationIndicies.size() and associationMetadatas.length must match
		// this must be asserted by devs as contract
		Map<Integer, SourceMetadata<?>> metadatas = new HashMap<>();
		
		associationIndicies = IntStream.range(0, columns.length)
				.filter(index -> {
					if (entityMetadata.isAssociation(columns[index])) {
						metadatas.put(index, associationMetadatas[metadatas.size()]);
						return true;
					}
					
					return false;
				})
				.boxed().collect(Collectors.toSet());
		this.associationMetadatas = metadatas;
	}
	
	public SourceMetadataImpl(
			Class<T> entityType,
			Collection<String> requestedColumns,
			SourceType sourceType,
			Class<?> representation,
			DomainEntityMetadata<T> entityMetadata,
			Map<Integer, SourceMetadata<?>> associationMetadatas) {
		super();
		this.entityType = entityType;
		this.columns = requestedColumns.toArray(String[]::new);
		this.sourceType = sourceType;
		this.representation = resolveRepresentation(sourceType, representation);
		// associationIndicies.size() and associationMetadatas.length must match
		// this must be asserted by devs as contract
		associationIndicies = IntStream.range(0, columns.length)
				.filter(index -> entityMetadata.isAssociation(columns[index]))
				.boxed().collect(Collectors.toSet());
		this.associationMetadatas = associationMetadatas;
	}
	// @formatter:on

	private Class<?> resolveRepresentation(SourceType type, Class<?> requestedRepresentation) {
		if (sourceType == SourceType.POJO) {
			return entityType;
		}

		if (sourceType == SourceType.OBJECT_ARRAY) {
			return Object[].class;
		}

		return requestedRepresentation;
	}

	@Override
	public Class<T> getEntityType() {
		return entityType;
	}

	@Override
	public Class<?> getRepresentation() {
		return representation;
	}

	@Override
	public SourceType getSourceType() {
		return sourceType;
	}

	@Override
	public String[] getColumns() {
		return columns;
	}

	@Override
	public boolean hasAssociation() {
		return !Collections.isEmpty(associationIndicies);
	}

	@Override
	public Set<Integer> getAssociationIndices() {
		return associationIndicies;
	}

	@Override
	public SourceMetadata<? extends DomainEntity> getAssociationMetadata(int index) {
		return associationMetadatas.get(index);
	}

}

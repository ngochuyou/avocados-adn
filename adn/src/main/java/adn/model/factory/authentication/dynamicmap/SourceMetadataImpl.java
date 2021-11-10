/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import adn.helpers.CollectionHelper;
import adn.helpers.StringHelper;
import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.SourceType;

/**
 * @author Ngoc Huy
 *
 */
public class SourceMetadataImpl<T extends DomainEntity> implements SourceMetadata<T> {

	private static final Logger logger = LoggerFactory.getLogger(SourceMetadataImpl.class);

	private final Class<T> entityType;
	private List<String> columns;
	private final Class<?> representation;
	private final Set<Integer> associationIndicies;
	private final SourceType sourceType;
	private final Map<Integer, SourceMetadata<?>> associationMetadatas;
	private Pageable paging;
	private Specification<T> specification;

	// @formatter:off
	public SourceMetadataImpl(
			Class<T> entityType,
			Collection<String> requestedColumns,
			SourceType sourceType,
			Class<?> representation,
			DomainEntityMetadata<T> entityMetadata) {
		super();
		this.entityType = entityType;
		
		setColumns(requestedColumns);
		
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

		setColumns(requestedColumns);
		
		this.sourceType = sourceType;
		this.representation = resolveRepresentation(sourceType, representation);
		// associationIndicies.size() and associationMetadatas.length must match
		// this must be asserted by devs as contract
		Map<Integer, SourceMetadata<?>> metadatas = new HashMap<>();
		
		associationIndicies = Collections.unmodifiableSet(IntStream.range(0, columns.size())
				.filter(index -> {
					if (entityMetadata.isAssociation(columns.get(index))) {
						metadatas.put(index, associationMetadatas[metadatas.size()]);
						return true;
					}
					
					return false;
				})
				.boxed().collect(Collectors.toSet()));
		this.associationMetadatas = Collections.unmodifiableMap(metadatas);
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

		setColumns(requestedColumns);
		
		this.sourceType = sourceType;
		this.representation = resolveRepresentation(sourceType, representation);
		// associationIndicies.size() and associationMetadatas.length must match
		// this must be asserted by devs as contract
		associationIndicies = associationMetadatas.keySet();
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
	public List<String> getColumns() {
		return columns;
	}

	@Override
	public boolean hasAssociation() {
		return !CollectionHelper.isEmpty(associationIndicies);
	}

	@Override
	public Set<Integer> getAssociationIndices() {
		return associationIndicies;
	}

	@Override
	public SourceMetadata<?> getAssociationMetadata(int index) {
		return associationMetadatas.get(index);
	}

	private void setColumns(Collection<String> columns) {
		setColumns(columns instanceof List ? (List<String>) columns : new ArrayList<>(columns));
	}

	@Override
	public void setColumns(List<String> columns) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("%s{%s} Setting columns [%s]", entityType.getSimpleName(), sourceType,
					columns.stream().collect(Collectors.joining(StringHelper.COMMON_JOINER))));
		}

		this.columns = columns;
	}

	@Override
	public Pageable getPaging() {
		return paging;
	}

	@Override
	public Specification<T> getSpecification() {
		return specification;
	}

	@Override
	public void setPaging(Pageable paging) {
		this.paging = paging;
	}

	@Override
	public void setSpecification(Specification<T> specification) {
		this.specification = specification;
	}

}

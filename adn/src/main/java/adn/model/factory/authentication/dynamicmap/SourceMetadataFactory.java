/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.model.DomainEntity;
import adn.model.entities.metadata.AssociationType;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.SourceType;

/**
 * @author Ngoc Huy
 *
 */
public class SourceMetadataFactory {

	private static final Logger logger = LoggerFactory.getLogger(SourceMetadataFactory.class);
	private static final ModelContextProvider MODEL_CONTEXT = ContextProvider.getBean(ModelContextProvider.class);

	public static <T extends DomainEntity> SourceMetadata<T> basic(Class<T> entityType, Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				MODEL_CONTEXT.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicArray(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				MODEL_CONTEXT.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicCollection(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				MODEL_CONTEXT.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicArrayCollection(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				MODEL_CONTEXT.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> associated(Class<T> entityType, Collection<String> columns,
			SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArray(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedCollection(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArrayCollection(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associated(Class<T> entityType, Collection<String> columns,
			Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArray(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedCollection(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArrayCollection(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				MODEL_CONTEXT.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> unknown(Class<T> entityType, List<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, entityType,
				MODEL_CONTEXT.getMetadata(entityType), resolveUnknownAssociationMetadatas(entityType, columns));
	}

	public static <T extends DomainEntity> SourceMetadata<T> unknownCollection(Class<T> entityType,
			List<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				MODEL_CONTEXT.getMetadata(entityType), resolveUnknownAssociationMetadatas(entityType, columns));
	}

	public static <T extends DomainEntity> SourceMetadata<T> unknownArray(Class<T> entityType, List<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, entityType,
				MODEL_CONTEXT.getMetadata(entityType), resolveUnknownAssociationMetadatas(entityType, columns));
	}

	public static <T extends DomainEntity> SourceMetadata<T> unknownArrayCollection(Class<T> entityType,
			List<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				MODEL_CONTEXT.getMetadata(entityType), resolveUnknownAssociationMetadatas(entityType, columns));
	}

	private static <T extends DomainEntity> Map<Integer, SourceMetadata<?>> resolveUnknownAssociationMetadatas(
			Class<T> owningType, List<String> owningTypeColumns) {
		DomainEntityMetadata<T> entityMetadata = MODEL_CONTEXT.getMetadata(owningType);
		Map<Integer, SourceMetadata<?>> associationMetadatas = new HashMap<>();

		IntStream.range(0, owningTypeColumns.size()).forEach(index -> {
			String column = owningTypeColumns.get(index);

			if (entityMetadata.isAssociation(column)) {
				Class<? extends DomainEntity> associationClass = entityMetadata.getAssociationClass(column);
				List<String> associationColumns = MODEL_CONTEXT.getMetadata(associationClass).getNonLazyPropertyNames();
				AssociationType associationType = entityMetadata.getAssociationType(column);

				if (associationType != AssociationType.COLLECTION && associationType != AssociationType.ENTITY) {
					logger.error(
							String.format("Unknown %s: [%s]", AssociationType.class.getSimpleName(), associationType));
					return;
				}

				associationMetadatas.put(index,
						associationType == AssociationType.ENTITY ? basic(associationClass, associationColumns)
								: basicCollection(associationClass, associationColumns));
				return;
			}
		});

		return associationMetadatas;
	}

}

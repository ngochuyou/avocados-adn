/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.Collection;
import java.util.Map;

import adn.application.context.ContextProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.model.DomainEntity;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.SourceType;

/**
 * @author Ngoc Huy
 *
 */
public class SourceMetadataFactory {

	private static final ModelContextProvider modelContext = ContextProvider.getBean(ModelContextProvider.class);

	public static <T extends DomainEntity> SourceMetadata<T> basic(Class<T> entityType, Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				modelContext.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicArray(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				modelContext.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicCollection(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				modelContext.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> basicArrayCollection(Class<T> entityType,
			Collection<String> columns) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				modelContext.getMetadata(entityType));
	}

	public static <T extends DomainEntity> SourceMetadata<T> associated(Class<T> entityType, Collection<String> columns,
			SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArray(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedCollection(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArrayCollection(Class<T> entityType,
			Collection<String> columns, SourceMetadata<?>... associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associated(Class<T> entityType, Collection<String> columns,
			Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.POJO, null,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArray(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.OBJECT_ARRAY, null,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedCollection(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, entityType,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

	public static <T extends DomainEntity> SourceMetadata<T> associatedArrayCollection(Class<T> entityType,
			Collection<String> columns, Map<Integer, SourceMetadata<?>> associationMetadatas) {
		return new SourceMetadataImpl<>(entityType, columns, SourceType.COLLECTION, Object[].class,
				modelContext.getMetadata(entityType), associationMetadatas);
	}

}

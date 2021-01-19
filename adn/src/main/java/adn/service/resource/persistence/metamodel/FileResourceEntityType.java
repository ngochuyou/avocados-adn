/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.springframework.boot.SpringApplication;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.service.resource.FileResource;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("unchecked")
public class FileResourceEntityType implements EntityType<FileResource> {

	private final IdentifierType identifierType = new IdentifierType(this);

	private final VersionType versionType = new VersionType(this);

	private final ContentType contentType = new ContentType(this);

	private transient Field identifierMember;

	private transient Field versionMember;

	private transient Field contentMember;

	private final String entityName = FileResource.class.getName();

	private final Set<Attribute<FileResource, ?>> attributeSet = Set.of(identifierType, versionType, contentType);

	/**
	 * 
	 */
	public FileResourceEntityType() {
		// TODO Auto-generated constructor stub
		try {
			identifierMember = FileResource.class.getDeclaredField(identifierType.identiferName);
			versionMember = FileResource.class.getDeclaredField(versionType.versionName);
			contentMember = FileResource.class.getDeclaredField(contentType.contentName);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
	}

	private <Y> void assertRequestedIdType(Class<Y> type) {
		Assert.isTrue(String.class.isAssignableFrom(type), "Can not find identifier of type " + type);
	}

	private <Y> void assertRequestedVersionType(Class<Y> type) {
		Assert.isTrue(String.class.isAssignableFrom(type), "Can not find identifier of type " + type);
	}

	@Override
	public <Y> SingularAttribute<? super FileResource, Y> getId(Class<Y> type) {
		// TODO Auto-generated method stub
		assertRequestedIdType(type);

		return (SingularAttribute<? super FileResource, Y>) identifierType;
	}

	@Override
	public <Y> SingularAttribute<FileResource, Y> getDeclaredId(Class<Y> type) {
		// TODO Auto-generated method stub
		assertRequestedIdType(type);

		return (SingularAttribute<FileResource, Y>) identifierType;
	}

	@Override
	public <Y> SingularAttribute<? super FileResource, Y> getVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		assertRequestedVersionType(type);

		return (SingularAttribute<? super FileResource, Y>) versionType;
	}

	@Override
	public <Y> SingularAttribute<FileResource, Y> getDeclaredVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		assertRequestedVersionType(type);

		return (SingularAttribute<FileResource, Y>) versionType;
	}

	@Override
	public IdentifiableType<? super FileResource> getSupertype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasVersionAttribute() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Set<SingularAttribute<? super FileResource, ?>> getIdClassAttributes() {
		// TODO Auto-generated method stub
		return Set.of(identifierType);
	}

	@Override
	public Type<?> getIdType() {
		// TODO Auto-generated method stub
		return identifierType;
	}

	@Override
	public Set<Attribute<? super FileResource, ?>> getAttributes() {
		// TODO Auto-generated method stub
		return attributeSet.stream().collect(Collectors.toSet());
	}

	@Override
	public Set<Attribute<FileResource, ?>> getDeclaredAttributes() {
		// TODO Auto-generated method stub
		return attributeSet;
	}

	@Override
	public <Y> SingularAttribute<? super FileResource, Y> getSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		// @formatter:off
		return (SingularAttribute<? super FileResource, Y>) attributeSet.stream()
				.filter(ele -> ele.getName().equals(name) && ele.getJavaType().equals(type))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	@Override
	public <Y> SingularAttribute<FileResource, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		// @formatter:off
		return (SingularAttribute<FileResource, Y>) attributeSet.stream()
				.filter(ele -> ele.getName().equals(name) && ele.getJavaType().equals(type))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	@Override
	public Set<SingularAttribute<? super FileResource, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		// @formatter:off
		return attributeSet
				.stream().map(ele -> (SingularAttribute<FileResource, ?>) ele)
				.collect(Collectors.toSet());
		// @formatter:on
	}

	@Override
	public Set<SingularAttribute<FileResource, ?>> getDeclaredSingularAttributes() {
		// TODO Auto-generated method stub
		// @formatter:off
		return attributeSet
				.stream().map(ele -> (SingularAttribute<FileResource, ?>) ele)
				.collect(Collectors.toSet());
		// @formatter:on
	}

	@Override
	public <E> CollectionAttribute<? super FileResource, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<FileResource, E> getDeclaredCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<? super FileResource, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<FileResource, E> getDeclaredSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<? super FileResource, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<FileResource, E> getDeclaredList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<? super FileResource, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<FileResource, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<? super FileResource, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Set<PluralAttribute<FileResource, ?, ?>> getDeclaredPluralAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Attribute<? super FileResource, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		// @formatter:off
		return attributeSet
				.stream().filter(ele -> ele.getName().equals(name))
				.findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public Attribute<FileResource, ?> getDeclaredAttribute(String name) {
		// TODO Auto-generated method stub
		// @formatter:off
		return attributeSet
				.stream().filter(ele -> ele.getName().equals(name))
				.findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public SingularAttribute<? super FileResource, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		// @formatter:off
		return (SingularAttribute<? super FileResource, ?>) attributeSet
				.stream().filter(ele -> ele instanceof SingularAttribute && ele.getName().equals(name))
				.findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public SingularAttribute<FileResource, ?> getDeclaredSingularAttribute(String name) {
		// TODO Auto-generated method stub
		// @formatter:off
		return (SingularAttribute<FileResource, ?>) attributeSet
				.stream().filter(ele -> ele instanceof SingularAttribute && ele.getName().equals(name))
				.findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public CollectionAttribute<? super FileResource, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<FileResource, ?> getDeclaredCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<? super FileResource, ?> getSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<FileResource, ?> getDeclaredSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<? super FileResource, ?> getList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<FileResource, ?> getDeclaredList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<? super FileResource, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<FileResource, ?, ?> getDeclaredMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.ENTITY;
	}

	@Override
	public Class<FileResource> getJavaType() {
		// TODO Auto-generated method stub
		return FileResource.class;
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return BindableType.ENTITY_TYPE;
	}

	@Override
	public Class<FileResource> getBindableJavaType() {
		// TODO Auto-generated method stub
		return FileResource.class;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return entityName;
	}

	class IdentifierType implements SingularAttribute<FileResource, String>, Type<String> {

		protected final String identiferName = "pathname";

		private final FileResourceEntityType declaringType;

		public IdentifierType(FileResourceEntityType declaringType) {
			super();
			Assert.notNull(declaringType, "Declaring type can not be null");
			this.declaringType = declaringType;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return identiferName;
		}

		@Override
		public PersistentAttributeType getPersistentAttributeType() {
			// TODO Auto-generated method stub
			return PersistentAttributeType.BASIC;
		}

		@Override
		public ManagedType<FileResource> getDeclaringType() {
			// TODO Auto-generated method stub
			return declaringType;
		}

		@Override
		public Class<String> getJavaType() {
			// TODO Auto-generated method stub
			return String.class;
		}

		@Override
		public Member getJavaMember() {
			// TODO Auto-generated method stub
			return identifierMember;
		}

		@Override
		public boolean isAssociation() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCollection() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public BindableType getBindableType() {
			// TODO Auto-generated method stub
			return BindableType.SINGULAR_ATTRIBUTE;
		}

		@Override
		public Class<String> getBindableJavaType() {
			// TODO Auto-generated method stub
			return String.class;
		}

		@Override
		public boolean isId() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isVersion() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isOptional() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Type<String> getType() {
			// TODO Auto-generated method stub
			return declaringType.identifierType;
		}

		@Override
		public PersistenceType getPersistenceType() {
			// TODO Auto-generated method stub
			return PersistenceType.BASIC;
		}

	}

	class VersionType implements SingularAttribute<FileResource, String>, Type<String> {

		protected final String versionName = "version";

		private final FileResourceEntityType declaringType;

		/**
		 * 
		 */
		public VersionType(FileResourceEntityType declaringType) {
			// TODO Auto-generated constructor stub
			super();
			Assert.notNull(declaringType, "Declaring type can not be null");
			this.declaringType = declaringType;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return versionName;
		}

		@Override
		public PersistentAttributeType getPersistentAttributeType() {
			// TODO Auto-generated method stub
			return PersistentAttributeType.BASIC;
		}

		@Override
		public ManagedType<FileResource> getDeclaringType() {
			// TODO Auto-generated method stub
			return declaringType;
		}

		@Override
		public Class<String> getJavaType() {
			// TODO Auto-generated method stub
			return String.class;
		}

		@Override
		public Member getJavaMember() {
			// TODO Auto-generated method stub
			return declaringType.versionMember;
		}

		@Override
		public boolean isAssociation() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCollection() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public BindableType getBindableType() {
			// TODO Auto-generated method stub
			return BindableType.SINGULAR_ATTRIBUTE;
		}

		@Override
		public Class<String> getBindableJavaType() {
			// TODO Auto-generated method stub
			return String.class;
		}

		@Override
		public boolean isId() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isVersion() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isOptional() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Type<String> getType() {
			// TODO Auto-generated method stub
			return declaringType.versionType;
		}

		@Override
		public PersistenceType getPersistenceType() {
			// TODO Auto-generated method stub
			return PersistenceType.BASIC;
		}

	}

	class ContentType implements SingularAttribute<FileResource, byte[]>, Type<byte[]> {

		protected final String contentName = "content";

		private final FileResourceEntityType declaringType;

		/**
		 * 
		 */
		public ContentType(FileResourceEntityType declaringType) {
			// TODO Auto-generated constructor stub
			super();
			Assert.notNull(declaringType, "Declaring type can not be null");
			this.declaringType = declaringType;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return contentName;
		}

		@Override
		public PersistentAttributeType getPersistentAttributeType() {
			// TODO Auto-generated method stub
			return PersistentAttributeType.BASIC;
		}

		@Override
		public ManagedType<FileResource> getDeclaringType() {
			// TODO Auto-generated method stub
			return declaringType;
		}

		@Override
		public Class<byte[]> getJavaType() {
			// TODO Auto-generated method stub
			return byte[].class;
		}

		@Override
		public Member getJavaMember() {
			// TODO Auto-generated method stub
			return declaringType.contentMember;
		}

		@Override
		public boolean isAssociation() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCollection() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public BindableType getBindableType() {
			// TODO Auto-generated method stub
			return BindableType.SINGULAR_ATTRIBUTE;
		}

		@Override
		public Class<byte[]> getBindableJavaType() {
			// TODO Auto-generated method stub
			return byte[].class;
		}

		@Override
		public boolean isId() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isVersion() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isOptional() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public Type<byte[]> getType() {
			// TODO Auto-generated method stub
			return declaringType.contentType;
		}

		@Override
		public PersistenceType getPersistenceType() {
			// TODO Auto-generated method stub
			return PersistenceType.BASIC;
		}

	}

}

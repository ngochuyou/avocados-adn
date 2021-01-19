/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Ngoc Huy
 *
 */
public class ManagedStringType implements ManagedType<String> {

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.BASIC;
	}

	@Override
	public Class<String> getJavaType() {
		// TODO Auto-generated method stub
		return String.class;
	}

	@Override
	public Set<Attribute<? super String, ?>> getAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Set<Attribute<String, ?>> getDeclaredAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public <Y> SingularAttribute<? super String, Y> getSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<String, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SingularAttribute<? super String, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Set<SingularAttribute<String, ?>> getDeclaredSingularAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public <E> CollectionAttribute<? super String, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<String, E> getDeclaredCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<? super String, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<String, E> getDeclaredSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<? super String, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<String, E> getDeclaredList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<? super String, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<String, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<? super String, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Set<PluralAttribute<String, ?, ?>> getDeclaredPluralAttributes() {
		// TODO Auto-generated method stub
		return Set.of();
	}

	@Override
	public Attribute<? super String, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<String, ?> getDeclaredAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingularAttribute<? super String, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SingularAttribute<String, ?> getDeclaredSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<? super String, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<String, ?> getDeclaredCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<? super String, ?> getSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<String, ?> getDeclaredSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<? super String, ?> getList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<String, ?> getDeclaredList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<? super String, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<String, ?, ?> getDeclaredMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}

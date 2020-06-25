/**
 * 
 */
package adn.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import adn.application.ApplicationContextProvider;
import adn.utilities.ClassReflector;

/**
 * @author Ngoc Huy
 *
 */
public class EntityTree {

	private EntityTree parent;

	private Class<? extends Model> node;

	private Set<EntityTree> childrens;

	private ClassReflector reflector = ApplicationContextProvider.getApplicationContext().getBean(ClassReflector.class);

	public EntityTree(EntityTree parent, Class<? extends Model> node, Set<EntityTree> childrens) {
		super();
		this.parent = parent;
		this.node = node == null ? Model.class : node;
		this.childrens = childrens == null ? new HashSet<>() : childrens;
	}

	public void add(Class<? extends Model> clazz) {
		if (clazz == null || clazz == this.node) {
			return;
		}

		if (this.node.equals(clazz.getSuperclass())) {
			this.childrens.add(new EntityTree(this, clazz, null));

			return;
		}

		Stack<Class<? extends Model>> stack = reflector.getModelClassStack(clazz);

		while (!stack.isEmpty()) {
			this.childrens.forEach(tree -> tree.add(stack.pop()));
		}
	}

	public boolean contains(Class<? extends Model> clazz) {
		if (clazz == null) {
			return false;
		}
		
		if (clazz == this.node) {
			return true;
		}
		
		for (EntityTree tree: this.childrens) {
			if (tree.contains(clazz)) {
				return true;
			}
		}

		return false;
	}
	
	public void forEach(Consumer<EntityTree> consumer) {
		consumer.accept(this);

		this.childrens.forEach(tree -> tree.forEach(consumer));
	}

	public EntityTree getParent() {
		return parent;
	}

	public void setParent(EntityTree parent) {
		this.parent = parent;
	}

	public Class<? extends Model> getNode() {
		return node;
	}

	public void setNode(Class<? extends Model> node) {
		this.node = node;
	}

	public Set<EntityTree> getChildrens() {
		return childrens;
	}

	public void setChildrens(Set<EntityTree> childrens) {
		this.childrens = childrens;
	}

}

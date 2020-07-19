/**
 * 
 */
package adn.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Ngoc Huy
 *
 */
public class EntityTree {

	private EntityTree parent;

	private Class<? extends Entity> node;

	private Set<EntityTree> childrens;

	public EntityTree(EntityTree parent, Class<? extends Entity> node, Set<EntityTree> childrens) {
		super();
		this.parent = parent;
		this.node = node == null ? Entity.class : node;
		this.childrens = childrens == null ? new HashSet<>() : childrens;
	}

	public void add(Class<? extends Entity> clazz) {
		if (clazz == null || clazz == Entity.class) {
			return;
		}

		if (this.contains(clazz)) {
			return;
		}

		if (this.node.equals(clazz.getSuperclass())) {
			this.childrens.add(new EntityTree(this, clazz, null));

			return;
		}

		this.childrens.forEach(tree -> tree.add(clazz));
	}

	public boolean contains(Class<? extends AbstractModel> clazz) {
		if (clazz == null) {
			return false;
		}

		if (clazz == this.node) {
			return true;
		}

		for (EntityTree tree : this.childrens) {
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

	public Class<? extends Entity> getNode() {
		return node;
	}

	public void setNode(Class<? extends Entity> node) {
		this.node = node;
	}

	public Set<EntityTree> getChildrens() {
		return childrens;
	}

	public void setChildrens(Set<EntityTree> childrens) {
		this.childrens = childrens;
	}

}

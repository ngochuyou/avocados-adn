/**
 * 
 */
package adn.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class ModelInheritanceTree<T extends AbstractModel> {

	private ModelInheritanceTree<T> parent;

	private Class<T> node;

	private Set<ModelInheritanceTree<T>> childrens;

	public ModelInheritanceTree(ModelInheritanceTree<T> parent, Class<T> node, Set<ModelInheritanceTree<T>> childrens) {
		super();
		this.parent = parent;
		this.node = node;
		this.childrens = childrens == null ? new HashSet<>() : childrens;
	}

	public void add(Class<T> clazz) {
		if (clazz == null || clazz == Entity.class) {
			return;
		}

		if (this.contains(clazz)) {
			return;
		}

		if (this.node.equals(clazz.getSuperclass())) {
			this.childrens.add(new ModelInheritanceTree<>(this, clazz, null));

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

		for (ModelInheritanceTree<T> tree : this.childrens) {
			if (tree.contains(clazz)) {
				return true;
			}
		}

		return false;
	}

	public void forEach(Consumer<ModelInheritanceTree<T>> consumer) {
		consumer.accept(this);

		this.childrens.forEach(tree -> tree.forEach(consumer));
	}

	public ModelInheritanceTree<T> getParent() {
		return parent;
	}

	public void setParent(ModelInheritanceTree<T> parent) {
		this.parent = parent;
	}

	public Class<T> getNode() {
		return node;
	}

	public void setNode(Class<T> node) {
		this.node = node;
	}

	public Set<ModelInheritanceTree<T>> getChildrens() {
		return childrens;
	}

	public void setChildrens(Set<ModelInheritanceTree<T>> childrens) {
		this.childrens = childrens;
	}

}

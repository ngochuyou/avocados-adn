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
public class ModelInheritanceTree<T extends AbstractModel> {

	private ModelInheritanceTree<? super T> parent;

	private Class<T> node;

	private Set<ModelInheritanceTree<? extends T>> childrens;

	public ModelInheritanceTree(ModelInheritanceTree<? super T> parent, Class<T> node,
			Set<ModelInheritanceTree<? extends T>> childrens) {
		super();
		this.parent = parent;
		this.node = node;
		this.childrens = childrens == null ? new HashSet<>() : childrens;
	}

	@SuppressWarnings("unchecked")
	public void add(Class<? extends AbstractModel> clazz) {
		if (clazz == null || clazz == AbstractModel.class) {
			return;
		}

		if (this.contains(clazz)) {
			return;
		}

		if (this.node.equals(clazz.getSuperclass())) {
			this.childrens.add(new ModelInheritanceTree<>(this, (Class<? extends T>) clazz, null));

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

		for (ModelInheritanceTree<? extends T> tree : this.childrens) {
			if (tree.contains(clazz)) {
				return true;
			}
		}

		return false;
	}

	public void forEach(Consumer<ModelInheritanceTree<?>> consumer) {
		consumer.accept(this);

		this.childrens.forEach(tree -> tree.forEach(consumer));
	}

	public ModelInheritanceTree<? super T> getParent() {
		return parent;
	}

	public Class<T> getNode() {
		return node;
	}

	public void setNode(Class<T> node) {
		this.node = node;
	}

	public Set<ModelInheritanceTree<? extends T>> getChildrens() {
		return childrens;
	}

}

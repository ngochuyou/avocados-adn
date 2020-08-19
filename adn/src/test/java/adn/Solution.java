/**
 * 
 */
package adn;

import java.util.function.Consumer;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Node tree = new Node(5);

		tree.add(2);
		tree.add(1);
		tree.add(8);
		tree.add(6);
		tree.add(9);
		tree.add(7);
		tree = tree.remove(1);

		tree.forEach(node -> System.out.println(node.val));
	}

	public static void quickSort(int[] nums, int left, int right) {
		if (left >= right) {
			return;
		}

		int pivot = nums[right], i = -1, t;

		for (int j = 0; j <= right; j++) {
			if (nums[j] >= pivot) {
				i++;
				t = nums[i];
				nums[i] = nums[j];
				nums[j] = t;
			}
		}

		quickSort(nums, 0, i - 1);
		quickSort(nums, i + 1, right);
	}

}

class Node {

	public Node left;

	public Node right;

	public int val;

	public Node(int val) {
		super();
		this.val = val;
	}

	public void add(int val) {
		if (this.val == val) {
			return;
		}

		if (this.val < val) {
			if (this.right != null) {
				this.right.add(val);
				return;
			}

			this.right = new Node(val);
			return;
		}

		if (this.left != null) {
			this.left.add(val);
			return;
		}

		this.left = new Node(val);
	}

	public void forEach(Consumer<Node> consumer) {
		if (this.left != null) {
			this.left.forEach(consumer);
		}

		consumer.accept(this);

		if (this.right != null) {
			this.right.forEach(consumer);
		}
	}

	public Node find(int val) {
		if (this.val == val) {
			return this;
		}

		if (this.val > val) {
			if (this.left != null) {
				return this.left.find(val);
			}

			return null;
		}

		if (this.right != null) {
			return this.right.find(val);
		}

		return null;
	}

	public Node findMin() {
		if (this.left == null) {
			return this;
		}

		return this.left.findMin();
	}

	public Node findMax() {
		if (this.right != null) {
			return this.right.findMax();
		}

		return this;
	}

	public Node remove(int val) {
		if (this.left == null && this.right == null) {
			return null;
		}

		if (this.val == val) {
			Node replacement;

			if (this.left != null) {
				replacement = this.left.findMax();
				this.val = replacement.val;
				this.left = this.left.remove(replacement.val);

				return this;
			}

			replacement = this.right.findMin();
			this.val = replacement.val;
			this.right = this.right.remove(replacement.val);

			return this;
		}

		if (this.val < val) {
			if (this.right != null) {
				this.right = this.right.remove(val);
			}

			return this;
		}

		if (this.left != null) {
			this.left = this.left.remove(val);
		}

		return this;
	}

}
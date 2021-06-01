/**
 * 
 */
package adn.test.application;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		A a = new A("a");

		a.next = new A("b");
		a.next.next = new A("c");

		System.out.println(a.toString());
	}

}

class A {

	String name;

	A next;

	/**
	 * 
	 */
	public A(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s%s", name, next == null ? "" : next.toString());
	}

}

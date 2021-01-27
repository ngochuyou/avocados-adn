/**
 * 
 */
package adn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		A a = new A(0, 1);

		Method aGetter = A.class.getDeclaredMethod("getA", new Class<?>[] { });
		Method aSetter = A.class.getDeclaredMethod("setA", new Class<?>[] { int.class });
		long t = System.currentTimeMillis();
		
		System.out.println(aGetter.invoke(a, new Object[] { }));
		aSetter.invoke(a, new Object[] {2});
		System.out.println(aGetter.invoke(a, new Object[] { }));
		System.out.println(System.currentTimeMillis() - t);
	}

	static class A {

		private int a;

		private int b;

		public A(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}

	}

}

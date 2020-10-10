/**
 * 
 */
package adn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
		Target t = new Target();

		t.setName("name");
		t.setVal("val");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(t);
		oos.close();
		
		Target tar = new Target();
		ObjectInputStream ios = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		
		tar = (Target) ios.readObject();

		System.out.println(tar.getName());
		System.out.println(tar.getVal());
	}

}

class Target implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient String val;

	private String name;

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
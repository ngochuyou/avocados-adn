/**
 * 
 */
package adn;

import java.util.HashMap;

import org.hibernate.engine.spi.Status;

import adn.service.resource.persistence.ResourceEntry;
import adn.service.resource.persistence.ResourceEntryImpl;

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
		HashMap<Integer, ResourceEntry> entries = new HashMap<>();
		ResourceEntry entry = new ResourceEntryImpl(entries, null, null, null);

		entries.put(1, entry);
		System.out.println(entries.get(1).getStatus());
		entries.compute(1, (k, v) -> {
			v.setStatus(Status.DELETED);

			return v;
		});
		System.out.println(entries.get(1).getStatus());
	}

}

/**
 * 
 */
package adn.test.application;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

	public static void main(String[] args) {
		// @formatter:off
//		List<List<String>> lists = Arrays.asList(
//				List.of("ADMIN", "PERSONNEL", "CUSTOMER"),
//				List.of("123", "456", "789")
//		);
//		List<String> results = new ArrayList<>();
//
//		for (int compound = 2; compound <= lists.size(); compound++) {
//			System.out.println("Compound = " + compound);
//			for (int start = 0; start + compound <= lists.size(); start++) {
//				List<String> currentList = lists.get(start);
//				List<List<String>> subList = lists.subList(start + 1, start + compound);
//
//				for (int distributionIndex = 0; distributionIndex < currentList.size(); distributionIndex++) {
//					results.addAll(multiDistribute(currentList.get(distributionIndex), subList, 0));
//				}
//			}
//			System.out.println("Done");
//		}
//
//		for (String result : results) {
//			System.out.println(result);
//		}
		// @formatter:on
	}

	public static List<String> multiDistribute(String distribution, List<List<String>> target, int index) {
		if (index == target.size() - 1) {
			return distribute(distribution, target.get(index));
		}

		return distribute(distribution, target.get(index)).stream()
				.map(result -> multiDistribute(result, target, index + 1)).flatMap(list -> list.stream())
				.collect(Collectors.toList());
	}

	public static List<String> distribute(String distributed, List<String> target) {
		return target.stream().map(string -> distributed + "\\" + string).collect(Collectors.toList());
	}

}

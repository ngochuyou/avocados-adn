/**
 * 
 */
package adn;

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

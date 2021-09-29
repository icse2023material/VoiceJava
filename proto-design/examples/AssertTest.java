public class AssertTest {

	public void test() {
		double x = Math.abs(-123.45);
		assert x >= 0;
		System.out.println(x);
	}

	void sort(int[] arr) {
		if (arr == null) {
			throw new IllegalArgumentException("array cannot be null");
		}
	}
}

public class ExpressionTest {

	public void test() {
		int a = 3 + 4;
		int b = 3 * (4 + 2);
		int c = (4 + 2) * 3;
		int d = (3 * (4 + 2)) + 5;
		int e = (3 + 4) * 5 * Math.addExact(a + b, (int) Math.ceil(Math.PI)) * 6;
		System.out.println(e);
	}
}

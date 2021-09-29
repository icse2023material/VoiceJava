public class WhileTest {
	public static void main(String[] args) {
		int sum = 0;
		int n = 0;
		while (n <= 100) {
			n++;
			sum = sum + n;
		}
		System.out.println(sum);

		sum = 0;
		n = 1;
		do {
			sum = sum + n;
			n++;
		} while (n <= 100);
		System.out.println(sum);
	}
}

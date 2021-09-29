public class SwitchTest {
	public static void main(String[] args) {
		int option = 2;
		switch (option) {
			case 1:
				System.out.println("Selected 1");
				break;
			case 2:
			default:
				System.out.println("Not selected");
				break;
		}
	}
}

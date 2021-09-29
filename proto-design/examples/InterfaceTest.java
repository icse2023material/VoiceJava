interface Person {
	void run();

	String getName();
}

public class InterfaceTest implements Person {
	private String name;

	public InterfaceTest(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		System.out.println(this.name + " run");
	}

	@Override
	public String getName() {
		return this.name;
	}
}

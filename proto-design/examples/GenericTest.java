import java.util.ArrayList;
import java.util.List;

public class GenericTest {

	public void test() {
		List<String> list = new ArrayList<String>();
		list.add("Hello");
		list.add("World");
		String first = list.get(0);
		String second = list.get(1);
	}

}

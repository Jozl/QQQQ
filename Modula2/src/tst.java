import java.util.HashSet;
import java.util.Set;

public class tst {
	public static void main(String[] args) {
		Set<String> test = new HashSet<>();
		test.add("123");
		test.add("456");
		System.out.println(test);
		Set<String> copy = new HashSet<>();
		copy.addAll(test);
		System.out.println(copy);
		System.out.println(test);
	}
}

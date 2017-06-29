package test.examples.live;

public class TestObject {

	private String string;

	public TestObject(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	@Override
	public String toString() {
		return string;
	}
}
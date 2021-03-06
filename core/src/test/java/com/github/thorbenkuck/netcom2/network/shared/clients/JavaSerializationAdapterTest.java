package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Testing(JavaSerializationAdapter.class)
public class JavaSerializationAdapterTest {

	@Test(expected = SerializationFailedException.class)
	public void getNotSerializable() throws Exception {
		// Arrange
		JavaSerializationAdapter adapter = new JavaSerializationAdapter();
		NotSerializableObject object = new NotSerializableObject();

		// Act
		adapter.apply(object);

		// Assert
	}

	@Test
	public void getIsBase64() throws Exception {
		// Arrange
		JavaSerializationAdapter adapter = new JavaSerializationAdapter();
		SerializableClass object = new SerializableClass("This is a test string!");
		Pattern pattern = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
		Predicate<String> testBase64 = pattern.asPredicate();

		// Act
		String serializedObject = adapter.apply(object);

		// Assert
		assertEquals(serializedObject.length() % 4, 0);
		assertTrue(testBase64.test(serializedObject));
	}

	@Test
	public void getSerializable() throws Exception {
		// Arrange
		JavaSerializationAdapter adapter = new JavaSerializationAdapter();
		JavaDeserializationAdapter deSerializationAdapter = new JavaDeserializationAdapter();
		SerializableClass object = new SerializableClass("This is a test string!");

		// Act
		String serializedObject = adapter.apply(object);
		Object deserializedObject = deSerializationAdapter.apply(serializedObject);

		// Assert
		assertEquals(object, deserializedObject);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNull() throws Exception {
		// Arrange
		JavaSerializationAdapter adapter = new JavaSerializationAdapter();

		// Act
		adapter.apply(null);

		// Assert
	}

	private static class NotSerializableObject {

	}

}
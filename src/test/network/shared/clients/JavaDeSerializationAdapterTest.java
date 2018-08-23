package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Testing(JavaDeSerializationAdapter.class)
public class JavaDeSerializationAdapterTest {

	@Test(expected = DeSerializationFailedException.class)
	public void getNotBase64() throws Exception {
		// Arrange
		JavaDeSerializationAdapter adapter = new JavaDeSerializationAdapter();
		String inputString = "NotBase64";

		// Act
		adapter.get(inputString);

		// Assert
	}

	@Test(expected = DeSerializationFailedException.class)
	public void getBase64ButNoObject() throws Exception {
		// Arrange
		JavaDeSerializationAdapter adapter = new JavaDeSerializationAdapter();
		String inputString = "aGFsbG8=";

		// Act
		adapter.get(inputString);

		// Assert
	}

	@Test
	public void get() throws Exception {
		// Arrange
		JavaDeSerializationAdapter adapter = new JavaDeSerializationAdapter();
		JavaSerializationAdapter serializationAdapter = new JavaSerializationAdapter();
		AnObject object = new AnObject("A test string");

		// Act
		String objectString = serializationAdapter.get(object);
		Object deserializedObject = adapter.get(objectString);

		// Assert
		assertEquals(object, deserializedObject);
	}

}
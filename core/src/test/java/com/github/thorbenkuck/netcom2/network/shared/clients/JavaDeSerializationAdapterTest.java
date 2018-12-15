package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Testing(JavaDeserializationAdapter.class)
public class JavaDeSerializationAdapterTest {

	@Test(expected = DeSerializationFailedException.class)
	public void getNotBase64() throws Exception {
		// Arrange
		JavaDeserializationAdapter adapter = new JavaDeserializationAdapter();
		String inputString = "NotBase64";

		// Act
		adapter.apply(inputString);

		// Assert
	}

	@Test(expected = DeSerializationFailedException.class)
	public void getBase64ButNoObject() throws Exception {
		// Arrange
		JavaDeserializationAdapter adapter = new JavaDeserializationAdapter();
		String inputString = "aGFsbG8=";

		// Act
		adapter.apply(inputString);

		// Assert
	}

	@Test
	public void get() throws Exception {
		// Arrange
		JavaDeserializationAdapter adapter = new JavaDeserializationAdapter();
		JavaSerializationAdapter serializationAdapter = new JavaSerializationAdapter();
		AnObject object = new AnObject("A test string");

		// Act
		String objectString = serializationAdapter.apply(object);
		Object deserializedObject = adapter.apply(objectString);

		// Assert
		assertEquals(object, deserializedObject);
	}

}
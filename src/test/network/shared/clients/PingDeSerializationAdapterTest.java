package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import org.junit.Test;

import static com.github.thorbenkuck.netcom2.TestUtils.UUID_SEED_1;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@Testing(PingDeSerializationAdapter.class)
public class PingDeSerializationAdapterTest {

	@Test(expected = IllegalArgumentException.class)
	public void getNull() throws Exception {
		// Arrange
		PingDeSerializationAdapter adapter = new PingDeSerializationAdapter();

		// Act
		adapter.get(null);

		// Assert
	}

	@Test(expected = DeSerializationFailedException.class)
	public void getNonPingString() throws Exception {
		// Arrange
		PingDeSerializationAdapter adapter = new PingDeSerializationAdapter();
		String string = "somestring|" + UUID_SEED_1;

		// Act
		adapter.get(string);

		// Assert
	}

	@Test
	public void get() throws Exception {
		// Arrange
		PingDeSerializationAdapter adapter = new PingDeSerializationAdapter();
		String string = "Ping|" + UUID_SEED_1;

		// Act
		Object returnedObject = adapter.get(string);

		// Assert
		assertThat(returnedObject, instanceOf(Ping.class));
	}

}
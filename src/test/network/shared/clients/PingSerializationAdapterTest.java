package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import org.junit.Test;

import static com.github.thorbenkuck.netcom2.TestUtils.UUID_SEED_1;
import static org.junit.Assert.assertEquals;

@Testing(PingSerializationAdapter.class)
public class PingSerializationAdapterTest {

	@Test(expected = IllegalArgumentException.class)
	public void getNull() throws Exception {
		// Arrange
		PingSerializationAdapter adapter = new PingSerializationAdapter();

		// Act
		adapter.get(null);

		// Assert
	}

	@Test
	public void getPing() throws Exception {
		// Arrange
		PingSerializationAdapter adapter = new PingSerializationAdapter();
		ClientID clientID = ClientID.fromString(UUID_SEED_1);
		Ping nullPing = new Ping(clientID);

		// Act
		String serializedPing = adapter.get(nullPing);

		// Assert
		assertEquals("Ping|" + clientID, serializedPing);
	}

}
package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;

@Testing(CachePushHandler.class)
public class CachePushHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		Cache cache = mock(Cache.class);
		CachePushHandler cachePushHandler = new CachePushHandler(cache);
		CachePush cachePush = new CachePush(new TestObject());

		// Act
		cachePushHandler.accept(cachePush);

		// Assert
		Mockito.verify(cache, atLeastOnce()).addAndOverride(cachePush.getObject());
		Mockito.verify(cache, atLeastOnce()).acquire();
		Mockito.verify(cache, atLeastOnce()).release();
	}

	private static class TestObject {
	}
}
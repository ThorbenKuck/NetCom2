package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class UnRegisterResponseHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		Cache cache = mock(Cache.class);
		InternalSender sender = mock(InternalSender.class);
		UnRegisterResponseHandler handler = new UnRegisterResponseHandler(cache, sender);
		UnRegisterResponse registerResponse = new UnRegisterResponse(new UnRegisterRequest(CacheClass.class), true);

		// Act
		handler.accept(registerResponse);

		// Assert
		verify(sender).removePendingObserver(eq(CacheClass.class));
		verify(cache).acquire();
		verify(cache).release();
		verify(cache).removeCacheObserver(any());
	}

	@Test
	public void accept1() throws Exception {
		// Arrange
		Cache cache = mock(Cache.class);
		InternalSender sender = mock(InternalSender.class);
		UnRegisterResponseHandler handler = new UnRegisterResponseHandler(cache, sender);
		UnRegisterResponse registerResponse = new UnRegisterResponse(new UnRegisterRequest(CacheClass.class), false);

		// Act
		handler.accept(registerResponse);

		// Assert
		verify(sender, never()).removePendingObserver(eq(CacheClass.class));
		verify(cache, never()).acquire();
		verify(cache, never()).release();
		verify(cache, never()).removeCacheObserver(any());
	}

	private class CacheClass {}
}
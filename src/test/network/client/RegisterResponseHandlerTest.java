package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@Testing(RegisterResponseHandler.class)
public class RegisterResponseHandlerTest {
	@Test
	public void accept() throws Exception {
		// Arrange
		Cache cache = mock(Cache.class);
		InternalSender sender = mock(InternalSender.class);
		CacheObserver cacheObserver = mock(CacheObserver.class);
		when(sender.removePendingObserver(CacheKey.class)).thenReturn(cacheObserver);
		RegisterResponseHandler handler = new RegisterResponseHandler(cache, sender);

		// Act
		handler.accept(new RegisterResponse(new RegisterRequest(CacheKey.class), true));

		// Assert
		verify(cache).acquire();
		verify(cache).release();
		verify(cache).addCacheObserver(eq(cacheObserver));
		verify(sender).removePendingObserver(CacheKey.class);
	}

	@Test
	public void accept1() throws Exception {
		// Arrange
		Cache cache = mock(Cache.class);
		InternalSender sender = mock(InternalSender.class);
		CacheObserver cacheObserver = mock(CacheObserver.class);
		when(sender.removePendingObserver(CacheKey.class)).thenReturn(cacheObserver);
		RegisterResponseHandler handler = new RegisterResponseHandler(cache, sender);

		// Act
		handler.accept(new RegisterResponse(new RegisterRequest(CacheKey.class), false));

		// Assert
		verify(cache, never()).acquire();
		verify(cache, never()).release();
		verify(cache, never()).addCacheObserver(eq(cacheObserver));
		verify(sender, never()).removePendingObserver(CacheKey.class);
	}

	private class CacheKey {
	}
}
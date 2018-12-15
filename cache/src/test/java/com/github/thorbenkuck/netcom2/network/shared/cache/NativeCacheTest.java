package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.keller.annotations.Testing;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

@Testing(NativeCache.class)
public class NativeCacheTest {
	@Test
	public void updateNonExisting() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();

		// Act
		cache.update(new CacheElement());

		// Assert
		assertFalse(cache.get(CacheElement.class).isPresent());
	}

	@Test
	public void updateCorrect() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement old = new CacheElement();
		CacheElement newElement = new CacheElement();

		// Act
		cache.addNew(old);
		cache.update(newElement);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertTrue(optional.isPresent());
		assertSame(newElement, optional.get());
		assertNotSame(old, optional.get());
	}

	@Test
	public void addNew() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();

		// Act
		cache.addNew(element);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertTrue(optional.isPresent());
		assertSame(element, optional.get());
	}

	@Test
	public void addNewAlreadyExists() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		CacheElement newElement = new CacheElement();

		// Act
		cache.addNew(element);
		cache.addNew(newElement);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertTrue(optional.isPresent());
		assertSame(element, optional.get());
		assertNotSame(newElement, optional.get());
	}

	@Test
	public void addAndOverride() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();

		// Act
		cache.addAndOverride(element);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertTrue(optional.isPresent());
		assertSame(element, optional.get());
	}

	@Test
	public void addAndOverrideExisting() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		CacheElement newElement = new CacheElement();

		// Act
		cache.addAndOverride(element);
		cache.addAndOverride(newElement);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertTrue(optional.isPresent());
		assertSame(newElement, optional.get());
		assertNotSame(element, optional.get());
	}

	@Test
	public void remove() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();

		// Act
		cache.remove(CacheElement.class);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertFalse(optional.isPresent());
	}

	@Test
	public void removeExisting() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		cache.addNew(element);

		// Act
		cache.remove(CacheElement.class);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertFalse(optional.isPresent());
	}

	@Test
	public void removeAddAndOverride() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		cache.addAndOverride(element);

		// Act
		cache.remove(CacheElement.class);

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertFalse(optional.isPresent());
	}

	@Test
	public void get() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		cache.addNew(element);

		// Act
		Optional<CacheElement> optional = cache.get(CacheElement.class);

		// Assert
		assertNotNull(optional);
		assertTrue(optional.isPresent());
		assertSame(element, optional.get());
	}

	@Test
	public void getEmpty() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();

		// Act
		Optional<CacheElement> optional = cache.get(CacheElement.class);

		// Assert
		assertFalse(optional.isPresent());
	}

	@Test
	public void isSet() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();

		// Act
		boolean isSet = cache.isSet(CacheElement.class);

		// Assert
		assertFalse(isSet);
	}

	@Test
	public void isSetNeg() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		cache.addNew(new CacheElement());

		// Act
		boolean isSet = cache.isSet(CacheElement.class);

		// Assert
		assertTrue(isSet);
	}

	@Test
	public void isSetOverride() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		cache.addNew(new CacheElement());
		cache.addAndOverride(new CacheElement());

		// Act
		boolean isSet = cache.isSet(CacheElement.class);

		// Assert
		assertTrue(isSet);
	}

	@Test
	public void addCacheObserver() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
			}
		};

		// Act
		cache.addCacheObserver(observer);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void addCacheObserverNewElement() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
				assertSame(element, cacheElement);
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}
		};
		cache.addCacheObserver(observer);

		// Act
		cache.addNew(element);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void addCacheObserverNewElementAddAndOverride() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
				assertSame(cacheElement, element);
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}
		};
		cache.addCacheObserver(observer);

		// Act
		cache.addAndOverride(element);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void addCacheObserverUpdatedElement() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement first = new CacheElement();
		CacheElement second = new CacheElement();
		cache.addNew(first);
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				assertSame(second, cacheElement);
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}
		};
		cache.addCacheObserver(observer);

		// Act
		cache.addAndOverride(second);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void addCacheObserverDeleteElement() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheElement element = new CacheElement();
		cache.addNew(element);
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				assertSame(element, cacheElement);
			}
		};
		cache.addCacheObserver(observer);

		// Act
		cache.remove(CacheElement.class);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void removeCacheObserver() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		CacheObserver<CacheElement> observer = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {
				fail();
			}
		};
		cache.addCacheObserver(observer);

		// Act
		cache.removeCacheObserver(observer);
		cache.addAndOverride(new CacheElement());

		// Assert
		assertEquals(0, cache.countObservers());
	}

	@Test
	public void addGeneralObserver() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};

		// Act
		cache.addGeneralObserver(observer);

		// Assert
		assertEquals(1, cache.countObservers());
	}

	@Test
	public void removeGeneralObserver() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);

		// Act
		cache.removeGeneralObserver(observer);

		// Assert
		assertEquals(0, cache.countObservers());
	}

	@Test
	public void clearObserversOne() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);

		// Act
		cache.clearObservers();

		// Assert
		assertEquals(0, cache.countObservers());
	}

	@Test
	public void clearObserversTwo() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);
		cache.addGeneralObserver(observer);

		// Act
		cache.clearObservers();

		// Assert
		assertEquals(0, cache.countObservers());
	}

	@Test
	public void clearObserversNoObserver() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();

		// Act
		cache.clearObservers();

		// Assert
		assertEquals(0, cache.countObservers());
	}

	@Test
	public void reset() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		cache.addAndOverride(new CacheElement());
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);

		// Act
		cache.reset();

		// Assert
		Optional<CacheElement> optional = cache.get(CacheElement.class);
		assertEquals(0, cache.countObservers());
		assertFalse(optional.isPresent());
	}

	@Test
	public void countObserversOne() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);

		// Act
		int count = cache.countObservers();

		// Assert
		assertEquals(1, count);
	}

	@Test
	public void countObserversTwo() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		GeneralCacheObserver observer2 = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);
		cache.addGeneralObserver(observer2);

		// Act
		int count = cache.countObservers();

		// Assert
		assertEquals(2, count);
	}

	@Test
	public void countObserversThree() throws Exception {
		// Arrange
		NativeCache cache = new NativeCache();
		GeneralCacheObserver observer = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		GeneralCacheObserver observer2 = new GeneralCacheObserver() {
			@Override
			public void newEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final Object o, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final Object o, final CacheObservable observable) {

			}
		};
		CacheObserver<CacheElement> cacheObserver = new AbstractCacheObserver<CacheElement>(CacheElement.class) {
			@Override
			public void newEntry(final CacheElement cacheElement, final CacheObservable observable) {

			}

			@Override
			public void updatedEntry(final CacheElement cacheElement, final CacheObservable observable) {

			}

			@Override
			public void deletedEntry(final CacheElement cacheElement, final CacheObservable observable) {

			}
		};
		cache.addGeneralObserver(observer);
		cache.addGeneralObserver(observer2);
		cache.addCacheObserver(cacheObserver);

		// Act
		int count = cache.countObservers();

		// Assert
		assertEquals(3, count);
	}

	private class CacheElement {
	}
}
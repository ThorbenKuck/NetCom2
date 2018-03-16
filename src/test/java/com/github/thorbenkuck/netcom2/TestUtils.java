package com.github.thorbenkuck.netcom2;

import java.util.Collection;

public final class TestUtils {

    /**
     * Constructs a matcher to test for exact equality of the <b>contents</b> of the collection to test,
     * <b>except for order</b>.
     * @param items The collection of expected elements
     * @param <T> The type of the elements of the collection
     * @return a TypeSafeDiagnosingMatcher to be passed as second parameter of assertThat
     */
    @SuppressWarnings("unchecked")
    public static <T> org.hamcrest.Matcher<java.lang.Iterable<? extends T>> consistsOf(final Collection<T> items) {
        return org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder((T[]) items.toArray());
    }

}
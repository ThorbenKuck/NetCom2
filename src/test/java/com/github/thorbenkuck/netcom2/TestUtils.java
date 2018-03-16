package com.github.thorbenkuck.netcom2;

import java.util.Collection;

public final class TestUtils {

    /**
     * String representation of a UUID
     */
    public static final String UUID_SEED_1 = "38400000-8cf0-11bd-b23e-10b96e4ef00d";
    public static final String UUID_SEED_2 = "8ec75794-eeef-4953-bb35-9ef0a4d98954";
    public static final String UUID_SEED_3 = "db8064c0-db2e-4d01-b5a9-12908e81c63e";
    public static final String UUID_SEED_4 = "9097fc49-7fb8-4a9c-a567-5da9aef0ee59";

    /**
     * Constructs a matcher to test for exact equality of the <b>contents</b> of the collection to test,
     * <b>except for order</b>.
     *
     * @param items The collection of expected elements
     * @param <T> The type of the elements of the collection
     * @return a TypeSafeDiagnosingMatcher to be passed as second parameter of assertThat
     */
    @SuppressWarnings("unchecked")
    public static <T> org.hamcrest.Matcher<java.lang.Iterable<? extends T>> consistsOf(final Collection<T> items) {
        return org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder((T[]) items.toArray());
    }

}
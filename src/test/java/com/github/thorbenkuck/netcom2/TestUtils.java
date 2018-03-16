package com.github.thorbenkuck.netcom2;

import java.util.Collection;

public final class TestUtils {

    @SuppressWarnings("unchecked")
    public static <T> org.hamcrest.Matcher<java.lang.Iterable<? extends T>> consistsOf(final Collection<T> items) {
        return org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder((T[]) items.toArray());
    }

}
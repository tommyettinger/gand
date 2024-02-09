package com.github.tommyettinger.gand.utils;

import com.github.tommyettinger.gand.algorithms.SearchStep;

/**
 * Essentially the same as a {@code Consumer<SearchStep<V>>}, this is a functional interface that is typically run by
 * search algorithms at each step. Its functional method is {@link #accept(Object)}, where accept is given a SearchStep
 * of type V.
 * @param <V> the type parameter for each SearchStep; the vertex type
 */
public interface SearchProcessor<V> extends ObjectConsumer<SearchStep<V>> {
}

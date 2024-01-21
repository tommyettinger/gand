package com.github.tommyettinger.gand.utils;

import java.util.function.Consumer;

import com.github.tommyettinger.gand.algorithms.SearchStep;

public interface SearchProcessor<V> extends Consumer<SearchStep<V>> {



}

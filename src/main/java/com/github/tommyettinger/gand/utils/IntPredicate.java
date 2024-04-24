package com.github.tommyettinger.gand.utils;

/**
 * Represents an operation that accepts one int argument and returns a {@code boolean} result.
 * <br>
 * This is a functional interface whose functional method is {@link #test(int)}.
 */
public interface IntPredicate {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param arg the input argument to test
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  boolean test(int arg);
}

package com.github.tommyettinger.gand.utils;

/**
 * Represents an operation that accepts three int arguments and returns a {@code boolean} result.
 * <br>
 * This is a functional interface whose functional method is {@link #test(int, int, int)}.
 */
public interface IntIntIntPredicate {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param first the first input argument
   * @param second the second input argument
   * @param third the third input argument
   * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
   */
  boolean test(int first, int second, int third);
}

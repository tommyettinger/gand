package com.github.tommyettinger.gand.utils;

/**
 * Represents an operation that accepts a single {@code T}-valued argument and
 * returns no result.
 * <br>
 * This is identical to {@code Consumer} in Java 8, and is present here so environments
 * that support lambdas but not Java 8 APIs (such as RoboVM) can use it.
 * <br>
 * This is a functional interface whose functional method is {@link #accept(Object)}.
 */
public interface ObjectConsumer<T> {
  /**
   * Performs this operation on the given argument.
   *
   * @param value the input argument
   */
  void accept(T value);
}

package com.github.tommyettinger.gand.function;

/**
 * Represents an operation on a single {@code T}-valued operand that produces
 * a {@code float}-valued result.
 * <br>
 * This is a functional interface whose functional method is {@link #applyAsFloat(Object)}.
 */
public interface ObjToFloatFunction<T> {
  /**
   * Applies this function to the given argument.
   *
   * @param value the function argument
   * @return the function result
   */
  float applyAsFloat(T value);
}

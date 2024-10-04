/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.tommyettinger.gand.utils;

/**
 * Various methods that have different implementations on GWT, such as {@link #imul(int, int)} to multiply two ints and
 * get an int that is guaranteed to act like it does on desktop.
 * @author Tommy Ettinger
 */
public final class Compatibility {
    /**
     * No need to instantiate.
     */
    private Compatibility() {
    }

    /**
     * Returns an int value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified int value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Integer#lowestOneBit(int)}, including on GWT. GWT calculates Integer.lowestOneBit() correctly,
     * but does not always calculate Long.lowestOneBit() correctly. This overload is here so you can use lowestOneBit on
     * an int value and get an int value back (which could be assigned to a long without losing data), or use it on a
     * long value and get the correct long result on both GWT and other platforms.
     *
     * @param num the value whose lowest one bit is to be computed
     * @return an int value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     * or zero if the specified value is itself equal to zero.
     */
    public static int lowestOneBit(int num) {
        return num & -num;
    }

    /**
     * Returns an long value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified long value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Long#lowestOneBit(long)}, but super-sourced to act correctly on GWT. At least on GWT 2.8.2,
     * {@link Long#lowestOneBit(long)} does not provide correct results for certain inputs on GWT. For example, when given
     * -17592186044416L, Long.lowestOneBit() returns 0 on GWT, possibly because it converts to an int at some point. On
     * other platforms, like desktop JDKs, {@code Long.lowestOneBit(-17592186044416L)} returns 17592186044416L.
     *
     * @param num the value whose lowest one bit is to be computed
     * @return a long value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     * or zero if the specified value is itself equal to zero.
     */
    public static long lowestOneBit(long num) {
        return num & -num;
    }

    /**
     * 32-bit signed integer multiplication that is correct on all platforms, including GWT. Unlike desktop, Android,
     * and iOS targets, GWT uses the equivalent of a {@code double} to represent an {@code int}, which means any
     * multiplication where the product is large enough (over 2 to the 53) can start to lose precision instead of being
     * wrapped, like it would on overflow in a normal JDK. Using this will prevent the possibility of precision loss.
     * <br>
     * This should compile down to a call to {@code Math.imul()} on GWT, hence the name here.
     * @param left the multiplicand
     * @param right the multiplier
     * @return the product of left times right, wrapping on overflow as is normal for Java
     */
    public static int imul(int left, int right) {
        return left * right;
    }

    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the sign bit and checking towards the
     * least-significant bit, stopping just before a '1' bit is encountered. Returns 0 for any negative input.
     * Returns 32 for an input of 0.
     * <br>
     * This simply calls {@link Integer#numberOfLeadingZeros(int)} on most platforms, but on GWT, it calls the
     * JS built-in function {@code Math.clz32(n)}. This probably performs better than the Integer method on GWT.
     * @param n any int
     * @return the number of '0' bits starting at the sign bit and going until just before a '1' bit is encountered
     */
    public static int countLeadingZeros(int n) {
        return Integer.numberOfLeadingZeros(n);
    }

    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the least-significant bit and checking towards
     * the sign bit, stopping just before a '1' bit is encountered. Returns 0 for any odd-number input.
     * Returns 32 for an input of 0.
     * <br>
     * This simply calls {@link Integer#numberOfTrailingZeros(int)} on most platforms, but on GWT, it uses the
     * JS built-in function {@code Math.clz32(n)} with some extra steps to get the trailing, rather than leading,
     * zeros. This probably performs better than the Integer method on GWT, though not as well as
     * {@link #countLeadingZeros(int)}
     * @param n any int
     * @return the number of '0' bits starting at the least-significant bit and going until just before a '1' bit is encountered
     */
    public static int countTrailingZeros(int n) {
        return Integer.numberOfTrailingZeros(n);
    }
    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the sign bit and checking towards the
     * least-significant bit, stopping just before a '1' bit is encountered. Returns 0 for any negative input.
     * Returns 64 for an input of 0.
     * <br>
     * This simply calls {@link Long#numberOfLeadingZeros(long)} on most platforms, but on GWT, it calls
     * {@link #countLeadingZeros(int)}, which calls the JS built-in function {@code Math.clz32(n)}.
     * This probably performs better than the Long method on GWT.
     * @param n any long
     * @return the number of '0' bits starting at the sign bit and going until just before a '1' bit is encountered
     */
    public static int countLeadingZeros(long n) {
        return Long.numberOfLeadingZeros(n);
    }
    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the least-significant bit and checking towards
     * the sign bit, stopping just before a '1' bit is encountered. Returns 0 for any odd-number input.
     * Returns 64 for an input of 0.
     * <br>
     * This simply calls {@link Long#numberOfTrailingZeros(long)} on most platforms, but on GWT, it calls
     * {@link #countTrailingZeros(int)}, which uses the JS built-in function {@code Math.clz32(n)}. This probably
     * performs better than the Long method on GWT, though not as well as {@link #countLeadingZeros(long)}.
     * @param n any int
     * @return the number of '0' bits starting at the least-significant bit and going until just before a '1' bit is encountered
     */
    public static int countTrailingZeros(long n) {
        return Long.numberOfTrailingZeros(n);
    }
}

/*
 * Copyright (c) 2022 See AUTHORS file.
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
 */

package com.github.tommyettinger.gand.utils;

public final class Compatibility {
	private Compatibility() {
	}

	public static int lowestOneBit(int num) {
		return num & -num;
	}

	public static long lowestOneBit(long num) {
		return num & ~(num - 1L);
	}

	public static native int imul(int left, int right)/*-{
	    return Math.imul(left, right);
	}-*/;

	public static native int countLeadingZeros(int n)/*-{
	    return Math.clz32(n);
	}-*/;

	public static native int countTrailingZeros(int n)/*-{
	    var i = -n;
	    return ((n | i) >> 31 | 32) & 31 - Math.clz32(n & i);
	}-*/;

	public static int countLeadingZeros(long n) {
		// we store the top 32 bits first.
		int x = (int)(n >>> 32);
		// if the top 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the low 32 bits.
		return x == 0 ? 32 + countLeadingZeros((int)n) : countLeadingZeros(x);
	}


	public static int countTrailingZeros(long n) {
		// we store the bottom 32 bits first.
		int x = (int)n;
		// if the bottom 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the high 32 bits.
		return x == 0 ? 32 + countTrailingZeros((int)(n >>> 32)) : countTrailingZeros(x);
	}

}

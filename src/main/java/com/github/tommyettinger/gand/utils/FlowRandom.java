/*
 * Copyright (c) 2023 See AUTHORS file.
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

import com.badlogic.gdx.utils.NumberUtils;

import java.util.Random;

/**
 * A drop-in replacement for {@link Random} that adds few new APIs, but is faster, has better statistical quality, and
 * has a guaranteed longer minimum period (also called cycle length). This is nearly identical to FlowRandom in the
 * Juniper library, and uses the same algorithm, but only extends Random, not Juniper's EnhancedRandom class. If you
 * depend on Juniper, you lose nothing from using the EnhancedRandom classes (they also extend Random), but this class
 * doesn't have as many features as Juniper's FlowRandom. This doesn't depend on anything outside the JDK, though,
 * so it fits easily as a small addition into gand.
 * <br>
 * This is meant to be used more like a hashing function than a random number generator, and its internal structure is
 * in fact based on a hashing function like the finalizer in MurmurHash3, called on a combination of two counters. In
 * this regard it is very close to Java 8's SplittableRandom class, and this also has streams and could, in theory,
 * split. A difference is that SplittableRandom only changes one counter, rather than the two here that change in
 * lockstep; SplittableRandom's stream is a variable like the second counter here, but doesn't change. This happens to
 * give FlowRandom twice as many possible "streams" at the expense of making no single stream equidistributed. However,
 * if all FlowRandom streams were concatenated and run through (which would take a few million years on current
 * hardware), the result would actually be 1-dimensionally equidistributed.
 */
public class FlowRandom extends Random {
    /**
     * The first state; can be any long.
     */
    public long stateA;
    /**
     * The second state; can be any long.
     */
    public long stateB;

    private static long seedFromMath () {
        return (long)((Math.random() - 0.5) * 0x1p52) ^ (long)((Math.random() - 0.5) * 0x1p64);
    }
    /**
     * Creates a new FlowRandom with a random state.
     */
    public FlowRandom() {
        super();
        stateA = seedFromMath();
        stateB = seedFromMath();
    }

    /**
     * Creates a new FlowRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public FlowRandom(long seed) {
        super(seed);
        setSeed(seed);
    }

    /**
     * Creates a new FlowRandom with the given two states; all {@code long} values are permitted for
     * stateA and for stateB. These states are not changed during assignment.
     *
     * @param stateA any {@code long} value
     * @param stateB any {@code long} value
     */
    public FlowRandom(long stateA, long stateB) {
        super(stateA);
        this.stateA = stateA;
        this.stateB = stateB;
    }

    /**
     * This initializes both states of the generator to the two given states, verbatim.
     * (2 to the 128) possible initial generator states can be produced here.
     *
     * @param stateA the first state
     * @param stateB the second state
     */
    public void setState (long stateA, long stateB) {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    /**
     * This initializes both states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here.
     *
     * @param seed the initial seed; may be any long
     */
    public void setSeed (long seed) {
        stateA = seed;
        stateB = ~seed;
    }

    public long nextLong () {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return x ^ x >>> 27;
    }

    public int next (int bits) {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return (int)(x ^ x >>> 27) >>> (32 - bits);
    }

    public int nextInt() {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return (int)(x ^ x >>> 27);
    }

    public int nextInt (int bound) {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return (int)(bound * ((x ^ x >>> 27) & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
    }

    public boolean nextBoolean() {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return (x ^ x >>> 27) < 0L;
    }

    public float nextFloat () {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return (x >>> 40) * 0x1p-24f;
    }

    public double nextDouble () {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return ((x ^ x >>> 27) >>> 11) * 0x1.0p-53;
    }

    /**
     * A way of taking a double in the (0.0, 1.0) range and mapping it to a Gaussian or normal distribution, so high
     * inputs correspond to high outputs, and similarly for the low range. This is centered on 0.0 and its standard
     * deviation seems to be 1.0 (the same as {@link Random#nextGaussian()}). If this is given an input of 0.0
     * or less, it returns -38.5, which is slightly less than the result when given {@link Double#MIN_VALUE}. If it is
     * given an input of 1.0 or more, it returns 38.5, which is significantly larger than the result when given the
     * largest double less than 1.0 (this value is further from 1.0 than {@link Double#MIN_VALUE} is from 0.0). If
     * given {@link Double#NaN}, it returns whatever {@link Math#copySign(double, double)} returns for the arguments
     * {@code 38.5, Double.NaN}, which is implementation-dependent. It uses an algorithm by Peter John Acklam, as
     * implemented by Sherali Karimov.
     * <a href="https://web.archive.org/web/20150910002142/http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java">Original source</a>.
     * <a href="https://web.archive.org/web/20151030215612/http://home.online.no/~pjacklam/notes/invnorm/">Information on the algorithm</a>.
     * <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia's page on the probit function</a> may help, but
     * is more likely to just be confusing.
     * <br>
     * Acklam's algorithm and Karimov's implementation are both quite fast. This appears faster when generating
     * Gaussian-distributed numbers than using either the Box-Muller Transform or Marsaglia's Polar Method, though it
     * isn't as precise and can't produce as extreme min and max results in the extreme cases they should appear. If
     * given a typical uniform random {@code double} that's exclusive on 1.0, it won't produce a result higher than
     * {@code 8.209536145151493}, and will only produce results of at least {@code -8.209536145151493} if 0.0 is
     * excluded from the inputs (if 0.0 is an input, the result is {@code -38.5}). This requires a fair amount of
     * floating-point multiplication and one division for all {@code d} where it is between 0 and 1 exclusive, but
     * roughly 1/20 of the time it need a {@link Math#sqrt(double)} and {@link Math#log(double)} as well.
     * <br>
     * This can be used both as an optimization for generating Gaussian random values, and as a way of generating
     * Gaussian values that match a pattern present in the inputs (which you could have by using a sub-random sequence
     * as the input, such as those produced by a van der Corput, Halton, Sobol or R2 sequence). Most methods of generating
     * Gaussian values (e.g. Box-Muller and Marsaglia polar) do not have any way to preserve a particular pattern.
     *
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return a normal-distributed double centered on 0.0; all results will be between -38.5 and 38.5, both inclusive
     */
    public static double probit (final double d) {
        if (d <= 0 || d >= 1) {
            return Math.copySign(38.5, d - 0.5);
        } else if (d < 0.02425) {
            final double q = Math.sqrt(-2.0 * Math.log(d));
            return (((((-7.784894002430293e-03 * q - 3.223964580411365e-01) * q - 2.400758277161838e+00) * q - 2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00) / (
                    (((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        } else if (0.97575 < d) {
            final double q = Math.sqrt(-2.0 * Math.log(1 - d));
            return -(((((-7.784894002430293e-03 * q - 3.223964580411365e-01) * q - 2.400758277161838e+00) * q - 2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00) / (
                    (((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        }
        final double q = d - 0.5;
        final double r = q * q;
        return (((((-3.969683028665376e+01 * r + 2.209460984245205e+02) * r - 2.759285104469687e+02) * r + 1.383577518672690e+02) * r - 3.066479806614716e+01) * r + 2.506628277459239e+00) * q / (
                ((((-5.447609879822406e+01 * r + 1.615858368580409e+02) * r - 1.556989798598866e+02) * r + 6.680131188771972e+01) * r - 1.328068155288572e+01) * r + 1.0);
    }

    public double nextGaussian() {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        final long bits = x ^ x >>> 27;
        return probit(NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12));
//        final long c = Long.bitCount(bits) - 32L << 32;
//        bits *= 0xC6AC29E4C6AC29E5L;
//        return 0x1.fb760cp-35 * (c + (bits & 0xFFFFFFFFL) - (bits >>> 32));
    }

    @Override
    public void nextBytes (byte[] bytes) {
        for (int i = 0; i < bytes.length; ) {
            for (long r = nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8) {
                bytes[i++] = (byte)r;
            }
        }
    }

    /**
     * Produces a String that holds the entire state of this FlowRandom. You can recover this state from such a
     * String by calling {@link #deserializeFromString(String)} on any FlowRandom, which will set that
     * FlowRandom's state. This does not serialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected if this state is loaded.
     * @return a String holding the current state of this FlowRandom, to be loaded by {@link #deserializeFromString(String)}
     */
    public String serializeToString() {
        return stateA + "~" + stateB;
    }

    /**
     * Given a String produced by {@link #serializeToString()}, this sets the state of this FlowRandom to the state
     * stored in that String.This does not deserialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected by this state.
     * @param data a String produced by {@link #serializeToString()}
     * @return this FlowRandom, after its state has been loaded from the given String
     */
    public FlowRandom deserializeFromString(String data) {
        if(data == null || data.length() < 3) return this;
        int tilde = data.indexOf('~');
        stateA = Long.parseLong(data.substring(0, tilde));
        stateB = Long.parseLong(data.substring(tilde+1));
        return this;
    }

    public FlowRandom copy() {
        return new FlowRandom(stateA, stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowRandom random = (FlowRandom) o;

        if (stateA != random.stateA) return false;
        return stateB == random.stateB;
    }

    @Override
    public int hashCode() {
        int result = (int) (stateA ^ (stateA >>> 32));
        result = 31 * result + (int) (stateB ^ (stateB >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "FlowRandom{" +
                "stateA=" + stateA +
                ", stateB=" + stateB +
                '}';
    }
}

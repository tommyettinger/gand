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

import com.github.tommyettinger.gdcrux.Distributor;

import java.util.Random;

/**
 * A random number generator that is optimized for performance on 32-bit machines and with Google Web Toolkit, this uses
 * only the most portable operations (including compatibility with JS), and has a period of exactly 2 to the 64.
 * This is a drop-in replacement for {@link Random} that adds few new APIs, but is faster, has better statistical
 * quality, and has a guaranteed longer minimum period (also called cycle length). This is nearly identical to
 * Taxon32Random in the Juniper library, and uses the same algorithm, but only extends Random, not Juniper's
 * EnhancedRandom class. If you depend on Juniper, you lose nothing from using the EnhancedRandom classes (they also
 * extend Random), but this class doesn't have as many features as Juniper's Taxon32Random. This doesn't depend on
 * anything outside the JDK, though, so it fits easily as a small addition into gand.
 * <br>
 * This is meant to be used more like a hashing function than a random number generator. If you set the state or seed
 * using {@link #setState(int, int)} or {@link #setSeed(long)}, every initial state/seed should produce a different
 * sequence of numbers, and numerically close-by states should produce very different sequences.
 */
public class Taxon32Random extends Random {
    /**
     * The first state; can be any int.
     */
    protected int stateA;
    /**
     * The second state; can be any int.
     */
    protected int stateB;

    /**
     * Creates a new Taxon32Random with a random state.
     */
    public Taxon32Random() {
        this((int)((Math.random() - 0.5) * 0x1p32), (int)((Math.random() - 0.5) * 0x1p32));
    }

    /**
     * Creates a new Taxon32Random with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public Taxon32Random(long seed) {
        super(seed);
        setSeed(seed);
    }

    /**
     * Creates a new Taxon32Random with the given two states; all {@code int} values are permitted.
     * These states will be used verbatim.
     *
     * @param stateA any {@code int} value
     * @param stateB any {@code int} value
     */
    public Taxon32Random(int stateA, int stateB) {
        super(stateA);
        this.stateA = stateA;
        this.stateB = stateB;
    }


    /**
     * Gets the state determined by {@code selection}, as-is. The value for selection should be
     * between 0 and 1, inclusive; if it is any other value this gets state B as if 1 was given.
     *
     * @param selection used to select which state variable to get; generally 0 or 1
     * @return the value of the selected state, which is an int that will be promoted to long
     */
    public int getSelectedState(int selection) {
        if (selection == 0) {
            return stateA;
        }
        return stateB;
    }

    /**
     * Sets one of the states, determined by {@code selection}, to the lower 32 bits of {@code value}, as-is.
     * Selections 0 and 1 refer to states A and B, and if the selection is anything
     * else, this treats it as 1 and sets stateB. This always casts {@code value} to an int before using it.
     *
     * @param selection used to select which state variable to set; generally 0 or 1
     * @param value     the exact value to use for the selected state, if valid
     */
    public void setSelectedState(int selection, int value) {
        if (selection == 0) {
            stateA = value;
        } else {
            stateB = value;
        }
    }

    /**
     * This initializes both states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()}.
     *
     * @param seed the initial seed; may be any long
     */
    @Override
    public void setSeed(long seed) {
        stateA = (int) seed;
        stateB = (int) (seed >>> 32);
    }

    public int getStateA() {
        return stateA;
    }

    /**
     * Sets the first part of the state by casting the parameter to an int.
     *
     * @param stateA can be any int, but will be cast to an int before use
     */
    public void setStateA(int stateA) {
        this.stateA = stateA;
    }

    public int getStateB() {
        return stateB;
    }

    /**
     * Sets the second part of the state by casting the parameter to an int.
     *
     * @param stateB can be any int, but will be cast to an int before use
     */
    public void setStateB(int stateB) {
        this.stateB = stateB;
    }

    /**
     * Sets the state completely to the given state variables, casting each to an int.
     * This is the same as calling {@link #setStateA(int)} and {@link #setStateB(int)}
     * as a group.
     *
     * @param stateA the first state; can be any int, but will be cast to an int before use
     * @param stateB the second state; can be any int, but will be cast to an int before use
     */
    public void setState(int stateA, int stateB) {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    @Override
    public long nextLong() {
        int x = (stateA = stateA + 0x9E3779BD ^ 0xD1B54A32);
        int t = x & 0xDB4F0B96 - x;
        int y = (stateB = stateB + (t << 1 | t >>> 31) ^ 0xAF723597);
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        int hi = y ^ y >>> 21;
        x = (stateA = stateA + 0x9E3779BD ^ 0xD1B54A32);
        t = x & 0xDB4F0B96 - x;
        y = (stateB = stateB + (t << 1 | t >>> 31) ^ 0xAF723597);
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        int lo = y ^ y >>> 21;
        return (long) hi << 32 ^ lo;
    }

    @Override
    public int next(int bits) {
        int x = (stateA = stateA + 0x9E3779BD ^ 0xD1B54A32);
        int t = x & 0xDB4F0B96 - x;
        int y = (stateB = stateB + (t << 1 | t >>> 31) ^ 0xAF723597);
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        return (y ^ y >>> 21) >>> (32 - bits);
    }

    @Override
    public int nextInt() {
        int x = (stateA = stateA + 0x9E3779BD ^ 0xD1B54A32);
        int t = x & 0xDB4F0B96 - x;
        int y = (stateB = stateB + (t << 1 | t >>> 31) ^ 0xAF723597);
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        return y ^ y >>> 21;
    }

    public long previousLong() {
        return previousInt() ^ (long) previousInt() << 32;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public int previousInt() {
        int y = stateB;
        final int x = stateA;
        int t = x & 0xDB4F0B96 - x;
        stateB = (y ^ 0xAF723597) - (t << 1 | t >>> 31) | 0; // no-op OR with 0 ensures this stays in-range in JS.
        stateA = (x ^ 0xD1B54A32) - 0x9E3779BD | 0;
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        return y ^ y >>> 21;
    }

    @Override
    public int nextInt(int bound) {
        return (int) (bound * (nextInt() & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; ) {
            for (int r = nextInt(), n = Math.min(bytes.length - i, 4); n-- > 0; r >>>= 8) {
                bytes[i++] = (byte) r;
            }
        }
    }

    public long nextLong(long inner, long outer) {
        final long randLow = nextInt() & 0xFFFFFFFFL;
        final long randHigh = nextInt() & 0xFFFFFFFFL;
        if (inner >= outer)
            return inner;
        final long bound = outer - inner;
        final long boundLow = bound & 0xFFFFFFFFL;
        final long boundHigh = (bound >>> 32);
        return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
    }

    public boolean nextBoolean() {
        return (nextInt()) < 0L;
    }

    public float nextFloat () {
        return (nextInt() >>> 8) * 0x1p-24f;
    }

    public double nextDouble () {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public double nextGaussian() {
        return Distributor.normal(nextLong());
    }

    /**
     * Produces a String that holds the entire state of this Taxon32Random. You can recover this state from such a
     * String by calling {@link #deserializeFromString(String)} on any Taxon32Random, which will set that
     * Taxon32Random's state. This does not serialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected if this state is loaded.
     * @return a String holding the current state of this Taxon32Random, to be loaded by {@link #deserializeFromString(String)}
     */
    public String serializeToString() {
        return stateA + "~" + stateB;
    }

    /**
     * Given a String produced by {@link #serializeToString()}, this sets the state of this Taxon32Random to the state
     * stored in that String.This does not deserialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected by this state.
     * @param data a String produced by {@link #serializeToString()}
     * @return this Taxon32Random, after its state has been loaded from the given String
     */
    public Taxon32Random deserializeFromString(String data) {
        if(data == null || data.length() < 3) return this;
        int tilde = data.indexOf('~');
        stateA = Integer.parseInt(data.substring(0, tilde));
        stateB = Integer.parseInt(data.substring(tilde+1));
        return this;
    }

    public Taxon32Random copy() {
        return new Taxon32Random(stateA, stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Taxon32Random random = (Taxon32Random) o;

        if (stateA != random.stateA) return false;
        return stateB == random.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }

    @Override
    public String toString() {
        return "Taxon32Random{" +
                "stateA=" + stateA +
                ", stateB=" + stateB +
                '}';
    }
}

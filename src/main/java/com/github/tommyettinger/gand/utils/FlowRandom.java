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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gdcrux.Distributor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
 * <br>
 * You should use this random number generator if you don't target GWT or TeaVM, and providing one or two long values to
 * {@link #setSeed(long)} or {@link #setState(long, long)} fits your needs. If you target GWT or TeaVM, you may prefer
 * {@link Choo32Random}, which is much faster on those targets, and takes one or four int values for its
 * {@link Choo32Random#setSeed(int)} or {@link Choo32Random#setState(int, int, int, int)} methods. It has a shorter
 * guaranteed minimum cycle length, but a much longer expected actual cycle length (2 to the
 * 32), but a much longer expected actual cycle length (longer than the others here, at least 2 to the 80 expected).
 * There is also {@link Taxon32Random}, which is in between the two on speed on GWT, but the slowest of the three on
 * desktop JVMs (and likely also on Android or iOS). It takes one or two int values for its seed/state, and has the same
 * cycle length as FlowRandom, 2 to the 64 (which generally takes years to exhaust). FlowRandom has many streams, and
 * the others do not.
 * <br>
 * FlowRandom is substantially faster at almost all operations than {@link Choo32Random} or {@link Taxon32Random} when
 * running on a desktop JDK. It is substantially slower at most operations than those two when run on GWT; this
 * disadvantage may persist on TeaVM as well. The reason for this is simple: GWT and TeaVM compile to JavaScript, which
 * doesn't natively support 64-bit integers, and all of FlowRandom's math is done on 64-bit integers. JavaScript does
 * fully support bitwise operations on 32-bit integers, and supports arithmetic on them with some caveats.
 * <br>
 * This class implements interfaces that allow it to be serialized by libGDX {@link Json} and by anything that knows how
 * to serialize an {@link Externalizable} object, such as <a href="https://fury.apache.org">Apache Fury</a>.
 */
public class FlowRandom extends Random implements Json.Serializable, Externalizable {
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

    public double nextGaussian() {
        long x = (stateA += 0xD1B54A32D192ED03L);
        long y = (stateB += 0x8CB92BA72F3D8DD7L);
        x = (x ^ (y << 37 | y >>> 27)) * 0x3C79AC492BA7B653L;
        x = (x ^ x >>> 33) * 0x1C69B3F74AC4AE35L;
        return Distributor.normal(x ^ x >>> 27);
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

    @Override
    public void write(Json json) {
        json.writeObjectStart("flow");
        json.writeValue("a", stateA);
        json.writeValue("b", stateB);
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        jsonData = jsonData.get("flow");
        stateA = jsonData.getLong("a");
        stateB = jsonData.getLong("b");

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(stateA);
        out.writeLong(stateB);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        stateA = in.readLong();
        stateB = in.readLong();
    }
}

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

import com.github.tommyettinger.gdcrux.Distributor;

import java.util.Random;

import static com.github.tommyettinger.gand.utils.Compatibility.imul;

/**
 * A random number generator that is optimized for performance on 32-bit machines and with Google Web Toolkit.
 * This uses only add, subtract, (variable) bitwise-rotate, and XOR operations in its state transition, but also
 * uses multiplication (via {@link Compatibility#imul(int, int)} on GWT) and unsigned right shifts for its output-mixing
 * step. It will usually be compiled out, but this does also use {@code variable = variable + constant | 0;} in order
 * to force additions to counters on GWT to actually overflow as they do (and should) on desktop JVMs.
 * <br>
 * Choo32Random has a guaranteed minimum period of 2 to the 32, and is very likely to have a much longer period for
 * almost all initial states. There are expected to be several (double-digit) relatively long sub-cycles that most
 * states will be within, and relatively few sub-cycles nearing the smallest possible size (2 to the 32, or over 4
 * billion).
 * <br>
 * The algorithm used here has four states purely to exploit instruction-level parallelism; one state is a counter
 * (this gives the guaranteed minimum period of 2 to the 32), and the others combine the values of the four states
 * across three variables. It is possible to invert the generator given a full 128-bit state; this is vital for its
 * period and quality. It is not possible to invert the generator given a known small number of outputs; the furthest
 * you can get when inverting the output is to get the current sum of all four states.
 * <br>
 * This uses part of an output mixer found using <a href="https://github.com/skeeto/hash-prospector">hash-prospector</a>
 * by TheIronBorn, and runs it on a combination of all four states.
 * <br>
 * This is nearly identical to
 * Choo32Random in the Juniper library, and uses the same algorithm, but only extends Random, not Juniper's
 * EnhancedRandom class. If you depend on Juniper, you lose nothing from using the EnhancedRandom classes (they also
 * extend Random), but this class doesn't have as many features as Juniper's Choo32Random. This doesn't depend on
 * anything outside the JDK and {@link Compatibility}, though, so it fits easily as a small addition into gand.
 * <br>
 * This is meant to be used more like a hashing function than a random number generator. If you set the state or seed
 * using {@link #setState(int, int, int, int)} or {@link #setSeed(long)}, every initial state/seed should produce a
 * different sequence of numbers, and numerically close-by states should produce very different sequences.
 */
@SuppressWarnings({"PointlessBitwiseExpression"}) // GWT actually needs these.
public class Choo32Random extends Random {

	/**
	 * The first state; can be any int.
	 */
	protected int stateA;
	/**
	 * The second state; can be any int.
	 */
	protected int stateB;
	/**
	 * The third state; can be any int.
	 */
	protected int stateC;
	/**
	 * The fourth state; can be any int.
	 */
	protected int stateD;

	/**
	 * Creates a new Choo32Random with a random state.
	 */
	public Choo32Random() {
		this((int)((Math.random() - 0.5) * 0x1p32), (int)((Math.random() - 0.5) * 0x1p32),
				(int)((Math.random() - 0.5) * 0x1p32), (int)((Math.random() - 0.5) * 0x1p32));
	}

	/**
	 * Creates a new Choo32Random with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public Choo32Random(long seed) {
		super(seed);
		setSeed(seed);
	}

	/**
	 * Creates a new Choo32Random with the given seed; all {@code int} values are permitted.
	 * The seed will be passed to {@link #setSeed(int)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code int} value
	 */
	public Choo32Random(int seed) {
		super(seed);
		setSeed(seed);
	}

	/**
	 * Creates a new Choo32Random with the given four states; all {@code int} values are permitted.
	 * These states will be used verbatim.
	 *
	 * @param stateA any {@code int} value
	 * @param stateB any {@code int} value
	 * @param stateC any {@code int} value
	 * @param stateD any {@code int} value
	 */
	public Choo32Random(int stateA, int stateB, int stateC, int stateD) {
		super(stateA);
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	/**
	 * This generator has 4 {@code int} states, so this returns 4.
	 *
	 * @return 4 (four)
	 */
	public int getStateCount () {
		return 4;
	}

	/**
	 * Gets the state determined by {@code selection}, as-is. The value for selection should be
	 * between 0 and 3, inclusive; if it is any other value this gets state D as if 3 was given.
	 *
	 * @param selection used to select which state variable to get; generally 0, 1, 2, or 3
	 * @return the value of the selected state, which is an int
	 */
	public int getSelectedState (int selection) {
		switch (selection) {
		case 0:
			return stateA;
		case 1:
			return stateB;
		case 2:
			return stateC;
		default:
			return stateD;
		}
	}

	/**
	 * Sets one of the states, determined by {@code selection}, to {@code value}, as-is.
	 * Selections 0, 1, 2, and 3 refer to states A, B, C, and D,  and if the selection is anything
	 * else, this treats it as 3 and sets stateD.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, or 3
	 * @param value     the exact value to use for the selected state, if valid
	 */
	public void setSelectedState (int selection, int value) {
		switch (selection) {
		case 0:
			stateA = value;
			break;
		case 1:
			stateB = value;
			break;
		case 2:
			stateC = value;
			break;
		default:
			stateD = value;
			break;
		}
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here.
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		int a = (int)seed ^ 0xDB4F0B91, b = (int)(seed >>> 16) ^ 0xBBE05633, c = (int)(seed >>> 32) ^ 0xA0F2EC75, d = (int)(seed >>> 48) ^ 0x89E18285;
		a = imul(a ^ a >>> 16, 0x21f0aaad);
		a = imul(a ^ a >>> 15, 0x735a2d97);
		stateA = a ^ a >>> 15;
		b = imul(b ^ b >>> 16, 0x21f0aaad);
		b = imul(b ^ b >>> 15, 0x735a2d97);
		stateB = b ^ b >>> 15;
		c = imul(c ^ c >>> 16, 0x21f0aaad);
		c = imul(c ^ c >>> 15, 0x735a2d97);
		stateC = c ^ c >>> 15;
		d = imul(d ^ d >>> 16, 0x21f0aaad);
		d = imul(d ^ d >>> 15, 0x735a2d97);
		stateD = d ^ d >>> 15;
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 32) possible initial generator states can be produced here.
	 *
	 * @param seed the initial seed; may be any int
	 */
	public void setSeed (int seed) {
		int a = seed ^ 0xDB4F0B91, b = (seed << 8 | seed >>> 24) ^ 0xBBE05633,
				c = (seed << 16 | seed >>> 16) ^ 0xA0F2EC75, d = (seed << 24 | seed >>> 8) ^ 0x89E18285;
		a = imul(a ^ a >>> 16, 0x21f0aaad);
		a = imul(a ^ a >>> 15, 0x735a2d97);
		stateA = a ^ a >>> 15;
		b = imul(b ^ b >>> 16, 0x21f0aaad);
		b = imul(b ^ b >>> 15, 0x735a2d97);
		stateB = b ^ b >>> 15;
		c = imul(c ^ c >>> 16, 0x21f0aaad);
		c = imul(c ^ c >>> 15, 0x735a2d97);
		stateC = c ^ c >>> 15;
		d = imul(d ^ d >>> 16, 0x21f0aaad);
		d = imul(d ^ d >>> 15, 0x735a2d97);
		stateD = d ^ d >>> 15;
	}

	public int getStateA () {
		return stateA;
	}

	/**
	 * Sets the first part of the state.
	 *
	 * @param stateA can be any int
	 */
	public void setStateA (int stateA) {
		this.stateA = stateA;
	}

	public int getStateB () {
		return stateB;
	}

	/**
	 * Sets the second part of the state.
	 *
	 * @param stateB can be any int
	 */
	public void setStateB (int stateB) {
		this.stateB = stateB;
	}

	public int getStateC () {
		return stateC;
	}

	/**
	 * Sets the third part of the state.
	 *
	 * @param stateC can be any int
	 */
	public void setStateC (int stateC) {
		this.stateC = stateC;
	}

	public int getStateD () {
		return stateD;
	}

	/**
	 * Sets the fourth part of the state.
	 *
	 * @param stateD can be any int
	 */
	public void setStateD (int stateD) {
		this.stateD = stateD;
	}

	/**
	 * Sets the state completely to the given four state variables, each an int.
	 * This is the same as calling {@link #setStateA(int)}, {@link #setStateB(int)},
	 * {@link #setStateC(int)}, and {@link #setStateD(int)} as a group.
	 *
	 * @param stateA the first state; can be any int
	 * @param stateB the second state; can be any int
	 * @param stateC the third state; can be any int
	 * @param stateD the fourth state; can be any int
	 */
	public void setState (int stateA, int stateB, int stateC, int stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	@Override
	public long nextLong () {
		// This is the same as the following, but inlined manually:
		//		return (long)nextInt() << 32 ^ nextInt();

		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;

		final int ga = fb - fc;
		final int gb = fa ^ fd;
		final int gc = (fb << fa | fb >>> -fa);
		final int gd = fd + 0xADB5B165;
		int hi = (ga + gb + gc + gd);
		hi = imul(hi ^ hi >>> 15, 0x735a2d97);

		stateA = gb - gc | 0;
		stateB = ga ^ gd;
		stateC = (gb << ga | gb >>> -ga);
		stateD = gd + 0xADB5B165 | 0;
		int lo = (stateA + stateB + stateC + stateD);
		lo = imul(lo ^ lo >>> 15, 0x735a2d97);

		return (long)(hi ^ hi >>> 16) << 32 ^ (lo ^ lo >>> 16);
	}

	public long previousLong () {
		// This is the same as the following, but inlined manually:
		//		return previousInt() ^ (long)previousInt() << 32;

		final int ga = stateA;
		final int gb = stateB;
		final int gc = stateC;
		final int gd = stateD;

		int lo = ga + gb + gc + gd;
		lo = imul(lo ^ lo >>> 15, 0x735a2d97);
		final int fd = gd - 0xADB5B165;
		final int fa = gb ^ fd;
		final int fb = (gc >>> fa | gc << -fa);
		final int fc = fb - ga;

		int hi = fa + fb + fc + fd;
		hi = imul(hi ^ hi >>> 15, 0x735a2d97);
		stateD = fd - 0xADB5B165 | 0;
		stateA = fb ^ stateD;
		stateB = (fc >>> stateA | fc << -stateA);
		stateC = stateB - fa | 0;

		return (lo ^ lo >>> 16) ^ (long)(hi ^ hi >>> 16) << 32;
	}

	public int previousInt() {
		final int ga = stateA;
		final int gb = stateB;
		final int gc = stateC;
		final int gd = stateD;
		int res = ga + gb + gc + gd;
		res = imul(res ^ res >>> 15, 0x735a2d97);
		stateA = gb ^ (stateD = gd - 0xADB5B165 | 0);
		stateB = (gc >>> stateA | gc << -stateA);
		stateC = stateB - ga | 0;
		return res ^ res >>> 16;
	}

	@Override
	public int next (int bits) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		stateA = fb - fc | 0;
		stateB = fa ^ fd;
		stateC = (fb << fa | fb >>> -fa);
		stateD = fd + 0xADB5B165 | 0;
		int res = (stateA + stateB + stateC + stateD);
		res = imul(res ^ res >>> 15, 0x735a2d97);
		return (res ^ res >>> 16) >>> (32 - bits);
	}

	@Override
	public int nextInt () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		stateA = fb - fc | 0;
		stateB = fa ^ fd;
		stateC = (fb << fa | fb >>> -fa);
		stateD = fd + 0xADB5B165 | 0;
		int res = (stateA + stateB + stateC + stateD);
		res = imul(res ^ res >>> 15, 0x735a2d97);
		return res ^ res >>> 16;
	}

	@Override
	public int nextInt (int bound) {
		return (int)(bound * (nextInt() & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
	}

	public int nextSignedInt (int outerBound) {
		outerBound = (int)(outerBound * (nextInt() & 0xFFFFFFFFL) >> 32);
		return outerBound + (outerBound >>> 31);
	}

	@Override
	public void nextBytes (byte[] bytes) {
		for (int i = 0; i < bytes.length; ) {for (int r = nextInt(), n = Math.min(bytes.length - i, 4); n-- > 0; r >>>= 8) {bytes[i++] = (byte)r;}}
	}

	public long nextLong (long inner, long outer) {
		final long randLow = nextInt() & 0xFFFFFFFFL;
		final long randHigh = nextInt() & 0xFFFFFFFFL;
		if (inner >= outer)
			return inner;
		final long bound = outer - inner;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long boundHigh = (bound >>> 32);
		return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	public long nextSignedLong (long inner, long outer) {
		if (outer < inner) {
			long t = outer;
			outer = inner + 1L;
			inner = t + 1L;
		}
		final long bound = outer - inner;
		final long randLow = nextInt() & 0xFFFFFFFFL;
		final long randHigh = nextInt() & 0xFFFFFFFFL;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long boundHigh = (bound >>> 32);
		return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	@Override
	public boolean nextBoolean () {
		return nextInt() < 0;
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
	 * Produces a String that holds the entire state of this Choo32Random. You can recover this state from such a
	 * String by calling {@link #deserializeFromString(String)} on any Choo32Random, which will set that
	 * Choo32Random's state. This does not serialize any fields inherited from {@link Random}, so the methods that
	 * use Random's side entirely, such as the Stream methods, won't be affected if this state is loaded.
	 * @return a String holding the current state of this Choo32Random, to be loaded by {@link #deserializeFromString(String)}
	 */
	public String serializeToString() {
		return stateA + "~" + stateB + "~" + stateC + "~" + stateD;
	}

	/**
	 * Given a String produced by {@link #serializeToString()}, this sets the state of this Choo32Random to the state
	 * stored in that String.This does not deserialize any fields inherited from {@link Random}, so the methods that
	 * use Random's side entirely, such as the Stream methods, won't be affected by this state.
	 * @param data a String produced by {@link #serializeToString()}
	 * @return this Choo32Random, after its state has been loaded from the given String
	 */
	public Choo32Random deserializeFromString(String data) {
		if(data == null || data.length() < 7) return this;
		int tilde = data.indexOf('~');
		stateA = Integer.parseInt(data.substring(0, tilde));
		stateB = Integer.parseInt(data.substring(tilde+1, tilde = data.indexOf('~', tilde+1)));
		stateC = Integer.parseInt(data.substring(tilde+1, tilde = data.indexOf('~', tilde+1)));
		stateD = Integer.parseInt(data.substring(tilde+1));
		return this;
	}

	public float nextInclusiveFloat () {
		return (0x1000001L * (nextInt() & 0xFFFFFFFFL) >> 32) * 0x1p-24f;
	}

	public Choo32Random copy () {
		return new Choo32Random(stateA, stateB, stateC, stateD);
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Choo32Random that = (Choo32Random)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC && stateD == that.stateD;
	}

	public String toString () {
		return "Choo32Random{" + "stateA=" + (stateA) + ", stateB=" + (stateB) + ", stateC=" + (stateC) + ", stateD=" + (stateD) + "}";
	}
}

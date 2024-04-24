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

package com.github.tommyettinger.gand.ds;

import com.github.tommyettinger.gand.utils.IntConsumer;
import com.github.tommyettinger.gand.utils.IntIterator;
import com.github.tommyettinger.gand.utils.IntPredicate;

import java.util.Iterator;

/**
 * Analogous to {@link java.util.Collection} but for primitive ints, this is built around
 * the primitive-specialized iterator {@link IntIterator} instead of the generic {@link Iterator}.
 * This is not necessarily a modifiable collection.
 */
public interface IntCollection {
	int size ();

	default boolean isEmpty () {
		return size() == 0;
	}

	default boolean notEmpty () {
		return size() != 0;
	}

	boolean add(int item);

	boolean remove(int item);

	boolean contains(int item);

	default boolean addAll(IntCollection other) {
		IntIterator it = other.iterator();
		boolean changed = false;
		while (it.hasNext()) {
			changed |= add(it.nextInt());
		}
		return changed;
	}

	default boolean addAll(int[] array) {
		return addAll(array, 0, array.length);
	}

	default boolean addAll(int[] array, int offset, int length) {
		boolean changed = false;
		for (int i = offset, n = 0; n < length && i < array.length; i++, n++) {
			changed |= add(array[i]);
		}
		return changed;
	}

	/**
	 * Removes from this collection all occurrences of any elements contained in the specified other collection.
	 *
	 * @param other a primitive collection of int items to remove fully, such as an IntList or an IntSet
	 * @return true if this collection was modified.
	 */
	default boolean removeAll(IntCollection other) {
		IntIterator it = other.iterator(), me;
		int originalSize = size();
		while (it.hasNext()) {
			int item = it.nextInt();
			me = iterator();
			while (me.hasNext()) {
				if (me.nextInt() == item) {
					me.remove();
				}
			}
		}
		return originalSize != size();
	}

	default boolean removeAll(int[] array) {
		return removeAll(array, 0, array.length);
	}

	default boolean removeAll(int[] array, int offset, int length) {
		IntIterator me;
		int originalSize = size();
		for (int i = offset, n = 0; n < length && i < array.length; i++, n++) {
			int item = array[i];
			me = iterator();
			while (me.hasNext()) {
				if (me.nextInt() == item) {
					me.remove();
				}
			}
		}
		return originalSize != size();
	}

	/**
	 * Removes from this collection element-wise occurrences of elements contained in the specified other collection.
	 * Note that if a value is present more than once in this collection, only one of those occurrences
	 * will be removed for each occurrence of that value in {@code other}. If {@code other} has the same
	 * contents as this collection or has additional items, then removing each of {@code other} will clear this.
	 *
	 * @param other a primitive collection of int items to remove one-by-one, such as an IntList or an IntSet
	 * @return true if this collection was modified.
	 */
	default boolean removeEach(IntCollection other) {
		IntIterator it = other.iterator();
		boolean changed = false;
		while (it.hasNext()) {
			changed |= remove(it.nextInt());
		}
		return changed;
	}

	default boolean removeEach(int[] array) {
		return removeEach(array, 0, array.length);
	}

	default boolean removeEach(int[] array, int offset, int length) {
		boolean changed = false;
		for (int i = offset, n = 0; n < length && i < array.length; i++, n++) {
			changed |= remove(array[i]);
		}
		return changed;
	}

	default boolean containsAll(IntCollection other) {
		IntIterator it = other.iterator();
		while (it.hasNext()) {
			if(!contains(it.nextInt())) return false;
		}
		return true;
	}

	default boolean containsAll(int[] array) {
		return containsAll(array, 0, array.length);
	}

	default boolean containsAll(int[] array, int offset, int length) {
		for (int i = offset, n = 0; n < length && i < array.length; i++, n++) {
			if(!contains(array[i])) return false;
		}
		return true;
	}

	default boolean containsAny(IntCollection other) {
		IntIterator it = other.iterator();
		while (it.hasNext()) {
			if(contains(it.nextInt())) return true;
		}
		return false;
	}

	default boolean containsAny(int[] array) {
		return containsAny(array, 0, array.length);
	}

	default boolean containsAny(int[] array, int offset, int length) {
		for (int i = offset, n = 0; n < length && i < array.length; i++, n++) {
			if(contains(array[i])) return true;
		}
		return false;
	}

	/**
	 * Removes all the elements of this collection that satisfy the given
	 * predicate.  Errors or runtime exceptions thrown during iteration or by
	 * the predicate are relayed to the caller.
	 *
	 * @param filter a predicate which returns {@code true} for elements to be
	 *               removed
	 * @return {@code true} if any elements were removed
	 * @throws UnsupportedOperationException if elements cannot be removed
	 *                                       from this collection.  Implementations may throw this exception if a
	 *                                       matching element cannot be removed or if, in general, removal is not
	 *                                       supported.
	 * @implSpec The default implementation traverses all elements of the collection using
	 * its {@link #iterator()}.  Each matching element is removed using
	 * {@link Iterator#remove()}.  If the collection's iterator does not
	 * support removal then an {@code UnsupportedOperationException} will be
	 * thrown on the first matching element.
	 */
	default boolean removeIf(IntPredicate filter) {
		boolean removed = false;
		final IntIterator each = iterator();
		while (each.hasNext()) {
			if (filter.test(each.nextInt())) {
				each.remove();
				removed = true;
			}
		}
		return removed;
	}

	default boolean retainAll(IntCollection other) {
		boolean changed = false;
		IntIterator it = iterator();
		while (it.hasNext()) {
			if (!other.contains(it.nextInt())) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	IntIterator iterator();

	void clear ();

	@Override
	int hashCode ();

	@Override
	boolean equals (Object other);

	/**
	 * Allocates a new int array with exactly {@link #size()} items, fills it with the
	 * contents of this PrimitiveCollection, and returns it.
	 *
	 * @return a new int array
	 */
	default int[] toArray() {
		final int sz = size();
		int[] receiver = new int[sz];
		IntIterator it = iterator();
		int i = 0;
		while (it.hasNext())
			receiver[i++] = it.nextInt();
		return receiver;
	}

	/**
	 * Fills the given array with the entire contents of this PrimitiveCollection, up to
	 * {@link #size()} items, or if receiver is not large enough, then this allocates a new
	 * int array with {@link #size()} items and returns that.
	 *
	 * @param receiver an int array that will be filled with the items from this, if possible
	 * @return {@code receiver}, if it was modified, or a new int array otherwise
	 */
	default int[] toArray(int[] receiver) {
		final int sz = size();
		if (receiver.length < sz)
			receiver = new int[sz];
		IntIterator it = iterator();
		int i = 0;
		while (it.hasNext())
			receiver[i++] = it.nextInt();
		return receiver;
	}

	/**
	 * Performs the given action for each element of the {@code IntCollection}
	 * until all elements have been processed or the action throws an
	 * exception.  Actions are performed in the order of iteration, if that
	 * order is specified.  Exceptions thrown by the action are relayed to the
	 * caller.
	 *
	 * @param action The action to be performed for each element
	 */
	default void forEach(IntConsumer action) {
		IntIterator it = iterator();
		while (it.hasNext())
			action.accept(it.nextInt());
	}

	/**
	 * Attempts to get the first item in this PrimitiveCollection, where "first" is only
	 * defined meaningfully if this type is ordered. Many times, this applies to a class
	 * that is not ordered, and in those cases it can get an arbitrary item, and that item
	 * is permitted to be different for different calls to first().
	 * <br>
	 * This is useful for cases where you would normally be able to call something like
	 * {@link java.util.List#get(int)} to get an item, any item, from a collection, but
	 * whatever class you're using doesn't necessarily provide a get(), first(), peek(),
	 * or similar method.
	 * <br>
	 * The default implementation uses {@link #iterator()}, tries to get the first item,
	 * or throws an IllegalStateException if this is empty.
	 *
	 * @return the first item in this PrimitiveCollection, as produced by {@link #iterator()}
	 * @throws IllegalStateException if this is empty
	 */
	default int first() {
		IntIterator it = iterator();
		if (it.hasNext())
			return it.nextInt();
		throw new IllegalStateException("Can't get the first() item of an empty PrimitiveCollection.");
	}

	/**
	 * Compares this IntCollection with another IntCollection by checking their identity,
	 * their types (both must implement IntCollection), and their sizes, before checking if other
	 * contains each item in this IntCollection, in any order or quantity. This is most useful for
	 * the key "set" or value collection in a primitive-backed map, since quantity doesn't matter for keys and
	 * order doesn't matter for either. Many implementations may need to reset the iterator on this
	 * IntCollection, but that isn't necessary for {@code other}.
	 * @param other another Object that should be a IntCollection
	 * @return true if other is another IntCollection with exactly the same items, false otherwise
	 */
	default boolean areEqual(Object other) {
		if(this == other) return true;
		if(!(other instanceof IntCollection)) return false;
		IntCollection pc = (IntCollection) other;
		if(size() != pc.size()) return false;
		IntIterator it = iterator();
		while (it.hasNext()) {
			if(pc.contains(it.nextInt())) return false;
		}
		return true;
	}
}

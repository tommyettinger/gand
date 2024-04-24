package com.github.tommyettinger.gand.utils;

/**
 * A way of measuring what cells are adjacent and how much further any adjacent cells are from other adjacent cells.
 * In practice, this is used for pathfinding first and foremost, with some other code using this to fill nearby cells in
 * some way. You will usually want to use either {@link #MANHATTAN} through an entire codebase when only moves in
 * cardinal directions are allowed, {@link #EUCLIDEAN} when you want some things to look circular instead of always
 * diamond-shaped as with MANHATTAN (this allows diagonal movement for pathfinders only if it is the best option), or
 * maybe {@link #CHEBYSHEV} if you consider using EUCLIDEAN for pathfinding (CHEBYSHEV allows cardinal and diagonal
 * movement with equal cost, but this permits pathfinders to make very strange choices).
 */
public enum GridMetric {

        /**
         * The distance it takes when only the four primary directions can be
         * moved in. The default.
         */
        MANHATTAN,
        /**
         * The distance it takes when diagonal movement costs the same as
         * cardinal movement.
         */
        CHEBYSHEV,
        /**
         * The distance it takes as the crow flies. This will NOT affect movement cost when calculating a path,
         * only the preferred squares to travel to (resulting in drastically more reasonable-looking paths).
         */
        EUCLIDEAN;

        public float heuristic(Direction target) {
            if (this == GridMetric.EUCLIDEAN &&
                (target == Direction.DOWN_LEFT || target == Direction.DOWN_RIGHT || target == Direction.UP_LEFT || target == Direction.UP_RIGHT)) {
                return 1.4142135623730951f; //Math.sqrt(2.0);
            }
            return 1f;
        }

        public int directionCount() {
            return this == GridMetric.MANHATTAN ? 4 : 8;
        }
}

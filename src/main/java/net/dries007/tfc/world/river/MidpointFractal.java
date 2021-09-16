package net.dries007.tfc.world.river;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.levelgen.RandomSource;

public class MidpointFractal
{
    private static final float JITTER_MIN = 0.2f;
    private static final float JITTER_RANGE = 2 * JITTER_MIN;

    private static final int MAX_BISECTIONS = 10;
    private static final float[] ENCOMPASSING_RANGES = new float[MAX_BISECTIONS];

    static
    {
        // All values are to be interpreted as linear functions of the initial inf-norm (n)
        float sqrt2 = (float) Math.sqrt(2);
        float delta = 0, midpointDelta = 0, prevMidpoint, norm = 1;
        for (int i = 0; i < MAX_BISECTIONS; i++)
        {
            ENCOMPASSING_RANGES[i] = Math.max(delta, midpointDelta);

            // Calculate the new midpoint, then the maximum two deltas will contain the midpoint, and max(delta, oldMidpoint)
            prevMidpoint = midpointDelta;
            midpointDelta = 0.5f * (delta + midpointDelta) + sqrt2 * JITTER_MIN * norm;
            delta = Math.max(prevMidpoint, delta);

            // Worst case reduction in the norm
            norm *= 0.5f + JITTER_MIN;
        }
    }

    /**
     * Performs {@code bisections} iterations of the line-bisection fractal algorithm.
     * The segments, both provided and returned, are a sequence of (x0, y0, x1, y1, ... xN, yN)
     */
    private static float[] bisect(RandomSource random, int bisections, float[] segments)
    {
        for (int i = 0; i < bisections; i++)
        {
            float[] splitSegments = new float[(segments.length << 1) - 2];

            // First point
            splitSegments[0] = segments[0];
            splitSegments[1] = segments[1];

            int sources = (segments.length >> 1) - 1;
            int splitIndex = 2;
            for (int index = 0; index < sources; index++)
            {
                // Bisect the segment from [source, source + 1]
                // The first source should already be in the array, we need to set the bisection point and the array

                float sourceX = segments[(index << 1)];
                float sourceY = segments[(index << 1) + 1];
                float drainX = segments[(index << 1) + 2];
                float drainY = segments[(index << 1) + 3];

                float norm = RiverHelpers.normInf(sourceX - drainX, sourceY - drainY);

                // Bisect at the midpoint, plus some variance that's up to ~30% of the distance in either direction
                float bisectX = (random.nextFloat() * JITTER_RANGE - JITTER_MIN) * norm + (sourceX + drainX) * 0.5f;
                float bisectY = (random.nextFloat() * JITTER_RANGE - JITTER_MIN) * norm + (sourceY + drainY) * 0.5f;

                // Copy the new split segments
                splitSegments[splitIndex] = bisectX;
                splitSegments[splitIndex + 1] = bisectY;
                splitSegments[splitIndex + 2] = drainX;
                splitSegments[splitIndex + 3] = drainY;

                splitIndex += 4;
            }

            segments = splitSegments;
        }
        return segments;
    }

    public final float[] segments;
    private final float norm;

    public MidpointFractal(RandomSource random, int bisections, float sourceX, float sourceY, float drainX, float drainY)
    {
        Preconditions.checkArgument(bisections >= 0 && bisections < MAX_BISECTIONS, "Bisections must be within [0, MAX_BISECTIONS)");

        this.segments = bisect(random, bisections, new float[] {sourceX, sourceY, drainX, drainY});
        this.norm = ENCOMPASSING_RANGES[bisections] * RiverHelpers.normInf(sourceX - drainX, sourceY - drainY);
    }

    /**
     * Checks if a given point (x, y) comes within a minimum {@code distance} of the bounding box of the fractal.
     * Faster and more efficient than checking {@link #intersect(float, float, float)}.
     */
    public boolean maybeIntersect(float x, float y, float distance)
    {
        float d = RiverHelpers.distancePointToLineSq(segments[0], segments[1], segments[segments.length - 2], segments[segments.length - 1], x, y);
        float t = distance + norm;
        return d <= t * t;
    }

    /**
     * @return {@code true} the provided point (x, y) comes within a minimum {@code distance} of the fractal.
     */
    public boolean intersect(float x, float y, float distance)
    {
        final float distSq = distance * distance;
        for (int i = 0; i < (segments.length >> 1) - 1; i++)
        {
            float d = RiverHelpers.distancePointToLineSq(segments[(i << 1)], segments[(i << 1) + 1], segments[(i << 1) + 2], segments[(i << 1) + 3], x, y);
            if (d < distSq)
            {
                return true;
            }
        }
        return false;
    }
}
package eu.hansolo.fx.world;

import static eu.hansolo.fx.world.World.*;

public final class WorldProjectionUtils {
    private WorldProjectionUtils() {
    }

    public static double toXPosition(final double longitude) {
        final double value = (longitude + 180) * (MAP_WIDTH / 360) + MAP_OFFSET_X;
        return clampPosition(value, 0.0, MAP_WIDTH);
    }

    public static double toYPosition(final double latitude) {
        final double value = MAP_HEIGHT / 2 -
                MAP_WIDTH * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2)) /
                        (2 * Math.PI) + MAP_OFFSET_Y;
        return clampPosition(value, 0.0, MAP_HEIGHT);
    }

    private static double clampPosition(final double value, final double min, final double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
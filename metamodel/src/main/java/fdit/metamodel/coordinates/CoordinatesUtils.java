package fdit.metamodel.coordinates;

public final class CoordinatesUtils {

    private static final double MAX_LATITUDE = 90;
    private static final double MAX_LONGITUDE = 180;

    private CoordinatesUtils() {
    }

    public static boolean isValidLatitude(final double latitude) {
        return latitude >= -MAX_LATITUDE && latitude <= MAX_LATITUDE;
    }

    public static boolean isValidLongitude(final double longitude) {
        return longitude >= -MAX_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    public static boolean areValidCoordinates(final double latitude, final double longitude) {
        return areValidCoordinates(new Coordinates(latitude, longitude));
    }

    public static boolean areValidCoordinates(final Coordinates coordinates) {
        return isValidLatitude(coordinates.getLatitude()) && isValidLongitude(coordinates.getLongitude());
    }
}

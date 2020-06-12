package fdit.metamodel.coordinates;

public class Coordinates {

    public static final double MAX_LATITUDE = 90;
    public static final double MIN_LATITUDE = -90;
    public static final double MAX_LONGITUDE = 180;
    public static final double MIN_LONGITUDE = -180;

    private final double latitude;
    private final double longitude;

    public Coordinates(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Coordinates other = (Coordinates) o;
        return other.latitude == latitude && other.longitude == longitude;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(latitude);
        int result = (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }
}

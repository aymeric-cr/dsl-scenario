package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;

import static fdit.metamodel.coordinates.Coordinates.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class StaticProperties {

    private int nbAltitudes = 0;
    private double sumAltitudes = 0;
    private double minAltitude = Integer.MAX_VALUE;
    private double maxAltitude = Integer.MIN_VALUE;

    private int nbPositions = 0;
    private double sumLatitudes = 0;
    private double minLatitude = MAX_LATITUDE;
    private double maxLatitude = MIN_LATITUDE;
    private double sumLongitudes = 0;
    private double minLongitude = MAX_LONGITUDE;
    private double maxLongitude = MIN_LONGITUDE;

    private int nbGroundSpeeds = 0;
    private double sumGroundSpeeds = 0;
    private double minGroundSpeed = Double.MAX_VALUE;
    private double maxGroundSpeed = Double.MIN_VALUE;

    private int nbVerticalRates = 0;
    private double sumVerticalRates = 0;
    private double minVerticalRate = Integer.MAX_VALUE;
    private double maxVerticalRate = Integer.MIN_VALUE;

    private int nbTracks = 0;
    private double sumTracks = 0;

    public void updateTracks(final Double track) {
        if (track != null) {
            nbTracks++;
            sumTracks += track;
        }
    }

    public void updateGroundSpeeds(final Double groundSpeed) {
        if (groundSpeed != null) {
            nbGroundSpeeds++;
            sumGroundSpeeds += groundSpeed;
            minGroundSpeed = min(minGroundSpeed, groundSpeed);
            maxGroundSpeed = max(maxGroundSpeed, groundSpeed);
        }
    }

    public void updateAltitudes(final double altitude) {
        nbAltitudes++;
        sumAltitudes += altitude;
        minAltitude = min(minAltitude, altitude);
        maxAltitude = max(maxAltitude, altitude);
    }

    public void updateCoordinates(final Coordinates coordinates) {
        nbPositions++;
        sumLatitudes += coordinates.getLatitude();
        minLatitude = min(minLatitude, coordinates.getLatitude());
        maxLatitude = max(maxLatitude, coordinates.getLatitude());
        sumLongitudes += coordinates.getLongitude();
        minLongitude = min(minLongitude, coordinates.getLongitude());
        maxLongitude = max(maxLongitude, coordinates.getLongitude());
    }

    public void updateVerticalRates(final Double verticalRate) {
        if (verticalRate != null) {
            nbVerticalRates++;
            sumVerticalRates += verticalRate;
            minVerticalRate = min(minVerticalRate, verticalRate);
            maxVerticalRate = max(maxVerticalRate, verticalRate);
        }
    }

    public double getMinAltitude() {
        return minAltitude;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    public double getMeanAltitude() {
        return sumAltitudes / nbAltitudes;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMeanLatitude() {
        return sumLatitudes / nbPositions;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    public double getMeanLongitude() {
        return sumLongitudes / nbPositions;
    }

    public double getMinGroundSpeed() {
        return minGroundSpeed;
    }

    public double getMaxGroundSpeed() {
        return maxGroundSpeed;
    }

    public double getMeanGroundSpeed() {
        return sumGroundSpeeds / nbGroundSpeeds;
    }

    public double getMinVerticalRate() {
        return minVerticalRate;
    }

    public double getMaxVerticalRate() {
        return maxVerticalRate;
    }

    public double getMeanVerticalRate() {
        return sumVerticalRates / nbVerticalRates;
    }

    public int getNbAltitudes() {
        return nbAltitudes;
    }

    public double getSumAltitudes() {
        return sumAltitudes;
    }

    public int getNbPositions() {
        return nbPositions;
    }

    public double getSumLatitudes() {
        return sumLatitudes;
    }

    public double getSumLongitudes() {
        return sumLongitudes;
    }

    public int getNbGroundSpeeds() {
        return nbGroundSpeeds;
    }

    public double getSumGroundSpeeds() {
        return sumGroundSpeeds;
    }

    public int getNbVerticalRates() {
        return nbVerticalRates;
    }

    public double getSumVerticalRates() {
        return sumVerticalRates;
    }

    public int getNbTracks() {
        return nbTracks;
    }

    public double getSumTracks() {
        return sumTracks;
    }
}
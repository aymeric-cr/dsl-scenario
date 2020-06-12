package fdit.metamodel.rap;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.zone.Zone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static fdit.tools.stream.StreamUtils.*;

public class RecognizedAirPicture {

    private final HashMap<Integer, Aircraft> aircrafts = new HashMap<>();
    private final HashMap<String, Zone> zones = new HashMap<>();
    private long relativeDuration;

    public RecognizedAirPicture() {
    }

    public RecognizedAirPicture(final Map<Integer, Aircraft> aircrafts) {
        this.aircrafts.putAll(aircrafts);
    }

    public RecognizedAirPicture(final Map<Integer, Aircraft> aircrafts, final Map<String, Zone> zones) {
        this.aircrafts.putAll(aircrafts);
        this.zones.putAll(zones);
    }

    public double getMaxAltitude() {
        return maxDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMaxAltitude()));
    }

    public double getMinAltitude() {
        return minDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMinAltitude()));
    }

    public double getMaxLatitude() {
        return maxDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMaxLatitude()));
    }

    public double getMinLatitude() {
        return minDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMinLatitude()));
    }

    public double getMaxLongitude() {
        return maxDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMaxLongitude()));
    }

    public double getMinLongitude() {
        return minDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMinLongitude()));
    }

    public double getMinGroundSpeed() {
        return minDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMinGroundSpeed()));
    }

    public double getMaxGroundSpeed() {
        return maxDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMaxGroundSpeed()));
    }

    public double getMinVerticalRate() {
        return minDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMinVerticalRate()));
    }

    public double getMaxVerticalRate() {
        return maxDouble(mapping(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getMaxVerticalRate()));
    }

    public double getMeanLongitude() {
        final int sumNbLongitudes = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbPositions());
        final double sumLongitudes = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumLongitudes());
        return sumLongitudes / sumNbLongitudes;
    }

    public double getMeanLatitude() {
        final int sumNbLatitudes = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbPositions());
        final double sumLatitudes = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumLatitudes());
        return sumLatitudes / sumNbLatitudes;
    }

    public double getMeanAltitude() {
        final int sumNbAltitudes = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbAltitudes());
        final double sumAltitudes = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumAltitudes());
        return sumAltitudes / sumNbAltitudes;
    }

    public double getMeanTrack() {
        final int sumNbTracks = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbTracks());
        final double sumTracks = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumTracks());
        return sumTracks / sumNbTracks;
    }

    public double getMeanGroundSpeed() {
        final int sumNbGroundSpeeds = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbGroundSpeeds());
        final double sumGroundSpeeds = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumGroundSpeeds());
        return sumGroundSpeeds / sumNbGroundSpeeds;
    }

    public double getMeanVerticalRate() {
        final int sumNbVerticalRates = sumInt(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getNbVerticalRates());
        final double sumVerticalRates = sumDouble(aircrafts.values(), aircraft -> aircraft.getStaticProperties().getSumVerticalRates());
        return sumVerticalRates / sumNbVerticalRates;
    }

    public void addZones(final Zone... zones) {
        for (final Zone zone : zones) {
            this.zones.put(zone.getName(), zone);
        }
    }

    public void addZones(final Collection<Zone> zones) {
        for (final Zone zone : zones) {
            this.zones.put(zone.getName(), zone);
        }
    }

    public Collection<Aircraft> getAircrafts() {
        return aircrafts.values();
    }

    public void setAircrafts(final Collection<Aircraft> aircrafts) {
        this.aircrafts.clear();
        for (final Aircraft aircraft : aircrafts) {
            this.aircrafts.put(aircraft.getIcao(), aircraft);
        }
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }

    public boolean zoneExists(final String name) {
        return zones.containsKey(name);
    }

    public Zone getZone(final String name) {
        return zones.get(name);
    }

    public Aircraft getAircraft(final Integer icao) {
        return aircrafts.get(icao);
    }

    public void deleteZones(final String... names) {
        for (final String name : names) zones.remove(name);
    }

    public void addAircrafts(final Aircraft... aircrafts) {
        for (final Aircraft aircraft : aircrafts) {
            this.aircrafts.put(aircraft.getIcao(), aircraft);
        }
    }

    public void addAircrafts(final Collection<Aircraft> aircrafts) {
        for (final Aircraft aircraft : aircrafts) {
            this.aircrafts.put(aircraft.getIcao(), aircraft);
        }
    }

    public long getRelativeDuration() {
        return relativeDuration;
    }

    public void setRelativeDuration(final long relativeDuration) {
        this.relativeDuration = relativeDuration;
    }
}
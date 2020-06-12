package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.metamodel.alteration.parameters.Characteristic;

import static fdit.storage.alteration.AlterationSpecificationStorage.*;

final class AttackScenarioInterpretationUtils {

    private AttackScenarioInterpretationUtils() {
    }

    static Characteristic getCharacteristicByString(final String str) {
        if (str.compareToIgnoreCase(VALUE_TYPE_ALTITUDE) == 0) {
            return Characteristic.ALTITUDE;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_CALL_SIGN) == 0) {
            return Characteristic.CALL_SIGN;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_EMERGENCY) == 0) {
            return Characteristic.EMERGENCY;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_GROUND_SPEED) == 0) {
            return Characteristic.GROUND_SPEED;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_ICAO) == 0) {
            return Characteristic.ICAO;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_LATITUDE) == 0) {
            return Characteristic.LATITUDE;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_LONGITUDE) == 0) {
            return Characteristic.LONGITUDE;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_SPI) == 0) {
            return Characteristic.SPI;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_SQUAWK) == 0) {
            return Characteristic.SQUAWK;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_TRACK) == 0) {
            return Characteristic.TRACK;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_EAST_WEST_VELOCITY) == 0) {
            return Characteristic.EAST_WEST_VELOCITY;
        }
        if (str.compareToIgnoreCase(VALUE_TYPE_NORTH_SOUTH_VELOCITY) == 0) {
            return Characteristic.NORTH_SOUTH_VELOCITY;
        }
        throw new RuntimeException("Unknown characteristic");
    }
}
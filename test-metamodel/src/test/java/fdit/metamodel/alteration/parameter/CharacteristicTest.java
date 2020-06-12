package fdit.metamodel.alteration.parameter;

import org.junit.Test;

import static fdit.metamodel.alteration.parameters.Characteristic.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharacteristicTest {

    @Test
    public void test_ICAO_Validation() {
        assertTrue(getValidationFunction(ICAO).test("39AC47"));
        assertTrue(getValidationFunction(ICAO).test("AAAAAA"));
        assertTrue(getValidationFunction(ICAO).test("111111"));

        assertFalse(getValidationFunction(ICAO).test(""));
        assertFalse(getValidationFunction(ICAO).test("39AC4"));
        assertFalse(getValidationFunction(ICAO).test("39AC478"));
        assertFalse(getValidationFunction(ICAO).test("39GC47"));
        assertFalse(getValidationFunction(ICAO).test("39A 47"));
    }

    @Test
    public void test_SQUAWK_Validation() {
        assertTrue(getValidationFunction(SQUAWK).test("1000"));
        assertTrue(getValidationFunction(SQUAWK).test("7700"));
        assertTrue(getValidationFunction(SQUAWK).test("7600"));

        assertFalse(getValidationFunction(SQUAWK).test(""));
        assertFalse(getValidationFunction(SQUAWK).test("1OOOO"));
        assertFalse(getValidationFunction(SQUAWK).test("L0000"));
        assertFalse(getValidationFunction(SQUAWK).test("7800"));
        assertFalse(getValidationFunction(SQUAWK).test("1900"));
    }

    @Test
    public void test_TIMESTAMP_NANO_Validation() {
        assertTrue(getValidationFunction(TIMESTAMP_NANO).test("1234567891011121314"));
        assertTrue(getValidationFunction(TIMESTAMP_NANO).test("-1234567891011121314"));
        assertTrue(getValidationFunction(TIMESTAMP_NANO).test("1"));

        assertFalse(getValidationFunction(TIMESTAMP_NANO).test(""));
        assertFalse(getValidationFunction(TIMESTAMP_NANO).test("234a6789"));
        assertFalse(getValidationFunction(TIMESTAMP_NANO).test("234a6789"));
    }

    @Test
    public void test_LATITUDE_Validation() {
        assertTrue(getValidationFunction(LATITUDE).test("-90"));
        assertTrue(getValidationFunction(LATITUDE).test("0"));
        assertTrue(getValidationFunction(LATITUDE).test("0.0"));
        assertTrue(getValidationFunction(LATITUDE).test("90"));
        assertTrue(getValidationFunction(LATITUDE).test("50.5"));
        assertTrue(getValidationFunction(LATITUDE).test("-50.2"));

        assertFalse(getValidationFunction(LATITUDE).test(""));
        assertFalse(getValidationFunction(LATITUDE).test("-90.1"));
        assertFalse(getValidationFunction(LATITUDE).test("90.0001"));
        assertFalse(getValidationFunction(LATITUDE).test("a84"));
    }

    @Test
    public void test_LONGITUDE_Validation() {
        assertTrue(getValidationFunction(LONGITUDE).test("-180"));
        assertTrue(getValidationFunction(LONGITUDE).test("0"));
        assertTrue(getValidationFunction(LONGITUDE).test("0.0"));
        assertTrue(getValidationFunction(LONGITUDE).test("180"));
        assertTrue(getValidationFunction(LONGITUDE).test("50.5"));
        assertTrue(getValidationFunction(LONGITUDE).test("-50.2"));

        assertFalse(getValidationFunction(LONGITUDE).test(""));
        assertFalse(getValidationFunction(LONGITUDE).test("-180.1"));
        assertFalse(getValidationFunction(LONGITUDE).test("180.0001"));
        assertFalse(getValidationFunction(LONGITUDE).test("a84"));
    }

    @Test
    public void test_GROUND_SPEED_Validation() {
        assertTrue(getValidationFunction(GROUND_SPEED).test("0"));
        assertTrue(getValidationFunction(GROUND_SPEED).test("0.1"));
        assertTrue(getValidationFunction(GROUND_SPEED).test("50.1"));
        assertTrue(getValidationFunction(GROUND_SPEED).test("102.2"));

        assertFalse(getValidationFunction(GROUND_SPEED).test(""));
        assertFalse(getValidationFunction(GROUND_SPEED).test("1a1"));
        assertFalse(getValidationFunction(GROUND_SPEED).test("-0.1"));
        assertFalse(getValidationFunction(GROUND_SPEED).test("15.14"));
        assertFalse(getValidationFunction(GROUND_SPEED).test("102.21"));
    }

    @Test
    public void test_TRACK_Validation() {
        assertTrue(getValidationFunction(TRACK).test("0"));
        assertTrue(getValidationFunction(TRACK).test("0.1"));
        assertTrue(getValidationFunction(TRACK).test("125.5"));
        assertTrue(getValidationFunction(TRACK).test("359.9"));

        assertFalse(getValidationFunction(TRACK).test(""));
        assertFalse(getValidationFunction(TRACK).test("1a"));
        assertFalse(getValidationFunction(TRACK).test("-0.1"));
        assertFalse(getValidationFunction(TRACK).test("15.14"));
        assertFalse(getValidationFunction(TRACK).test("360"));
    }

    @Test
    public void test_CALL_SIGN_Validation() {
        assertTrue(getValidationFunction(CALL_SIGN).test("1234567"));
        assertTrue(getValidationFunction(CALL_SIGN).test("ABCDEFG"));
        assertTrue(getValidationFunction(CALL_SIGN).test("1234ABC"));
        assertTrue(getValidationFunction(CALL_SIGN).test("1234"));
        assertTrue(getValidationFunction(CALL_SIGN).test("ABCD"));

        assertFalse(getValidationFunction(CALL_SIGN).test(""));
        assertFalse(getValidationFunction(CALL_SIGN).test("1234567!"));
        assertFalse(getValidationFunction(CALL_SIGN).test("DIµSH"));
    }

    @Test
    public void test_ALTITUDE_Validation() {
        assertTrue(getValidationFunction(ALTITUDE).test("0"));
        assertTrue(getValidationFunction(ALTITUDE).test("-1000"));
        assertTrue(getValidationFunction(ALTITUDE).test("50175"));

        assertFalse(getValidationFunction(ALTITUDE).test(""));
        assertFalse(getValidationFunction(ALTITUDE).test("O"));
        assertFalse(getValidationFunction(ALTITUDE).test("0.1"));
        assertFalse(getValidationFunction(ALTITUDE).test("-1001"));
        assertFalse(getValidationFunction(ALTITUDE).test("50176"));
    }

    @Test
    public void test_EAST_WEST_VELOCITY_Validation() {
        assertTrue(getValidationFunction(EAST_WEST_VELOCITY).test("0"));
        assertTrue(getValidationFunction(EAST_WEST_VELOCITY).test("-1023"));
        assertTrue(getValidationFunction(EAST_WEST_VELOCITY).test("126"));
        assertTrue(getValidationFunction(EAST_WEST_VELOCITY).test("1023"));

        assertFalse(getValidationFunction(EAST_WEST_VELOCITY).test(""));
        assertFalse(getValidationFunction(EAST_WEST_VELOCITY).test("O"));
        assertTrue(getValidationFunction(EAST_WEST_VELOCITY).test("126"));
        assertFalse(getValidationFunction(EAST_WEST_VELOCITY).test("-1023.1"));
        assertFalse(getValidationFunction(EAST_WEST_VELOCITY).test("1023.1"));
    }
}
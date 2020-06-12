package fdit.triggcondition.tests

import org.eclipse.xtext.testing.InjectWith
import org.junit.runner.RunWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import fdit.triggcondition.triggeringCondition.Model
import org.eclipse.xtext.testing.util.ParseHelper
import com.google.inject.Inject
import fdit.triggcondition.triggeringCondition.TriggeringConditionPackage
import fdit.triggcondition.validation.TriggeringConditionValidator
import org.junit.Test

@RunWith(XtextRunner)
@InjectWith(TriggeringConditionInjectorProvider)
class TriggeringConditionValidatingTest {
    @Inject extension ParseHelper<Model>
    @Inject extension ValidationTestHelper


    @Test
    def void testtypeCheck0Fail() {
        '''
            eval as_soon_as (RAP.GROUNDSPEED == false)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testtypeCheck0() {
        '''
            eval as_soon_as (RAP.GROUNDSPEED == 2000)
        '''.parse.assertNoErrors
    }

    @Test
    def void testtypeCheck0Arithm() {
        '''
            eval as_soon_as (RAP.GROUNDSPEED == 2000 + 1)
        '''.parse.assertNoErrors
    }

    @Test
    def void testtypeCheck1() {
        '''
            eval as_soon_as (RAP.ALTITUDE > 14)
        '''.parse.assertNoErrors
    }

    @Test
    def void testtypeCheck1Arithm() {
        '''
            eval as_soon_as (RAP.ALTITUDE > (3 * 14))
        '''.parse.assertNoErrors
    }


    @Test
    def void testTypeCheck1Fail() {
        '''
            eval until (AIRCRAFT.ALTITUDE == "test" or ("shia".KNOWN_POSITIONS <= 6))
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck2() {
        '''
            eval not_when ("target".ICAO == "AFR6834") or until (RAP.KNOWN_POSITIONS <= 6)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck2Arithm() {
        '''
            eval not_when ("target".ICAO == "AFR6834") or until (RAP.KNOWN_POSITIONS <= (12 - 3) /6 )
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck2fail() {
        '''
            eval when ("filter".ICAO > 2) or until (RAP.KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckStringFail
    }

    @Test
    def void testTypeCheck2failArithm() {
        '''
            eval when ("filter".ICAO > 2 - 1) or until (RAP.KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckStringFail
    }

    @Test
    def void testTypeCheck3z() {
        '''
            eval when (not("servigne".ICAO == "AFR6834")) or until (RAP.LATITUDE > 32)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck3() {
        '''
            eval as_soon_as (RAP.KNOWN_POSITIONS <= 6 or "Lordon".ALERT == 2)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4() {
        '''
            eval as_soon_as (AIRCRAFT.KNOWN_POSITIONS <= 6 or RAP.ALERT == "true")
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck6() {
        '''
            eval as_soon_as (AIRCRAFT.KNOWN_POSITIONS <= 6 or "Friot".ALERT < true)
        '''.parse.assertBooleanBadComparison
    }

    @Test
    def void testTypeCheck5() {
        '''
            eval as_soon_as (RAP.KNOWN_POSITIONS <= 6 or "Ruffin".ALERT == false)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck3d() {
        '''
            eval as_soon_as (RAP.IS_ON_GROUND == 2 and AIRCRAFT.KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4b() {
        '''
            eval as_soon_as ("marx".IS_ON_GROUND == "true" or AIRCRAFT.ALTITUDE < 34000)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck6b() {
        '''
            eval as_soon_as ("Dr snuggles".IS_ON_GROUND < true or RAP.ALTITUDE < 34000)
        '''.parse.assertBooleanBadComparison
    }

    @Test
    def void testTypeCheck5b() {
        '''
            eval as_soon_as (RAP.ALTITUDE < 34000 or AIRCRAFT.IS_ON_GROUND == false)
        '''.parse.assertNoErrors
    }



    @Test
    def void testTypeCheck3c() {
        '''
            eval when (AIRCRAFT.VERTICAL_RATE == "TRUC" and "Chaud marron".KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck4c() {
        '''
            eval as_soon_as (RAP.VERTICAL_RATE <> false or RAP.ALTITUDE < 34000)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck6c() {
        '''
            eval when (RAP.VERTICAL_RATE > 1000)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck5c() {
        '''
            eval as_soon_as (AIRCRAFT.ALTITUDE < 34000 or "butternut".VERTICAL_RATE == 12)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck5cArithm() {
        '''
            eval as_soon_as (AIRCRAFT.ALTITUDE < 0.8 * RAP.MAX_ALTITUDE or "butternut".VERTICAL_RATE == 12)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck3a() {
        '''
            eval when (ALTITUDE > 9500 and AIRCRAFT.SPI <> 3)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4a() {
        '''
            eval as_soon_as ("Les chatouilles".ALTITUDE > 9500 and RAP.SPI <= "false")
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck5a() {
        '''
            eval as_soon_as (RAP.ALTITUDE > 9500 and AIRCRAFT.SPI == true)
        '''.parse.assertNoErrors
    }

    /*  @Test
        def void testTypeCheck3Fails() {
            '''
                eval when not("AOEI" == "AFR6834") or until RAP.LATITUDE > 32
            '''.parse.assertMissingAircraftProperty
        }

        @Test
        def void testTypeCheck4Fails() {
            '''
                eval when 12 > 7
            '''.parse.assertMissingAircraftProperty
        }*/

    @Test
    def void testStringBadlyCompared1() {
        '''
            eval as_soon_as (AIRCRAFT.ICAO > "AC49ER")
        '''.parse.assertStringBadComparison
    }

    @Test
    def void testStringCompared1() {
        '''
            eval as_soon_as (AIRCRAFT.ICAO == "AC49ER")
        '''.parse.assertNoErrors
    }

    @Test
    def void testBooleanBadlyCompared1() {
        '''
            eval until ("PATA".TRACK < false)
        '''.parse.assertBooleanBadComparison
    }

    def private assertStringBadComparison(Model m) {
        m.assertError(TriggeringConditionPackage.eINSTANCE.stringLiteral, TriggeringConditionValidator.INVALID_COMPARISON_STRING)
    }

    def private assertBooleanBadComparison(Model m) {
        m.assertError(TriggeringConditionPackage.eINSTANCE.booleanLiteral, TriggeringConditionValidator.INVALID_COMPARISON_BOOLEAN)
    }

    def private assertTypeCheckNumberFail(Model m) {
        m.assertError(TriggeringConditionPackage.eINSTANCE.aircraftDynamicProperty, TriggeringConditionValidator.TYPE_CHECK_FAILS_NUMBER)
    }

    def private assertTypeCheckStringFail(Model m) {
        m.assertError(TriggeringConditionPackage.eINSTANCE.aircraftStaticProperty, TriggeringConditionValidator.TYPE_CHECK_FAILS_STRING)
    }

    def private assertTypeCheckBoolFail(Model m) {
        m.assertError(TriggeringConditionPackage.eINSTANCE.aircraftDynamicProperty, TriggeringConditionValidator.TYPE_CHECK_FAILS_BOOL)
    }
}
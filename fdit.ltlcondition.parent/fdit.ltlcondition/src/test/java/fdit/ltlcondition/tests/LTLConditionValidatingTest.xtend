package fdit.ltlcondition.tests

import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.InjectWith
import org.junit.runner.RunWith
import org.junit.Test
import com.google.inject.Inject
import org.eclipse.xtext.testing.util.ParseHelper
import fdit.ltlcondition.lTLCondition.LTLConditionPackage
import fdit.ltlcondition.validation.LTLConditionValidator
import fdit.ltlcondition.lTLCondition.Model
import org.eclipse.xtext.testing.validation.ValidationTestHelper

@RunWith(XtextRunner)
@InjectWith(LTLConditionInjectorProvider)
class LTLConditionValidatingTest {
    @Inject extension ParseHelper<Model>
    @Inject extension ValidationTestHelper

    @Test
    def void testDynamicPropMissesTemporalOpSimple() {
        '''
            eval ALTITUDE > 9500
        '''.parse.assertMissingTemporalOp()
    }

    @Test
    def void testStaticPropMissesTemporalOpSimple() {
        '''
            eval ICAO == "AC49ER"
        '''.parse.assertNoErrors
    }

    @Test
    def void testStaticPropHasTemporalOpDouble() {
        '''
            eval F(ICAO == "AC49ER") and TRACK == 2
        '''.parse.assertWarningStaticHasTemporal
    }

    @Test
    def void testStaticPropHasNoTemporalOpSimple() {
        '''
            eval ICAO == "AC49ER"
        '''.parse.assertNoErrors
    }

    @Test
    def void testDynamicPropHasTemporalOpSimple() {
        '''
            eval G(ALTITUDE > 9500)
        '''.parse.assertNoErrors
    }

    @Test
    def void testDynamicPropHasTemporalOpSimple2() {
        '''
            eval F(ALTITUDE > 9500)
        '''.parse.assertNoErrors
    }

    @Test
    def void testDynamicPropMissesTemporalOpAdvanced() {
        '''
            eval ALTITUDE > 9500 or G(KNOWN_POSITIONS <= 6)
        '''.parse.assertMissingTemporalOp
    }


    @Test
    def void testDynamicPropHasTemporalOpAdvanced() {
        '''
            eval G(ALTITUDE > 9500) and F(KNOWN_POSITIONS <= 6)
        '''.parse.assertNoErrors
    }

    @Test
    def void testDynamicPropHasTemporalOpAdvanced2() {
        '''
            eval G(ALTITUDE > 9500 or KNOWN_POSITIONS <= 6)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTemporalOpCascade() {
        '''
            eval G(ALTITUDE > 9500 or F(KNOWN_POSITIONS <= 6))
        '''.parse.assertCascadeTemporalOpEvent
    }

    @Test
    def void testTemporalOpCascadeAlways() {
        '''
            eval F(ALTITUDE > 9500 or G(KNOWN_POSITIONS <= 6))
        '''.parse.assertCascadeTemporalOpAlways
    }

    @Test
    def void testtypeCheck0() {
        '''
            eval G(GROUNDSPEED == false)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck1() {
        '''
            eval F(ALTITUDE == "test" or G(KNOWN_POSITIONS <= 6))
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck2() {
        '''
            eval F(ICAO > 2 or G(KNOWN_POSITIONS <= 6))
        '''.parse.assertTypeCheckStringFail
    }

    @Test
    def void testTypeCheck3() {
        '''
            eval G(KNOWN_POSITIONS <= 6 or ALERT == 2)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4() {
        '''
            eval G(KNOWN_POSITIONS <= 6 or ALERT == "true")
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck6() {
        '''
            eval G(KNOWN_POSITIONS <= 6 or ALERT < true)
        '''.parse.assertBooleanBadComparison
    }

    @Test
    def void testTypeCheck5() {
        '''
            eval G(KNOWN_POSITIONS <= 6 or ALERT == false)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck3b() {
        '''
            eval G(IS_ON_GROUND == 2 and KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4b() {
        '''
            eval G(IS_ON_GROUND == "true" or ALTITUDE < 34000)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck6b() {
        '''
            eval G(IS_ON_GROUND < true or ALTITUDE < 34000)
        '''.parse.assertBooleanBadComparison
    }

    @Test
    def void testTypeCheck5b() {
        '''
            eval G(ALTITUDE < 34000 or IS_ON_GROUND == false)
        '''.parse.assertNoErrors
    }



    @Test
    def void testTypeCheck3c() {
        '''
            eval F(VERTICAL_RATE == "TRUC" and KNOWN_POSITIONS <= 6)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck4c() {
        '''
            eval G(VERTICAL_RATE <> false or ALTITUDE < 34000)
        '''.parse.assertTypeCheckNumberFail
    }

    @Test
    def void testTypeCheck6c() {
        '''
            eval F(VERTICAL_RATE > 1000)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck5c() {
        '''
            eval G(ALTITUDE < 34000 or VERTICAL_RATE == 12)
        '''.parse.assertNoErrors
    }

    @Test
    def void testTypeCheck3a() {
        '''
            eval F(ALTITUDE > 9500 and SPI <> 3)
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck4a() {
        '''
            eval G(ALTITUDE > 9500 and SPI <= "false")
        '''.parse.assertTypeCheckBoolFail
    }

    @Test
    def void testTypeCheck5a() {
        '''
            eval G(ALTITUDE > 9500 and SPI == true)
        '''.parse.assertNoErrors
    }
    @Test
    def void testStringBadlyCompared1() {
        '''
            eval ICAO > "AC49ER"
        '''.parse.assertStringBadComparison
    }

    @Test
    def void testBooleanBadlyCompared1() {
        '''
            eval G(TRACK < false)
        '''.parse.assertBooleanBadComparison
    }

    @Test
    def void testAreaWithoutTempoSimple() {
        '''
        eval OUTSIDE prism with vertices (84,12),(34,2),(47,234) and altitude from 12000 to 4500
        '''.parse.assertMissingTemporalOpArea
    }

    @Test
    def void testAreaWithoutTempoAdvanced() {
        '''
        eval F(1400 <> GROUNDSPEED) and KNOWN_POSITIONS >= 4 or
            (CALLSIGN == "AC47ER" and INSIDE prism with vertices (84,12),(34,2),(47,234) and altitude from 12000 to 4500 and  G(LONGITUDE <= 51))
        '''.parse.assertMissingTemporalOpArea
    }

    @Test
    def void testAreaWithoutTempoSimpleSuccess() {
        '''
        eval G(INSIDE prism with vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
        '''.parse.assertNoErrors
    }

    @Test
    def void testAreaWithoutTempoAdvancedSuccess() {
        '''
        eval F(GROUNDSPEED <> 1400) and KNOWN_POSITIONS >= 4 or
                    (F(CALLSIGN == "AC47ER" and OUTSIDE prism with vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)) and  G(LONGITUDE <= 51)
        '''.parse.assertNoErrors
    }

    def private assertMissingTemporalOp(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.aircraftDynamicProperty, LTLConditionValidator.MISSING_TEMPORAL_OP_DYNAMICS)
    }

    def private assertMissingTemporalOpArea(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.area, LTLConditionValidator.MISSING_TEMPORAL_3D_AREA)
    }

    def private assertCascadeTemporalOpEvent(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.booleanEventually, LTLConditionValidator.CASCADE_TEMPORAL_OP)
    }

    def private assertCascadeTemporalOpAlways(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.booleanAlways, LTLConditionValidator.CASCADE_TEMPORAL_OP)
    }

    def private assertWarningStaticHasTemporal(Model m) {
        m.assertWarning(LTLConditionPackage.eINSTANCE.aircraftStaticProperty, LTLConditionValidator.EXTRA_TEMPORAL_OP)
    }

    def private assertStringBadComparison(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.stringLiteral, LTLConditionValidator.INVALID_COMPARISON_STRING)
    }

    def private assertBooleanBadComparison(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.booleanLiteral, LTLConditionValidator.INVALID_COMPARISON_BOOLEAN)
    }

    def private assertTypeCheckNumberFail(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.aircraftDynamicProperty, LTLConditionValidator.TYPE_CHECK_FAILS_NUMBER)
    }

    def private assertTypeCheckStringFail(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.aircraftStaticProperty, LTLConditionValidator.TYPE_CHECK_FAILS_STRING)
    }

    def private assertTypeCheckBoolFail(Model m) {
        m.assertError(LTLConditionPackage.eINSTANCE.aircraftDynamicProperty, LTLConditionValidator.TYPE_CHECK_FAILS_BOOL)
    }
}
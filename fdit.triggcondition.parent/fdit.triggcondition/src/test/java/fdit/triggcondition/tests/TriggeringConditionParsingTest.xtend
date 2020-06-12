package fdit.triggcondition.tests

import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.XtextRunner
import org.junit.runner.RunWith
import org.eclipse.xtext.testing.InjectWith
import com.google.inject.Inject
import org.junit.Test
import org.junit.Assert
import fdit.triggcondition.triggeringCondition.Model
import fdit.triggcondition.tests.TriggeringConditionInjectorProvider

@RunWith(XtextRunner)
@InjectWith(TriggeringConditionInjectorProvider)
class TriggeringConditionParsingTest {
    @Inject extension ParseHelper<Model>

    @Test
    def void testGreaterThan() {
        val model = '''
			eval as_soon_as ("filter_name".ICAO > 2)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanArithm() {
        val model = '''
			eval as_soon_as ("filter_name".ICAO > 2 + 1)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanArithm2() {
        val model = '''
			eval as_soon_as ("filter_name".ICAO > (3 * (2 + 1)))
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanFailSpace() {
        val model = '''
			eval as_soon_as (RAP. ICAO > 2)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testEquals() {
        val model = '''
			eval until (AIRCRAFT.CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testEqualsFailSpace() {
        val model = '''
			eval until (AIRCRAFT .CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThan() {
        val model = '''
			eval when ("coucou".ALTITUDE < 2000)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThanFailSpace() {
        val model = '''
			eval when ("no_way" . ALTITUDE < 2000)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThanOrEqual() {
        val model = '''
			eval as_soon_as (RAP.LONGITUDE <= 51)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThanOrEqualNotFails() {
        val model = '''
			eval not(as_soon_as RAP.LONGITUDE <= 51)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanOrEqual() {
        val model = '''
			eval until ("But I am le tired".KNOWN_POSITIONS >= 4)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testDifferent() {
        val model = '''
			eval when (AIRCRAFT.GROUNDSPEED <> 1400)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanReverse() {
        val model = '''
			eval as_soon_as (RAP.ICAO > 2)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testEqualsReverse() {
        val model = '''
			eval not_when ("fire the missiles".CALLSIGN =="AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThanReverse() {
        val model = '''
			eval until (RAP.ALTITUDE > 2000)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testLowerThanOrEqualReverse() {
        val model = '''
			eval as_soon_as (AIRCRAFT.LONGITUDE >= 51)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testGreaterThanOrEqualReverse() {
        val model = '''
			eval not_when ("Then take a nap".KNOWN_POSITIONS >= 4)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testDifferentReverse() {
        val model = '''
			eval not_when (AIRCRAFT.GROUNDSPEED <> 1400)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testFailTwoOperators() {
        val model = '''
			eval as_soon_as (1400 <> RAP.GROUNDSPEED == 2)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testNegationSimple() {
        val model = '''
			eval until (not ("Ah motherland!".GROUNDSPEED <> 1400))
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testNegationSimpleFail() {
        val model = '''
			eval not as_soon_as (AIRCRAFT.KNOWN_POSITIONS >= 4)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple3Points() {
        val model = '''
			eval not_when (AIRCRAFT OUTSIDE prism with_vertices (12.1,45),(1,1),(25,57) and altitude from 2.244893 to 8999.191919919)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsideRefArea() {
        val model = '''
			eval not_when (AIRCRAFT OUTSIDE area "georges")
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5Points() {
        val model = '''
			eval as_soon_as (RAP OUTSIDE prism with_vertices (12,45),(1,1.13),(25,57),(89.3424,3),(23.3432,41.3245) and altitude from 1000.243 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5PointsFailMissingContext() {
        val model = '''
			eval as_soon_as (OUTSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5PointsFailMissingTimeWindow() {
        val model = '''
			eval RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5PointsMissingEverything() {
        val model = '''
			eval OUTSIDE (prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple2PointsFail() {
        val model = '''
			eval until ("oulala"" OUTSIDE prism with_vertices (12,45),(1,2) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple1PointFail() {
        val model = '''
			eval as_soon_as (AIRCRAFT OUTSIDE prism with_vertices (1,2) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5PointsMissingAlt1() {
        val model = '''
			eval as_soon_as (RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOutsidePrismSimple5PointsMissingAlt2() {
        val model = '''
			eval as_soon_as (RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testInsidePrismSimple3Points() {
        val model = '''
			eval not_when ("bingo" INSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2 to 8999)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testInsidePrismSimple5Points() {
        val model = '''
			eval until (AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testInsidePrismSimple5PointsFail() {
        val model = '''
			eval until AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testInsidePrismSimple2PointsFail() {
        val model = '''
			eval as_soon_as (RAP INSIDE prism with_vertices (12,45),(1,2) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testInsidePrismSimple1PointFail() {
        val model = '''
		eval not_when (RAP INSIDE prism with_vertices (1,2) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimple() {
        val model = '''
			eval not_when (AIRCRAFT.GROUNDSPEED > 200 and "yolo".ICAO == "TRUC")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimpleErr() {
        val model = '''
			eval not_when AIRCRAFT.GROUNDSPEED > 200 and "allez sochaux".ICAO == "TRUC"
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimplePar() {
        val model = '''
			eval as_soon_as RAP.GROUNDSPEED > 200 and ("poivron".KNOWN_POSITIONS >= 4)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimple2Par() {
        val model = '''
			eval as_soon_as ((RAP.GROUNDSPEED > 200) and "san pellegrino".KNOWN_POSITIONS >= 4)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimple2Par2() {
        val model = '''
			eval as_soon_as (RAP.GROUNDSPEED > 200 and ("flibustier".KNOWN_POSITIONS >= 4))
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimple3par() {
        val model = '''
			eval as_soon_as ((RAP.GROUNDSPEED > 200) and AIRCRAFT.KNOWN_POSITIONS > 4)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndSimple3par2() {
        val model = '''
			eval as_soon_as ((RAP.GROUNDSPEED > 200) and (AIRCRAFT.KNOWN_POSITIONS > 4))
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimpleFail() {
        val model = '''
			eval until AIRCRAFT.GROUNDSPEED > 200 or as_soon_as RAP.LONGITUDE <= 51
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimpleFail2() {
        val model = '''
			eval until (AIRCRAFT.GROUNDSPEED > 200) or as_soon_as RAP.LONGITUDE <= 51
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimpleFail3() {
        val model = '''
			eval until (AIRCRAFT.GROUNDSPEED > 200) or as_soon_as RAP.LONGITUDE <= 51
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimplePar() {
        val model = '''
			eval until (AIRCRAFT.GROUNDSPEED > 200) or as_soon_as (RAP.LONGITUDE <= 51)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimple2Par() {
        val model = '''
			eval until (AIRCRAFT.GROUNDSPEED > 200) or (as_soon_as (RAP.LONGITUDE <= 51))
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimple3Par() {
        val model = '''
			eval (until (AIRCRAFT.GROUNDSPEED > 200)) or as_soon_as (RAP.LONGITUDE <= 51)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrSimple3Par2() {
        val model = '''
			eval (until (AIRCRAFT.GROUNDSPEED > 200) or as_soon_as (RAP.LONGITUDE <= 51))
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndOrSimple() {
        val model = '''
			eval not_when ((AIRCRAFT.GROUNDSPEED <> 1400 and RAP.KNOWN_POSITIONS >= 4) or "osef".CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }


    @Test
    def void testAndOrSimple2() {
        val model = '''
			eval not_when (AIRCRAFT.GROUNDSPEED <> 1400 and RAP.KNOWN_POSITIONS >= 4) or until ("BINGO".CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndOrSimple2Fail() {
        val model = '''
			eval not_when (AIRCRAFT.GROUNDSPEED <> 1400 and RAP.KNOWN_POSITIONS >= 4) or until (BINGO.CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndOrSimple3() {
        val model = '''
			eval (not_when (AIRCRAFT.GROUNDSPEED <> 1400 and RAP.KNOWN_POSITIONS >= 4)) or until ("192873".CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndOrSimple3Fail() {
        val model = '''
			eval (not_when (AIRCRAFT.GROUNDSPEED <> 1400 and RAP.KNOWN_POSITIONS >= 4)) or until (192873.CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrAndSimple() {
        val model = '''
			eval  until (RAP.GROUNDSPEED <> 1400 or RAP.KNOWN_POSITIONS >= 4 and RAP.CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrAndComplex() {
        val model = '''
			eval  until (RAP.GROUNDSPEED <> 1400 or RAP.KNOWN_POSITIONS >= 4) or (as_soon_as (RAP.ALTITUDE > 2000) and when (AIRCRAFT.CALLSIGN == "AC47ER"))
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrAndDouble() {
        val model = '''
			eval until (AIRCRAFT.GROUNDSPEED <> 1400 or "allez là".KNOWN_POSITIONS >= 4) and as_soon_as ("Chiche".CALLSIGN == "AC47ER" or AIRCRAFT.GROUNDSPEED > 1000)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrPrism() {
        val model = '''
			eval as_soon_as (RAP.LONGITUDE <= 51) or when (AIRCRAFT INSIDE prism with_vertices (12,45.29382938293),(1,1),(25,57),(89.1,3.99),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrPrismFail() {
        val model = '''
			eval as_soon_as (RAP.LONGITUDE <= 51) or when (AIRCRAFT INSIDE prism with_vertices (12,45.29382938293),(1,1),(25,57.),(89.1,3.99),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrPrismFilters() {
        val model = '''
			eval when ("booom".LONGITUDE <= 51) or as_soon_as ("il faut lancer dédé" INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)
        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrPrismPar() {
        val model = '''
			eval until ((RAP.LONGITUDE <= 51) or (AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001))
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testOrPrismReverse() {
        val model = '''
			eval until (("TARGET" INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001) or RAP.LONGITUDE <= 51)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }



    @Test
    def void testorPrisms() {
        val model = '''
			eval until ((RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2.2 to 8999) or AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndDoublePrism2() {
        val model = '''
			eval  as_soon_as (AIRCRAFT OUTSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001) and when (RAP INSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2 to 8999)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndORCascade() {
        val model = '''
			eval until (RAP.GROUNDSPEED <> 1400 and AIRCRAFT.KNOWN_POSITIONS >= 4) or
			when ((AIRCRAFT.CALLSIGN == "AC47ER" and RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2 to 8999) and  "bernard minet".LONGITUDE <= 51)
			and  as_soon_as (AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndORCascadeFail() {
        val model = '''
			eval until ((RAP.GROUNDSPEED <> 1400 and AIRCRAFT.KNOWN_POSITIONS >= 4) or
			not_when (AIRCRAFT.CALLSIGN == "AC47ER" and RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2 to 8999) and  "ambroise croizat".LONGITUDE <= 51)
			and  as_soon_as (AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertFalse(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndORCascade2() {
        val model = '''
			eval until ((RAP.GROUNDSPEED <> 1400 and AIRCRAFT.KNOWN_POSITIONS >= 4) or
			 (AIRCRAFT.CALLSIGN == "AC47ER" and RAP OUTSIDE prism with_vertices (12,45),(1,1),(25,57) and altitude from 2 to 8999) and  "kropotkin".LONGITUDE <= 51)
			and  as_soon_as (AIRCRAFT INSIDE prism with_vertices (12,45),(1,1),(25,57),(89,3),(23,41) and altitude from 1000 to 1001)
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }



    @Test
    def void testAndNotOnRAP() {
        val model = '''
			eval as_soon_as (not(AIRCRAFT.LONGITUDE <= 51) and AIRCRAFT.ICAO == "TRUC")
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

    @Test
    def void testAndOrOnRAPEvent() {
        val model = '''
			eval until (RAP.GROUNDSPEED <> 1400 and not(AIRCRAFT.KNOWN_POSITIONS >= 4)) or not_when ("bakounine".CALLSIGN == "AC47ER")
		'''.parse
        Assert.assertNotNull(model)

        Assert.assertTrue(model.eResource.errors.isEmpty)
    }

}

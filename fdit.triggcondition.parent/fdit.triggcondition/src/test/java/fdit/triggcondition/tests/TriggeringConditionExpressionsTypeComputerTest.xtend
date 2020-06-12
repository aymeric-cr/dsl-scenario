package fdit.triggcondition.tests

import static extension org.junit.Assert.*

import org.eclipse.xtext.testing.XtextRunner
import org.junit.runner.RunWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.util.ParseHelper
import com.google.inject.Inject
import fdit.triggcondition.triggeringCondition.Model
import fdit.triggcondition.triggeringCondition.ArithmExpr
import org.junit.Test
import fdit.triggcondition.triggeringCondition.Equals
import fdit.triggcondition.triggeringCondition.Expression
import fdit.triggcondition.triggeringCondition.GreaterThanOrEq
import fdit.triggcondition.triggeringCondition.LowerThan
import fdit.triggcondition.triggeringCondition.ASAPTimeWindow
import fdit.triggcondition.triggeringCondition.WhenTimeWindow
import fdit.triggcondition.triggeringCondition.NotWhenTimeWindow
import fdit.triggcondition.typing.TriggeringConditionExpressionsTypeComputer
import static fdit.triggcondition.typing.TriggeringConditionExpressionsTypeComputer.*
import fdit.triggcondition.typing.TriggeringConditionExpressionsType
import fdit.triggcondition.triggeringCondition.UntilTimeWindow

@RunWith(XtextRunner)
@InjectWith(TriggeringConditionInjectorProvider)
class TriggeringConditionExpressionsTypeComputerTest {
    @Inject extension ParseHelper<Model>
    @Inject extension TriggeringConditionExpressionsTypeComputer

    @Test def void intLiteral() { "10".assertEvalIntType }
    @Test def void stringLiteral() { "\"foo\"".assertEvalStringType }
    @Test def void floatLiteral() { "10.1".assertEvalFloatType }
    @Test def void boolLiteral() { "true".assertEvalBoolType }

    def assertEvalIntType(CharSequence input) {
        var evt =  (("eval as_soon_as (RAP.ALTITUDE >= " + input+")").parse.expression as ASAPTimeWindow)
        (evt.expr as GreaterThanOrEq).expr.assertType(INT_TYPE)
    }

    def assertEvalBoolType(CharSequence input) {
        var alw = ("eval when (AIRCRAFT.SPI == " + input+")").parse.expression as WhenTimeWindow
        (alw.expr as Equals).expr.assertType(BOOL_TYPE)
    }

    def assertEvalFloatType(CharSequence input) {
        var evt =  (("eval not_when (RAP.GROUNDSPEED < " + input+")").parse.expression as NotWhenTimeWindow)
        (evt.expr as LowerThan).expr.assertType(DOUBLE_TYPE)
    }

    def assertEvalStringType(CharSequence input) {
        var bo = (("eval until (AIRCRAFT.CALLSIGN == " + input).parse.expression as UntilTimeWindow)
        (bo.expr as Equals).expr.assertType(STRING_TYPE)
    }

    def assertType(ArithmExpr ae, TriggeringConditionExpressionsType expectedType) {
        expectedType.assertSame(ae.typeFor)
    }
}

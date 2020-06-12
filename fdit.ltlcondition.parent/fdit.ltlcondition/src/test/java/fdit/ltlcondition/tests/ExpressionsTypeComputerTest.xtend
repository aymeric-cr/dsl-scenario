package fdit.ltlcondition.tests

import static extension org.junit.Assert.*

import org.eclipse.xtext.testing.XtextRunner
import org.junit.runner.RunWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.util.ParseHelper
import com.google.inject.Inject
import fdit.ltlcondition.typing.ExpressionsTypeComputer
import static fdit.ltlcondition.typing.ExpressionsTypeComputer.*
import fdit.ltlcondition.lTLCondition.Model
import fdit.ltlcondition.typing.ExpressionsType
import fdit.ltlcondition.lTLCondition.ArithmExpr
import org.junit.Test
import fdit.ltlcondition.lTLCondition.Equals
import fdit.ltlcondition.lTLCondition.Expression
import fdit.ltlcondition.lTLCondition.BooleanEventually
import fdit.ltlcondition.lTLCondition.GreaterThanOrEq
import fdit.ltlcondition.lTLCondition.BooleanAlways
import fdit.ltlcondition.lTLCondition.LowerThan

@RunWith(XtextRunner)
@InjectWith(LTLConditionInjectorProvider)
class ExpressionsTypeComputerTest {
    @Inject extension ParseHelper<Model>
    @Inject extension ExpressionsTypeComputer

    @Test def void intLiteral() { "10".assertEvalIntType }
    @Test def void stringLiteral() { "\"foo\"".assertEvalStringType }
    @Test def void doubleLiteral() { "10.1".assertEvalDoubleType }
    @Test def void boolLiteral() { "true".assertEvalBoolType }


    def assertEvalIntType(CharSequence input) {
        var evt =  (("eval F(ALTITUDE >= " + input+")").parse.expression as BooleanEventually)
        (evt.expression as GreaterThanOrEq).expr.assertType(INT_TYPE)
    }

    def assertEvalBoolType(CharSequence input) {
        var alw = ("eval G(SPI == " + input+")").parse.expression as BooleanAlways
        (alw.expression as Equals).expr.assertType(BOOL_TYPE)
    }

    def assertEvalDoubleType(CharSequence input) {
        var evt =  (("eval F(LATITUDE < " + input+")").parse.expression as BooleanEventually)
        (evt.expression as LowerThan).expr.assertType(DOUBLE_TYPE)
    }

    def assertEvalStringType(CharSequence input) {
        (("eval CALLSIGN == " + input).parse.expression as Equals).expr.assertType(STRING_TYPE)
    }

    def assertType(ArithmExpr ae, ExpressionsType expectedType) {
        expectedType.assertSame(ae.typeFor)
    }
}
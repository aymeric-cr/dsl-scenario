/*
 * generated by Xtext 2.12.0
 */
package fdit.triggcondition.validation


import org.eclipse.xtext.validation.Check
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator
import fdit.tools.i18n.MessageTranslator
import fdit.triggcondition.triggeringCondition.TriggeringConditionPackage
import fdit.triggcondition.triggeringCondition.LowerThan
import fdit.triggcondition.triggeringCondition.AircraftStaticProperty
import fdit.triggcondition.triggeringCondition.AircraftDynamicProperty
import fdit.triggcondition.triggeringCondition.GreaterThan
import fdit.triggcondition.triggeringCondition.GreaterThanOrEq
import fdit.triggcondition.triggeringCondition.LowerThanOrEq
import fdit.triggcondition.triggeringCondition.Equals
import fdit.triggcondition.triggeringCondition.Different
import fdit.triggcondition.triggeringCondition.IntLiteral
import fdit.triggcondition.triggeringCondition.Prism
import fdit.triggcondition.triggeringCondition.StringLiteral
import fdit.triggcondition.triggeringCondition.BooleanLiteral
import fdit.triggcondition.triggeringCondition.ArithmExpr
import fdit.triggcondition.typing.TriggeringConditionExpressionsTypeComputer
import com.google.inject.Inject
import fdit.ltlcondition.lTLCondition.DoubleLiteral

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */

class TriggeringConditionValidator extends AbstractTriggeringConditionValidator {

    @Inject extension TriggeringConditionExpressionsTypeComputer

    static val MessageTranslator TRANSLATOR = createMessageTranslator(TriggeringConditionValidator)

    public static val INVALID_COMPARISON = 'invalidComparison'
    public static val INVALID_COMPARISON_STRING = INVALID_COMPARISON + ".string"
    public static val INVALID_COMPARISON_BOOLEAN = INVALID_COMPARISON + ".boolean"
    public static val INVALID_ALTITUDE_RANGE = 'invalidAltitudeRange'
    public static val MISSING_TEMPORAL_OP = 'missingTemporalOp'
    public static val MISSING_TEMPORAL_OP_DYNAMICS = MISSING_TEMPORAL_OP + '.dynamics'
    public static val MISSING_TEMPORAL_3D_AREA = MISSING_TEMPORAL_OP + '.3Darea'
    public static val EXTRA_TEMPORAL_OP = 'extraTemporalOp'
    public static val CASCADE_TEMPORAL_OP = 'cascadeTemporalOp'
    public static val TYPE_CHECK_FAILS = 'typeCheckFails'
    public static val TYPE_CHECK_FAILS_NUMBER = TYPE_CHECK_FAILS + '.number'
    public static val TYPE_CHECK_FAILS_STRING = TYPE_CHECK_FAILS + '.string'
    public static val TYPE_CHECK_FAILS_BOOL = TYPE_CHECK_FAILS + '.bool'

    @Check
    def checkParameterFromAtLeastOneSideOfExprsLowerThan(LowerThan lowerThan) {
        if(!(lowerThan.prop instanceof AircraftStaticProperty || lowerThan.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::LOWER_THAN__EXPR,
            INVALID_COMPARISON)
        }
    }

    @Check
    def checkParameterFromAtLeastOneSideOfExprsGreaterThan(GreaterThan greater_than) {
        if(!(greater_than.prop instanceof AircraftStaticProperty || greater_than.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::GREATER_THAN__EXPR,
            INVALID_COMPARISON)
        }
    }

    @Check
    def checkParameterFromAtLeastOneSideOfExprsGreaterThanOrEq(GreaterThanOrEq greater_than_or_eq) {
        if(!(greater_than_or_eq.prop instanceof AircraftStaticProperty || greater_than_or_eq.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::GREATER_THAN_OR_EQ__EXPR,
            INVALID_COMPARISON)
        }
    }

    @Check
    def checkParameterFromAtLeastOneSideOfExprsLowerThanOrEq(LowerThanOrEq lower_than_or_eq) {
        if(!(lower_than_or_eq.prop instanceof AircraftStaticProperty || lower_than_or_eq.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::LOWER_THAN_OR_EQ__EXPR,
            INVALID_COMPARISON)
        }
    }

    @Check
    def checkParameterFromAtLeastOneSideOfExprsEquals(Equals equals) {
        if(!(equals.prop instanceof AircraftStaticProperty || equals.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::EQUALS__EXPR,
            INVALID_COMPARISON)
        }
    }

    @Check
    def checkParameterFromAtLeastOneSideOfExprsDiffs(Different diffs) {
        if(!(diffs.prop instanceof AircraftStaticProperty || diffs.prop instanceof AircraftDynamicProperty)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON),
            TriggeringConditionPackage$Literals::DIFFERENT__EXPR,
            INVALID_COMPARISON)
        }
    }


    // ----------- Making sure that altitude range are well formed ------------ //

    @Check
    def checkAltitudeRangePrism(Prism prism) {
        if(!(prism.lowerAltitude.numberValue < prism.upperAltitude.numberValue)) {
            error(TRANSLATOR.getMessage(INVALID_ALTITUDE_RANGE),
            TriggeringConditionPackage$Literals::PRISM__LOWER_ALTITUDE,
            INVALID_ALTITUDE_RANGE)
        }
    }

    // ----------- TypeChecking ------------ //


    @Check
    def checkTypeOfDynamicProp(AircraftDynamicProperty dynamic_prop) {

        val parent_op = dynamic_prop.eContainer
        var compare_value = parent_op.eContents.get(1) as ArithmExpr
        switch (dynamic_prop.value) {
            case EMERGENCY: {
            }
            case ALTITUDE,
            case GROUND_SPEED,
            case LATITUDE,
            case VERTICAL_RATE,
            case LONGITUDE: {
                if(!(compare_value.typeFor.isDoubleType  || compare_value.typeFor.isIntType)) {
                    error(TRANSLATOR.getMessage(TYPE_CHECK_FAILS_NUMBER, dynamic_prop.value),
                    TriggeringConditionPackage$Literals::AIRCRAFT_DYNAMIC_PROPERTY__VALUE,
                    TYPE_CHECK_FAILS_NUMBER)
                }
            }
            case SQUAWK:  {
                if(!(compare_value.typeFor.isIntType)) {
                    error(TRANSLATOR.getMessage(TYPE_CHECK_FAILS_NUMBER, dynamic_prop.value),
                    TriggeringConditionPackage$Literals::AIRCRAFT_DYNAMIC_PROPERTY__VALUE,
                    TYPE_CHECK_FAILS_NUMBER)
                }
            }
            case SPI,
            case IS_ON_GROUND,
            case ALERT: {
                if(!(compare_value.typeFor.isBoolType)) {
                    error(TRANSLATOR.getMessage(TYPE_CHECK_FAILS_BOOL, dynamic_prop.value),
                    TriggeringConditionPackage$Literals::AIRCRAFT_DYNAMIC_PROPERTY__VALUE,
                    TYPE_CHECK_FAILS_BOOL)
                }
            }
        }
    }

    @Check
    def checkTypeOfStaticProp(AircraftStaticProperty static_prop) {

        val parent_op = static_prop.eContainer
        var compare_value = parent_op.eContents.get(1) as ArithmExpr
        switch (static_prop.value) {
            case CALLSIGN,
            case ICAO: {
                if(!(compare_value.typeFor.isStringType)) {
                    error(TRANSLATOR.getMessage(TYPE_CHECK_FAILS_STRING, static_prop.value),
                    TriggeringConditionPackage$Literals::AIRCRAFT_STATIC_PROPERTY__VALUE,
                    TYPE_CHECK_FAILS_STRING)
                }
            }
            case KNOWN_POSITIONS: {
                if(!(compare_value.typeFor.isIntType)) {
                    error(TRANSLATOR.getMessage(TYPE_CHECK_FAILS_NUMBER, static_prop.value),
                    TriggeringConditionPackage$Literals::AIRCRAFT_STATIC_PROPERTY__VALUE,
                    TYPE_CHECK_FAILS_NUMBER)
                }
            }
            case TRACK: {
            }

        }
    }

    // StringLiteral only accepts == and <>

    @Check
    def checkStringLiteralIsCorrectlyCompared(StringLiteral str) {
        val parent_op = str.eContainer
        if(!(parent_op instanceof Equals || parent_op instanceof Different)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON_STRING),
            TriggeringConditionPackage$Literals::STRING_LITERAL__VALUE,
            INVALID_COMPARISON_STRING)
        }
    }

    // BooleanLiteral only accepts == and <>
    @Check
    def checkBooleanLiteralIsCorrectlyCompared(BooleanLiteral bool) {
        val parent_op = bool.eContainer
        if(!(parent_op instanceof Equals || parent_op instanceof Different)) {
            error(TRANSLATOR.getMessage(INVALID_COMPARISON_BOOLEAN),
            TriggeringConditionPackage$Literals::BOOLEAN_LITERAL__VALUE,
            INVALID_COMPARISON_BOOLEAN)
        }

    }

    def private dispatch getNumberValue(IntLiteral il) {
        return il.value
    }

    def private dispatch getNumberValue(DoubleLiteral fl) {
        return fl.value
    }
}
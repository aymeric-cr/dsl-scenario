package fdit.triggcondition.typing

import fdit.triggcondition.triggeringCondition.IntLiteral
import fdit.triggcondition.triggeringCondition.StringLiteral
import fdit.triggcondition.triggeringCondition.ArithmExpr
import fdit.triggcondition.triggeringCondition.BooleanLiteral
import fdit.triggcondition.triggeringCondition.MulOrDiv
import fdit.triggcondition.triggeringCondition.Minus
import fdit.triggcondition.triggeringCondition.DoubleLiteral
import fdit.triggcondition.typing.StringType
import fdit.triggcondition.typing.IntType
import fdit.triggcondition.typing.BoolType
import fdit.triggcondition.typing.DoubleType
import fdit.triggcondition.typing.TriggeringConditionExpressionsType
import fdit.triggcondition.triggeringCondition.Plus
import fdit.triggcondition.triggeringCondition.CommonStaticProperty

class TriggeringConditionExpressionsTypeComputer {
    public static val STRING_TYPE = new StringType
    public static val INT_TYPE = new IntType
    public static val BOOL_TYPE = new BoolType
    public static val DOUBLE_TYPE = new DoubleType

    def isStringType(TriggeringConditionExpressionsType type) {
        type === STRING_TYPE
    }

    def isIntType(TriggeringConditionExpressionsType type) {
        type === INT_TYPE
    }

    def isDoubleType(TriggeringConditionExpressionsType type) {
        type === DOUBLE_TYPE
    }

    def isBoolType(TriggeringConditionExpressionsType type) {
        type === BOOL_TYPE
    }

    def dispatch TriggeringConditionExpressionsType typeFor(ArithmExpr ae) {
        switch (ae) {
            StringLiteral: STRING_TYPE
            IntLiteral: INT_TYPE
            BooleanLiteral: BOOL_TYPE
            DoubleLiteral: DOUBLE_TYPE
            CommonStaticProperty: typeFor(ae)
        }
    }

    def dispatch TriggeringConditionExpressionsType typeFor(CommonStaticProperty rp) {
        switch (rp.value) {
            case MIN_ALTITUDE : INT_TYPE
            case MAX_ALTITUDE : INT_TYPE
            case MEAN_ALTITUDE : INT_TYPE
            case MIN_LATITUDE : DOUBLE_TYPE
            case MAX_LATITUDE : DOUBLE_TYPE
            case MEAN_LATITUDE : DOUBLE_TYPE
            case MIN_LONGITUDE : DOUBLE_TYPE
            case MAX_LONGITUDE : DOUBLE_TYPE
            case MEAN_LONGITUDE : DOUBLE_TYPE
            case MIN_GROUNDSPEED : DOUBLE_TYPE
            case MAX_GROUNDSPEED : DOUBLE_TYPE
            case MEAN_GROUNDSPEED : DOUBLE_TYPE
        }
    }

    def dispatch TriggeringConditionExpressionsType typeFor(Plus e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if (leftType.isStringType || rightType.isStringType)
            STRING_TYPE
        else if (leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
        INT_TYPE
    }

    def dispatch TriggeringConditionExpressionsType typeFor(Minus e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if (leftType.isStringType || rightType.isStringType)
            STRING_TYPE
        else if (leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
            INT_TYPE
    }

    def dispatch TriggeringConditionExpressionsType typeFor(MulOrDiv e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if (leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
            INT_TYPE
    }
}
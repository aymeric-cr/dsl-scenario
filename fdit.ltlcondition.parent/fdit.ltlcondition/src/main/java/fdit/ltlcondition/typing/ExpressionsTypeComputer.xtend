package fdit.ltlcondition.typing

import fdit.ltlcondition.lTLCondition.IntLiteral
import fdit.ltlcondition.lTLCondition.StringLiteral
import fdit.ltlcondition.lTLCondition.ArithmExpr
import fdit.ltlcondition.lTLCondition.BooleanLiteral
import fdit.ltlcondition.lTLCondition.MulOrDiv
import fdit.ltlcondition.lTLCondition.Minus
import fdit.ltlcondition.lTLCondition.DoubleLiteral
import fdit.ltlcondition.typing.StringType
import fdit.ltlcondition.typing.IntType
import fdit.ltlcondition.typing.BoolType
import fdit.ltlcondition.typing.DoubleType
import fdit.ltlcondition.typing.ExpressionsType
import fdit.ltlcondition.lTLCondition.Plus
import fdit.ltlcondition.lTLCondition.CommonStaticProperty

class ExpressionsTypeComputer {
    public static val STRING_TYPE = new StringType
    public static val INT_TYPE = new IntType
    public static val BOOL_TYPE = new BoolType
    public static val DOUBLE_TYPE = new DoubleType

    def isStringType(ExpressionsType type) {
        type === STRING_TYPE
    }

    def isIntType(ExpressionsType type) {
        type === INT_TYPE
    }

    def isDoubleType(ExpressionsType type) {
        type === DOUBLE_TYPE
    }

    def isBoolType(ExpressionsType type) {
        type === BOOL_TYPE
    }

    def dispatch ExpressionsType typeFor(ArithmExpr ae) {
        switch (ae) {
            StringLiteral: STRING_TYPE
            IntLiteral: INT_TYPE
            BooleanLiteral: BOOL_TYPE
            DoubleLiteral: DOUBLE_TYPE
            CommonStaticProperty: typeFor(ae)
        }
    }

    def dispatch ExpressionsType typeFor(CommonStaticProperty rp) {
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

    def dispatch ExpressionsType typeFor(Plus e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if(leftType.isStringType || rightType.isStringType)
            STRING_TYPE
        else if(leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
            INT_TYPE
    }

    def dispatch ExpressionsType typeFor(Minus e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if(leftType.isStringType || rightType.isStringType)
            STRING_TYPE
        else if(leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
            INT_TYPE
    }

    def dispatch ExpressionsType typeFor(MulOrDiv e) {
        val leftType = e.left.typeFor
        val rightType = e.right?.typeFor
        if(leftType.isDoubleType || rightType.isDoubleType)
            DOUBLE_TYPE
        else
            INT_TYPE
    }
}
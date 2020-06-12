package fdit.triggcondition.interpreter

import fdit.triggcondition.triggeringCondition.ContextExpr
import fdit.triggcondition.triggeringCondition.ContextType
import java.util.ArrayList

class ExpressionEvaluationStorage {
    val ContextExpr expr
    val ContextType type
    val ArrayList<Boolean> result

    new(ContextExpr expr, ContextType type, ArrayList<Boolean> result) {
        this.expr = expr
        this.type = type
        this.result = result
    }
}
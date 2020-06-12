package fdit.ltlcondition.rewriter

import java.io.File

class LTLConditionRewriter {
    val public static File REMOVE_NEGATION = getWizard("removeNegations.ewl")

    def static File getWizard(String name) {
        new File(LTLConditionRewriter.classLoader.getResource("fdit/ltlcondition/validation/"+name).file)
    }

}
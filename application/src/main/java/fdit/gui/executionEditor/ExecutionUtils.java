package fdit.gui.executionEditor;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.recording.Recording;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;

import java.util.Collection;
import java.util.Vector;

import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.nameChecker.CheckResult.fail;
import static fdit.storage.nameChecker.CheckResult.success;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ExecutionUtils {

    public static final String TEST_CASE_DIR_NAME = "test_case_";
    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ExecutionUtils.class);

    private ExecutionUtils() {
    }

    static Recording extractRecording(final Execution execution) {
        return execution.getRecording();
    }

    static CheckResult isConfigurationValid(final Execution execution) {
        if (execution.getSchemas().isEmpty()) {
            return fail(TRANSLATOR.getMessage("error.noSchemas"));
        }
        if (execution.getRecording() == EMPTY_RECORDING) {
            return fail(TRANSLATOR.getMessage("error.noSelectedRecording"));
        }
        return success();
    }

    static AlterationSpecification[][] allUniqueCombinations(final Collection<Vector<AlterationSpecification>> data) {
        int n = data.size();
        int solutions = 1;

        for (Vector<AlterationSpecification> vector : data) {
            solutions *= vector.size();
        }

        AlterationSpecification[][] allCombinations = new AlterationSpecification[solutions][];

        for (int i = 0; i < solutions; i++) {
            Vector<AlterationSpecification> combination = new Vector<>(n);
            int j = 1;
            for (Vector<AlterationSpecification> vec : data) {
                combination.add(vec.get((i / j) % vec.size()));
                j *= vec.size();
            }
            allCombinations[i] = combination.toArray(new AlterationSpecification[n]);
        }
        return allCombinations;
    }
}
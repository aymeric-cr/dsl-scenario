package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.dsl.ide.CompletionProposal;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.schema.Schema;
import fdit.tools.functional.ThrowableSupplier;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class SchemaInterpreter {

    private final AttackScenarioFacade attackScenarioFacade;
    private final SchemaSwitchCompletion syntaxicCompleter;
    private final SchemaSwitchSemanticAnalyse semanticAnalyser;
    private final SchemaCombinationSwitchInterpretation combinationInterpreter;
    private final SchemaSwitchInterpretation scenarioInterpreter;

    public SchemaInterpreter(final AttackScenarioFacade attackScenarioFacade) {
        try {
            this.attackScenarioFacade = attackScenarioFacade;
            initialize();
            semanticAnalyser = new SchemaSwitchSemanticAnalyse(attackScenarioFacade);
            combinationInterpreter = new SchemaCombinationSwitchInterpretation(attackScenarioFacade);
            scenarioInterpreter = new SchemaSwitchInterpretation(attackScenarioFacade);
            syntaxicCompleter = new SchemaSwitchCompletion(attackScenarioFacade);
        } catch (final Exception e) {
            ThrowableSupplier.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public final void initialize() {
        attackScenarioFacade.initialize();
    }

    public void shutdown() {
        attackScenarioFacade.shutdown();
    }

    public void parseScenario(final Schema schema) {
        try {
            attackScenarioFacade.parse(schema.getContent());
        } catch (final IOException e) {
            ThrowableSupplier.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        attackScenarioFacade.getAST().clear();
    }

    public Collection<CompletionProposal> getProposals(final Schema schema,
                                                       final int offset,
                                                       final int selectedTextLength) {
        try {
            semanticAnalyser.processAnalysis(schema);
            return syntaxicCompleter.processCompletion(schema, semanticAnalyser.getMemory(), offset, selectedTextLength);
        } catch (final Exception ex) {
            return newArrayList();
        }
    }

    public Collection<AlterationSpecification> extractSpecifications(final Schema schema) {
        final Collection<AlterationSpecification> specifications = newArrayList();
        for (final Schema concreteSchema : interpreteAbstractScenario(schema)) {
            specifications.add(interpreteConcreteScenario(concreteSchema));
        }
        return specifications;
    }

    public List<Schema> interpreteAbstractScenario(final Schema schema) {
        return combinationInterpreter.processCombination(schema);
    }

    public AlterationSpecification interpreteConcreteScenario(final Schema schema) {
        return scenarioInterpreter.processInterpretation(schema);
    }

    public String getSemanticErrors(final Schema schema) {
        return semanticAnalyser.processAnalysis(schema);
    }
}
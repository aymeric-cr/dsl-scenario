package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.xtext.standalone.AttackScenarioDslFacade;
import fdit.dsl.xtext.standalone.CompletionProposal;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.schema.Schema;
import javafx.beans.property.BooleanProperty;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.inject.internal.util.$Lists.newArrayList;

public class SchemaInterpreter {

    private final AttackScenarioDslFacade attackScenarioDslFacade;
    private final SchemaSwitchCompletion syntaxicCompleter;
    private final SchemaSwitchSemanticAnalyse semanticAnalyser;
    private final SchemaCombinationSwitchInterpretation combinationInterpreter;
    private final SchemaSwitchInterpretation scenarioInterpreter;

    public SchemaInterpreter(final AttackScenarioDslFacade attackScenarioDslFacade) {
        try {
            this.attackScenarioDslFacade = attackScenarioDslFacade;
            initialize();
            semanticAnalyser = new SchemaSwitchSemanticAnalyse(attackScenarioDslFacade);
            combinationInterpreter = new SchemaCombinationSwitchInterpretation(attackScenarioDslFacade);
            scenarioInterpreter = new SchemaSwitchInterpretation(attackScenarioDslFacade);
            syntaxicCompleter = new SchemaSwitchCompletion(attackScenarioDslFacade);
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public final void initialize() {
        attackScenarioDslFacade.initialize();
    }

    public void shutdown() {
        attackScenarioDslFacade.shutdown();
    }

    public void parseScenario(final Schema schema) {
        try {
            attackScenarioDslFacade.parse(schema.getContent());
        } catch (final IOException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        attackScenarioDslFacade.getAST().clear();
    }

    public Collection<CompletionProposal> getProposals(final Schema schema,
                                                       final int offset,
                                                       final int selectedTextLength) {
        try {
            semanticAnalyser.processAnalysis(schema);
            return syntaxicCompleter.processCompletion(schema, semanticAnalyser.getMemory(), offset, selectedTextLength);
        } catch (final Exception ignored) {
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

    public boolean isScenarioConvertible() {
        return semanticAnalyser.convertibleProperty().get();
    }

    public BooleanProperty scenarioConvertibleProperty() {
        return semanticAnalyser.convertibleProperty();
    }
}
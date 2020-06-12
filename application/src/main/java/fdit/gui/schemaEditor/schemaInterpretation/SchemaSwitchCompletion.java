package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.xtext.standalone.AttackScenarioDslFacade;
import fdit.dsl.xtext.standalone.CompletionProposal;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Constant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.ListConstant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Memory;
import fdit.gui.schemaEditor.schemaInterpretation.memory.RangeConstant;
import fdit.metamodel.alteration.parameters.Characteristic;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import org.eclipse.emf.ecore.EObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.alteration.parameters.Characteristic.*;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.tools.stream.StreamUtils.filter;
import static java.lang.Double.parseDouble;

public class SchemaSwitchCompletion extends AttackScenarioSwitch<Collection<CompletionProposal>> {

    private int offset;
    private Memory memory;
    private final AttackScenarioDslFacade attackScenarioDslFacade;
    private Schema schema;

    SchemaSwitchCompletion(final AttackScenarioDslFacade attackScenarioDslFacade) {
        this.attackScenarioDslFacade = attackScenarioDslFacade;
    }

    public Collection<CompletionProposal> processCompletion(final Schema schema,
                                                            final Memory memory,
                                                            final int offset,
                                                            final int selectedTextLength) {
        this.schema = schema;
        this.memory = memory;
        this.offset = offset;
        try {
            attackScenarioDslFacade.parse(schema.getContent());
        } catch (IOException e) {
            return newArrayList();
        }
        final Optional<EObject> currentNode = attackScenarioDslFacade.getCurrentNode(offset, selectedTextLength);
        if (currentNode.isPresent()) {
            return doSwitch(currentNode.get());
        } else {
            return newArrayList();
        }
    }

    private static boolean isValidConstantCriterion(final ASTBinaryOp object, final Constant constant) {
        if (object instanceof ASTEqual ||
                object instanceof ASTIn ||
                object instanceof ASTDifferent) {
            switch (object.getName()) {
                case MAX_ALTITUDE:
                    return isValidAltitude(constant);
                case CALLSIGN:
                    return isValidCallsign(constant);
                case ICAO:
                    return isValidIcao(constant);
                case MIN_ALTITUDE:
                    return isValidAltitude(constant);
                case KNOWN_POSITIONS:
                    return isValidKnowsPositions(constant);
            }
        }
        return false;
    }

    private static boolean isValidConstantParameter(final ASTParameter object, final Constant constant) {
        final String characteristicName = object.getName().getLiteral().toUpperCase();
        final Characteristic characteristic = AttackScenarioInterpretationUtils.getCharacteristicByString(characteristicName);

        if (characteristic.canBeAList()) {
            return isValidList(constant, getValidationFunction(characteristic));
        }
        if (characteristic.canBeARange()) {
            return isValidRange(constant, getValidationFunction(characteristic));
        }
        return false;
    }

    private static boolean isValidList(final Constant constant, final Predicate<String> predicate) {
        if (constant instanceof ListConstant) {
            for (final Object value : ((ListConstant<?>) constant).getValues()) {
                if (!predicate.test(value.toString())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isValidRange(final Constant constant, final Predicate<String> predicate) {
        if (constant instanceof RangeConstant) {
            final Object start = ((RangeConstant<?>) constant).getStart();
            final Object end = ((RangeConstant<?>) constant).getEnd();
            return predicate.test(start.toString()) && predicate.test(end.toString());
        }
        return !(constant instanceof ListConstant);
    }

    private static boolean isValidKnowsPositions(final Constant knownPositions) {
        if (knownPositions instanceof RangeConstant) {
            final Object start = ((RangeConstant<?>) knownPositions).getStart();
            final Object end = ((RangeConstant<?>) knownPositions).getEnd();
            return isNumberValid(start.toString()) && isNumberValid(end.toString()) &&
                    parseDouble(start.toString()) >= 0 && parseDouble(end.toString()) >= 0;
        }
        if (knownPositions instanceof ListConstant) {
            for (final Object value : ((ListConstant<?>) knownPositions).getValues()) {
                if (isNumberValid(value.toString()) && parseDouble(value.toString()) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidAltitude(final Constant altitude) {
        if (altitude instanceof RangeConstant) {
            final Object start = ((RangeConstant<?>) altitude).getStart();
            final Object end = ((RangeConstant<?>) altitude).getEnd();
            return isNumberValid(start.toString()) && isNumberValid(end.toString());
        }
        return !(altitude instanceof ListConstant);
    }

    private static boolean isValidCallsign(final Constant callsign) {
        if (callsign instanceof ListConstant) {
            for (final Object value : ((ListConstant<?>) callsign).getValues()) {
                if (!isCallSignValid(value.toString())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isValidIcao(final Constant icao) {
        if (icao instanceof ListConstant) {
            for (final Object value : ((ListConstant<?>) icao).getValues()) {
                if (!isIcaoValid(value.toString())) {
                    return false;
                }
            }
            return true;
        }
        return !(icao instanceof RangeConstant);
    }

    @Override
    public Collection<CompletionProposal> caseASTPlaneFrom(final ASTPlaneFrom object) {
        return recordingCompletion();
    }

    @Override
    public Collection<CompletionProposal> caseASTAllPlaneFrom(final ASTAllPlaneFrom object) {
        return recordingCompletion();
    }

    private Collection<CompletionProposal> recordingCompletion() {
        final Collection<CompletionProposal> proposals = newArrayList();
        final String prefix = getPrefix();
        for (final Recording recording : gatherAllRecordings(FDIT_MANAGER.getRoot())) {
            final String proposal = '"' + recording.getName() + "\" ";
            proposals.add(new CompletionProposal(
                    prefix,
                    proposal,
                    recording.getName(),
                    offset + proposal.length() - prefix.length()));
        }
        for (final ListConstant constant : filter(memory.getConstants(), ListConstant.class)) {
            if (containsAllFilters(constant)) {
                final String proposal = constant.getName() + ' ';
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTTrigger(final ASTTrigger object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        final String prefix = getPrefix();
        for (final ActionTrigger trigger : gatherAllAlterationTriggers(FDIT_MANAGER.getRoot())) {
            final String proposal = '"' + trigger.getName() + "\" ";
            proposals.add(new CompletionProposal(
                    prefix,
                    proposal,
                    trigger.getName(),
                    offset + proposal.length() - prefix.length()));
        }
        for (final ListConstant<?> constant : filter(memory.getConstants(), ListConstant.class)) {
            if (containsAllTriggers(constant)) {
                final String proposal = constant.getName() + ' ';
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTFilters(final ASTFilters object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        final String prefix = getPrefix();
        for (final LTLFilter ltlFilter : gatherAllLTLFilters(FDIT_MANAGER.getRoot())) {
            final String proposal = '"' + ltlFilter.getName() + "\" ";
            proposals.add(new CompletionProposal(
                    prefix,
                    proposal,
                    ltlFilter.getName(),
                    offset + proposal.length() - prefix.length()));
        }
        for (final ListConstant<?> constant : filter(memory.getConstants(), ListConstant.class)) {
            if (containsAllFilters(constant)) {
                final String proposal = constant.getName() + ' ';
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTParameter(final ASTParameter object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantParameter(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTEqual(final ASTEqual object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTDifferent(final ASTDifferent object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTLt(final ASTLt object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTGt(final ASTGt object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTLte(final ASTLte object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTGte(final ASTGte object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    @Override
    public Collection<CompletionProposal> caseASTIn(final ASTIn object) {
        final Collection<CompletionProposal> proposals = newArrayList();
        for (final Constant constant : memory.getConstants()) {
            if (isValidConstantCriterion(object, constant)) {
                final String proposal = constant.getName();
                final String prefix = getPrefix();
                proposals.add(new CompletionProposal(
                        prefix,
                        proposal,
                        proposal,
                        offset + proposal.length() - prefix.length()));
            }
        }
        return proposals;
    }

    private boolean containsAllTriggers(final ListConstant<?> constant) {
        return new ListConstant.ListConstantTypeSwitch<Boolean>() {
            @Override
            public Boolean visitInteger(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitDouble(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitFloat(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitString(final ListConstant<?> listConstant) {
                boolean validTrigger = true;
                for (final Object value : listConstant.getValues()) {
                    validTrigger &= findActionTrigger((String) value, FDIT_MANAGER.getRoot()).isPresent();
                }
                return validTrigger;
            }
        }.doSwitch(constant);
    }

    private boolean containsAllFilters(final ListConstant<?> constant) {
        return new ListConstant.ListConstantTypeSwitch<Boolean>() {
            @Override
            public Boolean visitInteger(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitDouble(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitFloat(final ListConstant<?> listConstant) {
                return false;
            }

            @Override
            public Boolean visitString(final ListConstant<?> listConstant) {
                boolean validFilter = true;
                for (final Object value : listConstant.getValues()) {
                    validFilter &= findLTLFilter((String) value, FDIT_MANAGER.getRoot()).isPresent();
                }
                return validFilter;
            }
        }.doSwitch(constant);
    }

    private String getPrefix() {
        final String text = schema.getContent().substring(0, offset);
        if (!text.endsWith(" ")) {
            final String[] words = text.split(" ");
            if (words.length > 0) {
                return words[words.length - 1];
            }
        }
        return "";
    }
}
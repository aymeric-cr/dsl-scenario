package fdit.gui.triggerEditor;

import com.google.common.collect.Lists;
import com.google.inject.internal.util.$Lists;
import fdit.gui.application.FditManager;
import fdit.gui.application.FditManagerListener;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeIntegerProperty;
import fdit.gui.utils.ThreadSafeObjectProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.history.Command;
import fdit.history.FditHistoryListener;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.TimeInterval;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.rap.RecognizedAirPicture;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;
import fdit.triggcondition.ide.CompletionProposal;
import fdit.triggcondition.ide.StylesPosition;
import fdit.triggcondition.ide.SyntaxFault;
import fdit.triggcondition.ide.TriggeringConditionFacade;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.IndexRange;
import org.codefx.libfx.listener.handle.ListenerDetachHandle;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.triggerEditor.ActionTriggerEditorController.EMPTY_FILTER;
import static fdit.gui.utils.FXUtils.startRunnableInBackground;
import static fdit.ltlcondition.ide.LTLFilterUtils.filterAircrafts;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.triggcondition.ide.TriggeringConditionHighlightingCalculator.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Double.parseDouble;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

class ActionTriggerEditorModel {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ActionTriggerEditorModel.class);

    private static final Collection<String> TO_EXCLUDE = Lists.newArrayList("value", "filtername");
    private static final Collection<String> TO_TRIM = Lists.newArrayList("AIRCRAFT", "RAP", ".");
    private static final int MAX_PROPOSALS = 50;
    private static final IndexRange EMPTY_RANGE = new IndexRange(0, 0);

    private final ActionTrigger trigger;
    private final TriggeringConditionFacade triggerFacade;
    private final ObservableMap<Aircraft, Collection<TimeInterval>> aircraftsIntervals = observableHashMap();
    private final ObservableList<Recording> selectableRecordings = observableArrayList();
    private final ObjectProperty<Recording> selectedRecording = new ThreadSafeObjectProperty<>(EMPTY_RECORDING);
    private final ObservableList<LTLFilter> selectableFilters = observableArrayList();
    private final ObjectProperty<LTLFilter> selectedFilter = new ThreadSafeObjectProperty<>(EMPTY_FILTER);
    private final StringProperty text = new ThreadSafeStringProperty("");
    private final ObjectProperty<IndexRange> selectedText = new SimpleObjectProperty<>(EMPTY_RANGE);
    private final IntegerProperty caretPosition = new ThreadSafeIntegerProperty(0);
    private final StringProperty startInterval = new ThreadSafeStringProperty("0");
    private final StringProperty endInterval = new ThreadSafeStringProperty("0");
    private final ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpans = new SimpleObjectProperty<>(null);
    private final ObservableList<CompletionProposal> proposals = observableArrayList();
    private final BooleanProperty completionIsOpened = new ThreadSafeBooleanProperty(false);
    private final BooleanProperty withinIntervalChecked = new ThreadSafeBooleanProperty(false);
    private final BooleanProperty saveDisable = new ThreadSafeBooleanProperty(true);
    private final BooleanProperty timeIntervalDisable = new ThreadSafeBooleanProperty(true);
    private final StringProperty errors = new ThreadSafeStringProperty("");
    private final BooleanProperty hasError = new ThreadSafeBooleanProperty(true);
    private String editedText;
    private ActionTriggerEditionCommand lastEditionCommand;
    private PlainTextChange change;
    private ListenerHandle fditManagerListenerHandle;
    private BooleanProperty interpretButtonDisabled = new ThreadSafeBooleanProperty(true);
    private Collection<ListenerHandle> listenerHandles = newArrayList();

    ActionTriggerEditorModel(final ActionTrigger trigger,
                             final TriggeringConditionFacade triggerFacade) {
        this.trigger = trigger;
        this.triggerFacade = triggerFacade;
        FDIT_MANAGER.getCommandExecutor().getHistory().addListener(codeCommandsListener());
        fditManagerListenerHandle = ListenerHandles.createFor(FDIT_MANAGER,
                createFditManagerListener())
                .onAttach(FditManager::addListener)
                .onDetach(FditManager::removeListener)
                .buildAttached();
    }

    private static int computeNewCaretPosition(final PlainTextChange contentChange) {
        return contentChange.getPosition() + contentChange.getInserted().length();
    }

    private static StyleSpans<Collection<String>> noHighlighting() {
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.add(EMPTY_LIST, 0);
        return spansBuilder.create();
    }

    public void initialize() {
        triggerFacade.initialize(FDIT_MANAGER.getRoot());
        selectableRecordings.add(EMPTY_RECORDING);
        selectableRecordings.addAll(gatherAllRecordings(FDIT_MANAGER.getRoot()));
        selectableFilters.add(EMPTY_FILTER);
        selectableFilters.addAll(gatherAllLTLFilters(FDIT_MANAGER.getRoot()));
        editedText = trigger.getContent();
        text.set(trigger.getContent());
        initializeListenersProperties();
        parseAndHighlight();
        setCaretPosition(trigger.getContent().length());
        computeErrors();
        updateTriggerIsSavable();
    }

    private FditHistoryListener codeCommandsListener() {
        return new FditHistoryListener() {
            @Override
            public void commandUndone(final Command command, final Command.CommandType commandType) {
                if (command instanceof ActionTriggerEditionCommand &&
                        ((ActionTriggerEditionCommand) command).getSubject() == trigger) {
                    requestUndoRedoTextChange(((ActionTriggerEditionCommand) command).getContentChange().invert());
                }
                if (command instanceof ActionTriggerSaveCommand) {
                    editedText = ((ActionTriggerSaveCommand) command).getOldContent();
                    updateTriggerIsSavable();
                }
            }

            @Override
            public void commandRedone(final Command command, final Command.CommandType commandType) {
                if (command instanceof ActionTriggerEditionCommand &&
                        ((ActionTriggerEditionCommand) command).getSubject() == trigger) {
                    requestUndoRedoTextChange(((ActionTriggerEditionCommand) command).getContentChange());
                }
                if (command instanceof ActionTriggerSaveCommand) {
                    editedText = trigger.getContent();
                    updateTriggerIsSavable();
                }
            }

            private void requestUndoRedoTextChange(final PlainTextChange contentChange) {
                lastEditionCommand = null;
                text.set(trigger.getContent());
                onTextChanged(contentChange);
            }
        };
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement elementAdded) {
                if (elementAdded instanceof Recording) {
                    selectableRecordings.add((Recording) elementAdded);
                }
                if (elementAdded instanceof LTLFilter) {
                    selectableFilters.add((LTLFilter) elementAdded);
                }
            }

            @Override
            public void elementRemoved(final FditElement elementRemoved) {
                if (elementRemoved instanceof Recording) {
                    selectableRecordings.remove(elementRemoved);
                }
                if (elementRemoved instanceof LTLFilter) {
                    selectableFilters.remove(elementRemoved);
                }
            }

            @Override
            public void elementEdited(final FditElement editedElement) {
                if (editedElement instanceof Recording) {
                    selectableRecordings.remove(editedElement);
                    selectableRecordings.add((Recording) editedElement);
                }
                if (editedElement instanceof LTLFilter) {
                    selectableFilters.remove(editedElement);
                    selectableFilters.add((LTLFilter) editedElement);
                }
            }
        };
    }

    private void initializeListenersProperties() {
        listenerHandles.add(createAttached(hasError, observable -> updateEvaluateButton()));
        listenerHandles.add(createAttached(selectedRecording, observable -> updateEvaluateButton()));
        listenerHandles.add(createAttached(selectedRecording, observable -> updateTimeIntervalFields()));
        listenerHandles.add(createAttached(withinIntervalChecked, observable -> updateTimeIntervalFields()));
        listenerHandles.add(createAttached(startInterval, observable -> computeErrors()));
        listenerHandles.add(createAttached(endInterval, observable -> computeErrors()));
    }

    private void updateEvaluateButton() {
        interpretButtonDisabled.set(selectedRecording.get() == EMPTY_RECORDING || hasError.get());
    }

    private void updateTimeIntervalFields() {
        timeIntervalDisable.set(selectedRecording.get() == EMPTY_RECORDING || !isWithinIntervalChecked());
        if (selectedRecording.get() == EMPTY_RECORDING) {
            endInterval.set("0");
        } else {
            selectedRecording.get().load();
            endInterval.set(String.valueOf(selectedRecording.get().getMaxRelativeDate() / 1000.0));
        }
        startInterval.set("0");
    }

    private void onTextChanged(final PlainTextChange change) {
        final int newCaretPosition = computeNewCaretPosition(change);
        caretPosition.set(newCaretPosition);
        setSelectedText(new IndexRange(newCaretPosition, newCaretPosition));
        startRunnableInBackground(() -> {
            parseAndHighlight();
            updateProposals();
            computeErrors();
            updateTriggerIsSavable();
        });
    }

    private void computeErrors() {
        final StringBuilder trgerrors = new StringBuilder();
        if (triggerFacade.getParseErrors().isEmpty()) {
            triggerFacade.getValidationErrors().getChildren().forEach(diagnostic ->
                    trgerrors.append(diagnostic.getMessage()).append('\n'));
        } else {
            triggerFacade.getParseErrors().forEach(syntaxFault ->
                    trgerrors.append(syntaxFault.getMessage())
                            .append(" (")
                            .append(syntaxFault.getLine())
                            .append(':')
                            .append(syntaxFault.getColumn())
                            .append(')'));
        }
        trgerrors.append(computeTimeIntervalsErrors());
        this.errors.setValue(trgerrors.toString());
        hasError.setValue(!this.errors.get().isEmpty());
    }

    private String computeTimeIntervalsErrors() {
        final StringBuilder errors = new StringBuilder();
        if (isWithinIntervalChecked()) {
            try {
                selectedRecording.get().load();
                final long maxRelativeDate = selectedRecording.get().getMaxRelativeDate();
                if (parseDouble(startInterval.get()) >= parseDouble(endInterval.get())) {
                    errors.append(TRANSLATOR.getMessage("error.timeInterval.lowerBound"))
                            .append('\n');
                }
                if (parseDouble(startInterval.get()) * 1000 >= maxRelativeDate ||
                        parseDouble(endInterval.get()) * 1000 > maxRelativeDate) {
                    errors.append(TRANSLATOR.getMessage("error.timeInterval.upperBound", maxRelativeDate));
                }
            } catch (final NumberFormatException e) {
                errors.append(TRANSLATOR.getMessage("error.timeInterval.badFormat"));
            }
        }
        return errors.toString();
    }

    private void updateProposals() {
        synchronized (triggerFacade) {
            if (!completionIsOpened.get()) {
                synchronized (proposals) {
                    proposals.clear();
                }
                return;
            }
            final IndexRange selection = selectedText.get();
            final Collection<CompletionProposal> completionProposals = computeCompletionProposals(
                    selection.getStart(),
                    selection.getLength());
            this.proposals.setAll(completionProposals);
        }
    }

    private Collection<CompletionProposal> computeCompletionProposals(final int offset, final int selectedTextLength) {
        try {
            final Collection<CompletionProposal> completionProposals = newArrayList();
            triggerFacade.parse(trigger.getContent());
            completionProposals.addAll(triggerFacade.getProposals(offset, selectedTextLength, MAX_PROPOSALS));
            completionProposals.removeIf(proposal -> TO_EXCLUDE.contains(proposal.getLabel()));
            trimProposals(completionProposals);
            return completionProposals;
        } catch (final Exception ignored) {
            return newArrayList();
        }
    }

    private void trimProposals(final Collection<CompletionProposal> proposals) {
        final Collection<CompletionProposal> toAdd = $Lists.newArrayList();
        final Collection<CompletionProposal> toRemove = $Lists.newArrayList();
        for (final CompletionProposal proposal : proposals) {
            if (TO_TRIM.contains(proposal.getLabel())) {
                toRemove.add(proposal);
                toAdd.add(new CompletionProposal(
                        proposal.getPrefix(),
                        proposal.getProposal().trim(),
                        proposal.getLabel(),
                        proposal.getNextCaretPosition() - 1));
            }
        }
        proposals.removeAll(toRemove);
        proposals.addAll(toAdd);
    }

    Optional<HashMap<Aircraft, List<TimeInterval>>> interpretTrigger() {
        if (FALSE.equals(hasError.getValue())) {
            final RecognizedAirPicture rap = new RecognizedAirPicture();
            final Recording recording = selectedRecording.get();
            recording.load();
            rap.addAircrafts(recording.getAircrafts());
            rap.addZones(gatherAllZones(FDIT_MANAGER.getRoot()));
            final long maxRelativeDate = selectedRecording.get().getMaxRelativeDate();
            rap.setRelativeDuration(maxRelativeDate);
            final TimeInterval interval;
            if (isWithinIntervalChecked()) {
                interval = new TimeInterval((long) parseDouble(startInterval.get()) * 1000,
                        (long) parseDouble(endInterval.get()) * 1000);
            } else {
                interval = new TimeInterval(0L, maxRelativeDate);
            }
            if (selectedFilter.get() == EMPTY_FILTER) {
                return of(triggerFacade.getAircraftIntervals(rap, rap.getAircrafts(), FDIT_MANAGER.getRoot(), interval, recording));
            } else {
                final Collection<Aircraft> targets = filterAircrafts(recording, selectedFilter.get(),
                        FDIT_MANAGER.getRoot());
                return of(triggerFacade.getAircraftIntervals(rap, targets, FDIT_MANAGER.getRoot(), interval, recording));
            }
        }
        return empty();
    }

    Optional<String> getErrorAtOffset(final int offset) {
        for (final SyntaxFault syntaxFault : triggerFacade.getParseErrors()) {
            final int errorEndOffset = syntaxFault.getOffset() + syntaxFault.getLength();
            if (offset >= syntaxFault.getOffset() && offset <= errorEndOffset) {
                return of(syntaxFault.getMessage());
            }
        }
        return empty();
    }

    private void updateTriggerIsSavable() {
        saveDisable.set(!hasDataChanged());
    }

    void requestTextChange(final PlainTextChange change) {
        this.change = change;
        registerChange();
        onTextChanged(change);
    }

    private void registerChange() {
        if (lastEditionCommand == null || !lastEditionCommand.mergeTextChangeWith(change)) {
            final ActionTriggerEditionCommand triggerEditionCommand =
                    new ActionTriggerEditionCommand(trigger, change);
            lastEditionCommand = triggerEditionCommand;
            FDIT_MANAGER.getCommandExecutor().execute(triggerEditionCommand);
        }
        text.set(trigger.getContent());
    }

    private boolean hasDataChanged() {
        return !editedText.equals(text.get());
    }

    public void save() {
        registerSave();
        editedText = trigger.getContent();
        updateTriggerIsSavable();
    }

    private void registerSave() {
        final ActionTriggerSaveCommand scenarioEditionCommand =
                new ActionTriggerSaveCommand(trigger, editedText);
        FDIT_MANAGER.getCommandExecutor().execute(scenarioEditionCommand);
    }

    private void parseAndHighlight() {
        synchronized (triggerFacade) {
            triggerFacade.parse(text.get());
            if (isBlank(getText())) {
                styleSpans.set(noHighlighting());
            } else {
                styleSpans.set(computeHighlighting(getText()));
            }
        }
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(final CharSequence text) {
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastStyleEnd = 0;
        for (final StylesPosition stylesPosition : triggerFacade.getHighlightingStyles()) {
            spansBuilder.add(EMPTY_LIST, stylesPosition.getOffset() - lastStyleEnd);
            final Collection<String> highlightings = newArrayList();
            for (final String style : stylesPosition.getStyles()) {
                switch (style) {
                    case CONSTANT_STYLE:
                        highlightings.add("constant");
                        break;
                    case ENUM_STYLE:
                        highlightings.add("enum");
                        break;
                    case KEYWORD_STYLE:
                        highlightings.add("keyword");
                        break;
                    case STRING_STYLE:
                        highlightings.add("string");
                        break;
                    case NUMBER_STYLE:
                        highlightings.add("number");
                        break;
                    case ERROR_STYLE:
                        highlightings.add("syntax-error");
                        break;
                    default:
                        throw new RuntimeException("unknown style");
                }
            }
            spansBuilder.add(highlightings, stylesPosition.getLength());
            lastStyleEnd = stylesPosition.getOffset() + stylesPosition.getLength();
        }
        spansBuilder.add(EMPTY_LIST, text.length() - lastStyleEnd);
        return spansBuilder.create();
    }

    void closeModel() {
        fditManagerListenerHandle.detach();
        fditManagerListenerHandle = null;
        listenerHandles.forEach(ListenerDetachHandle::detach);
        listenerHandles.clear();
    }

    void setCaretPosition(final int caretPosition, final IndexRange selection) {
        if (caretPosition != getCaretPosition()) {
            this.caretPosition.set(caretPosition);
            setSelectedText(selection);
            startRunnableInBackground(this::updateProposals);
        }
    }

    public int getCaretPosition() {
        return caretPosition.get();
    }

    void setCaretPosition(final int caretPosition) {
        setCaretPosition(caretPosition, new IndexRange(caretPosition, caretPosition));
    }

    private void setSelectedText(final IndexRange selectedText) {
        this.selectedText.set(selectedText);
    }

    ObservableList<CompletionProposal> getProposals() {
        synchronized (proposals) {
            return proposals;
        }
    }

    BooleanProperty interpretButtonDisabledProperty() {
        return interpretButtonDisabled;
    }

    BooleanProperty timeIntervalDisabledProperty() {
        return timeIntervalDisable;
    }

    BooleanProperty withinIntervalIsChecked() {
        return withinIntervalChecked;
    }

    boolean isWithinIntervalChecked() {
        return withinIntervalChecked.get();
    }

    StringProperty getStartInterval() {
        return startInterval;
    }

    StringProperty getEndInterval() {
        return endInterval;
    }

    void setCompletionIsOpened(final boolean completionIsOpened) {
        this.completionIsOpened.set(completionIsOpened);
        startRunnableInBackground(this::updateProposals);
    }

    public ObservableMap<Aircraft, Collection<TimeInterval>> getAircraftsIntervals() {
        return aircraftsIntervals;
    }

    public StringProperty textProperty() {
        return text;
    }

    ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpansProperty() {
        return styleSpans;
    }

    IntegerProperty caretPositionProperty() {
        return caretPosition;
    }

    BooleanProperty getSaveDisable() {
        return saveDisable;
    }

    Property<Boolean> hasError() {
        return hasError;
    }

    StringProperty getErrors() {
        return errors;
    }

    public String getText() {
        return text.get();
    }

    ObservableList<Recording> getSelectableRecordings() {
        return selectableRecordings;
    }

    ObservableList<LTLFilter> getSelectableFilters() {
        return selectableFilters;
    }

    ObjectProperty<Recording> selectedRecordingProperty() {
        return selectedRecording;
    }

    ObjectProperty<LTLFilter> selectedFilterProperty() {
        return selectedFilter;
    }

    Recording getSelectedRecording() {
        return selectedRecording.get();
    }
}
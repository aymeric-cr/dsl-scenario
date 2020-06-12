package fdit.gui.filterEditor.tabs.filter;

import fdit.gui.application.FditManager;
import fdit.gui.application.FditManagerListener;
import fdit.gui.application.commands.create.LTLFilterCreationCommand;
import fdit.gui.application.commands.delete.DeletionUtils;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeIntegerProperty;
import fdit.gui.utils.ThreadSafeObjectProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.ltlcondition.ide.CompletionProposal;
import fdit.ltlcondition.ide.LTLConditionFacade;
import fdit.ltlcondition.ide.StylesPosition;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.rap.RecognizedAirPicture;
import fdit.metamodel.recording.Recording;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;
import org.codefx.libfx.listener.handle.ListenerDetachHandle;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.utils.FXUtils.startRunnableInBackground;
import static fdit.ltlcondition.ide.LTLConditionHighlightingCalculator.*;
import static fdit.metamodel.element.DirectoryUtils.gatherAllRecordings;
import static fdit.metamodel.element.DirectoryUtils.gatherAllZones;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.nameChecker.FditElementNameChecker.checkRenameElementValidity;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.util.Collections.EMPTY_LIST;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class FilterEditionModel {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FilterEditionModel.class);

    private static final int MAX_PROPOSALS = 50;
    private static final IndexRange EMPTY_RANGE = new IndexRange(0, 0);
    private final LTLConditionFacade dslFacade;
    private final ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpans = new SimpleObjectProperty<>(null);

    private final UUID id;
    private final ObjectProperty<IndexRange> selectedText = new SimpleObjectProperty<>(EMPTY_RANGE);
    private final BooleanProperty tabIsFocused = new ThreadSafeBooleanProperty();
    private final ObservableList<Recording> selectableRecordings = observableArrayList();
    private final ObjectProperty<Recording> selectedRecording = new ThreadSafeObjectProperty<>(EMPTY_RECORDING);
    private final StringProperty name = new ThreadSafeStringProperty(TRANSLATOR.getMessage("name.newFilter"));
    private final StringProperty description = new ThreadSafeStringProperty("");
    private final StringProperty content = new ThreadSafeStringProperty("");
    private final BooleanProperty okButtonDisabled = new ThreadSafeBooleanProperty(true);
    private final BooleanProperty evaluateButtonDisabled = new ThreadSafeBooleanProperty(true);
    private final StringProperty errorMessage = new ThreadSafeStringProperty("");
    private final IntegerProperty caretPosition = new ThreadSafeIntegerProperty(0);
    private final Collection<ListenerHandle> listenerHandles = newArrayList();
    private final BooleanProperty completionIsOpened = new ThreadSafeBooleanProperty(false);
    private final Collection<FilterEditionModelListener> listeners = newArrayList();
    private final StringProperty ltlErrors = new ThreadSafeStringProperty("");
    private final ObservableList<Aircraft> selectedAircrafts = observableArrayList();
    private final ObservableList<CompletionProposal> proposals = observableArrayList();
    private LTLFilter editedFilter;
    private ListenerHandle fditManagerListenerHandle;
    private PlainTextChange change;

    FilterEditionModel(final LTLConditionFacade dslFacade,
                       final UUID id) {
        this.dslFacade = dslFacade;
        this.id = id;
        fditManagerListenerHandle = ListenerHandles.createFor(FDIT_MANAGER,
                createFditManagerListener())
                .onAttach(FditManager::addListener)
                .onDetach(FditManager::removeListener)
                .buildAttached();
    }

    private static StyleSpans<Collection<String>> noHighlighting() {
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.add(EMPTY_LIST, 0);
        return spansBuilder.create();
    }

    public void initialize() {
        dslFacade.initialize(FDIT_MANAGER.getRoot());
        selectableRecordings.add(EMPTY_RECORDING);
        selectableRecordings.addAll(gatherAllRecordings(FDIT_MANAGER.getRoot()));
        initializeListenersProperties();
        parseAndHighlight();
    }

    void setCompletionIsOpened(final boolean completionIsOpened) {
        this.completionIsOpened.set(completionIsOpened);
        startRunnableInBackground(this::updateProposals);
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement elementAdded) {
                if (elementAdded instanceof LTLFilter && ((LTLFilter) elementAdded).getId().equals(id)) {
                    editedFilter = (LTLFilter) elementAdded;
                    updateOkButton();
                }
                if (elementAdded instanceof Recording) {
                    selectableRecordings.add((Recording) elementAdded);
                }
            }

            @Override
            public void elementRemoved(final FditElement elementRemoved) {
                if (elementRemoved == editedFilter) {
                    editedFilter = null;
                    updateOkButton();
                }
                if (elementRemoved instanceof Recording) {
                    selectableRecordings.remove(elementRemoved);
                }
            }

            @Override
            public void elementEdited(final FditElement editedElement) {
                if (editedElement == editedFilter) {
                    restoreModelFromEditedFilter();
                    updateOkButton();
                }
                if (editedElement instanceof Recording) {
                    selectableRecordings.remove(editedElement);
                    selectableRecordings.add((Recording) editedElement);
                }
            }
        };
    }

    void updateSelectedAircrafts() {
        if (dslFacade.isValidated()) {
            selectedAircrafts.clear();
            final RecognizedAirPicture rap = new RecognizedAirPicture();
            selectedRecording.get().load();
            rap.addAircrafts(selectedRecording.get().getAircrafts());
            rap.addZones(gatherAllZones(FDIT_MANAGER.getRoot()));
            selectedAircrafts.addAll(dslFacade.filterAircraft(rap, selectedRecording.get()));
        }
    }

    private void updateOkButton() {
        okButtonDisabled.set(!validate() || !dataChanged());
    }

    public boolean dataChanged() {
        if (editedFilter == null) {
            return true;
        }
        return !(getName().equals(editedFilter.getName()) &&
                getContent().equals(editedFilter.getContent()) &&
                getDescription().equals(editedFilter.getDescription()));
    }

    private boolean validate() {
        final String newFilterName = getName();
        final CheckResult checkResult;
        final File rootFile = FDIT_MANAGER.getRootFile();
        if (editedFilter != null) {
            checkResult = checkRenameElementValidity(editedFilter, rootFile, newFilterName);
            if (checkResult.checkFailed()) {
                errorMessage.setValue(checkResult.getMessage());
                return false;
            }
        }
        errorMessage.set("");
        return true;
    }

    ObjectProperty<IndexRange> selectedTextProperty() {
        return selectedText;
    }

    private Directory getFilterFolder() {
        if (editedFilter != null) {
            return editedFilter.getFather();
        }
        return FDIT_MANAGER.getRoot();
    }

    private void initializeListenersProperties() {
        listenerHandles.add(createAttached(tabIsFocused, (observable, oldValue, newValue) -> {
            if (newValue) {
                listeners.forEach(FilterEditionModelListener::tabFocused);
            }
        }));
        listenerHandles.add(createAttached(ltlErrors, (observable, oldValue, newValue) -> updateEvaluateButton()));
        listenerHandles.add(createAttached(selectedRecording, observable -> updateEvaluateButton()));
        listenerHandles.add(createAttached(name, (observable, oldValue, newValue) -> updateOkButton()));
        listenerHandles.add(createAttached(content, (observable, oldValue, newValue) -> updateOkButton()));
    }

    private void updateEvaluateButton() {
        evaluateButtonDisabled.set(selectedRecording.get() == EMPTY_RECORDING || !ltlErrors.get().isEmpty());
    }

    void delete() throws IOException {
        if (editedFilter != null) {
            DeletionUtils.delete(editedFilter);
        }
    }

    void closeModel() {
        fditManagerListenerHandle.detach();
        fditManagerListenerHandle = null;
        listenerHandles.forEach(ListenerDetachHandle::detach);
        listenerHandles.clear();
    }

    BooleanProperty okButtonDisabledProperty() {
        return okButtonDisabled;
    }

    public LTLFilter getEditedFilter() {
        return editedFilter;
    }

    void setEditedFilter(final LTLFilter editedFilter) {
        this.editedFilter = editedFilter;
    }

    StringProperty contentProperty() {
        return content;
    }

    public String getContent() {
        return content.get();
    }

    public String getName() {
        return name.get();
    }

    StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    void setTabIsFocused(final boolean tabIsFocused) {
        this.tabIsFocused.set(tabIsFocused);
    }

    void restoreModelFromEditedFilter() {
        name.set(editedFilter.getName());
        description.set(editedFilter.getDescription());
        content.set(editedFilter.getContent());
    }

    void requestTextChange(final PlainTextChange change) {
        this.change = change;
        onTextChanged();
    }

    private void onTextChanged() {
        computeNewCaretPosition();
        computeNewContent();
        startRunnableInBackground(() -> {
            parseAndHighlight();
            computeErrors();
            updateProposals();
            //updateScenarioIsSavable();
        });
    }

    private void updateProposals() {
        synchronized (dslFacade) {
            if (!completionIsOpened.get()) {
                synchronized (proposals) {
                    proposals.clear();
                }
                return;
            }
            final IndexRange selection = selectedText.get();
            final Collection<CompletionProposal> proposals = computeCompletionProposals(
                    selection.getStart(),
                    selection.getLength());
            synchronized (this.proposals) {
                this.proposals.setAll(proposals);
            }
        }
    }

    private Collection<CompletionProposal> computeCompletionProposals(final int offset, final int selectedTextLength) {
        final Collection<CompletionProposal> proposals = dslFacade.getProposals(offset, selectedTextLength, MAX_PROPOSALS);
        if (proposals.isEmpty()) {
            synchronized (dslFacade) {
                synchronized (this.proposals) {
                    this.proposals.clear();
                }
            }
        }
        return proposals;
    }

    private void computeErrors() {
        final StringBuilder errors = new StringBuilder();
        if (dslFacade.getParseErrors().isEmpty()) {
            dslFacade.getValidationErrors().getChildren().forEach(diagnostic ->
                    errors.append(diagnostic.getMessage()).append('\n'));
            errors.append(dslFacade.getAnalysisErrors());
        } else {
            dslFacade.getParseErrors().forEach(syntaxFault ->
                    errors.append(syntaxFault.getMessage())
                            .append(" (")
                            .append(syntaxFault.getLine())
                            .append(':')
                            .append(syntaxFault.getColumn())
                            .append(')'));
        }
        ltlErrors.setValue(errors.toString());
    }

    private void computeNewCaretPosition() {
        caretPosition.set(change.getPosition() + change.getInserted().length());
    }

    private void computeNewContent() {
        final StringBuilder newContentBuilder = new StringBuilder(content.get());
        newContentBuilder.replace(change.getPosition(),
                change.getPosition() + change.getRemoved().length(),
                change.getInserted());
        content.set(newContentBuilder.toString());
    }

    private void parseAndHighlight() {
        synchronized (dslFacade) {
            dslFacade.parse(content.get());
            if (isBlank(content.get())) {
                styleSpans.set(noHighlighting());
            } else {
                styleSpans.set(computeHighlighting(content.get()));
            }
        }
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(final CharSequence text) {
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastStyleEnd = 0;
        for (final StylesPosition stylesPosition : dslFacade.getHighlightingStyles()) {
            spansBuilder.add(EMPTY_LIST, stylesPosition.getOffset() - lastStyleEnd);
            final Collection<String> highlightings = newArrayList();
            for (final String style : stylesPosition.getStyles()) {
                switch (style) {
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
                    case ENUM_STYLE:
                        highlightings.add("enum");
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

    public void save() {
        if (validate()) {
            startRunnableInBackground(() -> {
                if (editedFilter == null) {
                    FDIT_MANAGER.getCommandExecutor().executePreCommand(
                            new LTLFilterCreationCommand(FDIT_MANAGER.getRoot(), name.get(), id));
                }
                FDIT_MANAGER.getCommandExecutor().execute(new FilterEditionCommand(
                        editedFilter,
                        getName(),
                        getDescription(),
                        getContent()));
            });
        }
    }

    IntegerProperty caretPositionProperty() {
        return caretPosition;
    }

    public Recording getSelectedRecording() {
        return selectedRecording.get();
    }

    public ObservableList<Aircraft> getSelectedAircrafts() {
        return selectedAircrafts;
    }

    ObjectProperty<Recording> selectedRecordingProperty() {
        return selectedRecording;
    }

    BooleanProperty evaluateButtonDisabledProperty() {
        return evaluateButtonDisabled;
    }

    ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpansProperty() {
        return styleSpans;
    }

    public PlainTextChange getChange() {
        return change;
    }

    public void setChange(final PlainTextChange change) {
        this.change = change;
    }

    public String getDescription() {
        return description.get();
    }

    ObservableList<Recording> getSelectableRecordings() {
        return selectableRecordings;
    }

    StringProperty getLtlErrors() {
        return ltlErrors;
    }

    ObservableList<CompletionProposal> getProposals() {
        synchronized (proposals) {
            return proposals;
        }
    }

    public UUID getId() {
        return id;
    }

    public interface FilterEditionModelListener {
        void tabFocused();
    }
}
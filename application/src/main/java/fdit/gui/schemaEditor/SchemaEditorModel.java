package fdit.gui.schemaEditor;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.dsl.ide.CompletionProposal;
import fdit.dsl.ide.StylesPosition;
import fdit.dsl.ide.SyntaxFault;
import fdit.gui.application.FditManager;
import fdit.gui.application.FditManagerListener;
import fdit.gui.schemaEditor.schemaInterpretation.SchemaInterpreter;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeIntegerProperty;
import fdit.gui.utils.ThreadSafeObjectProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.FditHistoryListener;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
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
import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.dsl.ide.HighlightingStyles.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.utils.FXUtils.startRunnableInBackground;
import static fdit.metamodel.element.DirectoryUtils.gatherAllRecordings;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.alteration.AlterationSpecificationConverter.convertAlterationToIncident;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class SchemaEditorModel {

    public static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaEditorModel.class);

    private static final int MAX_PROPOSALS = 50;
    private static final IndexRange EMPTY_RANGE = new IndexRange(0, 0);

    private final SchemaInterpreter interpreter;
    private final Schema schema;
    private final AttackScenarioFacade dslFacade;
    private final ObservableList<Recording> selectableRecordings = observableArrayList();
    private final ObjectProperty<Recording> selectedRecording = new ThreadSafeObjectProperty<>(EMPTY_RECORDING);
    private final StringProperty text = new ThreadSafeStringProperty("");
    private final ObjectProperty<IndexRange> selectedText = new SimpleObjectProperty<>(EMPTY_RANGE);
    private final IntegerProperty caretPosition = new ThreadSafeIntegerProperty(0);
    private final ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpans = new SimpleObjectProperty<>(null);
    private final ObservableList<CompletionProposal> proposals = observableArrayList();
    private final BooleanProperty completionIsOpened = new ThreadSafeBooleanProperty(false);
    private final BooleanProperty saveDisable = new ThreadSafeBooleanProperty(true);
    private final BooleanProperty interpretDisable = new ThreadSafeBooleanProperty(true);
    private final StringProperty errors = new ThreadSafeStringProperty("");
    private final BooleanProperty hasError = new ThreadSafeBooleanProperty(true);
    private String editedText;
    private SchemaEditionCommand lastEditionCommand;
    private PlainTextChange change;
    private ListenerHandle fditManagerListenerHandle;
    private Collection<ListenerHandle> listenerHandles = newArrayList();

    public SchemaEditorModel(final Schema schema,
                             final AttackScenarioFacade dslFacade) {
        this.schema = schema;
        this.dslFacade = dslFacade;
        FDIT_MANAGER.getCommandExecutor().getHistory().addListener(codeCommandsListener());
        interpreter = new SchemaInterpreter(dslFacade);
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

    private static int computeNewCaretPosition(final PlainTextChange contentChange) {
        return contentChange.getPosition() + contentChange.getInserted().length();
    }

    public void initialize() {
        editedText = schema.getContent();
        text.set(schema.getContent());
        initializeListenersProperties();
        selectedRecording.setValue(schema.getRecording());
        selectableRecordings.add(EMPTY_RECORDING);
        selectableRecordings.addAll(gatherAllRecordings(FDIT_MANAGER.getRoot()));
        interpreter.initialize();
        parseAndHighlight();
        setCaretPosition(schema.getContent().length());
        computeErrors();
        updateScenarioIsSavable();
    }

    private void computeErrors() {
        final StringBuilder errors = new StringBuilder();
        if (dslFacade.getParseErrors().isEmpty()) {
            try {
                errors.append(interpreter.getSemanticErrors(schema));
            } catch (IllegalStateException ise) {
                errors.append(ise.getMessage());
            }
        } else {
            dslFacade.getParseErrors().forEach(syntaxFault ->
                    errors.append(syntaxFault.getMessage())
                            .append(" (")
                            .append(syntaxFault.getLine())
                            .append(':')
                            .append(syntaxFault.getColumn())
                            .append(')'));
        }
        this.errors.setValue(errors.toString());
        hasError.setValue(!errors.toString().isEmpty());
    }

    public void close() {
        fditManagerListenerHandle.detach();
        fditManagerListenerHandle = null;
        listenerHandles.forEach(ListenerDetachHandle::detach);
        listenerHandles.clear();
        interpreter.shutdown();
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public Property<Boolean> hasError() {
        return hasError;
    }

    public void setSelectedText(final IndexRange selectedText) {
        this.selectedText.set(selectedText);
    }

    public BooleanProperty getSaveDisable() {
        return saveDisable;
    }

    public int getCaretPosition() {
        return caretPosition.get();
    }

    public void setCaretPosition(final int caretPosition) {
        setCaretPosition(caretPosition, new IndexRange(caretPosition, caretPosition));
    }

    public void setCaretPosition(final int caretPosition, final IndexRange selection) {
        if (caretPosition != getCaretPosition()) {
            this.caretPosition.set(caretPosition);
            setSelectedText(selection);
            startRunnableInBackground(this::updateProposals);
        }
    }

    private boolean hasDataChanged() {
        return !editedText.equals(text.get());
    }

    public IntegerProperty caretPositionProperty() {
        return caretPosition;
    }

    public ObjectProperty<StyleSpans<? extends Collection<String>>> styleSpansProperty() {
        return styleSpans;
    }

    public void setCompletionIsOpened(final boolean completionIsOpened) {
        this.completionIsOpened.set(completionIsOpened);
        startRunnableInBackground(this::updateProposals);
    }

    public ObservableList<CompletionProposal> getProposals() {
        synchronized (proposals) {
            return proposals;
        }
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement elementAdded) {
                if (elementAdded instanceof Recording) {
                    selectableRecordings.add((Recording) elementAdded);
                }
            }

            @Override
            public void elementRemoved(final FditElement elementRemoved) {
                if (elementRemoved instanceof Recording) {
                    selectableRecordings.remove(elementRemoved);
                }
            }

            @Override
            public void elementEdited(final FditElement editedElement) {
                if (editedElement instanceof Recording) {
                    selectableRecordings.remove(editedElement);
                    selectableRecordings.add((Recording) editedElement);
                }
            }
        };
    }

    public void save() {
        registerSave();
        editedText = schema.getContent();
        updateScenarioIsSavable();
    }

    public Collection<File> interpret() {
        final Collection<File> incidentFiles = newArrayList();
        if (hasError.getValue().equals(FALSE) && schema.getRecording() != EMPTY_RECORDING) {
            int i = 0;
            for (final AlterationSpecification specification : interpreter.extractSpecifications(schema)) {
                incidentFiles.add(convertAlterationToIncident(
                        specification,
                        selectedRecording.get(),
                        i++,
                        FDIT_MANAGER.getRootFile()));
            }
            interpreter.clear();
        }
        return incidentFiles;
    }

    Optional<String> getErrorAtOffset(final int offset) {
        for (final SyntaxFault syntaxFault : dslFacade.getParseErrors()) {
            final int errorEndOffset = syntaxFault.getOffset() + syntaxFault.getLength();
            if (offset >= syntaxFault.getOffset() && offset <= errorEndOffset) {
                return of(syntaxFault.getMessage());
            }
        }
        return empty();
    }

    private FditHistoryListener codeCommandsListener() {
        return new FditHistoryListener() {
            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                if (command instanceof SchemaEditionCommand &&
                        ((SchemaEditionCommand) command).getSubject() == schema) {
                    requestUndoRedoTextChange(((SchemaEditionCommand) command).getContentChange().invert());
                }
                if (command instanceof SchemaSaveCommand) {
                    editedText = ((SchemaSaveCommand) command).getOldContent();
                    updateScenarioIsSavable();
                }
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                if (command instanceof SchemaEditionCommand &&
                        ((SchemaEditionCommand) command).getSubject() == schema) {
                    requestUndoRedoTextChange(((SchemaEditionCommand) command).getContentChange());
                }
                if (command instanceof SchemaSaveCommand) {
                    editedText = schema.getContent();
                    updateScenarioIsSavable();
                }
            }

            private void requestUndoRedoTextChange(final PlainTextChange contentChange) {
                lastEditionCommand = null;
                text.set(schema.getContent());
                onTextChanged(contentChange);
            }
        };
    }

    public void requestTextChange(final PlainTextChange change) {
        this.change = change;
        registerChange();
        onTextChanged(change);
    }

    private void onTextChanged(final PlainTextChange change) {
        final int newCaretPosition = computeNewCaretPosition(change);
        caretPosition.set(newCaretPosition);
        setSelectedText(new IndexRange(newCaretPosition, newCaretPosition));
        startRunnableInBackground(this::refresh);
    }

    private void refresh() {
        parseAndHighlight();
        updateProposals();
        computeErrors();
        updateScenarioIsSavable();
        updateScenarioIsInterpretable();
    }

    private void updateScenarioIsSavable() {
        saveDisable.set(!hasDataChanged());
    }

    private void registerChange() {
        if (lastEditionCommand == null || !lastEditionCommand.mergeTextChangeWith(change)) {
            final SchemaEditionCommand schemaEditionCommand =
                    new SchemaEditionCommand(schema, change);
            lastEditionCommand = schemaEditionCommand;
            FDIT_MANAGER.getCommandExecutor().execute(schemaEditionCommand);
        }
        text.set(schema.getContent());
    }

    private void registerSave() {
        final SchemaSaveCommand scenarioEditionCommand =
                new SchemaSaveCommand(schema, editedText);
        FDIT_MANAGER.getCommandExecutor().execute(scenarioEditionCommand);
    }

    private void parseAndHighlight() {
        synchronized (dslFacade) {
            interpreter.parseScenario(schema);
            if (isBlank(getText())) {
                styleSpans.set(noHighlighting());
            } else {
                styleSpans.set(computeHighlighting(getText()));
            }
        }
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
            synchronized (this.proposals) {
                this.proposals.setAll(computeCompletionProposals(selection.getStart(), selection.getLength()));
            }
        }
    }

    private Collection<CompletionProposal> computeCompletionProposals(final int offset, final int selectedTextLength) {
        final Collection<CompletionProposal> completionProposals =
                dslFacade.getProposals(offset, selectedTextLength, MAX_PROPOSALS);
        if (completionProposals.isEmpty()) {
            synchronized (dslFacade) {
                synchronized (this.proposals) {
                    this.proposals.clear();
                }
            }
            completionProposals.addAll(interpreter.getProposals(schema, offset, selectedTextLength));
        }
        return completionProposals;
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(final CharSequence text) {
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastStyleEnd = 0;
        for (final StylesPosition stylesPosition : dslFacade.getHighlightingStyles()) {
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

    private void initializeListenersProperties() {
        listenerHandles.add(createAttached(selectedRecording, observable -> {
            schema.setRecording(selectedRecording.get());
            refresh();
        }));
        listenerHandles.add(createAttached(hasError, observable -> updateScenarioIsInterpretable()));
    }

    private void updateScenarioIsInterpretable() {
        interpretDisable.set(hasError.get() || selectedRecording.get() == EMPTY_RECORDING);
    }

    public ObservableList<Recording> getSelectableRecordings() {
        return selectableRecordings;
    }

    public Schema getSchema() {
        return schema;
    }

    public StringProperty getErrors() {
        return errors;
    }

    public BooleanProperty interpretDisableProperty() {
        return interpretDisable;
    }

    public boolean isInterpretDisable() {
        return interpretDisable.get();
    }

    public ObjectProperty<Recording> selectedRecordingProperty() {
        return selectedRecording;
    }
}
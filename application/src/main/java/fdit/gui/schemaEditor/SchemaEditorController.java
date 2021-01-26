package fdit.gui.schemaEditor;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.dsl.ide.CompletionProposal;
import fdit.gui.EditorController;
import fdit.gui.utils.CustomCompletionPopupSkin;
import fdit.gui.utils.UpdateableComboBox;
import fdit.gui.utils.binding.BindingHandle;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.codefx.libfx.listener.handle.ListenerDetachHandle;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.fxmisc.flowless.ScaledVirtualized;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.PlainTextChange;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static com.google.inject.internal.util.$Lists.newArrayList;
import static fdit.gui.Images.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.utils.FXUtils.*;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.time.Instant.now;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;
import static org.fxmisc.richtext.MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.Nodes.addInputMap;

public class SchemaEditorController implements EditorController {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaEditorController.class);
    private static final CompletionProposal NO_PROPOSAL = new CompletionProposal("",
            "",
            TRANSLATOR.getMessage("noProposal"),
            -1);
    private final ComboBox<Recording> recordingsComboBox = new UpdateableComboBox<>();
    private final Button interpretationButton = new Button();
    private final Label applyToLabel = new Label();
    private final Button saveButton = new Button();
    private final ToolBar toolBar = new ToolBar();
    private final CodeArea codeArea = new CodeArea();
    private final ScaledVirtualized<CodeArea> codeAreaZoom = new ScaledVirtualized<>(codeArea);
    private final Popup errorPopup = new Popup();
    private final TextArea errorArea = new TextArea();
    private final AutoCompletePopup<CompletionProposal> completionPopup = new AutoCompletePopup<>();
    private final Schema schema;
    private final SchemaEditorModel model;
    @FXML
    private BorderPane schemaEditorPane;
    private String errorPopupStylesheetUrl;
    private String completionPopupStylesheetUrl;
    private Collection<BindingHandle> bindingHandles = newArrayList();
    private Collection<ListenerHandle> listenerHandles = newArrayList();

    public SchemaEditorController(final Schema schema) {
        this.schema = schema;
        model = new SchemaEditorModel(schema, new AttackScenarioFacade());
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
    }

    @Override
    public void initialize() {
        errorPopupStylesheetUrl = SchemaEditorController.class.getResource("errorPopup.css").toExternalForm();
        completionPopupStylesheetUrl = SchemaEditorController.class.getResource("completionPopup.css").toExternalForm();

        initializeToolbar();
        initializeErrorArea();
        initializeCodeArea();
        initializeCompletionPopup();
        initializeTexts();
        initializeImages();
        try {
            model.initialize();
        } catch (final Exception e) {
            createErrorDialog(TRANSLATOR.getMessage("error.parsingFailed", schema.getName()), e).show();
            //TODO close editor
        }
        initializeRecordingsComboBox();
        updateCompletionSuggestions();
    }

    private void initializeRecordingsComboBox() {
        final Callback<ListView<Recording>, ListCell<Recording>> cellFactory =
                new Callback<ListView<Recording>, ListCell<Recording>>() {
                    @Override
                    public ListCell<Recording> call(final ListView<Recording> param) {
                        return new ListCell<Recording>() {
                            protected void updateItem(final Recording recording, final boolean empty) {
                                super.updateItem(recording, empty);
                                final String text;
                                if (recording == null || empty) {
                                    text = null;
                                } else if (recording == EMPTY_RECORDING) {
                                    text = TRANSLATOR.getMessage("comboBox.noRecording");
                                } else {
                                    text = recording.getName();
                                }
                                Platform.runLater(() -> {
                                    setGraphic(new ImageView(getRecordingIcon(recording)));
                                    setText(text);
                                });
                            }
                        };
                    }
                };
        recordingsComboBox.setCellFactory(cellFactory);
        recordingsComboBox.setButtonCell(cellFactory.call(null));
        bindContent(recordingsComboBox.getItems(), model.getSelectableRecordings());
        listenerHandles.add(
                createAttached(recordingsComboBox.getSelectionModel().selectedItemProperty(),
                        (observable, oldValue, newValue) -> {
                            if (!newValue.equals(EMPTY_RECORDING) && !newValue.isLoaded())
                                startRunnableInBackground(() -> {
                                    synchronized (this) {
                                        newValue.load();
                                        model.selectedRecordingProperty().set(newValue);
                                    }
                                });
                            else model.selectedRecordingProperty().set(newValue);
                        })
        );
        if (!(model.selectedRecordingProperty().get() == null || model.selectedRecordingProperty().get().equals(EMPTY_RECORDING))) {
            recordingsComboBox.getSelectionModel().select(model.selectedRecordingProperty().get());
        } else
            recordingsComboBox.getSelectionModel().select(EMPTY_RECORDING);
    }

    private void initializeTexts() {
        interpretationButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("interpret")));
        saveButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("save")));
        applyToLabel.setText(TRANSLATOR.getMessage("label.applyTo"));
    }

    private void initializeImages() {
        interpretationButton.setGraphic(new ImageView(GEAR));
        saveButton.setGraphic(new ImageView(SAVE));
    }

    @Override
    public void onBackground() {
        completionPopup.hide();
        errorPopup.hide();
    }

    @Override
    public void onClose() {
        clearInnerListeners();
        model.close();
        completionPopup.hide();
        errorPopup.hide();
        //TODO #68 remove listeners
    }

    private void clearInnerListeners() {
        bindingHandles.forEach(BindingHandle::unbind);
        bindingHandles.clear();
        listenerHandles.forEach(ListenerDetachHandle::detach);
        listenerHandles.clear();
    }

    @Override
    public void requestFocus() {
        codeArea.requestFocus();
    }

    private void initializeToolbar() {
        interpretationButton.setDisable(model.isInterpretDisable());
        interpretationButton.disableProperty().bindBidirectional(model.interpretDisableProperty());
        interpretationButton.setOnMouseClicked(event -> interpretScenario());
        saveButton.setOnMouseClicked(event -> saveModel());
        saveButton.setDisable(!model.hasError().getValue());
        saveButton.disableProperty().bindBidirectional(model.getSaveDisable());
        toolBar.getItems().addAll(
                saveButton,
                interpretationButton,
                new Separator(),
                applyToLabel,
                recordingsComboBox);
        schemaEditorPane.setTop(toolBar);
    }

    private void initializeErrorArea() {
        errorArea.setEditable(false);
        errorArea.setStyle("-fx-text-fill: red;");
        errorArea.textProperty().bindBidirectional(model.getErrors());
        schemaEditorPane.setBottom(errorArea);
    }

    private void initializeCodeArea() {
        model.textProperty().addListener((observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> {
                    if (!model.getText().equals(codeArea.getText())) {
                        codeArea.replaceText(newValue);
                    }
                }));
        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (codeArea.getSelection().getLength() == 0) {
                model.setCaretPosition(newValue);
            } else {
                model.setCaretPosition(newValue, codeArea.getSelection());
            }
        });
        model.caretPositionProperty().addListener((observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> codeArea.displaceCaret(newValue.intValue())));
        model.styleSpansProperty().addListener((observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> codeArea.setStyleSpans(0, newValue)));
        codeArea.getContent().richChanges().map(change -> new PlainTextChange(change.getPosition(),
                change.getRemoved().getText(),
                change.getInserted().getText()))
                .filter(pc -> !pc.getRemoved().equals(pc.getInserted()) &&
                        !model.getText().equals(codeArea.getText()))
                .subscribe(model::requestTextChange);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setMouseOverTextDelay(Duration.ofMillis(500));
        codeArea.addEventHandler(MOUSE_OVER_TEXT_BEGIN, event -> {
            final int offset = event.getCharacterIndex();
            final Optional<Bounds> characterBounds = codeArea.getCharacterBoundsOnScreen(offset, offset + 1);
            characterBounds.ifPresent(bounds -> displayErrorPopup(offset, bounds.getMinX(), bounds.getMaxY()));
        });
        addCodeAreaKeyListeners();
        schemaEditorPane.setCenter(codeAreaZoom);
    }

    private void addCodeAreaKeyListeners() {
        addInputMap(codeArea, consume(keyPressed(Z, CONTROL_DOWN), event -> FDIT_MANAGER.getCommandExecutor().undo()));
        addInputMap(codeArea, consume(keyPressed(Y, CONTROL_DOWN), event -> FDIT_MANAGER.getCommandExecutor().redo()));
        //useful to avoid Menu selected on ALT+TAB per example
        addInputMap(codeArea, consume(keyPressed(keyCode -> true, ALT_DOWN)));
        codeArea.setOnKeyPressed(event -> {
            errorPopup.hide();
            if (event.getCode() == F1 && event.isControlDown()) {
                final Optional<Bounds> caretScreenPosition = codeArea.getCaretBounds();
                caretScreenPosition.ifPresent(bounds ->
                        displayErrorPopup(codeArea.getCaretPosition(), bounds.getMinX(), bounds.getMaxY()));
            }
            if (event.getCode() == SPACE && event.isControlDown()) {
                displayCompletionPopup();
            }
        });
        codeArea.addEventFilter(ScrollEvent.ANY, e -> {
            if (e.isControlDown()) {
                final double scaleAmount = 0.9;
                final Scale zoom = codeAreaZoom.getZoom();
                double scale = e.getDeltaY() < 0 ? zoom.getY() * scaleAmount : zoom.getY() / scaleAmount;
                zoom.setX(scale);
                zoom.setY(scale);
            }
        });
    }

    private void initializeCompletionPopup() {
        model.getProposals().addListener((ListChangeListener<CompletionProposal>) change ->
                startRunnableInUIThread(this::updateCompletionPopup));
        completionPopup.showingProperty().addListener((observable, oldValue, newValue) ->
                model.setCompletionIsOpened(newValue));
        completionPopup.setConverter(new StringConverter<CompletionProposal>() {
            @Override
            public String toString(final CompletionProposal proposal) {
                return proposal.getLabel();
            }

            @Override
            public CompletionProposal fromString(final String s) {
                return null;
            }
        });
        completionPopup.setOnSuggestion(suggestionEvent -> {
            final CompletionProposal proposal = suggestionEvent.getSuggestion();
            if (proposal == NO_PROPOSAL) {
                return;
            }
            final int caretPosition = codeArea.getCaretPosition();
            final int selectedTextLength = codeArea.getSelectedText().length();
            if (selectedTextLength == 0) {
                codeArea.replaceText(caretPosition - proposal.getPrefix().length(),
                        caretPosition, proposal.getProposal());
            } else {
                final int selectionStart = codeArea.getSelection().getStart();
                codeArea.deselect();
                codeArea.replaceText(selectionStart, selectionStart + selectedTextLength, proposal.getProposal());
            }
            model.setCaretPosition(proposal.getNextCaretPosition());
            codeArea.requestFollowCaret();
        });
        completionPopup.setSkin(new CustomCompletionPopupSkin<>(completionPopup));
    }

    private void displayCompletionPopup() {
        final Scene scene = codeArea.getScene();
        if (!scene.getStylesheets().contains(completionPopupStylesheetUrl)) {
            scene.getStylesheets().add(completionPopupStylesheetUrl);
        }
        updateCompletionPopupPosition();
        completionPopup.show(codeArea.getScene().getWindow());
    }

    private void updateCompletionPopup() {
        updateCompletionPopupPosition();
        updateCompletionSuggestions();
    }

    private void updateCompletionPopupPosition() {
        final Optional<Bounds> caretScreenPosition = codeArea.getCaretBounds();
        if (caretScreenPosition.isPresent()) {
            completionPopup.setX(caretScreenPosition.get().getMinX());
            completionPopup.setY(caretScreenPosition.get().getMaxY());
        }
    }

    private synchronized void updateCompletionSuggestions() {
        final Collection<CompletionProposal> completionProposals = model.getProposals();
        if (completionProposals.isEmpty()) {
            completionPopup.getSuggestions().setAll(NO_PROPOSAL);
        } else {
            completionPopup.getSuggestions().setAll(completionProposals);
        }
        selectFirstCompletionProposal();
    }

    private void selectFirstCompletionProposal() {
        getCompletionListView().getSelectionModel().selectFirst();
    }

    private ListView getCompletionListView() {
        return (ListView) completionPopup.getSkin().getNode();
    }

    private void displayErrorPopup(final int offset, final double x, final double y) {
        final Optional<String> errorMessage = model.getErrorAtOffset(offset);
        if (errorMessage.isPresent()) {
            completionPopup.hide();
            errorPopup.getContent().clear();
            errorPopup.getContent().add(createErrorPopupContent(errorMessage.get()));
            errorPopup.setX(x);
            errorPopup.setY(y);
            errorPopup.show(codeArea.getScene().getWindow());
            errorPopup.setAutoHide(true);
            errorPopup.setHideOnEscape(true);
            errorPopup.setAutoFix(true);
        }
    }

    private Node createErrorPopupContent(final String errorMessage) {
        final Scene scene = codeArea.getScene();
        if (!scene.getStylesheets().contains(errorPopupStylesheetUrl)) {
            scene.getStylesheets().add(errorPopupStylesheetUrl);
        }
        final Pane errorPopupContent = new VBox();
        errorPopupContent.getChildren().add(new Label(errorMessage));
        errorPopupContent.getStyleClass().add("error-popup");
        return errorPopupContent;
    }

    private void saveModel() {
        try {
            model.save();
        } catch (final Exception e) {
            createErrorDialog(TRANSLATOR.getMessage("error.saveScenarioFailed", schema.getName()), e).show();
        }
    }

    private void interpretScenario() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(TRANSLATOR.getMessage("selectDestination"));
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final File destination = directoryChooser.showDialog(schemaEditorPane.getScene().getWindow());
        final File subDestination = new File(destination,
                "incidents_" + schema.getName() + "_" + dateFormat.format(Date.from(now())));
        if (subDestination.mkdir() && destination.exists() && destination.isDirectory()) {
            try {
                for (final File incidentFile : model.interpret()) {
                    copyFileToDirectory(incidentFile, subDestination);
                    incidentFile.delete();
                }
            } catch (final Exception e) {
                createErrorDialog(TRANSLATOR.getMessage("error.saveScenarioFailed", schema.getName()), e).show();
            }
        }
    }
}
package fdit.gui.triggerEditor;

import fdit.gui.EditorController;
import fdit.gui.utils.CustomCompletionPopupSkin;
import fdit.gui.utils.UpdateableComboBox;
import fdit.metamodel.aircraft.TimeInterval;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;
import fdit.triggcondition.ide.CompletionProposal;
import fdit.triggcondition.ide.TriggeringConditionFacade;
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
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.codefx.libfx.listener.handle.ListenerDetachHandle;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.fxmisc.flowless.ScaledVirtualized;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.PlainTextChange;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.Images.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.utils.FXUtils.createErrorDialog;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Double.parseDouble;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;
import static org.fxmisc.richtext.MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.Nodes.addInputMap;

public class ActionTriggerEditorController implements EditorController {

    static final LTLFilter EMPTY_FILTER = new LTLFilter("", randomUUID(), "", "");
    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ActionTriggerEditorController.class);
    private static final CompletionProposal NO_PROPOSAL = new CompletionProposal("",
            "",
            TRANSLATOR.getMessage("noProposal"),
            -1);
    private final Button interpretationButton = new Button();
    private final Button saveButton = new Button();
    private final ToolBar toolBar = new ToolBar();
    private final CodeArea codeArea = new CodeArea();
    private final ScaledVirtualized<CodeArea> codeAreaZoom = new ScaledVirtualized<>(codeArea);
    private final Popup errorPopup = new Popup();
    private final TextArea errorArea = new TextArea();
    private final ComboBox<Recording> recordingsComboBox = new UpdateableComboBox<>();
    private final ComboBox<LTLFilter> filtersComboBox = new UpdateableComboBox<>();
    private final Label applyToLabel = new Label();
    private final Label withinInterval = new Label();
    private final CheckBox withinIntervalCB = new CheckBox();
    private final TextField startInterval = new TextField();
    private final Label dotInterval = new Label();
    private final TextField endInterval = new TextField();
    private final Label closingInterval = new Label();
    private final Label filterLabel = new Label();
    private final AutoCompletePopup<CompletionProposal> completionPopup = new AutoCompletePopup<>();
    private final ActionTrigger trigger;
    private final ActionTriggerEditorModel model;
    @FXML
    private BorderPane triggerEditorPane;
    @FXML
    private ScrollPane timeLineContainer;
    private String errorPopupStylesheetUrl;
    private String completionPopupStylesheetUrl;
    private Collection<ListenerHandle> listenerHandles = newArrayList();

    public ActionTriggerEditorController(final ActionTrigger trigger) {
        this.trigger = trigger;
        model = new ActionTriggerEditorModel(trigger, TriggeringConditionFacade.get());
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
    }

    @Override
    public void initialize() {
        initializeToolbar();
        initializeErrorArea();
        initializeCodeArea();
        initializeCompletionPopup();
        initializeTexts();
        initializeImages();
        try {
            model.initialize();
        } catch (final Exception e) {
            createErrorDialog(TRANSLATOR.getMessage("error.parsingFailed", trigger.getName()), e).show();
            //TODO close editor
        }
        errorPopupStylesheetUrl = ActionTriggerEditorController.class.getResource("errorPopup.css").toExternalForm();
        completionPopupStylesheetUrl = ActionTriggerEditorController.class.getResource("completionPopup.css").toExternalForm();
        initializeRecordingsComboBox();
        initializeFiltersComboBox();
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
        listenerHandles.add(createAttached(recordingsComboBox.getSelectionModel().selectedItemProperty(),
                (observable, oldValue, newValue) -> model.selectedRecordingProperty().set(newValue)));
        recordingsComboBox.getSelectionModel().select(0);
    }

    private void initializeFiltersComboBox() {
        final Callback<ListView<LTLFilter>, ListCell<LTLFilter>> cellFactory =
                new Callback<ListView<LTLFilter>, ListCell<LTLFilter>>() {
                    @Override
                    public ListCell<LTLFilter> call(final ListView<LTLFilter> param) {
                        return new ListCell<LTLFilter>() {
                            protected void updateItem(final LTLFilter filter, final boolean empty) {
                                super.updateItem(filter, empty);
                                final String text;
                                if (filter == null || empty) {
                                    text = null;
                                } else if (filter == EMPTY_FILTER) {
                                    text = TRANSLATOR.getMessage("comboBox.noFilter");
                                } else {
                                    text = filter.getName();
                                }
                                Platform.runLater(() -> {
                                    setGraphic(new ImageView(LTLFILTER_ICON));
                                    setText(text);
                                });
                            }
                        };
                    }
                };
        filtersComboBox.setCellFactory(cellFactory);
        filtersComboBox.setButtonCell(cellFactory.call(null));
        bindContent(filtersComboBox.getItems(), model.getSelectableFilters());
        listenerHandles.add(createAttached(filtersComboBox.getSelectionModel().selectedItemProperty(),
                (observable, oldValue, newValue) -> model.selectedFilterProperty().set(newValue)));
        filtersComboBox.getSelectionModel().select(0);
    }

    private void initializeTexts() {
        interpretationButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("button.interpret")));
        saveButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("button.save")));
        applyToLabel.setText(TRANSLATOR.getMessage("label.applyTo"));
        filterLabel.setText(TRANSLATOR.getMessage("label.filter"));
        withinInterval.setText(TRANSLATOR.getMessage("label.withinInterval"));
        dotInterval.setText(" - ");
        closingInterval.setText("]");
    }

    private void initializeImages() {
        interpretationButton.setGraphic(new ImageView(EXECUTE));
        saveButton.setGraphic(new ImageView(SAVE));
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

    private void updateCompletionPopup() {
        updateCompletionPopupPosition();
        updateCompletionSuggestions();
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
        triggerEditorPane.setCenter(codeAreaZoom);
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

    private void displayCompletionPopup() {
        final Scene scene = codeArea.getScene();
        if (!scene.getStylesheets().contains(completionPopupStylesheetUrl)) {
            scene.getStylesheets().add(completionPopupStylesheetUrl);
        }
        updateCompletionPopupPosition();
        completionPopup.show(codeArea.getScene().getWindow());
    }

    private void displayErrorPopup(int caretPosition, double x, double y) {
        final Optional<String> errorMessage = model.getErrorAtOffset(caretPosition);
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

    private void updateCompletionPopupPosition() {
        final Optional<Bounds> caretScreenPosition = codeArea.getCaretBounds();
        if (caretScreenPosition.isPresent()) {
            completionPopup.setX(caretScreenPosition.get().getMinX());
            completionPopup.setY(caretScreenPosition.get().getMaxY());
        }
    }

    private void initializeErrorArea() {
        errorArea.setEditable(false);
        errorArea.setPrefRowCount(3);
        errorArea.setStyle("-fx-text-fill: red;");
        errorArea.textProperty().bindBidirectional(model.getErrors());
        triggerEditorPane.setBottom(errorArea);
    }

    private void initializeToolbar() {
        saveButton.setOnMouseClicked(event -> saveModel());
        saveButton.setDisable(!model.hasError().getValue());
        saveButton.disableProperty().bindBidirectional(model.getSaveDisable());
        interpretationButton.setOnMouseClicked(event -> {
                    model.interpretTrigger().ifPresent(aircraftCollectionHashMap ->
                            timeLineContainer.setContent(new TriggerTimeline(
                                            aircraftCollectionHashMap,
                                            model.getSelectedRecording(),
                                            new TimeInterval((long) parseDouble(startInterval.getText()) * 1000,
                                                    (long) parseDouble(endInterval.getText()) * 1000)
                                    )
                            )
                    );
                    saveModel();
                }

        );
        withinIntervalCB.selectedProperty().bindBidirectional(model.withinIntervalIsChecked());
        interpretationButton.disableProperty().bindBidirectional(model.interpretButtonDisabledProperty());
        startInterval.setPrefWidth(70);
        endInterval.setPrefWidth(70); //TODO TRIGGERPANEL: Resize based on maxRelativeDate of the chosen recording
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            final String input = change.getText();
            if (input.matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        };
        startInterval.disableProperty().bindBidirectional(model.timeIntervalDisabledProperty());
        startInterval.textProperty().bindBidirectional(model.getStartInterval());
        startInterval.setTextFormatter(new TextFormatter<String>(integerFilter));
        endInterval.disableProperty().bindBidirectional(model.timeIntervalDisabledProperty());
        endInterval.textProperty().bindBidirectional(model.getEndInterval());
        endInterval.setTextFormatter(new TextFormatter<String>(integerFilter));
        toolBar.getItems().addAll(
                saveButton,
                interpretationButton,
                new Separator(),
                applyToLabel,
                recordingsComboBox,
                filterLabel,
                filtersComboBox,
                withinIntervalCB,
                withinInterval,
                startInterval,
                dotInterval,
                endInterval,
                closingInterval
        );
        triggerEditorPane.setTop(toolBar);
    }

    private void saveModel() {
        try {
            model.save();
        } catch (final Exception e) {
            createErrorDialog(TRANSLATOR.getMessage("error.saveTriggerFailed", trigger.getName()), e).show();
        }
    }

    @Override
    public void requestFocus() {
        codeArea.requestFocus();
    }

    @Override
    public void onBackground() {
        completionPopup.hide();
        errorPopup.hide();
    }

    @Override
    public void onClose() {
        model.closeModel();
        completionPopup.hide();
        errorPopup.hide();
        clearInnerListeners();
    }

    private void clearInnerListeners() {
        listenerHandles.forEach(ListenerDetachHandle::detach);
        listenerHandles.clear();
    }
}
package fdit.gui.filterEditor.tabs.filter;

import fdit.gui.EditorController;
import fdit.gui.utils.CustomCompletionPopupSkin;
import fdit.gui.utils.RenamableTab;
import fdit.gui.utils.UpdateableComboBox;
import fdit.gui.utils.binding.BindingHandle;
import fdit.ltlcondition.ide.CompletionProposal;
import fdit.metamodel.recording.Recording;
import fdit.tools.i18n.MessageTranslator;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.fxmisc.flowless.ScaledVirtualized;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.PlainTextChange;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Sets.newTreeSet;
import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.Images.*;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;
import static fdit.gui.utils.binding.BindingHandles.createAttachBinding;
import static fdit.gui.utils.binding.BindingHandles.createAttachBindingBidirectional;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.String.valueOf;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.Nodes.addInputMap;

public class FilterEditionTabController implements EditorController {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FilterEditionTabController.class);

    private static final CompletionProposal NO_PROPOSAL = new CompletionProposal("",
            "",
            TRANSLATOR.getMessage("noProposal"),
            -1);
    private final Button saveButton = new Button();
    private final Button deleteButton = new Button();
    private final Button evaluateButton = new Button();
    private final ComboBox<Recording> recordingComboBox = new UpdateableComboBox<>();
    private final VBox aircraftsVBox = new VBox();
    private final Label aircraftsLabel = new Label();
    private final Label applyToLabel = new Label();
    private final CodeArea codeArea = new CodeArea();
    private final ScaledVirtualized<CodeArea> codeAreaZoom = new ScaledVirtualized<>(codeArea);
    private final ToolBar toolBar = new ToolBar();
    private final ScrollPane aircraftsScrollPane = new ScrollPane();
    private final ToolBar aircraftsBar = new ToolBar();
    private final TextArea errorArea = new TextArea();
    private final AutoCompletePopup<CompletionProposal> completionPopup = new AutoCompletePopup<>();
    private final RenamableTab filterEditionTab;
    private final FilterEditionModel model;
    private final Collection<ListenerHandle> listenerHandles = newArrayList();
    private final Collection<BindingHandle> bindingHandles = newArrayList();
    @FXML
    private BorderPane filterEditorPane;
    private String completionPopupStylesheetUrl;

    FilterEditionTabController(final RenamableTab filterEditionTab,
                               final FilterEditionModel model) {
        completionPopupStylesheetUrl = FilterEditionTabController.class.getResource("completionPopup.css")
                .toExternalForm();
        this.filterEditionTab = filterEditionTab;
        this.model = model;
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
        initializeImages();
    }

    @Override
    public void initialize() {
        model.initialize();
        initializeTexts();
        initializePropertiesBindings();
        initializeButtons();
        initializeCodeArea();
        initializeErrorArea();
        initializeCompletionPopup();
        initializeToolbars();
        initializeComboBox();
        filterEditionTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue) {
                model.setTabIsFocused(false);
            }
            if (newValue) {
                model.setTabIsFocused(true);
            }
        });
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
    }

    private void initializeComboBox() {
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
        recordingComboBox.setCellFactory(cellFactory);
        recordingComboBox.setButtonCell(cellFactory.call(null));
        bindContent(recordingComboBox.getItems(), model.getSelectableRecordings());
        listenerHandles.add(createAttached(recordingComboBox.getSelectionModel().selectedItemProperty(),
                (observable, oldValue, newValue) -> model.selectedRecordingProperty()
                        .set(newValue)));
        recordingComboBox.getSelectionModel().select(EMPTY_RECORDING);
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
            codeArea.requestFollowCaret();
        });
        completionPopup.setSkin(new CustomCompletionPopupSkin<>(completionPopup));
    }

    private void initializeToolbars() {
        toolBar.getItems().addAll(
                saveButton,
                deleteButton,
                evaluateButton,
                new Separator(),
                applyToLabel,
                recordingComboBox);
        filterEditorPane.setTop(toolBar);

        aircraftsScrollPane.setContent(aircraftsVBox);
        aircraftsBar.setOrientation(VERTICAL);
        aircraftsBar.getStyleClass().add("aircraftsBar");
        aircraftsLabel.getStyleClass().add("aircraftsLabel");
        aircraftsBar.getItems().addAll(
                aircraftsLabel,
                aircraftsScrollPane);
        filterEditorPane.setRight(aircraftsBar);
        model.getSelectedAircrafts().addListener((InvalidationListener) observable -> {
            aircraftsVBox.getChildren().clear();
            final Set<String> aircraftNames = newTreeSet();
            model.getSelectedAircrafts().forEach(aircraft -> {
                if (isNotBlank(aircraft.getCallSign())) {
                    aircraftNames.add(aircraft.getCallSign().toUpperCase());
                } else {
                    aircraftNames.add(aircraft.getStringICAO());
                }
            });
            aircraftNames.forEach(aircraftName -> aircraftsVBox.getChildren().add(new Label(aircraftName)));
            updateAircraftsLabel();
        });
    }

    @Override
    public void requestFocus() {
        codeArea.requestFocus();
    }

    @Override
    public void onClose() {
        completionPopup.hide();
    }

    @Override
    public void onBackground() {
        completionPopup.hide();
    }

    public void closeTab() {
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }

        for (final BindingHandle bindingHandle : bindingHandles) {
            bindingHandle.unbind();
        }
        bindingHandles.clear();
        listenerHandles.clear();
        model.closeModel();
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
        listenerHandles.add(createAttached(model.contentProperty(), (observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> {
                    if (!model.getContent().equals(codeArea.getText())) {
                        codeArea.replaceText(newValue);
                    }
                })));
        bindingHandles.add(createAttachBinding(
                model.selectedTextProperty(),
                codeArea.selectionProperty()));
        listenerHandles.add(createAttached(model.caretPositionProperty(), (observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> codeArea.displaceCaret(newValue.intValue()))));
        listenerHandles.add(createAttached(model.styleSpansProperty(), (observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> codeArea.setStyleSpans(0, newValue))));
        codeArea.getContent().richChanges().map(change -> new PlainTextChange(change.getPosition(),
                change.getRemoved().getText(),
                change.getInserted().getText()))
                .filter(pc -> !pc.getRemoved().equals(pc.getInserted()) &&
                        !model.getContent().equals(codeArea.getText()))
                .subscribe(model::requestTextChange);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setMouseOverTextDelay(Duration.ofMillis(500));
        codeArea.replaceText(model.getContent());
        if (codeArea.getContent().getLength() > 0) {
            codeArea.setStyleSpans(0, model.styleSpansProperty().get());
        }
        addCodeAreaKeyListeners();
        filterEditorPane.setCenter(codeAreaZoom);
    }

    private void initializeErrorArea() {
        errorArea.setEditable(false);
        errorArea.setStyle("-fx-text-fill: red;");
        errorArea.textProperty().bindBidirectional(model.getLtlErrors());
        errorArea.setPrefRowCount(5);
        filterEditorPane.setBottom(errorArea);
    }

    private void addCodeAreaKeyListeners() {
        //useful to avoid Menu selected on ALT+TAB per example
        addInputMap(codeArea, consume(keyPressed(keyCode -> true, ALT_DOWN)));
        codeArea.setOnKeyPressed(event -> {
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

    private void updateCompletionPopupPosition() {
        final Optional<Bounds> caretScreenPosition = codeArea.getCaretBounds();
        if (caretScreenPosition.isPresent()) {
            completionPopup.setX(caretScreenPosition.get().getMinX());
            completionPopup.setY(caretScreenPosition.get().getMaxY());
        }
    }

    private void initializePropertiesBindings() {
        bindingHandles.add(createAttachBindingBidirectional(
                filterEditionTab.nameProperty(),
                model.nameProperty()));
    }

    private void initializeTexts() {
        saveButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("tooltip.save")));
        deleteButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("tooltip.delete")));
        evaluateButton.setTooltip(new Tooltip(TRANSLATOR.getMessage("tooltip.interpret")));
        applyToLabel.setText(TRANSLATOR.getMessage("label.applyTo"));
        updateAircraftsLabel();
    }

    private void initializeImages() {
        saveButton.setGraphic(new ImageView(SAVE));
        deleteButton.setGraphic(new ImageView(DELETE));
        evaluateButton.setGraphic(new ImageView(EXECUTE));
    }

    private void updateAircraftsLabel() {
        final String aircraftsNumber;
        final Recording selectedRecording = model.getSelectedRecording();
        if (selectedRecording == EMPTY_RECORDING) {
            aircraftsNumber = "0";
        } else {
            selectedRecording.load();
            aircraftsNumber = valueOf(selectedRecording.getAircrafts().size());
        }
        aircraftsLabel.setText(TRANSLATOR.getMessage(
                "label.aircrafts",
                model.getSelectedAircrafts().size(),
                aircraftsNumber));
    }

    private void initializeButtons() {
        evaluateButton.setDisable(true);
        bindingHandles.add(createAttachBinding(evaluateButton.disableProperty(),
                model.evaluateButtonDisabledProperty()));
        bindingHandles.add(createAttachBinding(saveButton.disableProperty(),
                model.okButtonDisabledProperty()));
        saveButton.setOnAction(event -> model.save());
        evaluateButton.setOnAction(event -> {
            model.save();
            model.updateSelectedAircrafts();
            updateAircraftsLabel();
        });
        deleteButton.setOnAction(event -> {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, TRANSLATOR
                        .getMessage("dialog.validate"), ButtonType.YES,
                        ButtonType.NO);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    model.delete();
                    filterEditionTab.getTabPane().getTabs().remove(filterEditionTab);
                }
            } catch (final IOException e) {
                throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        });
    }
}
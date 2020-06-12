package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.utils.binding.BindingHandle;
import fdit.gui.utils.binding.BindingHandles;
import fdit.tools.i18n.MessageTranslator;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.utils.FXUtils.positionCaretToEnd;
import static fdit.gui.utils.dialog.DeletionConfirmationUtils.confirmDeletion;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.*;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;
import static javafx.scene.control.Tooltip.install;
import static javafx.scene.input.KeyCode.ENTER;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public abstract class ZoneEditionTabController implements Initializable {

    protected static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneEditionTabController.class);
    protected final ZoneEditionModel zoneEditionModel;
    protected final Tab zoneEditionTab;
    protected final Collection<ListenerHandle> listenerHandles = newArrayList();
    protected final Collection<BindingHandle> bindingHandles = newArrayList();
    @FXML
    protected Label zoneNameLabel;
    @FXML
    protected TextField zoneNameTextField;
    @FXML
    protected Label altitudeLowerBoundLabel;
    @FXML
    protected TextField altitudeLowerBoundTextField;
    @FXML
    protected Label altitudeUpperBoundLabel;
    @FXML
    protected TextField altitudeUpperBoundTextField;
    @FXML
    protected Label shapeNameLabel;
    @FXML
    protected FlowPane shapeCreationData;
    @FXML
    protected Button okButton;
    @FXML
    protected Button cancelButton;
    @FXML
    protected Button deleteButton;
    @FXML
    protected Label errorLabel;

    ZoneEditionTabController(final Tab zoneEditionTab,
                             final ZoneEditionModel zoneEditionModel) {
        this.zoneEditionTab = zoneEditionTab;
        this.zoneEditionModel = zoneEditionModel;
        LANGUAGES_MANAGER.addListener(observable -> {
            initializeLabel();
            initializeButton();
            initializeTextField();
        });
    }

    private static void saveLatitude(final TextInputControl latitude,
                                     final DoubleProperty[] polygonPoint) {
        try {
            setVertexLatitude(polygonPoint, parseDouble(latitude.getText()));
        } catch (final NumberFormatException e) {
            latitude.setText(valueOf(getVertexLatitude(polygonPoint)));
        } finally {
            positionCaretToEnd(latitude);
        }
    }

    private static void saveLongitude(final TextInputControl longitude,
                                      final DoubleProperty[] polygonPoint) {
        try {
            setVertexLongitude(polygonPoint, parseDouble(longitude.getText()));
        } catch (final NumberFormatException e) {
            longitude.setText(valueOf(getVertexLongitude(polygonPoint)));
        } finally {
            positionCaretToEnd(longitude);
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeLabel();
        initializeButton();
        initializeTextField();
        initializeData();
        bindingHandles.add(BindingHandles.createAttachBindingBidirectional(zoneEditionTab.textProperty(),
                zoneEditionModel.zoneNameProperty()));
    }

    public void onClose() {
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }

        for (final BindingHandle bindingHandle : bindingHandles) {
            bindingHandle.unbind();
        }
        listenerHandles.clear();
        bindingHandles.clear();
        zoneEditionModel.closeModel();
    }

    protected void initializeLabel() {
        zoneNameLabel.setText(TRANSLATOR.getMessage("label.name"));
        altitudeLowerBoundLabel.setText(TRANSLATOR.getMessage("label.lowerBound"));
        altitudeUpperBoundLabel.setText(TRANSLATOR.getMessage("label.upperBound"));
        bindingHandles.add(BindingHandles.createAttachBindingBidirectional(errorLabel.textProperty(),
                zoneEditionModel.errorMessageProperty()));
    }

    protected void initializeButton() {
        okButton.setText(TRANSLATOR.getMessage("button.ok"));
        cancelButton.setText(TRANSLATOR.getMessage("button.cancel"));
        deleteButton.setText(TRANSLATOR.getMessage("button.delete"));
        okButton.setOnAction(event -> zoneEditionModel.save());
        cancelButton.setOnAction(event -> zoneEditionModel.restoreModel());
        deleteButton.setOnAction(event -> {
            if (confirmDeletion(zoneEditionModel.getEditedZone())) {
                try {
                    zoneEditionModel.delete();
                } catch (final IOException e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }
        });
        okButton.setDisable(!zoneEditionModel.getSaveButtonEnabled());
        deleteButton.setDisable(!zoneEditionModel.isDeleteButtonEnabled());
        listenerHandles.add(createAttached(zoneEditionModel.saveButtonEnabledProperty(),
                (observable, oldValue, newValue) ->
                        okButton.setDisable(!newValue)));
        listenerHandles.add(createAttached(zoneEditionModel.deleteButtonEnabledProperty(),
                (observable, oldValue, newValue) ->
                        deleteButton.setDisable(!newValue)));
    }

    protected void initializeTextField() {
        bindingHandles.add(BindingHandles.createAttachBindingBidirectional(zoneNameTextField.textProperty(),
                zoneEditionModel.zoneNameProperty()));
        bindingHandles.add(BindingHandles.createAttachBindingBidirectional(altitudeLowerBoundTextField.textProperty(),
                zoneEditionModel.altitudeLowerBoundProperty()));
        bindingHandles.add(BindingHandles.createAttachBindingBidirectional(altitudeUpperBoundTextField.textProperty(),
                zoneEditionModel.altitudeUpperBoundProperty()));
    }

    protected abstract void initializeData();

    protected void initializeLatitudeTextField(final DoubleProperty[] polygonPoint,
                                               final Pane polygonPointPane) {
        final TextInputControl latitude = new TextField(valueOf(getVertexLatitude(polygonPoint)));
        final Tooltip latitudeTooltip = new Tooltip(TRANSLATOR.getMessage("tooltip.latitude"));
        install(latitude, latitudeTooltip);
        polygonPointPane.getChildren().add(latitude);
        listenerHandles.add(createAttached(latitude.focusedProperty(), (observable, oldValue, newValue) -> {
            if (!newValue) {
                saveLatitude(latitude, polygonPoint);
            }
        }));
        latitude.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                saveLatitude(latitude, polygonPoint);
            }
        });
    }

    protected void initializeLongitudeTextField(final DoubleProperty[] polygonPoint,
                                                final Pane polygonPointPane) {
        final TextInputControl longitude = new TextField(valueOf(getVertexLongitude(polygonPoint)));
        final Tooltip longitudeTooltip = new Tooltip(TRANSLATOR.getMessage("tooltip.longitude"));
        install(longitude, longitudeTooltip);
        polygonPointPane.getChildren().add(longitude);
        listenerHandles.add(createAttached(longitude.focusedProperty(), (observable, oldValue, newValue) -> {
            if (!newValue) {
                saveLongitude(longitude, polygonPoint);
            }
        }));
        longitude.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                saveLongitude(longitude, polygonPoint);
            }
        });
    }
}
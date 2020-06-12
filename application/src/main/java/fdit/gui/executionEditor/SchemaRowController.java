package fdit.gui.executionEditor;

import fdit.gui.utils.UpdateableComboBox;
import fdit.gui.utils.binding.BindingHandle;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.Images.TEXTUAL_SCENARIO_ICON;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;
import static fdit.metamodel.execution.Execution.SCHEMA;
import static javafx.beans.binding.Bindings.bindContentBidirectional;

public class SchemaRowController {

    private static final MessageTranslator TRANSLATOR = MessageTranslator.createMessageTranslator(SchemaRowController.class);

    private final SchemaRow view;
    private final SchemaRowModel model;
    private final Collection<BindingHandle> bindingHandles = newArrayList();
    private final Collection<ListenerHandle> listenerHandless = newArrayList();

    SchemaRowController(final SchemaRow view, final SchemaRowModel model) {
        this.view = view;
        this.model = model;
        initialize();
    }

    public SchemaRow getView() {
        return view;
    }

    public SchemaRowModel getModel() {
        return model;
    }

    public void initialize() {
        initializeButtons();
        initializeActionListener();
        initializeActionChooser();
    }

    private void initializeButtons() {
        view.getDeleteButton().setOnMouseClicked(event -> model.processDeletion());
    }

    private void initializeActionChooser() {
        final Callback<ListView<Schema>, ListCell<Schema>> cellFactory =
                new Callback<ListView<Schema>, ListCell<Schema>>() {
                    @Override
                    public ListCell<Schema> call(final ListView<Schema> param) {
                        return new ListCell<Schema>() {
                            protected void updateItem(final Schema scenario, final boolean empty) {
                                super.updateItem(scenario, empty);
                                final String text;
                                if (scenario == null || empty) {
                                    text = null;
                                } else if (scenario == SCHEMA) {
                                    text = TRANSLATOR.getMessage("selectionAction.noSchema");
                                } else {
                                    text = scenario.getName();
                                }
                                Platform.runLater(() -> {
                                    setGraphic(new ImageView(TEXTUAL_SCENARIO_ICON));
                                    setText(text);
                                });
                            }
                        };
                    }
                };
        final UpdateableComboBox<Schema> scenarioChooser = view.getSchemaChooser();
        scenarioChooser.setCellFactory(cellFactory);
        scenarioChooser.setButtonCell(cellFactory.call(null));
        scenarioChooser.getSelectionModel().select(model.getSelectedSchema());
    }

    private void initializeActionListener() {
        bindContentBidirectional(view.getSchemaChooser().getItems(), model.getSelectableSchemas());
        view.getSchemaChooser().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != model.getSelectedSchema()) {
                        model.setSelectedSchema(newValue);
                    }
                }
        );
        model.addListener(() -> view.getSchemaChooser().refresh());
        model.selectedSchemaProperty().addListener((observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> view.getSchemaChooser().getSelectionModel().select(newValue)));
    }

    public void onClose() {
        clearInnerListeners();
    }

    private void clearInnerListeners() {
        for (final BindingHandle bindingHandle : bindingHandles) {
            bindingHandle.unbind();
        }
        for (final ListenerHandle listenerHandle : listenerHandless) {
            listenerHandle.detach();
        }
        bindingHandles.clear();
        listenerHandless.clear();
    }
}
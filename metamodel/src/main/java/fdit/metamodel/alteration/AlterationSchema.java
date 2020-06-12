package fdit.metamodel.alteration;

import fdit.metamodel.alteration.action.Action;
import javafx.collections.ObservableList;

import java.util.Collection;

import static javafx.collections.FXCollections.observableArrayList;

public class AlterationSchema {

    private final String name;
    private final String description;
    private final ObservableList<Action> actions = observableArrayList();

    public AlterationSchema(final String name,
                            final String description,
                            final Collection<Action> actions) {
        this.name = name;
        this.description = description;
        this.actions.addAll(actions);
    }

    public AlterationSchema(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ObservableList<Action> getActions() {
        return actions;
    }

    public void addAction(final Action action) {
        actions.add(action);
    }
}

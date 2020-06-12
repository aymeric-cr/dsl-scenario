package fdit.gui.application.commands;

import fdit.history.Command;
import fdit.metamodel.element.FditElement;

public interface FditElementCommand<T extends FditElement> extends Command {

    T getSubject();

    OperationType getOperationType();

    enum OperationType {CREATION, EDITION, DELETION, TMP_EDITION}
}

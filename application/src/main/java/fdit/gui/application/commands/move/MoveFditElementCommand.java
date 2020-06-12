package fdit.gui.application.commands.move;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.FditElementVisitor;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class MoveFditElementCommand<T extends FditElement> implements FditElementCommand<T> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(MoveFditElementCommand.class);

    private final T elementToMove;
    private final Directory oldFather;
    private final Directory newFather;

    public MoveFditElementCommand(final T elementToMove,
                                  final Directory newFather) {
        this.elementToMove = elementToMove;
        oldFather = elementToMove.getFather();
        this.newFather = newFather;
    }

    @Override
    public T getSubject() {
        return elementToMove;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.move.descr", elementToMove.getName());
    }

    @Override
    public void execute() throws Exception {
        doMove(elementToMove, newFather);
    }

    @Override
    public void undo() throws Exception {
        doMove(elementToMove, oldFather);
    }

    @Override
    public void redo() throws Exception {
        doMove(elementToMove, newFather);
    }

    private void doMove(final FditElement elementToMove, final Directory newFather) throws Exception {
        FDIT_MANAGER.moveFditElement(elementToMove, newFather);
        moveAssociatedElements();
        save(elementToMove, FDIT_MANAGER.getRootFile());
    }

    private void moveAssociatedElements() {
        new FditElementVisitor<Void>() {

            @Override
            public Void visitDirectory(final Directory directory) {
                return null;
            }

            @Override
            public Void visitSchema(final Schema schema) {
                return null;
            }

            @Override
            public Void visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                return null;
            }

            @Override
            public Void visitExecution(final Execution execution) {
                return null;
            }

            @Override
            public Void visitRecording(final Recording recording) {
                return null;
            }

            @Override
            public Void visitZone(final Zone zone) {
                return null;
            }

            @Override
            public Void visitLTLFilter(final LTLFilter ltlFilter) {
                return null;
            }

            @Override
            public Void visitAlterationTrigger(ActionTrigger trigger) {
                return null;
            }
        }.accept(elementToMove);
    }
}
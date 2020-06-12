package fdit.metamodel;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;

public interface FditElementVisitor<T> {

    T visitDirectory(final Directory directory);

    T visitSchema(Schema schema);

    T visitAlterationSpecification(final AlterationSpecification alterationSpecification);

    T visitExecution(final Execution execution);

    T visitRecording(final Recording recording);

    T visitZone(final Zone zone);

    T visitLTLFilter(final LTLFilter ltlFilter);

    T visitAlterationTrigger(final ActionTrigger trigger);

    default T accept(final FditElement element) {
        if (element instanceof fdit.metamodel.element.Directory) {
            return visitDirectory((Directory) element);
        }
        if (element instanceof Schema) {
            return visitSchema((Schema) element);
        }
        if (element instanceof AlterationSpecification) {
            return visitAlterationSpecification((AlterationSpecification) element);
        }
        if (element instanceof Execution) {
            return visitExecution((Execution) element);
        }
        if (element instanceof Recording) {
            return visitRecording((Recording) element);
        }
        if (element instanceof Zone) {
            return visitZone((Zone) element);
        }
        if (element instanceof LTLFilter) {
            return visitLTLFilter((LTLFilter) element);
        }
        if (element instanceof ActionTrigger) {
            return visitAlterationTrigger((ActionTrigger) element);
        }
        throw new IllegalArgumentException("Unknown FditElement");
    }
}
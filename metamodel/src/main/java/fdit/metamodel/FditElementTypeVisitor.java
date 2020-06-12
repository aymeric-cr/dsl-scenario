package fdit.metamodel;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;

public interface FditElementTypeVisitor<T> {

    T visitAlteration();

    T visitRecording();

    T visitScenario();

    T visitZone();

    T visitDirectory();

    T visitExecution();

    T visitLTLFilter();

    T visitAlterationTrigger();

    default T accept(final Class<? extends FditElement> clazz) {
        if (clazz.equals(Object.class)) {
            throw new RuntimeException("Unknown FditElement type");
        }
        if (clazz.equals(AlterationSpecification.class)) {
            return visitAlteration();
        }
        if (clazz.equals(BaseStationRecording.class)) {
            return visitRecording();
        }
        if (clazz.equals(SiteBaseStationRecording.class)) {
            return visitRecording();
        }
        if (clazz.equals(Schema.class)) {
            return visitScenario();
        }
        if (clazz.equals(Zone.class)) {
            return visitZone();
        }
        if (clazz.equals(Directory.class)) {
            return visitDirectory();
        }
        if (clazz.equals(Execution.class)) {
            return visitExecution();
        }
        if (clazz.equals(LTLFilter.class)) {
            return visitLTLFilter();
        }
        if (clazz.equals(ActionTrigger.class)) {
            return visitAlterationTrigger();
        }
        return accept((Class<? extends FditElement>) clazz.getSuperclass());
    }
}
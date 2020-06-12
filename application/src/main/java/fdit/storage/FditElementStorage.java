package fdit.storage;

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

import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.alteration.AlterationSpecificationSaver.saveAlterationSpecification;
import static fdit.storage.execution.ExecutionSaver.saveExecution;
import static fdit.storage.filter.LTLFilterStorage.saveFilter;
import static fdit.storage.scenario.SchemaStorage.saveTextualScenario;
import static fdit.storage.trigger.ActionTriggerStorage.saveAlterationTrigger;
import static fdit.storage.zone.ZoneSaver.saveZone;
import static fdit.tools.stream.StreamUtils.filter;

public final class FditElementStorage {

    private FditElementStorage() {
    }

    public static void save(final FditElement element, final File rootFile) {
        doSave(element, rootFile);
        saveImpactedElements(element, rootFile);
    }

    private static void doSave(final FditElement element, final File rootFile) {
        new FditElementVisitor<Void>() {
            @Override
            public Void visitDirectory(final Directory directory) {
                return null;
            }

            @Override
            public Void visitSchema(final Schema schema) {
                try {
                    saveTextualScenario(schema, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Void visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                try {
                    saveAlterationSpecification(alterationSpecification, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Void visitExecution(final Execution execution) {
                try {
                    saveExecution(execution, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Void visitRecording(final Recording recording) {
                return null;
            }

            @Override
            public Void visitZone(final Zone zone) {
                try {
                    saveZone(zone, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Void visitLTLFilter(final LTLFilter ltlFilter) {
                try {
                    saveFilter(ltlFilter, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Void visitAlterationTrigger(ActionTrigger trigger) {
                try {
                    saveAlterationTrigger(trigger, rootFile);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.accept(element);
    }

    private static void saveImpactedElements(final FditElement modifiedElement, final File rootFile) {
        final Collection<FditElement> allFditElements = gatherAllChildren(retrieveRoot(modifiedElement));
        final Collection<FditElement> impactedElements = filter(allFditElements, isImpactedBy(modifiedElement));
        for (final FditElement impactedElement : impactedElements) {
            doSave(impactedElement, rootFile);
        }
    }

    private static Predicate<FditElement> isImpactedBy(final FditElement modifiedElement) {
        return element -> new FditElementVisitor<Boolean>() {
            @Override
            public Boolean visitDirectory(final Directory directory) {
                return false;
            }

            @Override
            public Boolean visitSchema(final Schema schema) {
                if (modifiedElement instanceof Directory) {
                    if (isAncestorOf((Directory) modifiedElement, schema)) {
                        return true;
                    }
                    if (schema.getRecording() != EMPTY_RECORDING) {
                        return isAncestorOf((Directory) modifiedElement, schema.getRecording());
                    }
                }
                return schema.getRecording() == modifiedElement;
            }

            @Override
            public Boolean visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                return false;
            }

            @Override
            public Boolean visitExecution(final Execution execution) {
                return false;
            }

            @Override
            public Boolean visitRecording(final Recording recording) {
                return false;
            }

            @Override
            public Boolean visitZone(final Zone zone) {
                return false;
            }

            @Override
            public Boolean visitLTLFilter(final LTLFilter ltlFilter) {
                return false;
            }

            @Override
            public Boolean visitAlterationTrigger(ActionTrigger trigger) {
                return false;
            }
        }.accept(element);
    }
}
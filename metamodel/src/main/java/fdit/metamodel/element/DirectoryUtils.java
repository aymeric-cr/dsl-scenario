package fdit.metamodel.element;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.tools.predicate.PredicateUtils.alwaysTrue;
import static fdit.tools.stream.StreamUtils.find;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class DirectoryUtils {
    public static Root retrieveRoot(final FditElement element) {
        if (element instanceof Root) {
            return (Root) element;
        }
        return retrieveRoot(element.getFather());
    }

    private static Collection<Directory> gatherDirectories(final Directory father) {
        return gatherChildren(father, Directory.class);
    }

    public static Collection<Schema> gatherAllTextualScenarios(final Directory father) {
        return gatherAllChildren(father, Schema.class);
    }

    public static Collection<Schema> gatherAllScenario(final Directory father) {
        return gatherAllChildren(father, Schema.class);
    }

    public static Collection<AlterationSpecification> gatherAllAlterationSpecifications(final Directory father) {
        return gatherAllChildren(father, AlterationSpecification.class);
    }

    public static Collection<Recording> gatherAllRecordings(final Directory father) {
        final HashSet<Recording> recordings = new HashSet<>();
        recordings.addAll(gatherAllBstRecordings(father));
        recordings.addAll(gatherAllSbsRecordings(father));
        return recordings;
    }

    public static Collection<BaseStationRecording> gatherAllBstRecordings(final Directory father) {
        return gatherAllChildren(father, BaseStationRecording.class);
    }

    public static Collection<SiteBaseStationRecording> gatherAllSbsRecordings(final Directory father) {
        return gatherAllChildren(father, SiteBaseStationRecording.class);
    }

    public static Collection<Zone> gatherAllZones(final Directory father) {
        return gatherAllChildren(father, Zone.class);
    }

    public static Collection<LTLFilter> gatherAllLTLFilters(final Directory father) {
        return gatherAllChildren(father, LTLFilter.class);
    }

    public static Collection<ActionTrigger> gatherAllAlterationTriggers(final Directory father) {
        return gatherAllChildren(father, ActionTrigger.class);
    }

    public static Collection<Execution> gatherAllExecutions(final Directory father) {
        return gatherAllChildren(father, Execution.class);
    }

    public static Optional<Directory> findDirectory(final String name, final Directory father) {
        try {
            return of(find(gatherDirectories(father), directory -> directory.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<ActionTrigger> findActionTrigger(final String name, final Directory father) {
        try {
            return of(find(gatherAllAlterationTriggers(father), trigger -> trigger.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<Schema> findTextualScenario(final String name, final Directory father) {
        try {
            return of(find(gatherAllTextualScenarios(father), scenario -> scenario.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<AlterationSpecification> findAlterationSpecification(final String name,
                                                                                final Directory father) {
        try {
            return of(find(gatherAllAlterationSpecifications(father),
                    specification -> specification.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<Recording> findRecording(final String name, final Directory father) {
        try {
            return of(find(gatherAllRecordings(father), recording -> recording.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<Zone> findZone(final String name, final Directory father) {
        try {
            return of(find(gatherAllZones(father), zone -> zone.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<LTLFilter> findLTLFilter(final String name, final Directory father) {
        try {
            return of(find(gatherAllLTLFilters(father), filter -> filter.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static Optional<Execution> findExecution(final String name,
                                                    final Directory father) {
        try {
            return of(find(gatherAllExecutions(father),
                    executionConf -> executionConf.getName().equals(name)));
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static <T extends FditElement> Collection<T> gatherChildren(final Directory dir) {
        return gatherChildren(dir, alwaysTrue());
    }

    public static <T extends FditElement> Collection<T> gatherChildren(final Directory dir,
                                                                       final Class<T> type) {
        return gatherChildren(dir, type::isInstance);
    }

    public static <T extends FditElement> Collection<T> gatherChildren(final Directory dir,
                                                                       final Predicate<T> constraint) {
        final Collection<T> result = newArrayList();
        for (final FditElement child : dir.getChildren()) {
            if (constraint.test((T) child)) {
                result.add((T) child);
            }
        }
        return result;
    }

    public static <T extends FditElement> Collection<T> gatherAllChildren(final Directory dir) {
        return gatherAllChildren(dir, alwaysTrue());
    }

    public static <T extends FditElement> Collection<T> gatherAllChildren(final Directory dir,
                                                                          final Class<T> type) {
        return gatherAllChildren(dir, type::isInstance);
    }

    public static <T extends FditElement> Collection<T> gatherAllChildren(final Directory dir,
                                                                          final Predicate<T> constraint) {
        final Collection<T> result = newArrayList();
        for (final FditElement child : dir.getChildren()) {
            if (child instanceof Directory) {
                result.addAll(gatherAllChildren((Directory) child, constraint));
            }
            if (constraint.test((T) child)) {
                result.add((T) child);
            }
        }
        return result;
    }

    public static boolean isAncestorOf(final Directory dir, final FditElement element) {
        for (final FditElement child : dir.getChildren()) {
            if (child == element) {
                return true;
            }
            if (child instanceof Directory) {
                if (isAncestorOf((Directory) child, element)) {
                    return true;
                }
            }
        }
        return false;
    }
}
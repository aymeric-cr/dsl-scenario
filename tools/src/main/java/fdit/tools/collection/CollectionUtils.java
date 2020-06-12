package fdit.tools.collection;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T1, T2> void zip2(final Iterable<T1> iterable1,
                                     final Iterable<T2> iterable2,
                                     final BiConsumer<T1, T2> closure) {
        if (size(iterable1) != size(iterable2)) {
            throw new IllegalArgumentException("The two collections must have the same size");
        }
        final Iterator<T1> iterator1 = iterable1.iterator();
        final Iterator<T2> iterator2 = iterable2.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            closure.accept(iterator1.next(), iterator2.next());
        }
    }

    public static <T> T head(final Iterable<T> collection) {
        return getFirst(collection, null);
    }

    public static <T> Collection<T> ensureCollection(final Iterable<T> collection) {
        if (collection instanceof Collection) {
            return (Collection<T>) collection;
        }
        return newArrayList(collection);
    }

    public static <T> Collection<T> nullToEmpty(final T[] elements) {
        if (elements == null) {
            return Collections.EMPTY_LIST;
        }
        return newArrayList(elements);
    }

    public static <T> boolean addIfNotNull(final Collection<? super T> collection, final T element) {
        return element != null && collection.add(element);
    }

    public static boolean addIfNotBlank(final Collection<String> collection, final String element) {
        return isNotBlank(element) && collection.add(element);
    }

    public static <T> Collection<T> concat(final Iterable<? extends T>... collections) {
        final Collection<T> result = newArrayList();
        for (final Iterable<? extends T> collection : collections) {
            addAll(result, collection);
        }
        return result;
    }

    public static <K, V> void put(final Map<K, Collection<V>> map,
                                  final K key,
                                  final V value,
                                  final Supplier<Collection<V>> constructor) {
        if (!map.containsKey(key)) {
            map.put(key, constructor.get());
        }
        map.get(key).add(value);
    }

    public static <T> boolean sameSequence(final Iterable<T> list1, final Iterable<T> list2) {
        final Iterator<T> iterator1 = list1.iterator();
        final Iterator<T> iterator2 = list2.iterator();
        while (iterator1.hasNext()) {
            if (iterator2.hasNext()) {
                final T e1 = iterator1.next();
                final T e2 = iterator2.next();
                if (!e1.equals(e2)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return !iterator2.hasNext();
    }

    public static <T> boolean sameCollectionsIgnoringOrder(final Collection<T> first, final Collection<T> second) {
        return first.containsAll(second) && first.size() == second.size();
    }

    public static <T> Optional<T> tryNext(final Iterator<T> iterator) {
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    public static <T> boolean startWith(final Iterable<T> collection, final Iterable<T> preamble) {
        final Iterator<T> collectionIterator = collection.iterator();
        for (final T item : preamble) {
            if (!collectionIterator.hasNext()) {
                return false;
            }
            if (!item.equals(collectionIterator.next())) {
                return false;
            }
        }
        return true;
    }
}


package fdit.tools.stream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static fdit.tools.collection.CollectionUtils.ensureCollection;
import static fdit.tools.collection.CollectionUtils.zip2;
import static fdit.tools.predicate.PredicateUtils.and;
import static fdit.tools.predicate.PredicateUtils.not;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

public final class StreamUtils {

    private StreamUtils() {
    }

    public static <E, T> List<T> distinctMapping(final Iterable<? extends E> collection,
                                                 final Function<E, T> mapper) {
        return ensureCollection(collection).stream().map(mapper).distinct().collect(toList());
    }

    public static <E, T> List<T> mapping(final Iterable<? extends E> collection,
                                         final Function<E, T> mapper) {
        return ensureCollection(collection).stream().map(mapper).collect(toList());
    }

    public static <E, T, C extends Collection<T>> C mapping(final Iterable<? extends E> collection,
                                                            final Function<E, T> mapper,
                                                            final Supplier<C> collectionFactory) {
        return ensureCollection(collection).stream().map(mapper).collect(toCollection(collectionFactory));
    }

    public static <E, T extends E, R> Collection<R> mapping(final Collection<? extends E> collection,
                                                            final Class<T> type,
                                                            final Function<T, R> mapper) {
        return mapping(filter(collection, type), mapper);
    }

    public static <E, T> Collection<T> mapping(final Collection<? extends E> collection,
                                               final Predicate<? super E> filter,
                                               final Function<E, T> mapper) {
        return mapping(filter(collection, filter), mapper);
    }

    public static <E, T> Collection<T> mapping(final Iterable<? extends E> collection,
                                               final Function<E, T> mapper,
                                               final Predicate<? super T> filter) {
        return filter(mapping(collection, mapper), filter);
    }

    public static <E, T, C extends Collection<T>> C mapping(final Collection<? extends E> collection,
                                                            final Predicate<? super E> filter,
                                                            final Function<E, T> mapper,
                                                            final Supplier<C> collectionFactory) {
        return mapping(filter(collection, filter), mapper, collectionFactory);
    }

    public static <E, T> Collection<T> mapping(final Function<E, T> mapper, final E... collection) {
        return mapping(asList(collection), mapper);
    }

    public static <E1, E2, T> Collection<T> mapping2(final Iterable<E1> collection1,
                                                     final Iterable<E2> collection2,
                                                     final BiFunction<E1, E2, T> biMapper) {
        final Collection<T> result = newArrayList();
        zip2(collection1, collection2, (e1, e2) -> result.add(biMapper.apply(e1, e2)));
        return result;
    }

    public static <T> Collection<T> concat(final Iterable<? extends T> collection1,
                                           final Iterable<? extends T> collection2) {
        return Stream.concat(ensureCollection(collection1).stream(),
                ensureCollection(collection2).stream()).collect(toList());
    }

    public static <E, R> Collection<R> distinctFlatMapping(final Iterable<? extends E> elements,
                                                           final Function<E, Collection<R>> function) {
        return ensureCollection(elements).stream().flatMap(x -> function.apply(x).stream()).distinct().collect(toList());
    }

    public static <E, R> List<R> flatMapping(final Iterable<? extends E> elements,
                                             final Function<E, Collection<R>> function) {
        return ensureCollection(elements).stream().flatMap(x -> function.apply(x).stream()).collect(toList());
    }

    public static String join(final Iterable<String> collection, final CharSequence delimiter) {
        return ensureCollection(collection).stream().collect(joining(delimiter));
    }

    public static <E> String join(final Iterable<? extends E> collection,
                                  final Function<E, String> mapper,
                                  final CharSequence delimiter) {
        return ensureCollection(collection).stream().map(mapper).collect(joining(delimiter));
    }

    public static <E> String join(final Iterable<? extends E> collection,
                                  final Function<E, String> mapper,
                                  final CharSequence delimiter,
                                  final CharSequence prefix,
                                  final CharSequence suffix) {
        return ensureCollection(collection).stream().map(mapper).collect(joining(delimiter, prefix, suffix));
    }

    public static <T> List<T> filter(final Collection<?> contents,
                                     final Class<T> type,
                                     final Predicate<T>... predicates) {
        return keeping(contents, type).filter(and(predicates)).collect(toList());
    }

    public static <T> Stream<T> keeping(final Collection<?> contents, final Class<T> type) {
        return contents.stream().filter(type::isInstance).map(e -> (T) e);
    }

    public static <T> T find(final Iterable<T> collection, final Predicate<? super T> criterion) {
        for (final T t : collection) {
            if (criterion.test(t)) {
                return t;
            }
        }
        throw new NoSuchElementException();
    }

    public static <T> T find(final Iterable<T> collection, final Predicate<? super T> criterion, final T defaultValue) {
        for (final T t : collection) {
            if (criterion.test(t)) {
                return t;
            }
        }
        return defaultValue;
    }

    public static <T> T find(final Iterable<? super T> collection, final Class<T> type) {
        for (final Object element : collection) {
            if (type.isInstance(element)) {
                return (T) element;
            }
        }
        throw new NoSuchElementException();
    }

    public static <T> T find(final Iterable<? super T> collection, final Class<T> type, final T defaultValue) {
        for (final Object element : collection) {
            if (type.isInstance(element)) {
                return (T) element;
            }
        }
        return defaultValue;
    }

    public static <T> T find(final Collection<?> collection, final Class<T> type, final Predicate<T>... predicates) {
        return find(filter(collection, type), and(predicates));
    }


    public static <T> Optional<T> tryFind(final Iterable<T> collection, final Predicate<? super T> criterion) {
        for (final T t : collection) {
            if (criterion.test(t)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> tryFind(final Collection<?> collection,
                                          final Class<T> type,
                                          final Predicate<T>... predicates) {
        return tryFind(filter(collection, type), and(predicates));
    }

    public static <T, U> Collection<T> filter(final Collection<T> collection,
                                              final Function<T, U> mapper,
                                              final Predicate<? super U> criterion) {
        return collection.stream().filter(t -> criterion.test(mapper.apply(t))).collect(toList());
    }

    public static <T, U> Collection<U> filter(final Collection<T> collection,
                                              final Predicate<? super T> criterion,
                                              final Function<T, U> mapper) {
        return collection.stream().filter(criterion::test).map(mapper).collect(toList());
    }

    public static <T> List<T> filter(final Collection<T> collection, final Predicate<? super T> criterion) {
        return collection.stream().filter(criterion).collect(toList());
    }

    public static <T, C extends Collection<T>> C filter(final Collection<T> collection,
                                                        final Predicate<? super T> criterion,
                                                        final Supplier<C> collectionFactory) {
        return collection.stream().filter(criterion).collect(toCollection(collectionFactory));
    }

    public static <T, U> boolean all(final Iterable<T> collection, final Class<U> type) {
        return all(collection, type::isInstance);
    }

    public static <T> boolean all(final Iterable<T> collection, final Predicate<? super T> criterion) {
        for (final T element : collection) {
            if (!criterion.test(element)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean exists(final Iterable<T> collection, final Predicate<? super T> criterion) {
        for (final T element : collection) {
            if (criterion.test(element)) {
                return true;
            }
        }
        return false;
    }

    public static <T> int sumInt(final Collection<T> collection, final ToIntFunction<T> function) {
        return collection.stream().mapToInt(function).sum();
    }

    public static <T> double sumDouble(final Collection<T> collection, final ToDoubleFunction<T> function) {
        return collection.stream().mapToDouble(function).sum();
    }

    public static <K, V> Map<K, V> asHashMap(final Iterable<K> collection, final Function<K, V> mapper) {
        final Map<K, V> result = newHashMap();
        for (final K key : collection) {
            if (result.containsKey(key)) {
                continue;
            }
            result.put(key, mapper.apply(key));
        }
        return result;
    }

    public static <T, K, V> Map<K, V> asHashMap(final Collection<T> collection,
                                                final Function<T, K> keyMapper,
                                                final Function<T, V> valueMapper) {
        return collection.stream().collect(toMap(keyMapper, valueMapper));
    }

    public static <T> int index(final Iterable<T> elements, final Predicate<T> candidate) {
        return Iterables.indexOf(elements, candidate::test);
    }

    public static <T, C extends T> boolean safeContains(final Iterable<T> elements, final C candidate) {
        return Iterables.contains(elements, candidate);
    }

    public static <T extends Collection<U>, U> T adding(final T collection, final U item) {
        collection.add(item);
        return collection;
    }

    public static <T> List<T> removeDuplicates(final Iterable<T> collection) {
        return ensureCollection(collection).stream().distinct().collect(toList());
    }

    public static <T, U> List<T> removeDuplicates(final Iterable<T> collection, final Function<T, U> keyExtractor) {
        final Set<U> seen = newHashSet();
        return ensureCollection(collection).stream().filter(t -> seen.add(keyExtractor.apply(t))).collect(toList());
    }

    public static <T> Collection<T> sort(final Iterable<T> collection, final Comparator<T> comparator) {
        return stream(collection.spliterator(), false).sorted(comparator).collect(toList());
    }

    public static <T, U> Map<U, List<T>> groupingBy(final Collection<T> collection, final Function<T, U> classifier) {
        return collection.stream().collect(Collectors.groupingBy(classifier));
    }

    public static <T> int count(final Collection<? super T> collection, final Class<T> type) {
        return filter(collection, type).size();
    }

    public static <T> int count(final Collection<T> collection, final Predicate<? super T> type) {
        return filter(collection, type).size();
    }

    public static <T, C> void forEach(final Collection<T> collection,
                                      final Class<C> filter,
                                      final Consumer<? super C> action) {
        filter(collection, filter).forEach(action);
    }

    public static <T> Collection<T> excluding(final Collection<T> collection, final Collection<T> exclusion) {
        return filter(collection, not(in(exclusion)));
    }

    public static <T> List<T> tail(final Collection<T> collection) {
        return skip(collection, 1);
    }

    public static <T> List<T> skip(final Collection<T> collection, final long numberToSkip) {
        return collection.stream().skip(numberToSkip).collect(toList());
    }

    public static <T> int safeIndexOf(final Iterable<T> elements, final T element) {
        return Iterables.indexOf(elements, element::equals);
    }

    public static <K, V> Map<K, V> filterKeys(final Map<K, V> map, final Predicate<K> keyFilter) {
        return Maps.filterKeys(map, keyFilter::test);
    }

    public static <K, V> V fromCache(final Map<K, V> cache, final K key, final Function<K, V> valueSupplier) {
        if (!cache.containsKey(key)) {
            cache.put(key, valueSupplier.apply(key));
        }
        return cache.get(key);
    }

    public static <K, V> V fromCache(final Map<K, V> cache, final K key, final Supplier<V> valueSupplier) {
        if (!cache.containsKey(key)) {
            cache.put(key, valueSupplier.get());
        }
        return cache.get(key);
    }

    public static <T> int max(final Collection<T> elements, final ToIntFunction<T> gauge, final int defaultValue) {
        return elements.stream().mapToInt(gauge).max().orElse(defaultValue);
    }

    public static <T> ListIterator<T> reverseIterator(final List<T> elements) {
        return elements.listIterator(elements.size());
    }

    /*
        return 0 if list is empty
     */
    public static int maxInt(final Collection<Integer> list) {
        if (list.isEmpty()) {
            return 0;
        }
        return list.stream().mapToInt(Integer::intValue).max().getAsInt();
    }

    public static int minInt(final Collection<Integer> list) {
        if (list.isEmpty()) {
            return 0;
        }
        return list.stream().mapToInt(Integer::intValue).min().getAsInt();
    }

    /*
        return 0.0 if list is empty
     */
    public static double maxDouble(final Collection<Double> list) {
        if (list.isEmpty()) {
            return 0.0;
        }
        return list.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
    }


    /*
        return 0.0 if list is empty
     */
    public static double minDouble(final Collection<Double> list) {
        if (list.isEmpty()) {
            return 0.0;
        }
        return list.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
    }
}

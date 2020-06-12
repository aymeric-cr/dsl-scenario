package fdit.storage;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.storage.LoadingResult.ResultType.*;
import static fdit.tools.collection.CollectionUtils.concat;
import static java.util.Collections.EMPTY_LIST;

public class LoadingResult<T> {

    private final ResultType resultType;
    private final T elementLoaded;
    private final Collection<String> messages;

    private LoadingResult(final ResultType resultType,
                          final T elementLoaded,
                          final Collection<String> messages) {
        this.resultType = resultType;
        this.elementLoaded = elementLoaded;
        this.messages = messages;
    }

    public static <T> LoadingResult<T> succeeded(final T elementLoaded) {
        return new LoadingResult<T>(SUCCESS, elementLoaded, EMPTY_LIST);
    }

    public static <T> LoadingResult<T> warn(final T elementLoaded, final String message) {
        return new LoadingResult<T>(WARNING, elementLoaded, newArrayList(message));
    }

    public static <T> LoadingResult<T> warn(final T elementLoaded, final Collection<String> messages) {
        return new LoadingResult<T>(WARNING, elementLoaded, messages);
    }

    public static <T> LoadingResult<T> failed(final String message) {
        return new LoadingResult<T>(ERROR, null, newArrayList(message));
    }

    public static <T> LoadingResult<T> failed(final Collection<String> messages) {
        return new LoadingResult<T>(ERROR, null, messages);
    }

    public static <T, U> LoadingResult<T> chain(final LoadingResult<U> other, final T elementLoaded) {
        if (other.checkFailed()) {
            return failed(other.messages);
        }
        return new LoadingResult<T>(other.resultType, elementLoaded, other.messages);
    }

    public ResultType getResultType() {
        return resultType;
    }

    public T getElementLoaded() {
        return elementLoaded;
    }

    public Collection<String> getMessages() {
        return messages;
    }

    public LoadingResult<T> combineResult(final LoadingResult<T> other) {
        if (checkFailed() || other.checkFailed()) {
            return failed(concat(messages, other.messages));
        }
        if (checkWarned() || other.checkWarned()) {
            return warn(null, concat(messages, other.messages));
        }
        return succeeded(null);
    }

    public boolean checkSucceeded() {
        return resultType == SUCCESS;
    }

    public boolean checkFailed() {
        return resultType == ERROR;
    }

    public boolean checkWarned() {
        return resultType == WARNING;
    }

    enum ResultType {SUCCESS, ERROR, WARNING}
}

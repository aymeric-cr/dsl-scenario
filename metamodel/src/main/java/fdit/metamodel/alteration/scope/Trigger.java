package fdit.metamodel.alteration.scope;

public class Trigger implements Scope {

    private final long timeMillis;

    public Trigger(final long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public long getTimeMillis() {
        return timeMillis;
    }
}

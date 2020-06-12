package fdit.metamodel.element;

public abstract class LazyFditElement extends FditElement {

    private boolean isLoaded;

    protected LazyFditElement(final String name) {
        super(name);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void load() {
        if (isLoaded) {
            return;
        }
        try {
            loadContent();
            isLoaded = true;
        } catch (final Exception e) {
            isLoaded = false;
            throw new RuntimeException(e);
        }
    }

    public void unload() {
        unloadContent();
        isLoaded = false;
    }

    protected abstract void loadContent() throws Exception;

    protected abstract void unloadContent();
}

package fdit.metamodel.element;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

public class Directory extends FditElement {

    private final Collection<FditElement> children = newArrayList();

    public Directory(final String name) {
        super(name);
    }

    public Collection<FditElement> getChildren() {
        return children;
    }

    public void addChild(final FditElement child) {
        children.add(child);
    }

    public void removeChild(final FditElement element) {
        children.remove(element);
    }
}

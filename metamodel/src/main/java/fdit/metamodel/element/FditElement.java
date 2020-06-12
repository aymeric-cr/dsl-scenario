package fdit.metamodel.element;

public abstract class FditElement {

    private String name;
    private Directory father;

    protected FditElement(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Directory getFather() {
        return father;
    }

    public void setFather(final Directory father) {
        this.father = father;
    }
}
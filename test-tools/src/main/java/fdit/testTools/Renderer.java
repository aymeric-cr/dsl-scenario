package fdit.testTools;

public interface Renderer {
    boolean handle(final Object object);

    String render(final Object object);
}

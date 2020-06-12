package fdit.metamodel.trigger;

import fdit.metamodel.element.FditElement;

import java.util.UUID;

public class ActionTrigger extends FditElement {

    private final UUID id;
    private String content;
    private String description;

    public ActionTrigger(final String name,
                         final UUID id,
                         final String description,
                         final String content) {
        super(name);
        this.id = id;
        this.content = content;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
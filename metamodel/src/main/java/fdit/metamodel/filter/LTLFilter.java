package fdit.metamodel.filter;

import java.util.UUID;

public class LTLFilter extends Filter {

    private final UUID id;
    private String content;
    private String description;

    public LTLFilter(final String name,
                     final UUID id,
                     final String description,
                     final String content) {
        super(name);
        this.id = id;
        this.description = description;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UUID getId() {
        return id;
    }
}
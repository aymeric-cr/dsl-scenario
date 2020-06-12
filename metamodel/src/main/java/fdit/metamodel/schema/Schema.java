package fdit.metamodel.schema;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.LazyFditElement;
import fdit.metamodel.recording.Recording;

import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;

public class Schema extends LazyFditElement {

    private Recording recording;
    private AlterationSpecification alterationSpecification;
    private String description;
    private String content;

    public Schema(final String name, final String description, final String content) {
        this(name, description, content, EMPTY_RECORDING);
    }

    public Schema(final String name, final String description, final String content, final Recording recording) {
        super(name);
        this.recording = recording;
        this.description = description;
        this.content = content;
    }

    public AlterationSpecification getAlterationSpecification() {
        return alterationSpecification;
    }

    public void setAlterationSpecification(final AlterationSpecification alterationSpecification) {
        this.alterationSpecification = alterationSpecification;
    }

    public Recording getRecording() {
        return recording;
    }

    public void setRecording(final Recording recording) {
        this.recording = recording;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    protected void loadContent() {

    }

    @Override
    protected void unloadContent() {

    }
}
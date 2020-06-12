package fdit.metamodel.execution;

import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static java.util.Collections.unmodifiableCollection;

public class Execution extends FditElement {

    public static final Schema SCHEMA = new Schema("", "", "");
    private final List<Schema> schemas = newLinkedList();
    private Recording recording = EMPTY_RECORDING;

    public Execution(final String name) {
        super(name);
    }

    public Recording getRecording() {
        return recording;
    }

    public void setRecording(Recording recording) {
        this.recording = recording;
    }

    public Collection<Schema> getSchemas() {
        return unmodifiableCollection(schemas);
    }

    public void addSchema(final Schema schema) {
        schemas.add(schema);
    }

    public void addSchema(final int index, final Schema schema) {
        schemas.add(index, schema);
    }

    public void removeSchema(final Schema schema) {
        schemas.remove(schema);
    }
}
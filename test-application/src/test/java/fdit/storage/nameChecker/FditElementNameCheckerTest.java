package fdit.storage.nameChecker;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.zone.Zone;
import fdit.tools.functional.ThrowableConsumer;
import org.junit.Test;

import static fdit.gui.application.FditManagerUtils.getFditElementFile;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.storage.nameChecker.FditElementNameChecker.checkNewFditElementNameValidity;
import static fdit.storage.nameChecker.FditElementNameChecker.checkRenameElementValidity;
import static fdit.testTools.FileTestUtils.createEmptyFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FditElementNameCheckerTest extends FditTestCase {

    private static ThrowableConsumer<Directory> emptyFile(final String name) {
        return father -> createEmptyFile(getFditElementFile(father), name);
    }

    @Test
    public void testElementNameValidity() {
        final Root root = root(bstRecording("recording"));
        final FditElement recording = findRecording("recording", root).get();

        assertTrue(checkRenameValidity(recording, "name"));
        assertTrue(checkRenameValidity(recording, "my name"));
        assertTrue(checkRenameValidity(recording, "my-name"));
        assertTrue(checkRenameValidity(recording, "my_name"));
        assertTrue(checkRenameValidity(recording, "my-name is éàùè"));
        assertTrue(checkRenameValidity(recording, "name.name"));
        assertTrue(checkRenameValidity(recording, "name%name"));

        assertFalse(checkRenameValidity(recording, "."));
        assertFalse(checkRenameValidity(recording, "name*name"));
        assertFalse(checkRenameValidity(recording, "name>name"));
        assertFalse(checkRenameValidity(recording, "name:name"));
        assertFalse(checkRenameValidity(recording, "name\"name"));
        assertFalse(checkRenameValidity(recording, "name/name"));
        assertFalse(checkRenameValidity(recording, "name\\name"));
        assertFalse(checkRenameValidity(recording, "name|name"));
        assertFalse(checkRenameValidity(recording, "name?name"));
        assertFalse(checkRenameValidity(recording, "name&name"));
    }

    @Test
    public void testElementNameValidity_sameName_sameType() {
        final Root root = root(bstRecording("file"),
                bstRecording("other"));
        final FditElement fileElement = findRecording("file", root).get();
        assertFalse(checkRenameValidity(fileElement, "other"));
    }

    @Test
    public void testElementNameValidity_sameName_sameType_differentLocation_any() {
        final Root root = root(bstRecording("file"),
                folder("dir", bstRecording("other")));
        final FditElement fileElement = findRecording("file", root).get();
        assertTrue(checkRenameValidity(fileElement, "other"));
    }

    @Test
    public void testElementNameValidity_sameName_sameType_differentLocation_tscenario() {
        final Root root = root(
                folder("dir", schema("other"),
                        folder("subDir", schema("tscenario"))));
        final FditElement fileElement = findSchema("tscenario", root).get();
        assertFalse(checkRenameValidity(fileElement, "other"));
    }

    @Test
    public void testElementNameValidity_sameName_sameType_differentLocation_ltlFilter() {
        final Root root = root(ltlFilter("ltlFilter"),
                folder("dir", ltlFilter("other")));
        final FditElement fileElement = findLTLFilter("ltlFilter", root).get();
        assertFalse(checkRenameValidity(fileElement, "other"));
    }

    @Test
    public void testElementNameValidity_sameName_sameType_differentLocation_actionTrigger() {
        final Root root = root(actionTrigger("actionTrigger"),
                folder("dir", actionTrigger("other")));
        final FditElement fileElement = findActionTrigger("actionTrigger", root).get();
        assertFalse(checkRenameValidity(fileElement, "other"));
    }

    @Test
    public void testNewElementNameValidity_SameName_SameType_notInFileBrowser() {
        root(emptyFile("any.xml"));
        assertFalse(checkNewNameValidity(getRoot(), "any", Zone.class));
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private boolean checkNewNameValidity(final Directory father,
                                         final String name,
                                         final Class<? extends FditElement> type) {
        return checkNewFditElementNameValidity(name, father, getRootFile(), type).checkSucceeded();
    }

    private boolean checkRenameValidity(final FditElement element, final String name) {
        return checkRenameElementValidity(element, getRootFile(), name).checkSucceeded();
    }

}
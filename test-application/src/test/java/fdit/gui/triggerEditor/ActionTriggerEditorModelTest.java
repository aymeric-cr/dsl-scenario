package fdit.gui.triggerEditor;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Root;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.triggcondition.ide.CompletionProposal;
import fdit.triggcondition.ide.TriggeringConditionFacade;
import org.fxmisc.richtext.model.PlainTextChange;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.function.Predicate;

import static fdit.metamodel.FditElementHelper.generateIdForTest;
import static fdit.metamodel.element.DirectoryUtils.findActionTrigger;
import static fdit.metamodel.trigger.AlterationTriggerHelper.content;
import static fdit.metamodel.trigger.AlterationTriggerHelper.description;
import static fdit.testTools.PredicateAssert.assertEqual;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.*;

public class ActionTriggerEditorModelTest extends FditTestCase {

    private static Predicate<CompletionProposal> aProposal(final String expected) {
        return completionProposal -> completionProposal.getLabel().equals(expected);
    }

    @Test
    public void codeEditorIsInitializedWithDescription() {
        final Root root = root(actionTrigger("trigger", generateIdForTest(1), content("eval"), description("")));
        final FakeTriggerEditor view = new FakeTriggerEditor(findActionTrigger("trigger", root).get());
        assertEqual(view.getText(), "eval");

        view.typeText(" as_soon_as");
        assertEqual(view.getText(), "eval as_soon_as");

        undo();
        assertEqual(view.getText(), "eval");

        redo();
        assertEqual(view.getText(), "eval as_soon_as");
    }

    @Test
    public void undoRedoCodeEdition() {
        final Root root = root(actionTrigger("trigger", generateIdForTest(1), content("eval when"), description("")));
        final FakeTriggerEditor view = new FakeTriggerEditor(findActionTrigger("trigger", root).get());
        assertEqual(view.getText(), "eval when");

        view.erase(9);
        view.typeText("eval as_soon_as");
        assertEqual(view.getText(), "eval as_soon_as");

        view.typeText(" ( AIRCRAFT.ALTITUDE > 15000 )");
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 15000 )");

        view.erase(6);
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 1");

        undo();
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 15000 )");

        undo();
        assertEqual(view.getText(), "");

        redo();
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 15000 )");

        view.erase(13);
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTIT");

        undo();
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 15000 )");

        redo();
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTIT");

        undo();
        assertEqual(view.getText(), "eval as_soon_as ( AIRCRAFT.ALTITUDE > 15000 )");

        undo();
        assertEqual(view.getText(), "");
    }

    @Test
    public void testCaretPosition_afterUndoRedoCodeEdition() {
        final Root root = root(actionTrigger("trigger", generateIdForTest(1), content(""), description("")));
        final FakeTriggerEditor view = new FakeTriggerEditor(findActionTrigger("trigger", root).get());

        view.typeText("eval as_soon_as");
        assertEqual(view.getCaretPosition(), 15);

        view.moveCaret(5);
        view.erase(5);
        assertEqual(view.getCaretPosition(), 0);

        view.typeText("eval ");
        assertEqual(view.getText(), "eval as_soon_as");
        assertEqual(view.getCaretPosition(), 5);

        undo();
        assertEqual(view.getText(), "as_soon_as");
        assertEqual(view.getCaretPosition(), 0);

        undo();
        assertEqual(view.getText(), "eval as_soon_as");
        assertEqual(view.getCaretPosition(), 5);

        redo();
        assertEqual(view.getText(), "as_soon_as");
        assertEqual(view.getCaretPosition(), 0);

        redo();
        assertEqual(view.getText(), "eval as_soon_as");
        assertEqual(view.getCaretPosition(), 5);
    }

    @Test
    public void proposalsAreUpdated_afterCaretPositionChanged_whenCompletionIsOpened() {
        final Root root = root(actionTrigger("trigger", generateIdForTest(1), content("eval "), description("")));
        final TriggeringConditionFacade facade = Mockito.spy(TriggeringConditionFacade.get());
        facade.initialize(root);
        final FakeTriggerEditor view = new FakeTriggerEditor(findActionTrigger("trigger", root).get(), facade);
        assertThat(view.getProposals(), isEmpty());

        view.openCompletion();
        assertThat(view.getProposals(), containsAll(
                aProposal("as_soon_as"),
                aProposal("until"),
                aProposal("not_when"),
                aProposal("when")));

        view.typeText("as");
        assertThat(view.getProposals(), containsOnly(aProposal("as_soon_as")));

        view.moveCaret(5);
        assertThat(view.getProposals(), containsAll(
                aProposal("as_soon_as"),
                aProposal("until"),
                aProposal("not_when"),
                aProposal("when")));

        view.closeCompletion();
        assertThat(view.getProposals(), isEmpty());

        view.moveCaret(7);
        assertThat(view.getProposals(), isEmpty());

        view.openCompletion();
        assertThat(view.getProposals(), containsOnly(aProposal("as_soon_as")));

        view.erase(2);
        assertThat(view.getProposals(), containsAll(
                aProposal("as_soon_as"),
                aProposal("until"),
                aProposal("not_when"),
                aProposal("when")));

        undo();
        assertThat(view.getProposals(), containsOnly(aProposal("as_soon_as")));

        redo();
        assertThat(view.getProposals(), containsAll(
                aProposal("as_soon_as"),
                aProposal("until"),
                aProposal("not_when"),
                aProposal("when")));
    }

    static class FakeTriggerEditor {
        private final ActionTriggerEditorModel model;
        private boolean initialized = false;

        FakeTriggerEditor(final ActionTrigger trigger) {
            this(trigger, TriggeringConditionFacade.get());
        }

        FakeTriggerEditor(final ActionTrigger trigger, final TriggeringConditionFacade facade) {
            //when(facade.getAircraftIntervals(new RecognizedAirPicture())).thenReturn(newHashMap());
            model = new ActionTriggerEditorModel(trigger, facade);
            if (!initialized) {
                model.initialize();
                initialized = true;
            }
        }

        String getText() {
            return model.getText();
        }

        int getCaretPosition() {
            return model.getCaretPosition();
        }

        Collection<CompletionProposal> getProposals() {
            return model.getProposals();
        }

        void typeText(final String text) {
            model.requestTextChange(new PlainTextChange(model.getCaretPosition(), "", text));
        }

        void erase(final int erasedLength) {
            final String text = getText();
            final int erasedStartIndex = model.getCaretPosition() - erasedLength;
            final String erased = text.substring(erasedStartIndex, model.getCaretPosition());
            model.requestTextChange(new PlainTextChange(erasedStartIndex, erased, ""));
        }

        void moveCaret(final int newPosition) {
            model.setCaretPosition(newPosition);
        }

        void openCompletion() {
            model.setCompletionIsOpened(true);
        }

        void closeCompletion() {
            model.setCompletionIsOpened(false);
        }
    }
}
package fdit.gui.schemaEditor;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.dsl.ide.CompletionProposal;
import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Root;
import fdit.metamodel.schema.Schema;
import org.eclipse.emf.common.util.BasicEList;
import org.fxmisc.richtext.model.PlainTextChange;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.function.Predicate;

import static fdit.metamodel.element.DirectoryUtils.findSchema;
import static fdit.metamodel.schema.SchemaHelper.content;
import static fdit.metamodel.schema.SchemaHelper.description;
import static fdit.testTools.PredicateAssert.assertEqual;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaEditorModelTest extends FditTestCase {

    private static Predicate<CompletionProposal> aProposal(final String expected) {
        return completionProposal -> completionProposal.getLabel().equals(expected);
    }

    @Test
    public void codeEditorIsInitializedWithScenarioDescription() {
        final Root root = root(schema("scenario", content("alter all"), description("")));
        final FakeScenarioEditor view = new FakeScenarioEditor(findSchema("scenario", root).get());
        assertEqual(view.getText(), "alter all");

        view.typeText("_planes");
        assertEqual(view.getText(), "alter all_planes");

        undo();
        assertEqual(view.getText(), "alter all");

        redo();
        assertEqual(view.getText(), "alter all_planes");
    }

    @Test
    public void undoRedoCodeEdition() {
        final Root root = root(schema("scenario", content("alter all"), description("")));
        final FakeScenarioEditor view = new FakeScenarioEditor(findSchema("scenario", root).get());
        assertEqual(view.getText(), "alter all");

        view.erase(9);
        view.typeText("saturate");
        assertEqual(view.getText(), "saturate");

        view.typeText(" all_planes error");
        assertEqual(view.getText(), "saturate all_planes error");

        view.erase(6);
        assertEqual(view.getText(), "saturate all_planes");

        undo();
        assertEqual(view.getText(), "saturate all_planes error");

        undo();
        assertEqual(view.getText(), "");

        redo();
        assertEqual(view.getText(), "saturate all_planes error");

        view.erase(13); // remove " planes error"
        assertEqual(view.getText(), "saturate all");

        undo();
        assertEqual(view.getText(), "saturate all_planes error");

        redo();
        assertEqual(view.getText(), "saturate all");

        undo();
        assertEqual(view.getText(), "saturate all_planes error");

        undo();
        assertEqual(view.getText(), "");
    }

    @Test
    public void testCaretPosition_afterUndoRedoCodeEdition() {
        final Root root = root(schema("scenario", content(""), description("")));
        final FakeScenarioEditor view = new FakeScenarioEditor(findSchema("scenario", root).get());

        view.typeText("saturate all_planes");
        assertEqual(view.getCaretPosition(), 19);

        view.moveCaret(8);
        view.erase(8); // remove "saturate"
        assertEqual(view.getCaretPosition(), 0);

        view.typeText("alter");
        assertEqual(view.getText(), "alter all_planes");
        assertEqual(view.getCaretPosition(), 5);

        undo();
        assertEqual(view.getText(), " all_planes");
        assertEqual(view.getCaretPosition(), 0);

        undo();
        assertEqual(view.getText(), "saturate all_planes");
        assertEqual(view.getCaretPosition(), 8);

        redo();
        assertEqual(view.getText(), " all_planes");
        assertEqual(view.getCaretPosition(), 0);

        redo();
        assertEqual(view.getText(), "alter all_planes");
        assertEqual(view.getCaretPosition(), 5);
    }

    @Test
    public void proposalsAreUpdated_afterCaretPositionChanged_whenCompletionIsOpened() {
        final Root root = root(schema("scenario", content(""), description("")));
        final AttackScenarioFacade attackScenarioDslFacade = Mockito.spy(new AttackScenarioFacade());
        final FakeScenarioEditor view =
                new FakeScenarioEditor(findSchema("scenario", root).get(), attackScenarioDslFacade);
        assertThat(view.getProposals(), isEmpty());

        view.openCompletion();
        assertThat(view.getProposals(), containsAll(aProposal("alter"), aProposal("saturate")));

        view.typeText("alt");
        assertThat(view.getProposals(), containsOnly(aProposal("alter")));

        view.moveCaret(0);
        assertThat(view.getProposals(), containsAll(aProposal("alter"), aProposal("saturate")));

        view.closeCompletion();
        assertThat(view.getProposals(), isEmpty());

        view.moveCaret(3);
        assertThat(view.getProposals(), isEmpty());

        view.openCompletion();
        assertThat(view.getProposals(), containsOnly(aProposal("alter")));

        view.erase(3); //remove "alt"
        assertThat(view.getProposals(), containsAll(aProposal("alter"), aProposal("saturate")));

        undo();
        assertThat(view.getProposals(), containsOnly(aProposal("alter")));

        redo();
        assertThat(view.getProposals(), containsAll(aProposal("alter"), aProposal("saturate")));
    }

    static class FakeScenarioEditor {
        private final SchemaEditorModel model;

        FakeScenarioEditor(final Schema schema) {
            this(schema, mock(AttackScenarioFacade.class));
        }

        FakeScenarioEditor(final Schema schema, final AttackScenarioFacade dslFacade) {
            when(dslFacade.getAST()).thenReturn(new BasicEList<>());
            model = new SchemaEditorModel(schema, dslFacade);
            model.initialize();
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
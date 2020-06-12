package fdit.gui.filterEditor.tabs.filter;

import fdit.gui.application.FditTestCase;
import fdit.ltlcondition.ide.LTLConditionFacade;
import fdit.metamodel.filter.LTLFilter;
import fdit.testTools.Saver;
import org.junit.Test;

import static fdit.gui.filterEditor.tabs.filter.FilterEditionModelHelper.*;
import static fdit.metamodel.FditElementHelper.defaultIdForTest;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterEditionModelTest extends FditTestCase {

    @Test
    public void testRestoreModelFromFilter() {
        final Saver<LTLFilter> saver = create();
        root(ltlFilter("myFilter", saver,
                defaultIdForTest(),
                "myDescription",
                "eval ICAO == \"39AC47\""));

        final FilterEditionModel filterEditionModel = new FilterEditionModel(LTLConditionFacade.get(), defaultIdForTest());
        filterEditionModel.setEditedFilter(saver.get());
        filterEditionModel.restoreModelFromEditedFilter();

        assertThat(filterEditionModel,
                aFilterEditionModel(
                        aName("myFilter"),
                        aDescription("myDescription"),
                        aContent("eval ICAO == \"39AC47\"")));
    }

    @Test
    public void dataChangeFilter() {
        final FilterEditionModel filterEditionModel =
                new FilterEditionModel(LTLConditionFacade.get(), defaultIdForTest());
        filterEditionModel.nameProperty().set("myFilter");
        filterEditionModel.descriptionProperty().set("myDescription");
        filterEditionModel.contentProperty().set("eval F(ALTITUDE > 30000)");

        assertTrue(filterEditionModel.dataChanged());
        filterEditionModel.save();
        assertFalse(filterEditionModel.dataChanged());

        filterEditionModel.nameProperty().set("monFiltre");
        assertTrue(filterEditionModel.dataChanged());
        filterEditionModel.nameProperty().set("myFilter");
        assertFalse(filterEditionModel.dataChanged());

        filterEditionModel.descriptionProperty().set("maDescription");
        assertTrue(filterEditionModel.dataChanged());
        filterEditionModel.descriptionProperty().set("myDescription");
        assertFalse(filterEditionModel.dataChanged());

        filterEditionModel.contentProperty().set("eval G(ALTITUDE > 30000)");
        assertTrue(filterEditionModel.dataChanged());
        filterEditionModel.contentProperty().set("eval F(ALTITUDE > 30000)");
        assertFalse(filterEditionModel.dataChanged());
    }
}
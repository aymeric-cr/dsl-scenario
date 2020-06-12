package fdit.ltlcondition.ide

import org.eclipse.xtext.testing.AbstractLanguageServerTest
import org.junit.Test
import org.junit.Assert
import org.junit.Ignore

class LTLConditionServerTest extends AbstractLanguageServerTest {
    new() {
        super("LTLCondition")
    }

    @Test
    def void test01_initialization() {
        val capabilities = initialize().capabilities
        Assert.assertTrue(
                capabilities.definitionProvider && capabilities.documentFormattingProvider)
    }

    //TODO: Xtext-LSP: FIND OUT WHY IT'S NOT WORKING

    @Test @Ignore
    def void test02_openFile() {
        initialize()

        val file = 'test02.ltlc'.writeFile("")
        print(file)
        file.open("eval G(ALTITUDE > 1000)")

        Assert.assertTrue("There're issues in file 'test02.ltlc'.", diagnostics.get(file).empty)
    }
}
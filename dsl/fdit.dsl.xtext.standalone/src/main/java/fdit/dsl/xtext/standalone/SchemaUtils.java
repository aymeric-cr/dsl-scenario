package fdit.dsl.xtext.standalone;

import fdit.metamodel.element.Directory;
import fdit.metamodel.schema.Schema;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.element.DirectoryUtils.gatherAllTextualScenarios;
import static fdit.tools.stream.StreamUtils.filter;

public final class SchemaUtils {

    private SchemaUtils() {
    }

    public static boolean isScenarioExecutable(final Schema schema) {
        final DslFacade dslFacade = new AttackScenarioDslFacade();
        dslFacade.initialize();
        try {
            dslFacade.parse(schema.getContent());
            return dslFacade.getParseErrors().isEmpty();
        } catch (final IOException e) {
            return false;
        }
    }

    public static Collection<Schema> gatherAllExecutableScenarios(final Directory father) {
        final Collection<Schema> schemas = newArrayList();
        final Collection<Schema> textualSchemas = gatherAllTextualScenarios(father);
        schemas.addAll(filter(textualSchemas, SchemaUtils::isScenarioExecutable));
        return schemas;
    }
}

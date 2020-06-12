package fdit.triggcondition.ide

import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator

import static org.eclipse.emf.common.util.URI.createURI
import java.io.ByteArrayInputStream
import org.eclipse.xtext.util.CancelIndicator
import java.util.Collection
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.rap.RecognizedAirPicture
import com.google.inject.Inject
import static fdit.tools.stream.StreamUtils.filter
import static fdit.tools.stream.StreamUtils.mapping
import org.eclipse.xtext.diagnostics.AbstractDiagnostic
import org.eclipse.emf.ecore.util.Diagnostician
import org.eclipse.emf.common.util.BasicDiagnostic
import org.eclipse.xtext.util.TextRegion
import java.util.Optional
import org.eclipse.emf.ecore.EObject
import fdit.triggcondition.interpreter.TriggeringConditionInterpreter
import fdit.triggcondition.triggeringCondition.Model
import fdit.metamodel.aircraft.TimeInterval
import fdit.metamodel.recording.Recording
import com.google.common.collect.Lists
import fdit.metamodel.element.Directory

class TriggeringConditionFacade  {

    var private static TriggeringConditionFacade single_instance = null

    @Inject TriggeringConditionInterpreter interpreter
    @Inject XtextResourceSet xtextResourceSet
    @Inject XtextResource xtextResource
    @Inject ContentAssistService contentAssistService
    @Inject ISemanticHighlightingCalculator semanticHighlightingCalculator

    //    var Document document
    var String currentFilter
    var BasicDiagnostic diagnostic
    var public Directory root

    def private TriggeringConditionFacade() {
    }

    def static TriggeringConditionFacade get() {
        if(single_instance === null) single_instance = new TriggeringConditionFacade
        single_instance
    }

    def void initialize(Directory root) {
        this.root = root
        val injector = new TriggeringConditionIdeSetup().createInjectorAndDoEMFRegistration
        xtextResourceSet = injector.getInstance(XtextResourceSet)
        xtextResourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
        xtextResource = xtextResourceSet.createResource(createURI("fdit:/triggconditionfacade.triggc")) as XtextResource
        contentAssistService = injector.getInstance(ContentAssistService)
        semanticHighlightingCalculator = injector.getInstance(ISemanticHighlightingCalculator)
        interpreter = injector.getInstance(TriggeringConditionInterpreter)
        //        document = injector.getInstance(Document)
    }

    def void parse(String filter) {
        currentFilter = filter
        var ByteArrayInputStream filterInputStream
        try {
            filterInputStream = new ByteArrayInputStream(currentFilter.bytes)
        } finally {
            if(xtextResource.isLoaded()) {
                xtextResource.unload
            }
            xtextResource.load(filterInputStream, xtextResourceSet.loadOptions)
            if(!xtextResource.getContents.isNullOrEmpty && parseErrors.isNullOrEmpty) {
                diagnostic = Diagnostician.INSTANCE.validate(xtextResource.getContents().get(0)) as BasicDiagnostic
            }
        }
    }

    def Collection<SyntaxFault> getParseErrors() {
        mapping(
                filter(xtextResource.errors, AbstractDiagnostic),
                [ diagnostic | new SyntaxFault(diagnostic.offset, diagnostic.line, diagnostic.column,
                    diagnostic.length, diagnostic.message)]
        )
    }

    def BasicDiagnostic getValidationErrors() {
        return diagnostic
    }

    def boolean isValidated() {
        diagnostic.severity < 4
    }

    def getAircraftIntervals(RecognizedAirPicture rap, Directory root, Recording recording) {
        getAircraftIntervals(rap,rap.aircrafts,root,recording)
    }

    def getAircraftIntervals(RecognizedAirPicture rap,Directory root, TimeInterval interval, Recording recording) {
        getAircraftIntervals(rap,rap.aircrafts, root, interval, recording)
    }

    def getAircraftIntervals(RecognizedAirPicture rap, Collection<Aircraft> targets, Directory root, Recording recording) {
        if(xtextResource.isLoaded  && getParseErrors.empty && isValidated) {
            var model = xtextResource.contents.get(0) as Model
            interpreter.getAlterationIntervals(model.expression,rap,targets, root,recording)
        } else if(!(getParseErrors.empty || isValidated)) {
            throw new Exception("cannot interpret filter as there are syntax/validation errors")
        }
    }

    def getAircraftIntervals(RecognizedAirPicture rap, Collection<Aircraft> targets, Directory root, TimeInterval interval, Recording recording) {
        if(xtextResource.isLoaded  && getParseErrors.empty && isValidated) {
            var model = xtextResource.contents.get(0) as Model
            interpreter.getAlterationIntervals(model.expression,rap,targets,root,interval, recording)
        } else if(!(getParseErrors.empty || isValidated)) {
            throw new Exception("cannot interpret filter as there are syntax/validation errors")
        }
    }

    def void shutdown() {
        contentAssistService.shutdown
    }

    def getProposals(int caretOffset, int selectionLength, int limit) {
        contentAssistService.createProposals(xtextResource,
        currentFilter,
        new TextRegion(caretOffset, selectionLength),
        caretOffset,
        limit
        )
    }

    def getHighlightingStyles() {
        val stylesPositions = Lists.newArrayList;
        semanticHighlightingCalculator.provideHighlightingFor(xtextResource,
        [offset, length, id | stylesPositions.add(new StylesPosition(offset, length, Lists.newArrayList(id)))],
        CancelIndicator.NullImpl)
        return stylesPositions
    }

    def Optional<EObject> getCurrentNode(int caretOffset, int selectionLength) {
        val contentAssistContexts =
            contentAssistService.getContexts(
                    xtextResource,
                    currentFilter,
                    new TextRegion(caretOffset, selectionLength),
                    caretOffset)
        if(contentAssistContexts.length > 0) {
            Optional.ofNullable(contentAssistContexts.get(0).getCurrentModel())
        } else Optional.empty
    }
}
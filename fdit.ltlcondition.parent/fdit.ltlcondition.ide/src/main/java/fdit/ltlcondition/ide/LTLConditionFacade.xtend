package fdit.ltlcondition.ide

import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator

import static org.eclipse.emf.common.util.URI.createURI
import java.io.ByteArrayInputStream
import org.eclipse.xtext.util.CancelIndicator
import java.util.Collection
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.ltlcondition.interpreter.LTLConditionInterpreter
import com.google.inject.Inject
import static fdit.tools.stream.StreamUtils.filter
import static fdit.tools.stream.StreamUtils.mapping
import org.eclipse.xtext.diagnostics.AbstractDiagnostic
import org.eclipse.emf.ecore.util.Diagnostician
import org.eclipse.emf.common.util.BasicDiagnostic
import fdit.ltlcondition.lTLCondition.Model
import org.eclipse.xtext.util.TextRegion
import java.util.Optional
import org.eclipse.emf.ecore.EObject
import fdit.metamodel.recording.Recording
import fdit.ltlcondition.validation.LTLConditionDataAnalysis
import fdit.metamodel.element.Directory
import fdit.metamodel.element.DirectoryUtils

class LTLConditionFacade  {

    var private static LTLConditionFacade single_instance = null

    @Inject LTLConditionInterpreter interpreter
    @Inject XtextResourceSet xtextResourceSet
    @Inject XtextResource xtextResource
    @Inject ContentAssistService contentAssistService
    @Inject ISemanticHighlightingCalculator semanticHighlightingCalculator
    var LTLConditionDataAnalysis dataAnalyzer = new LTLConditionDataAnalysis

    //    var Document document
    var String currentFilter
    var BasicDiagnostic diagnostic
    var RecognizedAirPicture rap
    var String dataAnalysis
    var boolean is_initialized
    var public Directory root

    def private LTLConditionFacade() {
    }

    def static LTLConditionFacade get() {
        if(single_instance === null) {
            single_instance = new LTLConditionFacade
            single_instance.is_initialized = false
        }
        single_instance
    }

    def void initialize(Directory root) {
        val injector = new LTLConditionIdeSetup().createInjectorAndDoEMFRegistration
        this.root = root
        xtextResourceSet = injector.getInstance(XtextResourceSet)
        xtextResourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
        xtextResource = xtextResourceSet.createResource(createURI("fdit:/ltlconditionfacade.ltlc")) as XtextResource
        contentAssistService = injector.getInstance(ContentAssistService)
        semanticHighlightingCalculator = injector.getInstance(ISemanticHighlightingCalculator)
        interpreter = injector.getInstance(LTLConditionInterpreter)
        dataAnalysis = ""
        is_initialized = true
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
                diagnostic = Diagnostician.INSTANCE.validate( xtextResource.getContents().get(0)) as BasicDiagnostic
                if(diagnostic.children.empty) {
                    dataAnalysis = analyzeData
                }
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

    def String  getAnalysisErrors() {
        return dataAnalysis;
    }

    def String analyzeData() {
        if(xtextResource.isLoaded  && getParseErrors.empty && isValidated) {
            var model = xtextResource.contents.get(0) as Model
            dataAnalyzer.analyze(model.expression, DirectoryUtils.gatherAllZones(root))
        } else ""
    }

    def String analyzeData(RecognizedAirPicture rap, Recording recording) {
        if(xtextResource.isLoaded  && getParseErrors.empty && isValidated) {
            this.rap = rap
            var model = xtextResource.contents.get(0) as Model
            dataAnalyzer.analyze(model.expression, this.rap, recording)
        } else ""
    }

    def String transformData() {
        /*if (xtextResource.isLoaded && getParseErrors.empty && isValidated && isAnalyzed) {
            var epsiModel = new InMemoryEmfModel(xtextResource)
            epsiModel.load
            var IEwlModule ewl = new EwlModule
            ewl.parse(LTLConditionRewriter.REMOVE_NEGATION)
            if (!ewl.parseProblems.isEmpty) {
                "Syntax errors found in validation logic. Exiting."
            } else {
                ewl.context.modelRepository.addModel(epsiModel)
                ewl.execute
                ""
            }
        } else*/ ""
    }

    def boolean isValidated() {
        diagnostic.severity < 4
    }

    def boolean isAnalyzed() {
        dataAnalysis.equals("")
    }

    def filterAircraft(RecognizedAirPicture rap, Recording recording) {
        if(xtextResource.isLoaded  && getParseErrors.empty && isValidated && isAnalyzed) {
            transformData
            this.rap = rap
            var model = xtextResource.contents.get(0) as Model
            interpreter.interpret(model.expression, this.rap, recording)
        } else if(!(getParseErrors.empty || isValidated || isAnalyzed)) {
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
        val stylesPositions = newArrayList()
        semanticHighlightingCalculator.provideHighlightingFor(xtextResource,
        [offset, length, id | stylesPositions.add(new StylesPosition(offset, length, newArrayList(id)))],
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
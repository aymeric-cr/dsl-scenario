package fdit.ltlcondition.ide

import org.eclipse.xtext.ide.server.contentassist.ContentAssistService

class LTLConditionContentAssistService extends ContentAssistService {

    /*    def completionList(XtextResource resource,  String text, TextRegion selection, int caretOffset, int limit) {
            val result = new CompletionList
            // we set isInComplete to true, so we get asked always, which is the best match to the expected behavior in Xtext
            result.setIsIncomplete(true);

            val acceptor = proposalAcceptorProvider.get
            val caretOffset = document.getOffSet(params.position)
    //        val caretPosition = params.position
            val position = new TextRegion(caretOffset, 0)
            try {
                createProposals(text, position, caretOffset, resource, acceptor)
            } catch (Throwable t) {
                if (!operationCanceledManager.isOperationCanceledException(t)) {
                    throw t
                }
            }
            acceptor.getEntries().forEach [ it, idx |
                val item = toCompletionItem(caretOffset, caretPosition, document)
                item.sortText = Strings.padStart(Integer.toString(idx), 5, "0")
                result.items += item
            ]
            return result
        }*/

}
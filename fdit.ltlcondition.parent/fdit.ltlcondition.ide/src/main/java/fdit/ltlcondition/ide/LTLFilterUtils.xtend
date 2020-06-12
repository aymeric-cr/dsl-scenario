package fdit.ltlcondition.ide

import java.util.Collection
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.filter.LTLFilter
import fdit.metamodel.element.Directory
import fdit.metamodel.element.DirectoryUtils
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.recording.Recording

class LTLFilterUtils {

    def static Collection<Aircraft> filterAircrafts(Recording recording, LTLFilter ltlFilter, Directory root) {
        var ltlConditionFacade = LTLConditionFacade.get();
        try {
            ltlConditionFacade.initialize(root)
            ltlConditionFacade.parse(ltlFilter.getContent())
            if(ltlConditionFacade.isValidated()) {
                var rap = new RecognizedAirPicture()
                rap.addAircrafts(recording.aircrafts)
                rap.addZones(DirectoryUtils.gatherAllZones(root))
                return ltlConditionFacade.filterAircraft(rap, recording)
            }
            return newArrayList()
        } finally {
            ltlConditionFacade.shutdown()
        }
    }
}
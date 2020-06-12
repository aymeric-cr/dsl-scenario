package fdit.ltlcondition.validation


import fdit.ltlcondition.lTLCondition.AndOrExpression
import fdit.ltlcondition.lTLCondition.Area
import fdit.ltlcondition.lTLCondition.BooleanAlways
import fdit.ltlcondition.lTLCondition.BooleanEventually
import fdit.ltlcondition.lTLCondition.BooleanNegation
import fdit.ltlcondition.lTLCondition.Expression
import fdit.ltlcondition.lTLCondition.ReferencedArea

import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.zone.Zone
import fdit.metamodel.recording.Recording

import fdit.tools.i18n.MessageTranslator
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator

import java.util.Collection

class LTLConditionDataAnalysis {

    public var Aircraft current_aircraft
    public var RecognizedAirPicture rap
    public var Collection<Zone> zones
    public var Recording recording
    private var StringBuilder errorBuilder
    private static val MessageTranslator TRANSLATOR = createMessageTranslator(LTLConditionDataAnalysis)

    def String analyze(Expression e, Collection<Zone> zones) {
        this.errorBuilder = new StringBuilder
        this.zones = zones
        this.errorBuilder.append(e.analyze)
        return this.errorBuilder.toString
    }

    def String analyze(Expression e, RecognizedAirPicture rap, Recording recording) {
        this.rap = rap
        this.recording = recording
        this.errorBuilder = new StringBuilder
        for(aircraft : rap.getAircrafts) {
            this.errorBuilder.append(aircraft.analyze(e))
        }
        return this.errorBuilder.toString
    }

    private def Object analyze(Aircraft aicraft, Expression e) {
        current_aircraft = aicraft
        e.analyze
    }

    private def String analyze(Expression e) {
        switch (e){
            BooleanAlways: e.expression.analyze
            BooleanEventually: e.expression.analyze
            BooleanNegation: e.expression.analyze
            AndOrExpression: e.analyzeAndOr.toString
            Area: e.analyzeArea.toString
            default: ""
        }
    }

    private def Object analyzeArea(Area area) {
        if(area.area instanceof ReferencedArea) {
            var zone_name = (area.area as ReferencedArea).name
            if(!zoneExists(zone_name)) {
                return TRANSLATOR.getMessage("error.unknownZone", zone_name) + '\n'
            }
        }
        ""
    }

    private def Object analyzeAndOr(AndOrExpression andor) {
        val StringBuilder errors = new StringBuilder
        errors.append(andor.left.analyze)
        errors.append(andor.right.analyze)
        return errors
    }

    private def boolean zoneExists(String zone_name) {
        for(zone : zones) {
            if(zone.name == zone_name) return true
        }
        false
    }
}
package fdit.ltlcondition.interpreter

import fdit.database.AircraftRequests
import fdit.ltlcondition.lTLCondition.ASTAreaPositionType
import fdit.ltlcondition.lTLCondition.ASTVertices
import fdit.ltlcondition.lTLCondition.AircraftDynamicProperty
import fdit.ltlcondition.lTLCondition.AircraftStaticProperty
import fdit.ltlcondition.lTLCondition.AndOrExpression
import fdit.ltlcondition.lTLCondition.Area
import fdit.ltlcondition.lTLCondition.BooleanAlways
import fdit.ltlcondition.lTLCondition.BooleanEventually
import fdit.ltlcondition.lTLCondition.BooleanLiteral
import fdit.ltlcondition.lTLCondition.BooleanNegation
import fdit.ltlcondition.lTLCondition.Different
import fdit.ltlcondition.lTLCondition.Equals
import fdit.ltlcondition.lTLCondition.Expression
import fdit.ltlcondition.lTLCondition.DoubleLiteral
import fdit.ltlcondition.lTLCondition.GreaterThan
import fdit.ltlcondition.lTLCondition.GreaterThanOrEq
import fdit.ltlcondition.lTLCondition.IntLiteral
import fdit.ltlcondition.lTLCondition.LowerThan
import fdit.ltlcondition.lTLCondition.LowerThanOrEq
import fdit.ltlcondition.lTLCondition.Prism
import fdit.ltlcondition.lTLCondition.ReferencedArea
import fdit.ltlcondition.lTLCondition.StringLiteral
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.aircraft.AircraftCriterion
import fdit.metamodel.coordinates.Coordinates
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.recording.Recording
import fdit.metamodel.zone.FditPolygon
import fdit.metamodel.zone.Zone
import fdit.metamodel.zone.ZoneUtils
import java.util.ArrayList
import org.apache.commons.lang.StringUtils
import org.apache.commons.collections4.CollectionUtils
import fdit.ltlcondition.lTLCondition.Model
import java.util.List
import java.util.LinkedList
import fdit.metamodel.aircraft.OutOfDateException
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID
import fdit.ltlcondition.lTLCondition.ArithmExpr
import fdit.ltlcondition.lTLCondition.Plus
import fdit.ltlcondition.lTLCondition.Minus
import fdit.ltlcondition.lTLCondition.MulOrDiv
import java.io.Serializable
import fdit.ltlcondition.lTLCondition.CommonStaticProperty

class LTLConditionInterpreter {

    public enum temporal{ALWAYS,EVENTUALLY,NONE}

    public var Aircraft current_aircraft
    public var RecognizedAirPicture rap
    public var Recording recording
    public var long TIME_INTERVAL = 1000 // aircraft state query interval
    public var List<Long> time_intervals


    def ArrayList<Aircraft> interpret(Expression e, RecognizedAirPicture rap, Recording recording) {
        this.rap = rap
        this.recording = recording
        var selected_aircrafts = new ArrayList<Aircraft>
        for(aircraft : rap.getAircrafts) {
            if(aircraft.interpret(e)) selected_aircrafts.add(aircraft)
            time_intervals = null
        }
        return selected_aircrafts
    }

    def boolean interpret(Aircraft aicraft, Expression e) {
        current_aircraft = aicraft
        e.interpret(temporal.NONE) as Boolean
    }

    private def createIntervals(Aircraft aircraft) {
        var long time_count = current_aircraft.timeOfFirstAppearance
        var long time_end = current_aircraft.timeOfLastAppearance
        time_intervals = new LinkedList<Long>
        while(time_count < time_end) {
            time_intervals.add(time_count)
            time_count += TIME_INTERVAL
            if(time_count > time_end && time_count < (time_end + TIME_INTERVAL)) time_count = time_end
        }
        time_intervals.add(current_aircraft.timeOfLastAppearance)
    }

    private def Object interpret(Expression e, temporal t) {
        switch (e){
            BooleanNegation: {
                var boolnegresult = e.expression.interpret(t)
                if(boolnegresult instanceof List) {
                    if(time_intervals === null) createIntervals(current_aircraft)
                    CollectionUtils.subtract(time_intervals,boolnegresult as List<Long>)
                } else !(boolnegresult as Boolean) }
            AndOrExpression:
                e.interpretAndOr(t)
            LowerThan,GreaterThan,Equals,Different,LowerThanOrEq,GreaterThanOrEq:
                e.interpretSingleExpr(t)
            BooleanAlways: e.interpretAlways
            BooleanEventually: e.interpretEventually
            Area: e.interpretArea(t)
            IntLiteral: e.value
            DoubleLiteral: e.value
            StringLiteral: e.value
            BooleanLiteral: Boolean.parseBoolean(e.value)
        }
    }

    private def interpretArea(Area area, temporal t) {
        val position = area.eventType
        val Zone zone =
            switch (area.area) {
                Prism: (area.area as Prism).createPrism
                ReferencedArea: rap.getZone((area.area as ReferencedArea).name)
            }
        if(zone === null) throw new Exception("the zone "+(area.area as ReferencedArea).name+" does not exist")
        if(area.isInsideDisjConj) {
            return filterZoneList(zone,t,position)
        }else {
            val boolean inside = if(position === ASTAreaPositionType.INSIDE) true else false
            if(t === temporal.EVENTUALLY) ZoneUtils.aircraftEventuallyInZone(zone,current_aircraft,inside,TIME_INTERVAL)
            else ZoneUtils.aircraftAlwaysInZone(zone,current_aircraft,inside,TIME_INTERVAL)
        }
    }

    private def List<Long> filterZoneList(Zone zone, temporal t, ASTAreaPositionType type) {
        var long time_count = current_aircraft.timeOfFirstAppearance
        var long time_end = current_aircraft.timeOfLastAppearance
        var List<Long> truetimes = new LinkedList<Long>
        while(time_count <= time_end) {
            val boolean result = try {
                ZoneUtils.positionInZone(zone, current_aircraft.query(time_count,AircraftCriterion.LATITUDE),
                current_aircraft.query(time_count,AircraftCriterion.LONGITUDE),
                current_aircraft.query(time_count,AircraftCriterion.ALTITUDE))
            } catch(OutOfDateException ex) {if(t.equals(temporal.EVENTUALLY)) false else /* always */ true}
            if(result && type === ASTAreaPositionType.INSIDE) {truetimes.add(time_count)}
            else if(!result && type === ASTAreaPositionType.OUTSIDE) {truetimes.add(time_count)}
            time_count += TIME_INTERVAL
            /* Histoire de bien rechoper la valeur à la borne de fin */
            if(time_count > time_end && time_count < (time_end + TIME_INTERVAL)) time_count = time_end
        }
        return truetimes
    }

    private def createPrism(Prism cyl) {
        new FditPolygon("prism",randomUUID(),cyl.lowerAltitude.numberValue,cyl.upperAltitude.numberValue
        ,createFditCoordinates(cyl.coordinates))
    }

    private def createFditCoordinates(ASTVertices vertices) {
        val fdit_coords = newArrayList
        for(coords: vertices.vertices)
            fdit_coords.add(new Coordinates(coords.latitude.numberValue, coords.longitude.numberValue))
        return fdit_coords
    }

    private def dispatch evalPlusExpr(Integer left, Integer right) {
        return left + right
    }

    private def dispatch evalPlusExpr(Double left, Integer right) {
        return left + right
    }

    private def dispatch evalPlusExpr(Integer left, Double right) {
        return left + right
    }

    private def dispatch evalPlusExpr(Double left, Double right) {
        return left + right
    }

    private def dispatch evalPlusExpr(String left, String right) {
        return left + right
    }

    private def dispatch evalMinusExpr(Integer left, Integer right) {
        return left - right
    }

    private def dispatch evalMinusExpr(Double left, Integer right) {
        return left - right
    }

    private def dispatch evalMinusExpr(Integer left, Double right) {
        return left - right
    }

    private def dispatch evalMinusExpr(Double left, Double right) {
        return left - right
    }

    private def dispatch evalMulExpr(Integer left, Integer right) {
        return left * right
    }

    private def dispatch evalMulExpr(Double left, Integer right) {
        return left * right
    }

    private def dispatch evalMulExpr(Integer left, Double right) {
        return left * right
    }

    private def dispatch evalMulExpr(Double left, Double right) {
        return left * right
    }

    private def dispatch evalDivExpr(Integer left, Integer right) {
        return left / right
    }

    private def dispatch evalDivExpr(Double left, Integer right) {
        return left / right
    }

    private def dispatch evalDivExpr(Integer left, Double right) {
        return left / right
    }

    private def dispatch evalDivExpr(Double left, Double right) {
        return left / right
    }

    private def Object evaluateArithmExpr(ArithmExpr ae) {
        switch (ae) {
            Plus: evalPlusExpr(ae.left.evaluateArithmExpr,ae.right.evaluateArithmExpr)
            Minus: evalMinusExpr(ae.left.evaluateArithmExpr as Number,ae.right.evaluateArithmExpr as Number)
            MulOrDiv: {
                val left = ae.left.evaluateArithmExpr
                val right = ae.right.evaluateArithmExpr

                if (ae.op == '*')
                    evalMulExpr(left as Number,right as Number)
                else
                    evalDivExpr(left as Number,right as Number)
            }
            StringLiteral: ae.value
            IntLiteral: ae.value
            DoubleLiteral: ae.value
            BooleanLiteral : Boolean.parseBoolean(ae.value)
            CommonStaticProperty: switch(ae.context) {
                case RAP: {
                    switch (ae.value) {
                        case MIN_ALTITUDE : this.rap.minAltitude
                        case MAX_ALTITUDE : this.rap.maxAltitude
                        case MEAN_ALTITUDE : this.rap.meanAltitude
                        case MIN_LATITUDE : this.rap.minLatitude
                        case MAX_LATITUDE : this.rap.maxLatitude
                        case MEAN_LATITUDE : this.rap.meanLatitude
                        case MIN_LONGITUDE: this.rap.minLongitude
                        case MAX_LONGITUDE: this.rap.maxLongitude
                        case MEAN_LONGITUDE : this.rap.meanLongitude
                        case MIN_GROUNDSPEED : this.rap.minGroundSpeed
                        case MAX_GROUNDSPEED : this.rap.maxGroundSpeed
                        case MEAN_GROUNDSPEED : this.rap.meanGroundSpeed
                    }
                }
                case AIRCRAFT:
                    AircraftRequests.fetchAircraftStaticPropertyValue(current_aircraft, ae.retrieveAircraftCriterion).get
            }
        }
    }

    //TODO refactor these functions in an Utils class
    def private dispatch getNumberValue(IntLiteral il) {
        return il.value
    }

    def private dispatch getNumberValue(DoubleLiteral fl) {
        return fl.value
    }

    private def interpretAlways(BooleanAlways always) {
        val result = always.expression.interpret(temporal.ALWAYS)
        if(result instanceof List) {
            if(time_intervals === null) createIntervals(current_aircraft)
            (result as List<Long>).size === time_intervals.size
        } else result
    }

    private def interpretEventually(BooleanEventually eventually) {
        val result = eventually.expression.interpret(temporal.EVENTUALLY)
        if(result instanceof List)
            (result as List<Long>).size > 0
        else result
    }

    private def interpretSingleExpr(Expression expr, temporal t) {
        var aircraftProperty = retrieveAircraftProp(expr).retrieveAircraftCriterion
        switch (t) {
            case ALWAYS:
                expr.evaluateAlwaysExpression(aircraftProperty)
            case EVENTUALLY:
                expr.evaluateEventuallyExpression(aircraftProperty)
            case NONE:
                expr.evaluateNoneExpression(aircraftProperty)
        }
    }

    private def evaluateAlwaysExpression(Expression expr, AircraftCriterion property) {
        if(expr.isInsideDisjConj || expr.isInsideNegation) {
            return filterTrueExpressions(expr,property)
        } else {
            var long time_count = current_aircraft.timeOfFirstAppearance
            var long time_end = current_aircraft.timeOfLastAppearance
            while(time_count <= time_end) {
                val boolean result = try {
                    expr.evaluateSingleExpression(current_aircraft.query(time_count,property), expr.retrieveComparedValue.evaluateArithmExpr as Serializable)
                } catch(OutOfDateException ex) {true} //TODO optim ne plus itérer si on en arrive là
                if(!result) return false
                else time_count += TIME_INTERVAL
                if(time_count > time_end && time_count < (time_end + TIME_INTERVAL)) time_count = time_end
            }
            true
        }
    }

    private def evaluateEventuallyExpression(Expression expr, AircraftCriterion property) {
        if(expr.isInsideDisjConj || expr.isInsideNegation) {
            return filterTrueExpressions(expr,property)
        } else {
            var long time_count = current_aircraft.timeOfFirstAppearance
            var long time_end = current_aircraft.timeOfLastAppearance
            while(time_count <= time_end) {
                val boolean result = try {
                    expr.evaluateSingleExpression(current_aircraft.query(time_count,property), expr.retrieveComparedValue.evaluateArithmExpr as Serializable)
                } catch(OutOfDateException ex) {false}
                if(result) return true
                else time_count += TIME_INTERVAL
                if(time_count > time_end && time_count < (time_end + TIME_INTERVAL)) time_count = time_end
            }
            false
        }
    }

    private def evaluateNoneExpression(Expression expr, AircraftCriterion property) {
        var property_value = AircraftRequests.fetchAircraftStaticPropertyValue(current_aircraft,property).get
        expr.evaluateSingleExpression(property_value as Serializable, expr.retrieveComparedValue.evaluateArithmExpr as Serializable)
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Number left, Integer right) {
        left.doubleValue < right
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Number left, Double right) {
        left.doubleValue < right
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Integer left, Number right) {
        left < right.doubleValue
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Double left, Number right) {
        left < right.doubleValue
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Number left, Integer right) {
        left.doubleValue <= right
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Number left, Double right) {
        left.doubleValue <= right
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Integer left, Number right) {
        left <= right.doubleValue
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Double left, Number right) {
        left <= right.doubleValue
    }

    private def dispatch evaluateSingleExpression(Equals op, Number left, Integer right) {
        left.doubleValue == right
    }

    private def dispatch evaluateSingleExpression(Equals op, Number left, Double right) {
        left.doubleValue == right
    }

    private def dispatch evaluateSingleExpression(Equals op, Integer left, Number right) {
        left == right.doubleValue
    }

    private def dispatch evaluateSingleExpression(Equals op, Double left, Number right) {
        left == right.doubleValue
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Number left, Integer right) {
        left.doubleValue > right
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Number left, Double right) {
        left.doubleValue > right
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Integer left, Number right) {
        left > right.doubleValue
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Double left, Number right) {
        left > right.doubleValue
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Number left, Integer right) {
        left.doubleValue >= right
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Number left, Double right) {
        left.doubleValue >= right
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Integer left, Number right) {
        left >= right.doubleValue
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Double left, Number right) {
        left >= right.doubleValue
    }

    private def dispatch evaluateSingleExpression(Different op, Number left, Integer right) {
        left.doubleValue != right
    }

    private def dispatch evaluateSingleExpression(Different op, Number left, Double right) {
        left.doubleValue != right
    }

    private def dispatch evaluateSingleExpression(Different op, Integer left, Number right) {
        left != right.doubleValue
    }

    private def dispatch evaluateSingleExpression(Different op, String left, String right) {
        !left.equalsIgnoreCase(right)
    }

    private def dispatch evaluateSingleExpression(Equals op, String left, String right) {
        if(right.endsWith("*")) {
            if(right.length == 1) return true
            else if(right.startsWith("*")) {
                StringUtils.containsIgnoreCase(left,right.substring(1,right.length-2))
            } else {
                left.regionMatches(false, 0, right, 0, right.length-1)
            }
        } else if(right.startsWith("*")) {
            left.regionMatches(false, left.length - (right.length - 1), right, 1, right.length-1)
        } else left.equalsIgnoreCase(right)
    }

    private def interpretAndOr(AndOrExpression andor, temporal t) {

        var leftres = andor.left.interpret(t)
        var rightres = andor.right.interpret(t)
        switch(t) {
            case NONE:
                return switch (andor.op) {
                    case "and": {
                        (leftres as Boolean) && (rightres as Boolean)
                    }
                    case "or": {
                        (leftres as Boolean) || (rightres as Boolean)
                    }
                }
            case ALWAYS:
                return switch (andor.op) {
                    case "and": {
                        CollectionUtils.intersection((leftres as List<Long>), (rightres as List<Long>))
                    }
                    case "or": {
                        CollectionUtils.union((leftres as List<Long>),(rightres as List<Long>))
                    }
                }
            case EVENTUALLY:
                return switch (andor.op) {
                    case "and": {
                        CollectionUtils.intersection((leftres as List<Long>), (rightres as List<Long>))
                    }
                    case "or": {
                        CollectionUtils.union((leftres as List<Long>),(rightres as List<Long>))
                    }
                }
        }
    }

    private def List<Long> filterTrueExpressions(Expression expr, AircraftCriterion property) {
        var long time_count = current_aircraft.timeOfFirstAppearance
        var long time_end = current_aircraft.timeOfLastAppearance
        var List<Long> truetimes = new LinkedList<Long>
        while(time_count <= time_end) {
            val boolean result = try {
                expr.evaluateSingleExpression(current_aircraft.query(time_count,property), expr.retrieveComparedValue.evaluateArithmExpr as Serializable)
            } catch(OutOfDateException ex) {false}
            if(result) truetimes.add(time_count)
            time_count += TIME_INTERVAL
            if(time_count > time_end && time_count < (time_end + TIME_INTERVAL)) time_count = time_end
        }
        return truetimes
    }

    private def Boolean isComplex(Expression e) {
        switch (e){
            BooleanNegation: (e.expression.isComplex)
            AndOrExpression:
                true
            LowerThan,GreaterThan,Equals,Different,LowerThanOrEq,GreaterThanOrEq:
                false
            BooleanAlways: !(e.expression.isComplex)
            BooleanEventually: !(e.expression.isComplex)
            Area, IntLiteral, DoubleLiteral, BooleanLiteral: false
        }
    }

    private def isInsideDisjConj(Expression e) {
        var parent = e.eContainer
        while(!(parent instanceof BooleanEventually || parent instanceof BooleanAlways || parent instanceof Model)) {
            if(parent instanceof AndOrExpression) {
                return true
            } else parent = parent.eContainer
        }
        false
    }

    private def isInsideNegation(Expression e) {
        var parent = e.eContainer
        while(!(parent instanceof BooleanEventually || parent instanceof BooleanAlways || parent instanceof Model)) {
            if(parent instanceof BooleanNegation) {
                return true
            } else parent = parent.eContainer
        }
        false
    }

    private def isInsideTemporal(Expression e) {
        var parent = e.eContainer
        while(!(parent instanceof Model)) {
            if(parent instanceof BooleanEventually || parent instanceof BooleanAlways) {
                return true
            } else parent = parent.eContainer
        }
        false
    }

    private def dispatch retrieveAircraftCriterion(AircraftDynamicProperty property) {
        switch(property.value) {
            case ALTITUDE: AircraftCriterion.ALTITUDE
            case EMERGENCY: AircraftCriterion.EMERGENCY
            case GROUND_SPEED: AircraftCriterion.GROUNDSPEED
            case LATITUDE:  AircraftCriterion.LATITUDE
            case LONGITUDE: AircraftCriterion.LONGITUDE
            case SQUAWK: AircraftCriterion.SQUAWK
            case SPI: AircraftCriterion.SPI
            case IS_ON_GROUND: AircraftCriterion.IS_ON_GROUND
            case ALERT: AircraftCriterion.ALERT
            case VERTICAL_RATE: AircraftCriterion.VERTICAL_RATE
        }
    }

    private def dispatch retrieveAircraftCriterion(AircraftStaticProperty property) {
        switch(property.value) {
            case CALLSIGN: AircraftCriterion.CALLSIGN
            case ICAO: AircraftCriterion.ICAO
            case KNOWN_POSITIONS: AircraftCriterion.KNOWN_POSITIONS
            case TRACK: AircraftCriterion.TRACK
            case MIN_ALTITUDE: AircraftCriterion.MIN_ALTITUDE
            case MAX_ALTITUDE: AircraftCriterion.MAX_ALTITUDE
            case MEAN_ALTITUDE : AircraftCriterion.MEAN_ALTITUDE
            case MIN_LATITUDE: AircraftCriterion.MIN_LATITUDE
            case MAX_LATITUDE: AircraftCriterion.MAX_LATITUDE
            case MEAN_LATITUDE : AircraftCriterion.MEAN_LATITUDE
            case MIN_LONGITUDE: AircraftCriterion.MIN_LONGITUDE
            case MAX_LONGITUDE: AircraftCriterion.MAX_LONGITUDE
            case MEAN_LONGITUDE : AircraftCriterion.MEAN_LONGITUDE
            case MIN_GROUNDSPEED: AircraftCriterion.MIN_GROUNDSPEED
            case MAX_GROUNDSPEED: AircraftCriterion.MAX_GROUNDSPEED
            case MEAN_GROUNDSPEED : AircraftCriterion.MEAN_GROUNDSPEED
        }
    }

    private def dispatch retrieveAircraftCriterion(CommonStaticProperty property) {
        switch(property.value) {
            case MIN_ALTITUDE: AircraftCriterion.MIN_ALTITUDE
            case MAX_ALTITUDE: AircraftCriterion.MAX_ALTITUDE
            case MEAN_ALTITUDE : AircraftCriterion.MEAN_ALTITUDE
            case MIN_LATITUDE: AircraftCriterion.MIN_LATITUDE
            case MAX_LATITUDE: AircraftCriterion.MAX_LATITUDE
            case MEAN_LATITUDE : AircraftCriterion.MEAN_LATITUDE
            case MIN_LONGITUDE: AircraftCriterion.MIN_LONGITUDE
            case MAX_LONGITUDE: AircraftCriterion.MAX_LONGITUDE
            case MEAN_LONGITUDE : AircraftCriterion.MEAN_LONGITUDE
            case MIN_GROUNDSPEED: AircraftCriterion.MIN_GROUNDSPEED
            case MAX_GROUNDSPEED: AircraftCriterion.MAX_GROUNDSPEED
            case MEAN_GROUNDSPEED : AircraftCriterion.MEAN_GROUNDSPEED
        }
    }

    private def retrieveAircraftProp(Expression expr) {
        switch (expr) {
            LowerThan: expr.prop
            LowerThanOrEq: expr.prop
            Equals: expr.prop
            GreaterThanOrEq: expr.prop
            GreaterThan: expr.prop
            Different: expr.prop
        }
    }


    private def retrieveComparedValue(Expression expr) {
        switch (expr) {
            LowerThan: expr.expr
            LowerThanOrEq: expr.expr
            Equals: expr.expr
            GreaterThanOrEq: expr.expr
            GreaterThan: expr.expr
            Different: expr.expr
        }
    }
}
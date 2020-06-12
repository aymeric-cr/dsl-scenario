package fdit.triggcondition.interpreter

import fdit.database.AircraftRequests
import fdit.ltlcondition.ide.LTLConditionFacade
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.aircraft.AircraftCriterion
import fdit.metamodel.aircraft.OutOfDateException
import fdit.metamodel.aircraft.TimeInterval
import fdit.metamodel.coordinates.Coordinates
import fdit.metamodel.element.Directory
import fdit.metamodel.element.DirectoryUtils
import fdit.metamodel.filter.LTLFilter
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.recording.Recording
import fdit.metamodel.zone.FditPolygon
import fdit.metamodel.zone.Zone
import fdit.metamodel.zone.ZoneUtils
import fdit.triggcondition.triggeringCondition.ASAPTimeWindow
import fdit.triggcondition.triggeringCondition.ASTAreaPositionType
import fdit.triggcondition.triggeringCondition.ASTVertices
import fdit.triggcondition.triggeringCondition.AircraftDynamicProperty
import fdit.triggcondition.triggeringCondition.AircraftProperty
import fdit.triggcondition.triggeringCondition.AircraftStaticProperty
import fdit.triggcondition.triggeringCondition.AndOrExpression
import fdit.triggcondition.triggeringCondition.AndOrTimeWindow
import fdit.triggcondition.triggeringCondition.Area
import fdit.triggcondition.triggeringCondition.BooleanNegation
import fdit.triggcondition.triggeringCondition.ContextExpr
import fdit.triggcondition.triggeringCondition.Different
import fdit.triggcondition.triggeringCondition.Equals
import fdit.triggcondition.triggeringCondition.Expression
import fdit.triggcondition.triggeringCondition.GreaterThan
import fdit.triggcondition.triggeringCondition.GreaterThanOrEq
import fdit.triggcondition.triggeringCondition.IntLiteral
import fdit.triggcondition.triggeringCondition.LowerThan
import fdit.triggcondition.triggeringCondition.LowerThanOrEq
import fdit.triggcondition.triggeringCondition.NotWhenTimeWindow
import fdit.triggcondition.triggeringCondition.Prism
import fdit.triggcondition.triggeringCondition.ReferencedArea
import fdit.triggcondition.triggeringCondition.ReferencedFilter
import fdit.triggcondition.triggeringCondition.StringLiteral
import fdit.triggcondition.triggeringCondition.UntilTimeWindow
import fdit.triggcondition.triggeringCondition.WhenTimeWindow
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.LinkedList
import java.util.List
import org.eclipse.xtext.util.Pair
import org.eclipse.xtext.util.Tuples
import org.eclipse.xtext.xbase.lib.Functions.Function1
import static com.google.common.collect.Maps.newHashMap
import static com.google.common.collect.Lists.newArrayList
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID
import fdit.triggcondition.triggeringCondition.ArithmExpr
import fdit.triggcondition.triggeringCondition.Plus
import fdit.triggcondition.triggeringCondition.Minus
import fdit.triggcondition.triggeringCondition.MulOrDiv
import fdit.triggcondition.triggeringCondition.BooleanLiteral
import java.io.Serializable
import fdit.triggcondition.triggeringCondition.DoubleLiteral
import fdit.triggcondition.triggeringCondition.CommonStaticProperty

class TriggeringConditionInterpreter {

    var RecognizedAirPicture rap
    var Aircraft current_aircraft
    var Recording recording
    var Collection<LTLFilter> filters
    var HashMap<Aircraft, List<TimeInterval>> alteration_intervals
    var HashMap<Expression, Boolean> prev_results
    var HashMap<String, List<Aircraft>> filter_results
    var HashMap<ContextExpr, ArrayList<Boolean>> rap_evaluation
    var HashMap<Area, ArrayList<Boolean>> area_evaluation
    var start_time = 0L
    var end_time = 0L
    var time_count = 0L
    var last_aircraft_appearance = 0L
    public var long beacon_interval = 1000

    def setBeaconInterval (long beacon_interval) {
        this.beacon_interval = beacon_interval
    }

    def HashMap<Aircraft,List<TimeInterval>> getAlterationIntervals(Expression e, RecognizedAirPicture rap, Directory root, Recording recording) {
        return getAlterationIntervals(e,rap,rap.aircrafts,root,new TimeInterval(0L, rap.relativeDuration),recording);
    }

    def HashMap<Aircraft,List<TimeInterval>> getAlterationIntervals(Expression e, RecognizedAirPicture rap, Collection<Aircraft> targets, Directory root, Recording recording) {
        return getAlterationIntervals(e,rap,targets,root,new TimeInterval(0L, rap.relativeDuration),recording);
    }

    def HashMap<Aircraft,List<TimeInterval>> getAlterationIntervals(Expression e,
    RecognizedAirPicture rap, Collection<Aircraft> targets,
    Directory root,
    TimeInterval interval,
    Recording recording) {
        this.rap = rap
        this.filters = DirectoryUtils.gatherAllLTLFilters(root)
        this.recording = recording
        filter_results = newHashMap
        alteration_intervals = newHashMap
        rap_evaluation = newHashMap
        area_evaluation = newHashMap
        LTLConditionFacade.get.initialize(root)
        start_time = interval.getStart
        end_time = interval.getEnd
        TimeInterval.setMinimumSize(beacon_interval)
        var total_intervals = 0

        for(ac: targets) {
            current_aircraft = ac
            prev_results = newHashMap
            time_count =
                    if(current_aircraft.timeOfFirstAppearance >= start_time) current_aircraft.timeOfFirstAppearance else start_time
            last_aircraft_appearance =
                    if(current_aircraft.timeOfLastAppearance <= end_time) current_aircraft.timeOfLastAppearance else end_time
            var List<TimeInterval> time_intervals = new LinkedList<TimeInterval>
            var long last_success = -1L
            var TimeInterval current_interval = null
            while(time_count <= last_aircraft_appearance) {
                var boolean result = e.interpret
                if(result) {
                    last_success = time_count
                    if(current_interval === null) {
                        current_interval = new TimeInterval(time_count)
                    }
                    if(time_count === last_aircraft_appearance) {
                        //TODO what if the interval starts at the very last known aircraft's state?
                        if(current_interval.start < time_count) current_interval.setEnd(time_count)
                        time_intervals.add(current_interval)
                        current_interval = null
                    }
                }
                else if(!result && current_interval !== null && last_success === (time_count - beacon_interval)) {
                    current_interval.end = time_count
                    time_intervals.add(current_interval)
                    current_interval = null
                }
                time_count += beacon_interval
                if(time_count > last_aircraft_appearance && time_count < (last_aircraft_appearance + beacon_interval))
                    time_count = last_aircraft_appearance
            }
            alteration_intervals.put(current_aircraft, time_intervals)
            println(current_aircraft.callSign+": "+time_intervals.size+" intervals")
            total_intervals += time_intervals.size
        }
        println("Result: "+total_intervals+" intervals")
        return alteration_intervals
    }

 /*   def Object preprocessArithm(Expression e) {
        switch (e) {
            ASAPTimeWindow: e.expr.preprocessArithm
            UntilTimeWindow: e.expr.preprocessArithm
            WhenTimeWindow: e.expr.preprocessArithm
            NotWhenTimeWindow: e.expr.preprocessArithm
            AndOrTimeWindow: {
                e.left.preprocessArithm
                e.right.preprocessArithm
            }
        }
    }

    def Object preprocessArithm(ContextExpr e) {
        switch (e) {
            AndOrExpression: {
                e.left.preprocessArithm
                e.right.preprocessArithm
            }
            BooleanNegation: e.expression.preprocessArithm
            LowerThan:
                e.expr.evaluateArithmExpr
            GreaterThan:
                e.expr.evaluateArithmExpr
            Equals:
                e.expr.evaluateArithmExpr
            Different:
                e.expr.evaluateArithmExpr
            LowerThanOrEq:
                e.expr.evaluateArithmExpr
            GreaterThanOrEq:
                e.expr.evaluateArithmExpr
        }
    }*/

    def boolean interpret(Expression e) {
        switch (e) {
            ASAPTimeWindow: { // we need to find one air state where the expression is true, the rest is always true after that
                if(prev_results.get(e) !== null && prev_results.get(e)) true
                else {
                    val result = e.expr.interpret
                    prev_results.put(e,result)
                    return result
                }
            }
            UntilTimeWindow: { // we need to find one air position where the expression is false, the rest is always false after that
                if(prev_results.get(e) !== null && prev_results.get(e)) false
                else {
                    val result = e.expr.interpret
                    prev_results.put(e,result)
                    return !result // if the expression is false, then we perform the alteration.
                }
            }
            WhenTimeWindow: e.expr.interpret
            NotWhenTimeWindow: !(e.expr.interpret)
            AndOrTimeWindow: e.interpretAndOrTW
            default: false
        }
    }

    def boolean interpret(ContextExpr e) {
        switch (e) {
            BooleanNegation: !(e.expression.interpret)
            AndOrExpression:
                e.interpretAndOrExpr
            LowerThan,GreaterThan,Equals,Different,LowerThanOrEq,GreaterThanOrEq:
                e.interpretSingleExpr
            Area: e.interpretArea
            default: false
        }
    }

    private def boolean interpretArea(Area area) {
        val position = area.eventType
        val context = area.context
        val Zone zone =
            switch (area.area) {
                Prism: (area.area as Prism).createPrism
                ReferencedArea: rap.getZone((area.area as ReferencedArea).name)
            }
        var boolean result = false
        if(context instanceof ReferencedFilter) {
            parseFilter(context.filtername)
            if(area_evaluation.get(area) === null) {
                area_evaluation.put(area,
                newArrayList(evaluateTargetsOnZone(filter_results.get(context.filtername), area.eventType, zone)))
            }
            result = area_evaluation.get(area).get(0)
        } else {
            switch(context.type) {
                case RAP: {
                    if(area_evaluation.get(area) === null) {
                        area_evaluation.put(area,
                        newArrayList(evaluateRAPOnZone(area.eventType, zone)))
                    }
                    result =  area_evaluation.get(area).get(0)
                }
                case AIRCRAFT:
                    result =  current_aircraft.evaluateAircraftOnZone(time_count,area.eventType, zone)
            }
        }
    }

    private def evaluateRAPOnZone(ASTAreaPositionType eventType, Zone area) {
        evaluateTargetsOnZone(rap.aircrafts,eventType,area)
    }

    private def evaluateTargetsOnZone(Collection<Aircraft> targets, ASTAreaPositionType eventType, Zone area) {
        val results = newArrayList
        val existences = newHashMap
        for(ac:targets) {
            existences.put(ac, ac.getFirstCriterionAppearance(AircraftCriterion.LONGITUDE)
                    -> ac.getLastCriterionAppearance(AircraftCriterion.LONGITUDE))
        }
        for(var long t = start_time ; t <= end_time ; t = t + beacon_interval ) {
            var boolean rap_holds = true
            var iter = targets.iterator
            while(rap_holds && iter.hasNext) {
                var Aircraft target = iter.next
                if(existences.get(target).key <= t && existences.get(target).value >= t)
                    rap_holds = target.evaluateAircraftOnZone(t,eventType, area)
            }
            results.add(rap_holds)
        }
        return results
    }

    private def Boolean evaluateAircraftOnZone(Aircraft ac, long time_zone, ASTAreaPositionType position, Zone zone) {
        try {
            val lat = current_aircraft.query(time_count,AircraftCriterion.LATITUDE)
            val lon = current_aircraft.query(time_count,AircraftCriterion.LONGITUDE)
            val alt = current_aircraft.query(time_count,AircraftCriterion.ALTITUDE)
            if(position === ASTAreaPositionType.OUTSIDE) {
                !ZoneUtils.positionInZone(zone,lat,lon,alt)
            } else if(position === ASTAreaPositionType.INSIDE) {
                ZoneUtils.positionInZone(zone,lat,lon,alt)
            } else false
        } catch(OutOfDateException e) { false}
    }

    def private dispatch getNumberValue(IntLiteral il) {
        return il.value
    }

    def private dispatch getNumberValue(DoubleLiteral fl) {
        return fl.value
    }

    private def createPrism(Prism cyl) {
        new FditPolygon("cylinder",randomUUID(), cyl.lowerAltitude.numberValue, cyl.upperAltitude.numberValue
        ,createFditCoordinates(cyl.coordinates))
    }

    private def createFditCoordinates(ASTVertices vertices) {
        val fdit_coords = newArrayList
        for(coords: vertices.vertices)
            fdit_coords.add(new Coordinates(coords.latitude.numberValue, coords.longitude.numberValue))
        return fdit_coords
    }

    private def interpretAndOrTW(AndOrTimeWindow andor) {
        switch (andor.op) {
            case "and": {(andor.left.interpret as Boolean) && (andor.right.interpret as Boolean)}
            case "or": {(andor.left.interpret as Boolean) || (andor.right.interpret as Boolean)}
        }
    }

    private def interpretAndOrExpr(AndOrExpression andor) {
        switch (andor.op) {
            case "and": {(andor.left.interpret as Boolean) && (andor.right.interpret as Boolean)}
            case "or": {(andor.left.interpret as Boolean) || (andor.right.interpret as Boolean)}
        }
    }

    private def boolean interpretSingleExpr(ContextExpr expr) {
        val Pair<Object, Object> both_sides = expr.retrieveDataFromExpr
        val aircraft_property = (both_sides.getFirst as AircraftProperty)
        val compared_value = (both_sides.getSecond as ArithmExpr).evaluateArithmExpr
        val context = aircraft_property.context

        var boolean result = false
        if(aircraft_property instanceof AircraftStaticProperty) {

            if(context instanceof ReferencedFilter) {
                parseFilter(context.filtername)
                if(rap_evaluation.get(expr) === null) {
                    rap_evaluation.put(expr,
                    newArrayList(evaluateTargetsonSingleExprStatic(filter_results.get(context.filtername),
                    expr, aircraft_property, compared_value)
                    ))
                }
                result = rap_evaluation.get(expr).get(0)
            } else {
                switch(context.type) {
                    case RAP: {
                        if(rap_evaluation.get(expr) === null) {
                            rap_evaluation.put(expr, newArrayList(evaluateRAPonSingleExprStatic(expr, aircraft_property, compared_value)))
                        }
                        result =  rap_evaluation.get(expr).get(0)
                    }
                    case AIRCRAFT:
                        result =  current_aircraft.evaluateAircraftOnSingleExprStatic(expr,aircraft_property,compared_value)
                }
            }
        } else if(aircraft_property instanceof AircraftDynamicProperty) {
            if(context instanceof ReferencedFilter) {
                parseFilter(context.filtername)
                if(rap_evaluation.get(expr) === null) {
                    rap_evaluation.put(expr,
                    evaluateTargetsonSingleExpr(filter_results.get(context.filtername), expr, aircraft_property, compared_value))
                }
                result = rap_evaluation.get(expr).get(((time_count - start_time) / beacon_interval).intValue)
            } else {
                return switch (context.type) {
                    case AIRCRAFT: {
                        current_aircraft.evaluateAircraftOnSingleExpr(expr,time_count,aircraft_property,compared_value)
                    }
                    case RAP: {
                        if(rap_evaluation.get(expr) === null) {
                            rap_evaluation.put(expr, evaluateRAPonSingleExpr(expr, aircraft_property, compared_value))
                        }
                        result = rap_evaluation.get(expr).get(((time_count - start_time) / beacon_interval).intValue)
                    }
                }
            }
        }
        return result
    }

    private def parseFilter(String fname) {
        if(!filter_results.containsKey(fname)) {
            var LTLFilter filter = IterableExtensions.findFirst(filters,findFilter(fname))
            LTLConditionFacade.get.parse(filter.content)
            filter_results.put(filter.name, LTLConditionFacade.get.filterAircraft(rap,recording))
        }
    }

    private def findFilter(String fname) {
        return new Function1<LTLFilter,Boolean>() {
            def override Boolean apply(LTLFilter it) {
                return Boolean.valueOf(it.name.equals(fname))
            }
        }
    }

    private def evaluateAircraftOnSingleExprStatic(Aircraft aircraft, ContextExpr expr, AircraftProperty aircraft_prop, Object compared_value) {
        var property_value =
            AircraftRequests.fetchAircraftStaticPropertyValue(aircraft, aircraft_prop.retrieveAircraftCriterion)
        return expr.evaluateSingleExpression(property_value.get as Serializable, compared_value as Serializable)
    }

    private def evaluateTargetsonSingleExprStatic(Collection<Aircraft> targets, ContextExpr expr, AircraftProperty aircraft_prop, Object compared_value) {
        var boolean rap_holds = true
        var iter = targets.iterator
        while(rap_holds && iter.hasNext) {
            var Aircraft target = iter.next
            if(!target.evaluateAircraftOnSingleExprStatic(expr,aircraft_prop, compared_value)) {
                rap_holds = false
            }
        }
        rap_holds
    }

    private def evaluateAircraftOnSingleExpr(Aircraft aircraft, ContextExpr expr, long time, AircraftProperty aircraft_prop, Object compared_value) {
        evaluateAircraftOnSingleExpr(aircraft,expr,time,aircraft_prop.retrieveAircraftCriterion, compared_value)
    }

    private def evaluateAircraftOnSingleExpr(Aircraft aircraft, ContextExpr expr, long time, AircraftCriterion aircraft_prop, Object compared_value) {
        var property_value = try {
            aircraft.query(time,aircraft_prop)
        } catch(OutOfDateException e) { return false}
        return expr.evaluateSingleExpression(property_value, compared_value as Serializable)
    }

    private def evaluateTargetsonSingleExpr(Collection<Aircraft> targets, ContextExpr expr, AircraftProperty aircraft_prop, Object compared_value) {
        val results = newArrayList
        val prop_crit = aircraft_prop.retrieveAircraftCriterion
        val existences = newHashMap
        for(ac:targets) {
            existences.put(ac, ac.getFirstCriterionAppearance(prop_crit) -> ac.getLastCriterionAppearance(prop_crit))
        }
        for(var long t = start_time ; t <= end_time ; t = t + beacon_interval ) {
            var boolean rap_holds = true
            var iter = targets.iterator
            while(rap_holds && iter.hasNext) {
                var Aircraft target = iter.next
                if(existences.get(target).key <= t && existences.get(target).value >= t)
                    rap_holds = target.evaluateAircraftOnSingleExpr(expr, t, prop_crit, compared_value)
            }
            results.add(rap_holds)
        }
        return results
    }

    private def evaluateRAPonSingleExprStatic(ContextExpr expr, AircraftProperty aircraft_prop, Object compared_value) {
        evaluateTargetsonSingleExprStatic(rap.aircrafts,expr,aircraft_prop,compared_value)
    }

    private def evaluateRAPonSingleExpr(ContextExpr expr, AircraftProperty aircraft_prop, Object compared_value) {
        evaluateTargetsonSingleExpr(rap.aircrafts,expr,aircraft_prop,compared_value)
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Number left, Integer right) {
        left.intValue < right
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Number left, Double right) {
        left.doubleValue.compareTo(right) == -1
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Integer left, Number right) {
        left < right.intValue
    }

    private def dispatch evaluateSingleExpression(LowerThan op, Double left, Number right) {
        left.compareTo(right.doubleValue) == -1
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Number left, Integer right) {
        left.intValue <= right
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Number left, Double right) {
        left.doubleValue.compareTo(right) < 1
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Integer left, Number right) {
        left <= right.intValue
    }

    private def dispatch evaluateSingleExpression(LowerThanOrEq op, Double left, Number right) {
        left.compareTo(right.doubleValue) < 1
    }

    private def dispatch evaluateSingleExpression(Equals op, Number left, Integer right) {
        left.intValue == right
    }

    private def dispatch evaluateSingleExpression(Equals op, Number left, Double right) {
        left.doubleValue.compareTo(right) == 0
    }

    private def dispatch evaluateSingleExpression(Equals op, Integer left, Number right) {
        left == right.intValue
    }

    private def dispatch evaluateSingleExpression(Equals op, Double left, Number right) {
        left.compareTo(right.doubleValue) == 0
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Number left, Integer right) {
        left.intValue > right
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Number left, Double right) {
        left.doubleValue.compareTo(right) == 1//left.doubleValue > right.getValue
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Integer left, Number right) {
        left > right.intValue
    }

    private def dispatch evaluateSingleExpression(GreaterThan op, Double left, Number right) {
        left.compareTo(right.doubleValue) == 1
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Number left, Integer right) {
        left.intValue >= right
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Number left, Double right) {
        left.doubleValue.compareTo(right) > -1
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Integer left, Number right) {
        left >= right.intValue
    }

    private def dispatch evaluateSingleExpression(GreaterThanOrEq op, Double left, Number right) {
        left.compareTo(right.doubleValue) > -1
    }

    private def dispatch evaluateSingleExpression(Different op, Number left, Integer right) {
        left.intValue != right
    }

    private def dispatch evaluateSingleExpression(Different op, Number left, Double right) {
        left.doubleValue.compareTo(right) != 0
    }

    private def dispatch evaluateSingleExpression(Different op, Integer left, Number right) {
        left != right.intValue
    }

    private def dispatch evaluateSingleExpression(Different op, String left, String right) {
        !left.equalsIgnoreCase(right)
    }

    private def dispatch evaluateSingleExpression(Equals op, String left, String right) {
        left.equalsIgnoreCase(right)
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
            CommonStaticProperty : switch(ae.context.type) {
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

    private def Pair<Object,Object> retrieveDataFromExpr(ContextExpr expr) {
        switch (expr) {
            LowerThan:
                Tuples.create(expr.prop,expr.expr)
            LowerThanOrEq:
                Tuples.create(expr.prop,expr.expr)
            Equals:
                Tuples.create(expr.prop,expr.expr)
            GreaterThanOrEq:
                Tuples.create(expr.prop,expr.expr)
            GreaterThan:
                Tuples.create(expr.prop,expr.expr)
            Different:
                Tuples.create(expr.prop,expr.expr)
        }
    }
}
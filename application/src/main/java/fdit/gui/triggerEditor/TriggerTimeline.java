package fdit.gui.triggerEditor;

import com.google.common.collect.Sets;
import fdit.gui.utils.GanttChart;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.TimeInterval;
import fdit.metamodel.recording.Recording;
import fdit.tools.i18n.MessageTranslator;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.StringConverter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.utils.GanttChart.ExtraData;
import static fdit.gui.utils.tooltip.AircraftTooltipUtils.formatAircraftSnapshotTooltip;
import static fdit.metamodel.aircraft.AircraftUtils.inTimeInterval;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Long.max;
import static java.lang.Long.min;
import static java.lang.Math.round;
import static java.util.Comparator.comparing;
import static javafx.geometry.Side.TOP;
import static javafx.scene.control.Tooltip.install;
import static org.apache.commons.lang.StringUtils.isNotBlank;

class TriggerTimeline extends Pane {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(TriggerTimeline.class);

    private static final double SERIES_HEIGHT = 30;
    private static final String TRIGGERED = "status-blue";
    private static final String UNTRIGGERED = "status-red";
    private static final String UNKNOWN = "status-grey";

    private static final Comparator<Entry<Aircraft, List<TimeInterval>>> AIRCRAFT_INTERVALS_COMPARATOR =
            comparing(((Function<Entry<Aircraft, List<TimeInterval>>, Aircraft>) Entry::getKey)
                    .andThen(aircraft -> {
                        if (isNotBlank(aircraft.getCallSign())) {
                            return aircraft.getCallSign().toUpperCase();
                        }
                        return aircraft.getStringICAO();
                    }))
                    .reversed();

    private final CategoryAxis aircraftsAxis;
    private final NumberAxis positionsAxis;

    TriggerTimeline(final Map<Aircraft, List<TimeInterval>> intervalsMap,
                    final Recording recording,
                    final TimeInterval interpretInterval) {
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
        final long maxRelativeDate = interpretInterval != null ?
                interpretInterval.getEnd() : recording.getMaxRelativeDate();
        positionsAxis = new NumberAxis();
        positionsAxis.setForceZeroInRange(false);
        positionsAxis.setTickLabelFill(Color.CHOCOLATE);
        positionsAxis.setMinorTickCount(4);
        positionsAxis.setSide(TOP);
        positionsAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(final Number object) {
                return object.intValue() / 1000 + "s";
            }

            @Override
            public Number fromString(final String string) {
                return 0;
            }
        });
        aircraftsAxis = new CategoryAxis();
        aircraftsAxis.setTickLabelFill(Color.CHOCOLATE);
        aircraftsAxis.setTickLabelGap(10);
        initializeTexts();
        final GanttChart<Number, String> ganttChart = new GanttChart<>(positionsAxis, aircraftsAxis);
        ganttChart.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth() * 0.75);
        ganttChart.setMinWidth(Screen.getPrimary().getVisualBounds().getWidth() * 0.75);
        ganttChart.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth() * 0.75);
        ganttChart.setPrefHeight(SERIES_HEIGHT * intervalsMap.size());
        ganttChart.setMinHeight(SERIES_HEIGHT * intervalsMap.size());
        ganttChart.setMaxHeight(SERIES_HEIGHT * intervalsMap.size());
        ganttChart.getStylesheets().add(TriggerTimeline.class.getResource("ganttchart.css").toExternalForm());
        final Collection<String> seriesNames = newArrayList();
        final Collection<Series<Number, String>> series = newArrayList();
        final Set<Entry<Aircraft, List<TimeInterval>>> entries = Sets.newTreeSet(AIRCRAFT_INTERVALS_COMPARATOR);
        entries.addAll(intervalsMap.entrySet());
        for (final Entry<Aircraft, List<TimeInterval>> entry : entries) {
            final Aircraft aircraft = entry.getKey();
            final List<TimeInterval> intervals = entry.getValue();
            final String serieName;
            if (aircraft.getCallSign() == null) {
                serieName = aircraft.getStringICAO();
            } else {
                serieName = aircraft.getCallSign().toUpperCase();
            }
            if (!seriesNames.contains(serieName)) {
                seriesNames.add(serieName);
                Series<Number, String> serie = null;
                if (intervals.isEmpty() && inTimeInterval(aircraft, interpretInterval)) {
                    serie = unAlteredAircraft(interpretInterval, aircraft, serieName);
                    series.add(serie);
                } else if (!intervals.isEmpty()) {
                    serie = new Series<>();
                    int index = (int) interpretInterval.getStart();
                    long length = aircraft.getTimeOfFirstAppearance() - interpretInterval.getStart();
                    TimeInterval previous;
                    if (length > 0) {
                        previous = new TimeInterval(index, index + length);
                        serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNKNOWN)));
                        index += length;
                    } else {
                        previous = new TimeInterval(index, index);
                    }

                    for (final TimeInterval timeInterval : intervals) {
                        if (timeInterval.getStart() - previous.getEnd() > 0) {
                            // if the two alteration intervals are not
                            length = timeInterval.getStart() - previous.getEnd();
                            serie.getData().add(new Data<>(index,
                                    serieName,
                                    new ExtraData(length, UNTRIGGERED)));
                            index += length;
                        }
                        length = timeInterval.getEnd() - timeInterval.getStart();
                        serie.getData().add(new Data<>(index, serieName, new ExtraData(length, TRIGGERED)));
                        index += length;
                        previous = timeInterval;
                    }
                    // Check if aircraft still exists after alteration intervals and before end of interpret interval
                    final long aircraft_boundary = min(aircraft.getTimeOfLastAppearance(), interpretInterval.getEnd());
                    if (aircraft_boundary > index) {
                        length = aircraft_boundary - index;
                        serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNTRIGGERED)));
                        index += length;
                    }
                    if ((length = maxRelativeDate - index) > 0) {
                        serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNKNOWN)));
                    }
                    serie.setName(serieName);
                    series.add(serie);
                }
                if (serie != null) {
                    for (final Data<Number, String> data : serie.getData()) {
                        data.nodeProperty().addListener(o -> installTooltip(aircraft, data.getNode(), recording));
                    }
                }
            }
        }

        aircraftsAxis.getCategories().addAll(seriesNames);
        ganttChart.getData().addAll(series);
        ganttChart.setMinHeight(200);
        getChildren().add(ganttChart);
    }

    private static Series<Number, String> unAlteredAircraft(final TimeInterval interval,
                                                            final Aircraft aircraft,
                                                            final String serieName) {
        final Series<Number, String> serie = new Series<>();
        serie.setName(serieName);
        int index = (int) interval.getStart();
        final long first_appearance = max(aircraft.getTimeOfFirstAppearance(), interval.getStart());
        final long last_appearance = min(aircraft.getTimeOfLastAppearance(), interval.getEnd());
        long length = first_appearance - index;
        if (length > 0) {
            serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNKNOWN)));
            index += length;
        }
        length = last_appearance - index;

        serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNTRIGGERED)));
        index += length;
        if ((length = interval.getEnd() - index) > 0) {
            serie.getData().add(new Data<>(index, serieName, new ExtraData(length, UNKNOWN)));
        }
        return serie;
    }

    private void initializeTexts() {
        positionsAxis.setLabel(TRANSLATOR.getMessage("time"));
        aircraftsAxis.setLabel(TRANSLATOR.getMessage("aircrafts"));
    }

    private void installTooltip(final Aircraft aircraft, final Node seriesNode, final Recording recording) {
        final Tooltip tooltip = new Tooltip();
        tooltip.activatedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                final Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                final double relativeX = getRelativeX(mouseLocation.x, mouseLocation.y);
                final long relativeDate =
                        round((double) positionsAxis.getValueForDisplay(relativeX));
                tooltip.setText(formatAircraftSnapshotTooltip(aircraft, recording, relativeDate / 1000));
            }
        });
        install(seriesNode, tooltip);
    }

    private double getRelativeX(final double screenX, final double screenY) {
        return positionsAxis.screenToLocal(screenX, screenY).getX();
    }
}
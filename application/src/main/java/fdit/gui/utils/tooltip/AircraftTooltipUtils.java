package fdit.gui.utils.tooltip;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.AircraftState;
import fdit.metamodel.aircraft.AircraftState.AircraftStateVisitor;
import fdit.metamodel.aircraft.BaseStationAircraftState;
import fdit.metamodel.recording.Recording;
import fdit.tools.i18n.MessageTranslator;

import javax.annotation.Nullable;
import java.util.Locale;

import static fdit.database.AircraftRequests.fetchAircraftSnapshot;
import static fdit.tools.date.DateUtils.millisToSeconds;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public final class AircraftTooltipUtils {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(AircraftTooltipUtils.class);

    private AircraftTooltipUtils() {
    }

    public static String formatAircraftSnapshotTooltip(final Aircraft aircraft,
                                                       final Recording recording,
                                                       final long relativeDate) {
        try {
            final AircraftState snapshot = fetchAircraftSnapshot(aircraft, recording, relativeDate);
            final StringBuilder builder = new StringBuilder();
            formatAircraftSnapshotTooltip(aircraft, snapshot, builder);
            snapshot.accept((AircraftStateVisitor<Void>) state -> {
                formatBaseStationAircraftStateTooltip(state, builder);
                return null;
            });
            return builder.toString();
        } catch (final Exception e) {
            return "";
        }
    }

    private static void formatAircraftSnapshotTooltip(final Aircraft aircraft,
                                                      final AircraftState state,
                                                      final StringBuilder builder) {
        builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.id"),
                valueOf(aircraft.getAircraftId()), null));
        builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.icao"),
                aircraft.getStringICAO(), null));
        builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.callSign"),
                formatCallSign(aircraft), null));
        builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.relativeDate"),
                formatRelativeDateMillis(state.getRelativeDate()),
                TRANSLATOR.getMessage("aircraft.infos.relativeDate.unit")));
        state.getAltitude().ifPresent(altitude -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.altitude"),
                valueOf(round(altitude)), TRANSLATOR.getMessage("aircraft.infos.altitude.unit"))));
        state.getPosition().ifPresent(coordinates -> {
            builder.append(formatTooltipInfo(
                    TRANSLATOR.getMessage("aircraft.infos.latitude"),
                    valueOf(coordinates.getLatitude()), null));
            builder.append(formatTooltipInfo(
                    TRANSLATOR.getMessage("aircraft.infos.longitude"),
                    valueOf(coordinates.getLongitude()), null));
        });
    }

    public static String formatRelativeDateMillis(final long relativeDateMillis) {
        return format(Locale.US, "%.3f", millisToSeconds(relativeDateMillis));
    }

    private static void formatBaseStationAircraftStateTooltip(final BaseStationAircraftState state,
                                                              final StringBuilder builder) {
        state.getAlert().ifPresent(alert -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.alert"),
                alert, null)));
        state.getEmergency().ifPresent(emergency -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.emergency"),
                emergency, null)));
        state.getGroundSpeed().ifPresent(groundSpeed -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.groundSpeed"),
                valueOf(groundSpeed), null)));
        state.getIsOnGround().ifPresent(isOnGround -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.isOnGround"),
                isOnGround, null)));
        state.getSpi().ifPresent(spi -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.spi"),
                spi, null)));
        state.getSquawk().ifPresent(squawk -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.squawk"),
                valueOf(squawk), null)));
        state.getTrack().ifPresent(track -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.track"),
                valueOf(track), null)));
        state.getVerticalRate().ifPresent(verticalRate -> builder.append(formatTooltipInfo(
                TRANSLATOR.getMessage("aircraft.infos.verticalRate"),
                valueOf(verticalRate), null)));
    }

    private static String formatTooltipInfo(final String key, final String value, @Nullable final String unit) {
        final StringBuilder builder = new StringBuilder(key + " : " + value);
        if (isNotBlank(unit)) {
            builder.append(' ').append(unit);
        }
        return builder.append('\n').toString();
    }

    private static String formatCallSign(final Aircraft aircraft) {
        final String callSign = aircraft.getCallSign();
        if (callSign == null) {
            return TRANSLATOR.getMessage("aircraft.infos.unknown");
        }
        return callSign;
    }
}

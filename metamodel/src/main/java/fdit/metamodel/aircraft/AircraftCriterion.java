package fdit.metamodel.aircraft;

public enum AircraftCriterion {
    // statics
    ICAO,
    CALLSIGN,
    KNOWN_POSITIONS,
    MIN_ALTITUDE,
    MAX_ALTITUDE,
    MEAN_ALTITUDE,
    MIN_LATITUDE,
    MAX_LATITUDE,
    MEAN_LATITUDE,
    MIN_LONGITUDE,
    MAX_LONGITUDE,
    MEAN_LONGITUDE,
    MIN_GROUNDSPEED,
    MAX_GROUNDSPEED,
    MEAN_GROUNDSPEED,
    // dynamics
    ALTITUDE,
    SQUAWK,
    LATITUDE,
    LONGITUDE,
    SPI,
    GROUNDSPEED,
    EMERGENCY,
    TRACK,
    ALERT,
    IS_ON_GROUND,
    VERTICAL_RATE;


    public static boolean isStatusCriterion(final AircraftCriterion criterion) {
        return new AircraftCriterionSwitch<Boolean>() {
            @Override
            public Boolean visitKnownPositions() {
                return false;
            }

            @Override
            public Boolean visitMaxAltitude() {
                return false;
            }

            @Override
            public Boolean visitMinAltitude() {
                return false;
            }

            @Override
            public Boolean visitMeanAltitude() {
                return false;
            }

            @Override
            public Boolean visitMaxLatitude() {
                return false;
            }

            @Override
            public Boolean visitMinLatitude() {
                return false;
            }

            @Override
            public Boolean visitMeanLatitude() {
                return false;
            }

            @Override
            public Boolean visitMaxLongitude() {
                return false;
            }

            @Override
            public Boolean visitMinLongitude() {
                return false;
            }

            @Override
            public Boolean visitMeanLongitude() {
                return false;
            }

            @Override
            public Boolean visitMaxGroundSpeed() {
                return false;
            }

            @Override
            public Boolean visitMinGroundSpeed() {
                return false;
            }

            @Override
            public Boolean visitMeanGroundSpeed() {
                return false;
            }

            @Override
            public Boolean visitAltitude() {
                return false;
            }

            @Override
            public Boolean visitCallSign() {
                return false;
            }

            @Override
            public Boolean visitEmergency() {
                return true;
            }

            @Override
            public Boolean visitGroundSpeed() {
                return false;
            }

            @Override
            public Boolean visitIcao() {
                return false;
            }

            @Override
            public Boolean visitLatitude() {
                return false;
            }

            @Override
            public Boolean visitLongitude() {
                return false;
            }

            @Override
            public Boolean visitSpi() {
                return true;
            }

            @Override
            public Boolean visitSquawk() {
                return true;
            }

            @Override
            public Boolean visitTrack() {
                return false;
            }

            @Override
            public Boolean visitAlert() {
                return true;
            }

            @Override
            public Boolean visitIsOnGround() {
                return true;
            }

            @Override
            public Boolean visitVerticalRate() {
                return false;
            }
        }.doSwitch(criterion);
    }

    public interface AircraftCriterionSwitch<T> {

        default T visitKnownPositions() {
            return visitDefault();
        }

        default T visitMaxAltitude() {
            return visitDefault();
        }

        default T visitMinAltitude() {
            return visitDefault();
        }

        default T visitMeanAltitude() {
            return visitDefault();
        }

        default T visitMaxLatitude() {
            return visitDefault();
        }

        default T visitMinLatitude() {
            return visitDefault();
        }

        default T visitMeanLatitude() {
            return visitDefault();
        }

        default T visitMaxLongitude() {
            return visitDefault();
        }

        default T visitMinLongitude() {
            return visitDefault();
        }

        default T visitMeanLongitude() {
            return visitDefault();
        }

        default T visitMaxGroundSpeed() {
            return visitDefault();
        }

        default T visitMinGroundSpeed() {
            return visitDefault();
        }

        default T visitMeanGroundSpeed() {
            return visitDefault();
        }

        default T visitAltitude() {
            return visitDefault();
        }

        default T visitCallSign() {
            return visitDefault();
        }

        default T visitEmergency() {
            return visitDefault();
        }

        default T visitGroundSpeed() {
            return visitDefault();
        }

        default T visitIcao() {
            return visitDefault();
        }

        default T visitLatitude() {
            return visitDefault();
        }

        default T visitLongitude() {
            return visitDefault();
        }

        default T visitSpi() {
            return visitDefault();
        }

        default T visitSquawk() {
            return visitDefault();
        }

        default T visitTrack() {
            return visitDefault();
        }

        default T visitAlert() {
            return visitDefault();
        }

        default T visitIsOnGround() {
            return visitDefault();
        }

        default T visitVerticalRate() {
            return visitDefault();
        }

        default T visitDefault() {
            throw new RuntimeException("Unknown criterion");
        }

        @SuppressWarnings({"UnnecessaryDefault", "SwitchStatementWithTooManyBranches"})
        default T doSwitch(final AircraftCriterion criterion) {
            switch (criterion) {
                case MAX_ALTITUDE:
                    return visitMaxAltitude();
                case MIN_ALTITUDE:
                    return visitMinAltitude();
                case MEAN_ALTITUDE:
                    return visitMeanAltitude();
                case MAX_LATITUDE:
                    return visitMaxLatitude();
                case MIN_LATITUDE:
                    return visitMinLatitude();
                case MEAN_LATITUDE:
                    return visitMeanLatitude();
                case MAX_LONGITUDE:
                    return visitMaxLongitude();
                case MIN_LONGITUDE:
                    return visitMinLongitude();
                case MEAN_LONGITUDE:
                    return visitMeanLongitude();
                case MAX_GROUNDSPEED:
                    return visitMaxGroundSpeed();
                case MIN_GROUNDSPEED:
                    return visitMinGroundSpeed();
                case MEAN_GROUNDSPEED:
                    return visitMeanGroundSpeed();
                case KNOWN_POSITIONS:
                    return visitKnownPositions();
                case ALTITUDE:
                    return visitAltitude();
                case CALLSIGN:
                    return visitCallSign();
                case EMERGENCY:
                    return visitEmergency();
                case GROUNDSPEED:
                    return visitGroundSpeed();
                case ICAO:
                    return visitIcao();
                case LATITUDE:
                    return visitLatitude();
                case LONGITUDE:
                    return visitLongitude();
                case SPI:
                    return visitSpi();
                case SQUAWK:
                    return visitSquawk();
                case TRACK:
                    return visitTrack();
                case ALERT:
                    return visitAlert();
                case IS_ON_GROUND:
                    return visitIsOnGround();
                case VERTICAL_RATE:
                    return visitVerticalRate();
                default:
                    return visitDefault();
            }
        }
    }
}
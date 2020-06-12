package fdit.metamodel.recording;

public class RecordingUtils {

    private static final String SUFFIX_BST = "bst";
    private static final String SUFFIX_SBS = "sbs";
    private static final String PREFIX_SBS = "sbs_";

    private RecordingUtils() {
    }

    public static String renderRecordingPrefix(final Recording recording) {
        return new Recording.RecordingSwitch<String>() {
            @Override
            public String visitBaseStationRecording(final Recording recording) {
                return PREFIX_SBS;
            }

            @Override
            public String visitSiteBaseStationRecording(final Recording recording) {
                return PREFIX_SBS;
            }
        }.doSwitch(recording);
    }

    public static String renderRecordingSuffix(final Recording recording) {
        return new Recording.RecordingSwitch<String>() {
            @Override
            public String visitBaseStationRecording(final Recording recording) {
                return SUFFIX_BST;
            }

            @Override
            public String visitSiteBaseStationRecording(final Recording recording) {
                return SUFFIX_SBS;
            }
        }.doSwitch(recording);
    }
}
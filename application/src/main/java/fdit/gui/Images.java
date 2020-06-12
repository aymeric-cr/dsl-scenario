package fdit.gui;


import fdit.metamodel.recording.Recording;
import fdit.metamodel.recording.Recording.RecordingSwitch;
import javafx.scene.image.Image;

public final class Images {

    public static final Image APPLCATION_ICON = getImage("aircraft-appli.png");
    public static final Image ADD_SCHEMA_ICON = getImage("add-configuration.png");
    public static final Image ARROW_DOWN = getImage("arrow_down.png");
    public static final Image ARROW_RIGHT = getImage("arrow_right.png");
    public static final Image DELETE_CROSS_ICON = getImage("delete_cross.png");
    public static final Image DIRECTORY_ICON = getImage("directory.png");
    public static final Image ERROR_ICON = getImage("error.png");
    public static final Image WAITING_ICON = getImage("loading.png");
    public static final Image PLUS_ICON = getImage("plus.png");
    public static final Image SBS_ICON = getImage("sbs.png");
    public static final Image TEXTUAL_SCENARIO_ICON = getImage("scenario.png");
    public static final Image START_EXECUTION_ICON = getImage("start-execution.png");
    public static final Image SUCCESS_ICON = getImage("success.png");
    public static final Image ZONES_ICON = getImage("zones.png");
    public static final Image LTLFILTER_ICON = getImage("filter.png");
    public static final Image TRIGGER_ICON = getImage("trigger.png");
    public static final Image GEAR = getImage("gear2.png");
    public static final Image SAVE = getImage("save.png");
    public static final Image EXECUTE = getImage("execute.png");
    public static final Image EXECUTE_RED = getImage("execute-red.png");
    public static final Image EXECUTE_GREEN = getImage("execute-green.png");
    public static final Image DELETE = getImage("delete.png");
    public static final Image DELETE_FILE = getImage("delete_file.png");
    public static final Image ENGLISH_FLAG = getImage("english_flag.png");
    public static final Image FLAG = getImage("flag.png");
    public static final Image FRENCH_FLAG = getImage("french_flag.png");
    public static final Image IMPORT = getImage("import.png");
    public static final Image OPEN = getImage("open.png");
    public static final Image REDO = getImage("redo.png");
    public static final Image REMOVE_CACHE = getImage("remove_cache.png");
    public static final Image UNDO = getImage("undo.png");
    public static final Image CREATE = getImage("create.png");
    public static final Image RENAME = getImage("rename.png");
    public static final Image ERASE = getImage("erase.png");

    private Images() {
    }

    private static Image getImage(final String name) {
        return new Image(Images.class.getClassLoader().getResourceAsStream("icons/" + name));
    }

    public static Image getRecordingIcon(final Recording recording) {
        return new RecordingSwitch<Image>() {
            @Override
            public Image visitBaseStationRecording(Recording recording) {
                return SBS_ICON;
            }

            @Override
            public Image visitSiteBaseStationRecording(Recording recording) {
                return SBS_ICON;
            }

            @Override
            public Image visitDefault() {
                return SBS_ICON;
            }
        }.doSwitch(recording);
    }
}
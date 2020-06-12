package fdit.storage.recording;

public interface RecordingInDatabaseLoadingCallback {

    void onDatabaseLoadingStarted();

    void onDatabaseLoadingEnded();

    void onDatabaseRegisteringStarted();

    void onDatabaseRegisteringEnded();
}

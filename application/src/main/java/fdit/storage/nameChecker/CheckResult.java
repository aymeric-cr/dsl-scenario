package fdit.storage.nameChecker;

public class CheckResult {
    private final boolean result;
    private final String message;

    private CheckResult(final boolean result, final String message) {
        this.result = result;
        this.message = message;
    }

    public static CheckResult success() {
        return new CheckResult(true, "");
    }

    public static CheckResult fail(final String message) {
        return new CheckResult(false, message);
    }

    public String getMessage() {
        return message;
    }

    public boolean checkSucceeded() {
        return result;
    }

    public boolean checkFailed() {
        return !result;
    }
}

package dmdevgo.hunky.core;

/**
 * @author Dmitriy Gorbunov
 */
public final class ProcessError {

    private String message;
    private int code;
    private boolean networkError = false;

    private ProcessError(String message, int code, boolean networkError) {
        this.message = message;
        this.code = code;
        this.networkError = networkError;
    }

    public static ProcessError newError(String message, int code) {
        return new ProcessError(message, code, false);
    }

    public static ProcessError newNetworkError(String message, int code) {
        return new ProcessError(message, code, true);
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public boolean isNeworkError() {
        return networkError;
    }
}

package dmdevgo.hunky.core;

/**
 * @author Dmitriy Gorbunov
 */
public final class Report {

    public enum Status {
        WAITING,
        STARTED,
        PROGRESS,
        SUCCESS,
        FAILURE,
        CANCELED,
    }

    private Status status;
    private Object data;
    private ProcessError error;
    private int progress;

    BaseProcessor from;

    private Report(Status status, int progress, Object data, ProcessError error) {
        this.status = status;
        this.progress = progress;
        this.data = data;
        this.error = error;
    }

    static Report newWaitingReport(BaseProcessor from) {
        Report report = new Report(Status.WAITING, 0, null, null);
        report.from = from;
        return report;
    }

    public static Report newStartReport() {
        return new Report(Status.STARTED, 0, null, null);
    }

    public static Report newProgressReport(int progress) {
        return new Report(Status.PROGRESS, progress, null, null);
    }

    public static Report newSuccessReport(Object data) {
        return new Report(Status.SUCCESS, 100, data, null);
    }

    public static Report newErrorReport(ProcessError error) {
        return new Report(Status.FAILURE, 0, null, error);
    }

    public static Report newCancelReport() {
        return new Report(Status.CANCELED, 0, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public ProcessError getError() {
        return error;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isRunning() {
        return status == Status.STARTED || status == Status.PROGRESS;
    }

    public boolean isFinished() {
        return !isRunning();
    }

    public boolean isCanceled() {
        return status == Status.CANCELED;
    }

}

package dmdevgo.hunky.core;

/**
 * @author Dmitriy Gorbunov
 */
public interface ProcessListener<P> {

    public String getFilterTag();
    public Class<P> getProcessorClass();

    public void onStart(P processor);
    public void onSuccess(P processor, Object result);
    public void onFailure(P processor, ProcessError error);
    public void onProgress(P processor, int progress);
    public void onCanceled(P processor);
    public void onEnd(P processor);

}

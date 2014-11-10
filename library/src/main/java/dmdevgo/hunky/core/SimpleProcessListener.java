package dmdevgo.hunky.core;

/**
 * @author Dmitriy Gorbunov
 */
public abstract class SimpleProcessListener<P extends BaseProcessor> implements ProcessListener<P>, ServiceCallbackListener {

    @Override
    public String getFilterTag() {
        return null;
    }

    abstract public Class<P> getProcessorClass();

    @Override
    public void onStart(P processor) {

    }

    @Override
    public void onSuccess(P processor, Object result) {

    }

    @Override
    public void onFailure(P processor, ProcessError error) {

    }

    @Override
    public void onProgress(P processor, int progress) {

    }

    @Override
    public void onCanceled(P processor) {

    }

    @Override
    public void onEnd(P processor) {

    }

    @Override
    final public boolean onServiceCallback(BaseProcessor processor, Report report) {

        if (getProcessorClass().isInstance(processor)) {

            String filterTag = getFilterTag();

            if (filterTag == null || processor.getTag().equals(filterTag)) {

                switch (report.getStatus()) {

                    case STARTED:
                        onStart((P) processor);
                        break;

                    case PROGRESS:
                        onProgress((P) processor, report.getProgress());
                        break;

                    case SUCCESS:
                        onSuccess((P) processor, report.getData());
                        onEnd((P) processor);
                        break;

                    case FAILURE:
                        onFailure((P) processor, report.getError());
                        onEnd((P) processor);
                        break;

                    case CANCELED:
                        onCanceled((P) processor);
                        onEnd((P) processor);
                        break;

                }

                return true;

            }

        }

        return false;
    }
}

package dmdevgo.hunky.core;

import android.content.Context;

/**
 * @author Dmitriy Gorbunov
 */
public abstract class BaseProcessor {

    private String tag;

	public abstract Report execute(Context context, ProgressListener progressListener);

    public boolean isSticky() {
        return false;
    }

    public final String getTag() {

        if (tag == null) {
            tag = ((Object) this).getClass().getName() + getSubTag();
        }

        return tag;
    }

    public String getSubTag() {
        return "";
    }

    public static interface ProgressListener {
        public void onProgress(int progress);
    }

}
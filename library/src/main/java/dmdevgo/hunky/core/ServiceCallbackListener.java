package dmdevgo.hunky.core;

/**
 * @author Dmitriy Gorbunov
 */
public interface ServiceCallbackListener {
	
	public boolean onServiceCallback(BaseProcessor processor, Report result);

}


package dmdevgo.hunky.core;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Dmitriy Gorbunov
 */
public final class HunkyManager {

    private static final HunkyManager INSTANCE = new HunkyManager();

	private final List<ServiceCallbackListener> listeners = new CopyOnWriteArrayList<ServiceCallbackListener>();
	private final Map<String, Report> reports = new ConcurrentHashMap<String, Report>();
	private final List<BaseProcessor> pendingProcessors = new ArrayList<BaseProcessor>();
	
	private Context context;
    private volatile boolean isConnected = false;
    private volatile HunkyService service = null;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            synchronized (HunkyManager.this) {
                service = ((HunkyService.WorkServiceBinder) iBinder).getService();
                isConnected = true;
                for (BaseProcessor processor : pendingProcessors) {
                    startRequest(processor);
                }
                pendingProcessors.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (HunkyManager.this) {
                isConnected = false;
                service = null;
            }
        }
    };

    private HunkyManager(){}

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
    }
	
	public static HunkyManager getInstance() {
        return INSTANCE;
	}

	public void registerServiceCallbackListener(ServiceCallbackListener listener) {
		listeners.add(listener);
        dispatchAllReports(listener);
	}

    public void unregisterServiceCallbackListener(ServiceCallbackListener listener) {
		listeners.remove(listener);
        unbindFromServiceIfAllTasksAreFinished();
	}

    private void bindToServiceIfDisconnected() {
        if (!isConnected) {
            context.bindService(new Intent(context, HunkyService.class), serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    private void unbindFromServiceIfAllTasksAreFinished() {
        if (isConnected) {
            if (service.isIdling() && listeners.isEmpty()) {
                context.unbindService(serviceConnection);
                isConnected = false;
                service = null;
            }
        }
    }

    private void dispatchReport(Report report) {

        int k = 0;

        for (ServiceCallbackListener listener : listeners) {
            if (listener.onServiceCallback(report.from, report)) {
                k++;
            }
        }

        if (report.isFinished()) {
            if (k >= 1 || !report.from.isSticky()) {
                reports.remove(report.from.getTag());
            }
        }
    }

    private void dispatchAllReports(ServiceCallbackListener listener) {
        Iterator<Report> iterator = reports.values().iterator();
        while (iterator.hasNext()) {
            Report report = iterator.next();
            boolean handled = listener.onServiceCallback(report.from, report);
            if (report.isFinished()) {
                if (handled || !report.from.isSticky() ) {
                    reports.remove(report.from.getTag());
                }
            }
        }
     }

    void handleReport(Report report) {
        reports.put(report.from.getTag(), report);
        dispatchReport(report);
        unbindFromServiceIfAllTasksAreFinished();
    }

    synchronized public void startRequest(BaseProcessor processor) {

        if (reports.containsKey(processor.getTag())) {
            if (reports.get(processor.getTag()).isRunning()) {
                return;
            }
        }

        reports.put(processor.getTag(), Report.newWaitingReport(processor));

        if (isConnected) {
            service.startProcessor(processor);
        } else {
            pendingProcessors.add(processor);
            bindToServiceIfDisconnected();
        }

	}

    public boolean isRequestRunning(Class<? extends BaseProcessor> clazz) {
        return reports.containsKey(clazz.getName());
    }

    public boolean isRequestRunning(String tag) {
        return reports.containsKey(tag);
    }
}

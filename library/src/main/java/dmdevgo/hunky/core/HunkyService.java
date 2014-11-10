package dmdevgo.hunky.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Dmitriy Gorbunov
 */
public class HunkyService extends Service {

    private static final int MESSAGE_PROCESSOR_REPORT = 0;

    private WorkServiceBinder binder = new WorkServiceBinder();
	private ExecutorService pool = Executors.newCachedThreadPool();;
    private WorkServiceHandler handler;
    private Map<String, Future<?>> tasks = new ConcurrentHashMap<String, Future<?>>();

    @Override
	public void onCreate() {
		super.onCreate();
        handler = new WorkServiceHandler(this);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        pool.shutdown();
     }

    @Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    public boolean isIdling() {
        return tasks.isEmpty();
    }

    void startProcessor(BaseProcessor processor) {
        Task task = new Task(processor);
        tasks.put(processor.getTag(), pool.submit(task));
    }

    private void removeFutureIfFinished(Report result) {
        if (result.isFinished()) {
            tasks.remove(result.from.getTag());
        }
    }

    private void sendReportMessage(Report report) {
        Message msg = handler.obtainMessage(MESSAGE_PROCESSOR_REPORT, report);
        handler.sendMessage(msg);
    }
	
	private class Task implements Runnable, BaseProcessor.ProgressListener {

		private BaseProcessor processor;
		
		public Task(BaseProcessor processor) {
			this.processor = processor;
		}

        @Override
        public void run() {
            sendReport(Report.newStartReport());
            Report report = processor.execute(HunkyService.this.getApplicationContext(), this);
            sendReport(report);
        }

        @Override
        public void onProgress(int progress) {
            sendReport(Report.newProgressReport(progress));
        }

        private void sendReport(Report report) {
            report.from = processor;
            sendReportMessage(report);
        }
	}

    private static class WorkServiceHandler extends Handler {

        private WeakReference<HunkyService> service;

        WorkServiceHandler(HunkyService service) {
            super(Looper.getMainLooper());
            this.service = new WeakReference<HunkyService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            HunkyService hunkyService = service.get();
            if (hunkyService != null) {
                Report report = (Report) msg.obj;
                hunkyService.removeFutureIfFinished(report);
                HunkyManager.getInstance().handleReport(report);
            }
        }
    }

    class WorkServiceBinder extends Binder {
        HunkyService getService() {
            return HunkyService.this;
        }
    }
}

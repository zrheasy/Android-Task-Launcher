package com.zrh.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zrh
 * @date 2023/7/4
 */
public class TaskLauncher implements TaskCallback {
    private Logger mLogger;
    private final String mName;
    private Map<String, Task> mTasks = new HashMap<>();
    private boolean isStart = false;
    private CountDownLatch countDownLatch;
    private final ExecutorService mExecutor;

    public TaskLauncher() {
        this("TaskLauncher");
    }

    public TaskLauncher(String name) {
        this.mName = name;
        mExecutor = Executors.newCachedThreadPool();
    }

    public void setLogger(Logger logger) {
        this.mLogger = logger;
    }

    public void addTask(Task task) {
        mTasks.put(task.getTaskId(), task);
    }

    public void addMainTask(String id, Set<String> dependencies, Runnable task) {
        addTask(new MainTask(this, id, dependencies, task));
    }

    public void addAsyncTask(String id, Set<String> dependencies, Runnable task) {
        addTask(new AsyncTask(this, id, dependencies, task));
    }

    public void start() throws InterruptedException {
        if (isStart) {
            log("Launcher is already start!");
            return;
        }
        isStart = true;
        long now = System.currentTimeMillis();
        List<Task> tasks = new TaskSortUtils(mTasks).sort();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            if (i != 0) sb.append(",");
            sb.append(tasks.get(i).getTaskId());
        }
        log("---------------->start[" + sb + "]");

        internalStart(tasks);

        long cost = System.currentTimeMillis() - now;
        log("<----------------end(" + cost + "ms)");
    }

    private void internalStart(List<Task> tasks) throws InterruptedException {
        countDownLatch = new CountDownLatch(tasks.size());
        List<Task> asyncTasks = new ArrayList<>();
        List<Task> mainTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isMainThread()) {
                mainTasks.add(task);
            } else {
                asyncTasks.add(task);
            }
            task.addCallback(this);
        }
        // 先将异步任务放入线程池中执行
        for (Task asyncTask : asyncTasks) {
            mExecutor.submit(() -> {
                try {
                    asyncTask.execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        // 再执行主线程任务
        for (Task mainTask : mainTasks) {
            mainTask.execute();
        }

        countDownLatch.await();
    }

    Task getTask(String id) {
        return mTasks.get(id);
    }

    void log(String msg) {
        if (mLogger != null) mLogger.log(mName, msg);
    }

    @Override
    public void onCompleted() {
        countDownLatch.countDown();
    }

    public interface Logger {
        void log(String tag, String msg);
    }
}

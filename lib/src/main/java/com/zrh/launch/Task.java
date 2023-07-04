package com.zrh.launch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import kotlin.jvm.Synchronized;

/**
 * @author zrh
 * @date 2023/7/4
 */
abstract public class Task {

    private TaskLauncher mLauncher;
    private volatile boolean isCompleted = false;
    private final Set<TaskCallback> mCallbacks = Collections.synchronizedSet(new HashSet<>());

    public Task(TaskLauncher launcher) {
        mLauncher = launcher;
    }

    void execute() throws InterruptedException {
        waitDependenciesComplete();
        mLauncher.log(getTaskId() + " is running in " + Thread.currentThread().getName());
        long now = System.currentTimeMillis();

        onExecute();

        long cost = System.currentTimeMillis() - now;
        mLauncher.log(getTaskId() + " is completed(" + cost + "ms)");
        onComplete();
    }

    @Synchronized
    void addCallback(TaskCallback callback) {
        if (isCompleted) {
            callback.onCompleted();
        } else {
            mCallbacks.add(callback);
        }
    }

    void onComplete() {
        isCompleted = true;
        for (TaskCallback mCallback : mCallbacks) {
            mCallback.onCompleted();
        }
    }

    private void waitDependenciesComplete() throws InterruptedException {
        Set<String> taskIds = dependencies();
        if (taskIds.isEmpty()) return;

        String str = String.join(",", taskIds);
        mLauncher.log(getTaskId() + " is waiting [" + str + "]");

        CountDownLatch countDownLatch = new CountDownLatch(taskIds.size());
        for (String taskId : taskIds) {
            Task task = mLauncher.getTask(taskId);
            task.addCallback(countDownLatch::countDown);
        }
        countDownLatch.await();
    }

    public abstract boolean isMainThread();

    public abstract String getTaskId();

    public abstract Set<String> dependencies();

    public abstract void onExecute();
}

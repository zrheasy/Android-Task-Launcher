package com.zrh.launch;

import java.util.Set;

/**
 * @author zrh
 * @date 2023/7/4
 */
final class MainTask extends Task {
    private final String id;
    private final Set<String> dependencies;
    private final Runnable task;

    MainTask(TaskLauncher launcher, String id, Set<String> dependencies, Runnable task) {
        super(launcher);
        this.id = id;
        this.dependencies = dependencies;
        this.task = task;
    }

    @Override
    public boolean isMainThread() {
        return true;
    }

    @Override
    public String getTaskId() {
        return id;
    }

    @Override
    public Set<String> dependencies() {
        return dependencies;
    }

    @Override
    public void onExecute() {
        task.run();
    }
}

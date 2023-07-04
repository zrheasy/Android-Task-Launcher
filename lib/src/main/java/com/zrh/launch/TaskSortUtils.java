package com.zrh.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zrh
 * @date 2023/7/4
 */
class TaskSortUtils {
    private final Map<String, Task> tasks;
    private final List<Task> result = new ArrayList<>();
    // 0 未访问 1 已访问 2 已完成
    private final Map<String, Integer> state = new HashMap<>();

    public TaskSortUtils(Map<String, Task> tasks) {
        this.tasks = tasks;
        for (String id : tasks.keySet()) {
            state.put(id, 0);
        }
    }

    /**
     * 使用深度优先遍历对任务进行拓扑排序
     *
     * @return 返回排序后的列表
     */
    List<Task> sort() {
        for (String id : tasks.keySet()) {
            visit(id);
        }
        return result;
    }

    private void visit(String taskId) {
        // 修改访问状态
        state.put(taskId, 1);
        Task task = tasks.get(taskId);

        for (String id : task.dependencies()) {
            int taskState = state.get(id);
            if (taskState == 0) {
                // 未访问过则继续向下访问
                visit(id);
            } else if (taskState == 1) {
                // 已访问过说明依赖成环
                throw new IllegalStateException("循环依赖：" + taskId + "->" + id);
            } else if (taskState == 2) {
                // 已完成访问，继续访问其他依赖节点
            }
        }
        // 访问完成加入集合
        state.put(taskId, 2);
        result.add(task);
    }
}

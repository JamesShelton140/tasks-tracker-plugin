package net.reldo.taskstracker.panel.filters;

import java.util.List;

import com.google.gson.Gson;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

public class SimpleFilter extends Filter {
    SimpleFilter(TasksTrackerConfig config, String keyword, Gson gson) {
        super(config, keyword, gson);
    }

    @Override
    public boolean meetsCriteria(Task task) {
        List<String> filterValues = getCurrentFilters();

//        return filterValues.contains(task.getProperty(keyword).toLowerCase());
        return filterValues.contains(task.getTier().toLowerCase()); //@todo replace "getTier()" with generic property getter "getProperty(keyword)"
    }
}

package net.reldo.taskstracker.panel.filters;

import com.google.gson.Gson;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

import javax.inject.Inject;
@Slf4j
public abstract class Filter
{
    private final Gson gson;
    private final TasksTrackerConfig config;
    String keyword;

    protected Filter(TasksTrackerConfig config, String keyword, Gson gson)
    {
        this.config = config;
        this.keyword = keyword;
        this.gson = gson;
    }

    protected List<String> getCurrentFilters()
    {
        FilterData filterData = this.gson.fromJson(config.propFilter(), FilterData.class);

        String taskType = config.taskType().name();

        return filterData.getFilterValues(taskType + "_" + keyword);
    }

    public abstract boolean meetsCriteria(Task task);

}

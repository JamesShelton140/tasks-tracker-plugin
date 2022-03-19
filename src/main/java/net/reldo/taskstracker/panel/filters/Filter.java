package net.reldo.taskstracker.panel.filters;

import com.google.gson.Gson;
import java.util.List;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

public abstract class Filter
{
    private final TasksTrackerConfig config;
    String keyword;

    protected Filter(TasksTrackerConfig config, String keyword)
    {
        this.config = config;
        this.keyword = keyword;
    }

    protected List<String> getCurrentFilters()
    {
        Gson gson = new Gson();
        FilterData filterData = gson.fromJson(config.propFilter(), FilterData.class);

        return filterData.getFilterValues(keyword);
    }

    public abstract boolean meetsCriteria(Task task);

}

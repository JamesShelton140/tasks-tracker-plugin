package net.reldo.taskstracker.panel.filters;

import net.reldo.taskstracker.TasksTrackerConfig;

public class FilterFactory
{
    public Filter createFilterFromJson(String json)
    {
        return null;
    }

    public static Filter createFilterFromType(TasksTrackerConfig config, String type, String keyword)
    {
        switch (type)
        {
            case "simple":
                return new SimpleFilter(config, keyword);
            case "exclusive":
                return new ExclusiveFilter(config, keyword);
            default:
                return null;
        }
    }
}

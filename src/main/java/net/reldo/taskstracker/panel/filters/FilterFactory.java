package net.reldo.taskstracker.panel.filters;

import java.util.Arrays;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.RequiredSkill;
import net.reldo.taskstracker.tasktypes.Task;

public class FilterFactory
{
    public Filter createFilterFromJson()
    {
        return null;
    }

    private abstract class Filter
    {
        protected final TasksTrackerConfig config;
        String keyword;

        public Filter(TasksTrackerConfig config, String keyword)
        {
            this.config = config;
            this.keyword = keyword;
        }

        protected String getFilters()
        {
            String allFilters = config.propFilter();
            String propFilter = allFilters.substring(allFilters.indexOf(keyword) + keyword.length());

            return propFilter.substring(0, propFilter.indexOf(";"));
        }

        public abstract boolean meetsCriteria(Task task);
    }

    private class SimpleFilter extends Filter
    {
        public SimpleFilter(TasksTrackerConfig config, String keyword)
        {
            super(config, keyword);
        }

        @Override
        public boolean meetsCriteria(Task task)
        {
            String filters = getFilters();

            return filters.contains("f-" + task.getProperty(keyword).toLowerCase());
        }
    }

    private class ExclusiveFilter extends Filter
    {
        public ExclusiveFilter(TasksTrackerConfig config, String keyword)
        {
            super(config, keyword);
        }

        @Override
        public boolean meetsCriteria(Task task)
        {
//            if(task.getSkills() == null) return true;

            String filters = getFilters();

            if (task.getSkills().length > 0 && !Arrays.stream(task.getSkills())
                    .allMatch((RequiredSkill skill) -> filters.contains(skill.getSkill().toLowerCase())))
            {
                return false;
            }

            return task.getSkills().length != 0 || filters.contains("na");
        }
    }
}

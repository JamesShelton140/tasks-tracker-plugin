package net.reldo.taskstracker.panel.filters;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.RequiredSkill;
import net.reldo.taskstracker.tasktypes.Task;

public class ExclusiveFilter extends Filter {
    ExclusiveFilter(TasksTrackerConfig config, String keyword, Gson gson) {
        super(config, keyword, gson);
    }

    @Override
    public boolean meetsCriteria(Task task) {
        List<String> filterValues = getCurrentFilters();

        if (task.getSkills() == null || filterValues == null) return true;

        if (task.getSkills().length > 0 && !Arrays.stream(task.getSkills())
                .allMatch((RequiredSkill skill) -> filterValues.contains(skill.getSkill().toLowerCase()))) //@todo replace "skill" with generic property
        {
            return false;
        }

        return task.getSkills().length != 0 || filterValues.contains("na"); //@todo replace "getSkills()" with generic property getter "getProperty(keyword)"
    }
}

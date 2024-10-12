package net.reldo.taskstracker.config;


public class ConfigValues {

    public enum CompletedFilterValues
    {
        COMPLETE_AND_INCOMPLETE,
        COMPLETE,
        INCOMPLETE;
    }

    public enum TrackedFilterValues
    {
        TRACKED_AND_UNTRACKED,
        TRACKED,
        UNTRACKED;
    }

    public enum IgnoredFilterValues
    {
        NOT_IGNORED,
        IGNORED_AND_NOT_IGNORED,
        IGNORED;
    }

    public enum TaskListTabs
    {
        TRACKED("Tracked Tasks"),
        ALL("All Tasks"),
        CUSTOM("Custom");

        private final String tabLabel;

        TaskListTabs(String tabLabel)
        {
            this.tabLabel = tabLabel;
        }

        public static TaskListTabs getTabByLabel(String label)
        {
            for (TaskListTabs value : values())
            {
                if (value.tabLabel.equalsIgnoreCase(label))
                {
                    return value;
                }
            }

            return null;
        }
    }

}

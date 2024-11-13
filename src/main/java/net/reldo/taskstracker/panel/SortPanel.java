package net.reldo.taskstracker.panel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.components.MultiToggleButton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class SortPanel extends FixedWidthPanel
{

    private final TasksTrackerPlugin plugin;
    private final TaskService taskService;
    private final TaskListPanel taskListPanel;
    private final ConfigManager configManager;
    private JComboBox<String> sortDropdown;
    private MultiToggleButton directionButton;

    public SortPanel(TasksTrackerPlugin plugin, TaskService taskService, TaskListPanel taskListPanel)
    {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.taskService = taskService;
        this.taskListPanel = taskListPanel;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    public void redraw()
    {
        removeAll();

        List<String> criteriaList =
                Stream.of(taskService.getCurrentTaskTypeDefinition().getIntParamMap().keySet(),
                        taskService.getCurrentTaskTypeDefinition().getStringParamMap().keySet())
                .flatMap(Set::stream)
                .map((str) -> str.substring(0, 1).toUpperCase() + str.substring(1))
                .collect(Collectors.toList());
        criteriaList.add("Completion %");
        criteriaList.add("Default");
        String[] criteriaArray = criteriaList.toArray(new String[0]);
        sortDropdown = new JComboBox<>(criteriaArray);
        sortDropdown.setAlignmentX(LEFT_ALIGNMENT);
        sortDropdown.setSelectedItem(criteriaArray[0]);
        sortDropdown.addActionListener(e -> {
            updateConfig();
            SwingUtilities.invokeLater(() -> {
                taskListPanel.redraw();
                taskListPanel.refresh(null);
            });
        });
        sortDropdown.setFocusable(false);

        directionButton = new MultiToggleButton(2);
        SwingUtil.removeButtonDecorations(directionButton);
        directionButton.setIcons(new Icon[]{Icons.ASCENDING_ICON, Icons.DESCENDING_ICON});
        directionButton.setToolTips(new String[]{"Ascending", "Descending"});
        directionButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        directionButton.setStateChangedAction(e -> {
            updateConfig();
            SwingUtilities.invokeLater(() -> {
                taskListPanel.redraw();
                taskListPanel.refresh(null);
            });
        });

        add(sortDropdown);
        add(directionButton);
    }

    protected void updateConfig()
    {
        log.debug("updateConfig {}, {}, {}", TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortCriteria", sortDropdown.getItemAt(sortDropdown.getSelectedIndex()).toLowerCase());
        configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortCriteria", sortDropdown.getItemAt(sortDropdown.getSelectedIndex()).toLowerCase());

        ConfigValues.SortDirections configValue = ConfigValues.SortDirections.values()[directionButton.getState()];
        log.debug("updateConfig {}, {}, {}", TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortDirection", configValue);
        configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortDirection", configValue);
    }
}
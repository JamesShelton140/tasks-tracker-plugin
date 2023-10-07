package net.reldo.taskstracker.panel.subfilters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.filters.FilterData;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public abstract class FilterPanel extends FixedWidthPanel
{

    protected final TasksTrackerPlugin plugin;

    protected final Map<String, AbstractButton> buttons = new HashMap<>();
    protected String configKey;


    public FilterPanel(TasksTrackerPlugin plugin, String configKey)
    {
        this.plugin = plugin;
        this.configKey = configKey;
    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makePanel();

    protected abstract AbstractButton makeButton(String name, BufferedImage image);

    protected JPanel allOrNoneButtons()
    {
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttonWrapper.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        JButton all = new JButton("all");
        SwingUtil.removeButtonDecorations(all);
        all.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        all.setFont(FontManager.getRunescapeSmallFont());
        all.addActionListener(e -> {
            setAllSelected(true);
            saveFilterState();
            plugin.refresh();
        });

        JButton none = new JButton("none");
        SwingUtil.removeButtonDecorations(none);
        none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        none.setFont(FontManager.getRunescapeSmallFont());
        none.addActionListener(e -> {
            setAllSelected(false);
            saveFilterState();
            plugin.refresh();
        });

        JLabel separator = new JLabel("|");
        separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        buttonWrapper.add(all);
        buttonWrapper.add(separator);
        buttonWrapper.add(none);

        return buttonWrapper;
    }

    protected void saveFilterState()
    {
        TasksTrackerConfig config = plugin.getConfig();
        Gson gson = new Gson();
        FilterData filterData;
        try
        {
            filterData = gson.fromJson(config.propFilter(), FilterData.class);
        }
        catch (JsonSyntaxException e)
        {
            filterData = new FilterData();
        }

        List<String> filterValues = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        String taskType = config.taskType().name();

        filterData.put(taskType + "_" + configKey, filterValues);

        plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "propFilter", gson.toJson(filterData));
    }

    public void loadFilterState()
    {
        TasksTrackerConfig config = plugin.getConfig();

        Gson gson = new Gson();
        FilterData filterData = gson.fromJson(config.propFilter(), FilterData.class);
        List<String> filterState = filterData.getFilterValues(config.taskType().name() + "_" + configKey);

        if(filterState == null) return;

        buttons.forEach((key, value) -> value.setSelected(filterState.contains(key)));
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }

    public abstract void redraw();
}

package net.reldo.taskstracker.panel.subfilters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import net.runelite.client.config.Config;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public abstract class FilterButtonPanel extends FixedWidthPanel
{
    protected final TasksTrackerPlugin plugin;

    protected final Map<String, JToggleButton> buttons = new HashMap<>();
    protected String configKey;


    public FilterButtonPanel(TasksTrackerPlugin plugin)
    {
        this.plugin = plugin;

    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makeButtonPanel();

    protected JToggleButton makeButton(String name, BufferedImage image)
    {
        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new EmptyBorder(2, 0, 2, 0));

        if(image != null) {
            ImageIcon selectedIcon = new ImageIcon(image);
            ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

            button.setIcon(deselectedIcon);
            button.setSelectedIcon(selectedIcon);
        }

        button.setToolTipText(name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase());

        button.addActionListener(e -> {
            updateFilterText();
            plugin.refresh();
        });

        button.setSelected(true);

        return button;
    }

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
            updateFilterText();
            plugin.refresh();
        });

        JButton none = new JButton("none");
        SwingUtil.removeButtonDecorations(none);
        none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        none.setFont(FontManager.getRunescapeSmallFont());
        none.addActionListener(e -> {
            setAllSelected(false);
            updateFilterText();
            plugin.refresh();
        });

        JLabel separator = new JLabel("|");
        separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        buttonWrapper.add(all);
        buttonWrapper.add(separator);
        buttonWrapper.add(none);

        return buttonWrapper;
    }

    protected void updateFilterText()
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
//        List<String> filterValues = filterData.getData().containsKey(configKey) ? filterData.getFilterValues(configKey) : new ArrayList<>();

        List<String> filterValues = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        filterData.put(configKey, filterValues);

        plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "propFilter", gson.toJson(filterData));
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        buttons.clear();
        removeAll();

        add(makeButtonPanel(), BorderLayout.CENTER);
        add(allOrNoneButtons(), BorderLayout.SOUTH);
        updateFilterText();

        validate();
        repaint();
    }
}

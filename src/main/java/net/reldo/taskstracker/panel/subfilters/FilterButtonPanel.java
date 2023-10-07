package net.reldo.taskstracker.panel.subfilters;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public abstract class FilterButtonPanel extends FilterPanel
{
    public FilterButtonPanel(TasksTrackerPlugin plugin, Gson gson, String configKey)
    {
        super(plugin, gson, configKey);
    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makePanel();

    protected AbstractButton makeButton(String name, BufferedImage image)
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
            saveFilterState();
            plugin.refresh();
        });

        button.setSelected(true);

        return button;
    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        buttons.clear();
        removeAll();

        add(makePanel(), BorderLayout.CENTER);
        add(allOrNoneButtons(), BorderLayout.SOUTH);

        if(plugin.getConfig().saveSubFilterState())
        {
            loadFilterState();
        }
        else
        {
            saveFilterState();
        }

        validate();
        repaint();
    }
}

package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.subfilters.DifficultyFilterPanel;
import net.reldo.taskstracker.panel.subfilters.FilterPanel;
import net.reldo.taskstracker.panel.subfilters.SkillFilterPanel;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

public class SubFilterPanel extends FixedWidthPanel
{
    private final List<FilterPanel> filterPanels = new ArrayList<>();

    public SubFilterPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager, Gson gson)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 0, 0, 0));// Right border to offset scroll pane width extension (16)
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setVisible(false);

        addFilterPanel(new SkillFilterPanel(plugin, gson));
        addFilterPanel(new DifficultyFilterPanel(plugin, spriteManager, gson));
    }

    public void addFilterPanel(FilterPanel panel)
    {
        filterPanels.add(panel);
        add(panel);
    }

    public void redraw()
    {
        filterPanels.forEach(FilterPanel::redraw);
    }
}

package net.reldo.taskstracker.panel.components;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JProgressBar;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.ProgressType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskProgressDefinition;
import net.reldo.taskstracker.data.task.ITask;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TaskProgressBar extends JProgressBar
{
	private final TasksTrackerPlugin plugin;
	private final ITask task;
	private final TaskProgressDefinition def;

	public TaskProgressBar(TasksTrackerPlugin plugin, ITask task, TaskProgressDefinition def, int barHeight, boolean withText)
	{
		this.plugin = plugin;
		this.task = task;
		this.def = def;

		int current = getCurrentValue();
		int target = def.getTarget();

		setMinimum(0);
		setMaximum(target);
		setValue(Math.min(current, target));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, barHeight));
		setPreferredSize(new Dimension(0, barHeight));
		setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		setForeground(new Color(0, 168, 0));
		setBorderPainted(false);
		setStringPainted(withText);
		if (withText)
		{
			setString(formatProgressText(current, target));
			setFont(FontManager.getRunescapeSmallFont());
		}
	}

	public void refresh()
	{
		int current = getCurrentValue();
		int target = def.getTarget();

		setValue(Math.min(current, target));
		if (isStringPainted())
		{
			setString(formatProgressText(current, target));
		}
	}

	private int getCurrentValue()
	{
		switch (def.getType())
		{
			case EXPERIENCE:
				if (plugin.playerXp == null)
				{
					return 0;
				}
				try
				{
					Skill skill = Skill.valueOf(def.getSkill().toUpperCase());
					return plugin.playerXp[skill.ordinal()];
				}
				catch (IllegalArgumentException | NullPointerException ex)
				{
					log.warn("Unknown skill for EXPERIENCE progress: {}", def.getSkill());
					return 0;
				}
			case LEVEL:
				if (plugin.playerSkills == null)
				{
					return 0;
				}
				try
				{
					Skill skill = Skill.valueOf(def.getSkill().toUpperCase());
					return plugin.playerSkills[skill.ordinal()];
				}
				catch (IllegalArgumentException | NullPointerException ex)
				{
					log.warn("Unknown skill for LEVEL progress: {}", def.getSkill());
					return 0;
				}
			case VARP:
				if (def.getId() == null)
				{
					return 0;
				}
				return task.getProgressValue(ProgressType.VARP, def.getId());
			case VARBIT:
				if (def.getId() == null)
				{
					return 0;
				}
				return task.getProgressValue(ProgressType.VARBIT, def.getId());
			default:
				return 0;
		}
	}

	private String formatProgressText(int current, int target)
	{
		if (target >= 1_000_000)
		{
			return String.format("%s / %s", formatLargeNumber(current), formatLargeNumber(target));
		}
		return current + " / " + target;
	}

	private String formatLargeNumber(int value)
	{
		if (value >= 1_000_000)
		{
			return String.format("%.1fM", value / 1_000_000.0);
		}
		if (value >= 1_000)
		{
			return String.format("%.1fK", value / 1_000.0);
		}
		return String.valueOf(value);
	}
}

package net.reldo.taskstracker.data.route;

import java.awt.event.ActionListener;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;

@Slf4j
public class RouteEditActions // todo: move these to RouteManager as non-static
{
	public static ActionListener addTaskAction(TasksTrackerPlugin plugin, CustomRoute route, Integer taskStructId)
	{
		return e -> {
			if (plugin == null || route == null || taskStructId == null)
			{
				return;
			}

			log.debug("Adding task {} to route {}", taskStructId, route.getName());
			if (route.addItem(taskStructId))
			{
				saveRoute(plugin, route, false).actionPerformed(e);
			}
		};
	}

	public static ActionListener addTaskToSectionAction(TasksTrackerPlugin plugin, CustomRoute route, String sectionId, Integer taskStructId)
	{
		return e -> {
			if (plugin == null || route == null || sectionId == null || taskStructId == null)
			{
				return;
			}

			log.debug("Adding task {} to section {} in route {}", taskStructId, sectionId, route.getName());
			if (route.addItem(sectionId, RouteItem.forTask(taskStructId)))
			{
				saveRoute(plugin, route, false).actionPerformed(e);
			}
		};
	}

	public static ActionListener removeTaskAction(TasksTrackerPlugin plugin, CustomRoute route, Integer taskStructId)
	{
		return e -> {
			if (plugin == null || route == null || taskStructId == null)
			{
				return;
			}

			log.debug("Removing task {} from route {}", taskStructId, route.getName());
			if (route.remove(taskStructId))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	public static ActionListener addNewSectionAction(TasksTrackerPlugin plugin, CustomRoute route)
	{
		return e -> {
			if (plugin == null || route == null)
			{
				return;
			}
			String name = JOptionPane.showInputDialog(
				plugin.pluginPanel,
				"Enter section name:",
				"Create Section",
				JOptionPane.PLAIN_MESSAGE
			);

			log.debug("Adding section {} to route {}", name, route.getName());
			if (name != null && !name.trim().isEmpty())
			{
				name = name.trim();
				route.addSection(new RouteSection(UUID.randomUUID().toString(), name));
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	public static ActionListener removeSectionAction(TasksTrackerPlugin plugin, CustomRoute route, String sectionId)
	{
		return e -> {
			if (plugin == null || route == null || sectionId == null)
			{
				return;
			}

			log.debug("Removing section {} from route {}", sectionId, route.getName());
			RouteSection section = route.get(sectionId);
			removeSectionAction(plugin, route, section).actionPerformed(e);
		};
	}

	public static ActionListener removeSectionAction(TasksTrackerPlugin plugin, CustomRoute route, RouteSection section)
	{
		return e -> {
			if (plugin == null || route == null || section == null)
			{
				return;
			}
			int removeConfirmed = JOptionPane.showConfirmDialog(
				plugin.pluginPanel,
				"Are you sure you want to remove section - " + section.getName() + " - from route - " + route.getName() + "?"
					+ "\nThis action is irreversible.",
				"Remove Section",
				JOptionPane.OK_CANCEL_OPTION
			);

			log.debug("Removing section {} from route {}, dialog returned {}", section.getName(), route.getName(), removeConfirmed);
			if (removeConfirmed == JOptionPane.YES_OPTION && route.remove(section))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	private static ActionListener saveRoute(TasksTrackerPlugin plugin, CustomRoute activeRoute, boolean redraw)
	{
		return e -> {
			plugin.getTaskService().addRouteIndex(activeRoute);
			plugin.getTrackerGlobalConfigStore().addRoute(plugin.getTaskService().getCurrentTaskType().getTaskJsonName(), activeRoute);
			if (redraw)
			{
				SwingUtilities.invokeLater(plugin::redrawTaskList);
			}
		};
	}
}

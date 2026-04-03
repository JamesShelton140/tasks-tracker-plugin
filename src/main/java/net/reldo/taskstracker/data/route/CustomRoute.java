package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Optional;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * A user-defined or imported route for completing tasks in a specific order.
 * Routes are scoped to a task type (e.g., COMBAT, EXPLORATION) and consist
 * of named sections, each containing an ordered list of tasks and custom items.
 *
 * Routes are stored per-taskType and can be selected independently per tab.
 */
@Data
@Slf4j
public class CustomRoute
{
	/** Unique name identifying this route (used as lookup key). */
	@Expose
	@NonNull
	private String name;

	/** The task type this route applies to (e.g., "COMBAT", "EXPLORATION"). */
	@Expose
	private String taskType;

	@Expose
	private String author;

	@Expose
	private String description;

	/** Ordered list of sections making up this route. */
	@Expose
	private List<RouteSection> sections;

	/** Returns all task IDs in route order, flattened across all sections. */
	public List<Integer> getFlattenedOrder()
	{
		if (sections == null)
		{
			return List.of();
		}
		return sections.stream()
			.flatMap(s -> s.getTaskIds().stream())
			.collect(Collectors.toList());
	}

	/** Returns all items (tasks and custom) in route order, flattened across all sections. */
	public List<RouteItem> getFlattenedItems()
	{
		if (sections == null)
		{
			return List.of();
		}
		return sections.stream()
			.flatMap(s -> s.getItems().stream())
			.collect(Collectors.toList());
	}

	public RouteSection getSectionForTask(int taskId)
	{
		if (sections == null)
		{
			return null;
		}
		return sections.stream()
			.filter(s -> s.containsTask(taskId))
			.findFirst()
			.orElse(null);
	}

	public int getTaskCount()
	{
		return getFlattenedOrder().size();
	}

	public int getItemCount()
	{
		return getFlattenedItems().size();
	}

	/** Returns true if the given task is the first task in its section (useful for rendering section headers). */
	public boolean isFirstTaskInSection(int taskId)
	{
		if (sections == null)
		{
			return false;
		}
		for (RouteSection section : sections)
		{
			List<Integer> ids = section.getTaskIds();
			if (!ids.isEmpty() && ids.get(0) == taskId)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Inserts a custom item before or after the specified task.
	 * Searches all sections to find the task.
	 */
	public CustomRouteItem insertCustomItem(int taskId, String customType, boolean insertAfter)
	{
		if (sections == null)
		{
			return null;
		}
		for (RouteSection section : sections)
		{
			CustomRouteItem result = section.insertCustomItem(taskId, customType, insertAfter);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	/** Finds a custom item by its unique ID across all sections. */
	public CustomRouteItem findCustomItem(String customItemId)
	{
		if (sections == null)
		{
			return null;
		}
		for (RouteSection section : sections)
		{
			for (CustomRouteItem ci : section.getCustomItems())
			{
				if (customItemId.equals(ci.getId()))
				{
					return ci;
				}
			}
		}
		return null;
	}

	/** Removes a custom item by ID from whichever section contains it. Returns true if found. */
	public boolean removeCustomItem(String customItemId)
	{
		if (sections == null)
		{
			return false;
		}
		for (RouteSection section : sections)
		{
			if (section.removeCustomItem(customItemId))
			{
				return true;
			}
		}
		return false;
	}

	public RouteSection get(String sectionName)
	{
		return sections.stream()
			.filter(listSection -> listSection.getName().equals(sectionName))
			.findFirst()
			.orElse(null);
	}

	public boolean remove(RouteSection section)
	{
		if (sections == null)
		{
			return false;
		}

		return sections.remove(section);
	}

	public boolean remove(Integer taskId)
	{
		RouteItem item = getFlattenedItems().stream()
			.filter(filterItem -> filterItem.isTask() && filterItem.getTaskId().equals(taskId))
			.findFirst()
			.orElse(null);
		return remove(item);
	}

	public boolean remove(RouteItem item)
	{
		if (sections == null || item == null)
		{
			return false;
		}
		for (RouteSection section : sections)
		{
			if (section.remove(item))
			{
				return true;
			}
		}
		return false;
	}

	public void addSectionAfter(String beforeSectionName, String afterSectionName)
	{
		int index = sections.stream().filter(section -> section.getName().equals(beforeSectionName))
			.findFirst()
			.map( section -> sections.indexOf(section) + 1)
			.orElse(0);

		addSection(index, afterSectionName);
	}

	public void addSection(int index, String sectionName)
	{
		RouteSection section = get(sectionName);
		if (section == null)
		{
			section = new RouteSection(sectionName);
		}

		addSection(index, section);
	}

	public void addSection(RouteSection section)
	{
		if (sections == null)
		{
			sections = new ArrayList<>();
		}

		addSection(sections.size(), section);
	}

	public void addSection(int index, RouteSection section)
	{
		if (sections == null)
		{
			sections = new ArrayList<>();
		}

		int sectionWasRemoved = sections.subList(0, index).remove(section) ? 1 : 0;
		if (sectionWasRemoved == 0)
		{
			sections.remove(section);
		}

		sections.add(index - sectionWasRemoved, section);
	}

	public boolean addItem(Integer taskId)
	{
		return addItem(RouteItem.forTask(taskId));
	}

	public boolean addItem(RouteItem item)
	{
		if (sections == null)
		{
			return false;
		}

		remove(item);

		// Append to last section
		sections.get(sections.size() - 1).add(item);
		return true;
	}

	public boolean addItem(int index, Integer taskId)
	{
		return addItem(index, taskId, false);
	}

	public boolean addItem(int index, Integer taskId, boolean countSectionHeaders)
	{
		return addItem(index, RouteItem.forTask(taskId), countSectionHeaders);
	}

	public boolean addItem(int index, RouteItem item)
	{
		return addItem(index, item, false);
	}

	/** Enforces uniqueness of route items. */
	public boolean addItem(int index, RouteItem item, boolean countSectionHeaders)
	{
		if (sections == null)
		{
			return false;
		}

		remove(item);

		int sectionHeaderPad = countSectionHeaders ? 1 : 0;
		int targetIndex = (index == 0) ? index : index - sectionHeaderPad;

		for (RouteSection section : sections)
		{
			int itemsInSection = section.getItemCount();
			if (targetIndex <= itemsInSection)
			{
				section.add(targetIndex, item);
				return true;
			}

			targetIndex -= itemsInSection + sectionHeaderPad;
		}

		// Index after last section so append to it
		sections.get(sections.size() - 1).add(item);

		return true;
	}

	/**
	 * Add item to the specified section.
	 */
	public boolean addItem(String sectionName, RouteItem item)
	{
		Optional<RouteSection> foundSection = sections.stream()
			.filter(section -> section.getName().equals(sectionName))
			.findFirst();

		if (foundSection.isEmpty())
		{
			return false;
		}
		remove(item);

		foundSection.get().add(item);
		return true;
	}

}

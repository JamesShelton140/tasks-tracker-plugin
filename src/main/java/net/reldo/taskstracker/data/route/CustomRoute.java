package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import java.util.ArrayList;
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
	/** Unique ID for this route. */
	@Expose
	@NonNull
	private String id;

	@Expose
	@NonNull
	private String name;

	/** The task type this route applies to (e.g., "COMBAT", "LEAGUE_5"). */
	@Expose
	@NonNull
	private String taskType;

	@Expose
	private String author;

	@Expose
	private String description;

	/** Ordered list of sections making up this route. Can be empty but not null. */
	@Expose
	@NonNull
	private List<RouteSection> sections;

	/** Returns all task IDs in route order, flattened across all sections. */
	public List<Integer> getFlattenedOrder()
	{
		return sections.stream()
			.flatMap(s -> s.getTaskIds().stream())
			.collect(Collectors.toList());
	}

	/** Returns all items (tasks and custom) in route order, flattened across all sections. */
	public List<RouteItem> getFlattenedItems()
	{
		return sections.stream()
			.flatMap(s -> s.getItems().stream())
			.collect(Collectors.toList());
	}

	public boolean contains(int taskId)
	{
		return getFlattenedOrder().contains(taskId);
	}

	public RouteSection getSectionForTask(int taskId)
	{
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
	public CustomRouteItem insertCustomItem(int taskId, CustomRouteItem customItem, boolean insertAfter)
	{
		for (RouteSection section : sections)
		{
			CustomRouteItem result = section.insertCustomItem(taskId, customItem, insertAfter);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	/** Returns all custom item IDs across all sections (for duplicate detection). */
	public List<String> getAllCustomItemIds()
	{
		List<String> ids = new ArrayList<>();
		for (RouteSection section : sections)
		{
			for (CustomRouteItem ci : section.getCustomItems())
			{
				ids.add(ci.getId());
			}
		}
		return ids;
	}

	/** Finds a custom item by its unique ID across all sections. */
	public CustomRouteItem findCustomItem(String customItemId)
	{
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
		for (RouteSection section : sections)
		{
			if (section.removeCustomItem(customItemId))
			{
				return true;
			}
		}
		return false;
	}

	public RouteSection get(String sectionId)
	{
		return sections.stream()
			.filter(listSection -> listSection.getId().equals(sectionId))
			.findFirst()
			.orElse(null);
	}

	public boolean remove(RouteSection section)
	{
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
		if (item == null)
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

	public void addSectionAfter(String beforeSectionId, String afterSectionId)
	{
		int index = sections.stream().filter(section -> section.getId().equals(beforeSectionId))
			.findFirst()
			.map( section -> sections.indexOf(section) + 1)
			.orElse(0);

		addSection(index, afterSectionId);
	}

	public void addSection(int index, String sectionId)
	{
		RouteSection section = get(sectionId);
		if (section == null)
		{
			section = new RouteSection(UUID.randomUUID().toString(), "Section " + (sections.size() + 1));
		}

		addSection(index, section);
	}

	public void addSection(RouteSection section)
	{
		addSection(sections.size(), section);
	}

	public void addSection(int index, RouteSection section)
	{
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
	public boolean addItem(String sectionId, RouteItem item)
	{
		Optional<RouteSection> foundSection = sections.stream()
			.filter(section -> section.getId().equals(sectionId))
			.findFirst();

		if (foundSection.isEmpty())
		{
			return false;
		}
		remove(item);

		foundSection.get().add(item);
		return true;
	}

	@Override
	public String toString()
	{
		return getName();
	}

}

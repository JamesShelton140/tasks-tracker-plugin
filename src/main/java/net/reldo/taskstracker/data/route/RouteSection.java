package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * A named section within a route, containing an ordered list of items.
 * Sections allow routes to be organized into logical groups (e.g., by region or theme).
 *
 * When reading, the "items" format takes precedence if present.
 * When writing, only the "items" format is used (taskIds is cleared).
 */
@Data
public class RouteSection
{
	@Expose
	@NonNull
	private String id;

	@Expose
	@NonNull
	private String name;

	@Expose
	private String description;

	/** Legacy format: list of task IDs only. Prefer using items instead. */
	@Expose
	private List<Integer> taskIds;

	/** Current format: list of RouteItem (tasks and custom items interleaved). */
	@Expose
	private List<RouteItem> items;

	/**
	 * Returns all items in this section.
	 * Initialises item list if it hasn't been already.
	 */
	public List<RouteItem> getItems()
	{
		if (items != null)
		{
			return items;
		}
		if (taskIds != null)
		{
			setItems(taskIds.stream()
				.filter(id -> id != null)
				.map(id -> RouteItem.forTask(id))
				.collect(Collectors.toList()));

			return items;
		}
		setItems(new ArrayList<>());
		return items;
	}

	public void setItems(List<RouteItem> items)
	{
		this.items = items;
		this.taskIds = null;
	}

	/** Extracts just the task IDs from this section (ignoring custom items). */
	public List<Integer> getTaskIds()
	{
		return getItems().stream()
			.filter(RouteItem::isTask)
			.map(RouteItem::getTaskId)
			.collect(Collectors.toList());
	}

	/** Returns only the custom items (non-task items) in this section. */
	public List<CustomRouteItem> getCustomItems()
	{
		return getItems().stream()
			.filter(item -> !item.isTask())
			.map(RouteItem::getCustomItem)
			.collect(Collectors.toList());
	}

	/** Returns true if this section contains the given task ID. */
	public boolean containsTask(int taskId)
	{
		return getTaskIds().contains(taskId);
	}

	/** Returns the number of tasks (not including custom items) in this section. */
	public int getTaskCount()
	{
		return getTaskIds().size();
	}

	/** Returns the number of tasks (not including custom items) in this section. */
	public int getItemCount()
	{
		return getItems().size();
	}

	/**
	 * Inserts a custom item before or after the specified task.
	 *
	 * @param taskId the task to insert relative to
	 * @param customItem the custom item to insert
	 * @param insertAfter true to insert after the task, false to insert before
	 * @return the inserted CustomRouteItem, or null if the task was not found
	 */
	public CustomRouteItem insertCustomItem(int taskId, CustomRouteItem customItem, boolean insertAfter)
	{
		List<RouteItem> currentItems = new ArrayList<>(getItems());
		int position = -1;

		for (int i = 0; i < currentItems.size(); i++)
		{
			RouteItem item = currentItems.get(i);
			if (item.isTask() && item.getTaskId() == taskId)
			{
				position = i;
				break;
			}
		}

		if (position == -1)
		{
			return null;
		}

		int insertPos = insertAfter ? position + 1 : position;
		currentItems.add(insertPos, RouteItem.forCustom(customItem));

		setItems(currentItems);

		return customItem;
	}

	public boolean removeCustomItem(String customItemId)
	{
		if (getItems() == null)
		{
			return false;
		}
		return getItems().removeIf(item ->
			!item.isTask()
			&& item.getCustomItem() != null
			&& customItemId.equals(item.getCustomItem().getId()));
	}

	public boolean remove(Integer taskId)
	{
		if (getItems() == null)
		{
			return false;
		}
		return getItems().removeIf(item ->
			item.isTask()
				&& taskId.equals(item.getTaskId()));
	}

	public boolean remove(String customId)
	{
		if (getItems() == null)
		{
			return false;
		}
		return getItems().removeIf(item ->
			!item.isTask() &&
			item.getCustomItem() != null &&
			item.getCustomItem().getId().equals(customId));
	}

	public boolean remove(RouteItem item)
	{
		if (getItems() == null)
		{
			return false;
		}

		return getItems().removeIf(anItem -> anItem.equals(item));
	}

	public void add(int index, Integer taskId)
	{
		add(index, RouteItem.forTask(taskId));
	}

	public void add(int index, RouteItem item)
	{
		getItems().add(index, item);
	}

	public void add(Integer taskId)
	{
		getItems().add(RouteItem.forTask(taskId));
	}

	public void add(RouteItem item)
	{
		getItems().add(item);
	}
}

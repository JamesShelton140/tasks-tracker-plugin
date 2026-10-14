package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import java.util.Arrays;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Quest;

/**
 * Represents a non-task item in a route, such as a bank stop, teleport, or custom step.
 * Generic data model: label + icon + name, no hardcoded types.
 * Factory presets provide convenience for common items.
 */
@Data
@NoArgsConstructor
public class CustomRouteItem
{
	/** Preset custom item types, each with a default label and icon.
	 *  Generic item type 'Custom' does not have a default icon.*/
	@Getter
	@RequiredArgsConstructor
	public enum Type
	{
		CUSTOM("Custom", null),
		BANK("Bank", 1453 /* Bank map icon. */),
		TELEPORT("Teleport", 1504 /* Transportation map icon. */),
		QUEST("Quest", 835 /* Quest tab icon. */);

		private final String label;
		private final Integer icon;

		@Override
		public String toString()
		{
			return label;
		}
	}

	/** Unique identifier (8-char UUID prefix). Counterpart to structId/taskId. */
	@Expose
	@NonNull
	private String id;

	/** Primary display text (e.g., "Bank", "Teleport"). Counterpart to task name. */
	@Expose
	@NonNull
	private String label;

	/** Sprite archive ID for the icon. Counterpart to tier sprite. Null for text fallback. */
	@Expose
	private Integer icon;

	/** Detail text shown below the label. Counterpart to task description. */
	@Expose
	private String description;

	/** RuneLite Quest id for auto-completion tracking. Null for non-quest items. */
	@Expose
	private Integer questId;

	/** Returns the label for display, falling back to "Custom" if empty. */
	public String getDisplayLabel()
	{
		if (label == null || label.isEmpty())
		{
			return Type.CUSTOM.getLabel();
		}
		return label;
	}

	private static String generateId()
	{
		return UUID.randomUUID().toString();
	}

	/** Creates an item with the type's default label and icon. */
	public static CustomRouteItem create(Type type, String description)
	{
		CustomRouteItem item = new CustomRouteItem();
		item.setId(generateId());
		item.setLabel(type.getLabel());
		item.setIcon(type.getIcon());
		item.setDescription(description);
		return item;
	}

	public static CustomRouteItem createQuest(Integer questId)
	{
		Quest quest = Arrays.stream(Quest.values())
			.filter(questValue -> questValue.getId() == questId)
			.findFirst()
			.orElse(null);
		String description = quest != null ? quest.getName() : "Unknown Quest Id";

		CustomRouteItem item = create(Type.QUEST, description);
		item.setQuestId(questId);

		return item;
	}

	public static CustomRouteItem createCustom(String label, String description)
	{
		CustomRouteItem item = create(Type.CUSTOM, description);
		item.setLabel(label);
		return item;
	}
}

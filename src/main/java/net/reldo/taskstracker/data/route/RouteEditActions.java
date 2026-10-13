package net.reldo.taskstracker.data.route;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.api.Quest;

@Slf4j
public class RouteEditActions // todo: move these to RouteManager as non-static
{
	private static final String CUSTOM_ITEM_TYPE = "Custom";
	private static final String QUEST_ITEM_TYPE = "Quest";

	/**
	 * Builds an action that adds a task to the end of the provided route.
	 *
	 * @param plugin the plugin instance used to persist and redraw after the change
	 * @param route  the route to add the task to
	 * @param taskId the id of the task to add
	 * @return an action that performs the add and saves the route
	 */
	public static ActionListener addTaskAction(TasksTrackerPlugin plugin, CustomRoute route, Integer taskId)
	{
		return e -> {
			if (plugin == null || route == null || taskId == null)
			{
				return;
			}

			log.debug("Adding task {} to route {}", taskId, route.getName());
			if (route.addItem(taskId))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that adds a task to a specific section of a route.
	 *
	 * @param plugin    the plugin instance used to persist and redraw after the change
	 * @param route     the route containing the section
	 * @param sectionId the id of the section to add the task to
	 * @param taskId    the id of the task to add
	 * @return an action that performs the add and saves the route
	 */
	public static ActionListener addTaskToSectionAction(TasksTrackerPlugin plugin, CustomRoute route, String sectionId, Integer taskId)
	{
		return e -> {
			if (plugin == null || route == null || sectionId == null || taskId == null)
			{
				return;
			}

			log.debug("Adding task {} to section {} in route {}", taskId, sectionId, route.getName());
			if (route.addItem(sectionId, RouteItem.forTask(taskId)))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that removes a task from a route, wherever it appears.
	 *
	 * @param plugin the plugin instance used to persist and redraw after the change
	 * @param route  the route to remove the task from
	 * @param taskId the id of the task to remove
	 * @return an action that performs the removal and saves the route
	 */
	public static ActionListener removeTaskAction(TasksTrackerPlugin plugin, CustomRoute route, Integer taskId)
	{
		return e -> {
			if (plugin == null || route == null || taskId == null)
			{
				return;
			}

			log.debug("Removing task {} from route {}", taskId, route.getName());
			if (route.remove(taskId))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that prompts the user for a custom or quest item via
	 * {@link #getCustomItem(Component)} and, if confirmed, adds the resulting custom item
	 * to the end of the provided route.
	 *
	 * @param plugin the plugin instance used to persist and redraw after the change
	 * @param route  the route to add the custom item to
	 * @return an action that prompts for the item and, if confirmed, saves the route
	 */
	public static ActionListener addNewCustomItemAction(TasksTrackerPlugin plugin, CustomRoute route)
	{
		return e -> {
			if (plugin == null || route == null)
			{
				return;
			}

			CustomRouteItem item = getCustomItem(plugin.pluginPanel);
			if (item == null)
			{
				return;
			}

			log.debug("Adding custom item \"{}\" to route {}", item.getLabel(), route.getName());
			if (route.addItem(RouteItem.forCustom(item)))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Prompts the user for a custom item. A dropdown at the top switches between types
	 * of custom item. Any data entered into fields not shown for selected type are ignored.
	 *
	 * @param parent the component to anchor the dialog to
	 * @return the created custom item, or null if the user cancelled the dialog
	 */
	private static CustomRouteItem getCustomItem(Component parent)
	{
		JComboBox<String> typeDropdown = new JComboBox<>(new String[]{CUSTOM_ITEM_TYPE, QUEST_ITEM_TYPE});

		// Custom item fields
		JTextField labelField = new JTextField();
		JTextArea descriptionField = new JTextArea(3, 20);
		descriptionField.setLineWrap(true);
		descriptionField.setWrapStyleWord(true);

		JPanel customPanel = new JPanel();
		customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
		customPanel.add(new JLabel("Label:"));
		customPanel.add(labelField);
		customPanel.add(Box.createVerticalStrut(8));
		customPanel.add(new JLabel("Description (optional):"));
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionField);
		customPanel.add(descriptionScrollPane);

		// Quest item fields
		JComboBox<Quest> questDropdown = createQuestDropdown();

		JPanel questPanel = new JPanel();
		questPanel.setLayout(new BoxLayout(questPanel, BoxLayout.Y_AXIS));
		questPanel.add(new JLabel("Quest:"));
		questPanel.add(questDropdown);

		// Show only the fields for the selected type
		CardLayout fieldsLayout = new CardLayout();
		JPanel fieldsPanel = new JPanel(fieldsLayout);
		fieldsPanel.add(customPanel, CUSTOM_ITEM_TYPE);
		fieldsPanel.add(questPanel, QUEST_ITEM_TYPE);
		typeDropdown.addActionListener(e -> fieldsLayout.show(fieldsPanel, (String) typeDropdown.getSelectedItem()));

		JPanel dialogPanel = new JPanel();
		dialogPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.add(new JLabel("Type:"));
		dialogPanel.add(typeDropdown);
		dialogPanel.add(Box.createVerticalStrut(8));
		dialogPanel.add(fieldsPanel);

		// Inputs default to centre alignment, which offsets the left-aligned labels in BoxLayout
		typeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelField.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		questDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

		while (true)
		{
			int result = JOptionPane.showConfirmDialog(
				parent,
				dialogPanel,
				"Create Custom Task",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
			);

			if (result != JOptionPane.OK_OPTION)
			{
				return null;
			}

			if (QUEST_ITEM_TYPE.equals(typeDropdown.getSelectedItem()))
			{
				Quest quest = (Quest) questDropdown.getSelectedItem();
				if (quest == null)
				{
					JOptionPane.showMessageDialog(parent, "Quest is required.", "Create Custom Task", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				return CustomRouteItem.createQuest(quest.getId(), quest.getName());
			}

			String label = labelField.getText().trim();
			if (label.isEmpty())
			{
				JOptionPane.showMessageDialog(parent, "Label is required.", "Create Custom Task", JOptionPane.WARNING_MESSAGE);
				continue;
			}

			String description = descriptionField.getText().trim();
			return CustomRouteItem.createCustom(label, description.isEmpty() ? null : description);
		}
	}

	/**
	 * Creates a dropdown of all quests, sorted by name.
	 */
	private static JComboBox<Quest> createQuestDropdown()
	{
		Quest[] quests = Arrays.stream(Quest.values())
			.sorted(Comparator.comparing(Quest::getName, String.CASE_INSENSITIVE_ORDER))
			.toArray(Quest[]::new);

		JComboBox<Quest> questDropdown = new JComboBox<>(quests);
		questDropdown.setMaximumRowCount(15);
		questDropdown.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Quest)
				{
					setText(((Quest) value).getName());
				}
				return this;
			}
		});
		return questDropdown;
	}

	/**
	 * Builds an action that adds an arbitrary route item to the end of the provided route.
	 *
	 * @param plugin    the plugin instance used to persist and redraw after the change
	 * @param route     the route to add the item to
	 * @param routeItem the item to add
	 * @return an action that performs the add and saves the route
	 */
	public static ActionListener addRouteItemAction(TasksTrackerPlugin plugin, CustomRoute route, RouteItem routeItem)
	{
		return e -> {
			if (plugin == null || route == null || routeItem == null)
			{
				return;
			}

			log.debug("Adding item to route {}", route.getName());
			if (route.addItem(routeItem))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that adds an arbitrary route item to a specific section of a route.
	 *
	 * @param plugin    the plugin instance used to persist and redraw after the change
	 * @param route     the route containing the section
	 * @param sectionId the id of the section to add the item to
	 * @param routeItem the item to add
	 * @return an action that performs the add and saves the route
	 */
	public static ActionListener addRouteItemToSectionAction(TasksTrackerPlugin plugin, CustomRoute route, String sectionId, RouteItem routeItem)
	{
		return e -> {
			if (plugin == null || route == null || sectionId == null || routeItem == null)
			{
				return;
			}

			log.debug("Adding item to section {} in route {}", sectionId, route.getName());
			if (route.addItem(sectionId, routeItem))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that removes a route item from a route, wherever it appears.
	 *
	 * @param plugin    the plugin instance used to persist and redraw after the change
	 * @param route     the route to remove the item from
	 * @param routeItem the item to remove
	 * @return an action that performs the removal and saves the route
	 */
	public static ActionListener removeRouteItemAction(TasksTrackerPlugin plugin, CustomRoute route, RouteItem routeItem)
	{
		return e -> {
			if (plugin == null || route == null || routeItem == null)
			{
				return;
			}

			log.debug("Removing item from route {}", route.getName());
			if (route.remove(routeItem))
			{
				saveRoute(plugin, route, true).actionPerformed(e);
			}
		};
	}

	/**
	 * Builds an action that prompts the user for a section name and, if confirmed, adds a
	 * new section to a route.
	 *
	 * @param plugin the plugin instance used to persist and redraw after the change
	 * @param route  the route to add the section to
	 * @return an action that prompts for the name and, if confirmed, saves the route
	 */
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

	/**
	 * Builds an action that looks up a section by id and delegates to
	 * {@link #removeSectionAction(TasksTrackerPlugin, CustomRoute, RouteSection)}.
	 *
	 * @param plugin    the plugin instance used to persist and redraw after the change
	 * @param route     the route containing the section
	 * @param sectionId the id of the section to remove
	 * @return an action that confirms and performs the removal
	 */
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

	/**
	 * Builds an action that asks the user to confirm and, if confirmed, removes a section
	 * (and its contents) from a route.
	 *
	 * @param plugin  the plugin instance used to persist and redraw after the change
	 * @param route   the route containing the section
	 * @param section the section to remove
	 * @return an action that confirms and performs the removal
	 */
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

	/**
	 * Builds an action that persists a route and, optionally, triggers a redraw of the task list.
	 *
	 * @param plugin      the plugin instance used to persist and redraw the route
	 * @param activeRoute the route to save
	 * @param redraw      whether to redraw the task list after saving
	 * @return an action that saves the route and redraws if requested
	 */
	private static ActionListener saveRoute(TasksTrackerPlugin plugin, CustomRoute activeRoute, boolean redraw)
	{
		return e -> {
			plugin.getTaskService().addRouteIndex(activeRoute);
			plugin.getTrackerGlobalConfigStore().addRoute(plugin.getTaskService().getCurrentTaskType().getTaskJsonName(), activeRoute);
			if (redraw)
			{
				plugin.redrawTaskList();
			}
		};
	}
}

package net.reldo.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.task.TaskService;
import net.runelite.client.ui.ColorScheme;

public class RouteEditPanel extends JPanel
{
	private static final String ADD_SECTION_LABEL = "Add Section";
	private static final String ADD_SECTION_TOOLTIP = "Add new section";

	private static final String ADD_CUSTOM_ITEM_LABEL = "Add Custom";
	private static final String ADD_CUSTOM_ITEM_TOOLTIP = "Add new custom item";

	private final JButton addSectionButton;
	private final JButton addCustomItemButton;
	private final TasksTrackerPlugin plugin;
	private final TaskService taskService;

	public RouteEditPanel(TasksTrackerPlugin plugin, TaskService taskService)
	{
		this.plugin = plugin;
		this.taskService = taskService;
//		setLayout(new BorderLayout(5, 0));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		addSectionButton = new JButton(ADD_SECTION_LABEL);
		addSectionButton.setToolTipText(ADD_SECTION_TOOLTIP);
		addSectionButton.setForeground(ColorScheme.TEXT_COLOR);
		addSectionButton.setFocusable(false);
		addButton(addSectionButton);

		addCustomItemButton = new JButton(ADD_CUSTOM_ITEM_LABEL);
		addCustomItemButton.setToolTipText(ADD_CUSTOM_ITEM_TOOLTIP);
		addCustomItemButton.setForeground(ColorScheme.TEXT_COLOR);
		addCustomItemButton.setFocusable(false);
		addButton(addCustomItemButton);

	}

	private void addButton(JButton button)
	{
		JLabel label = new JLabel(" L");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		JPanel buttonContainer = new JPanel(new BorderLayout(5, 0));
		buttonContainer.add(label, BorderLayout.WEST);
		buttonContainer.add(button, BorderLayout.CENTER);
		add(buttonContainer);
		add(Box.createVerticalStrut(2));
	}

	public void addAddSectionListener(ActionListener listener)
	{
		addSectionButton.addActionListener(listener);
	}

	public void addAddCustomListener(ActionListener listener)
	{
		addCustomItemButton.addActionListener(listener);
	}

	public void redraw()
	{
		setVisible(taskService.activeRouteInEditMode());
	}
}

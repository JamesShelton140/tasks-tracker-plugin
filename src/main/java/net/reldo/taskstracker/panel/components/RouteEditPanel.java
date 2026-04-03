package net.reldo.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.task.TaskService;
import net.runelite.client.ui.ColorScheme;

public class RouteEditPanel extends JPanel
{
	private static final String ADD_SECTION_LABEL = "Add Section";
	private static final String ADD_SECTION_TOOLTIP = "Add new section";

	private final JButton addSectionButton;
	private final TasksTrackerPlugin plugin;
	private final TaskService taskService;

	public RouteEditPanel(TasksTrackerPlugin plugin, TaskService taskService)
	{
		this.plugin = plugin;
		this.taskService = taskService;
		setLayout(new BorderLayout(5, 0));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel label = new JLabel(" L");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		addSectionButton = new JButton(ADD_SECTION_LABEL);
		addSectionButton.setToolTipText(ADD_SECTION_TOOLTIP);
		addSectionButton.setForeground(ColorScheme.TEXT_COLOR);
		addSectionButton.setFocusable(false);

		add(label, BorderLayout.WEST);
		add(addSectionButton, BorderLayout.CENTER);
	}

	public void addAddSectionListener(ActionListener listener)
	{
		addSectionButton.addActionListener(listener);
	}

	public void redraw()
	{
		setVisible(taskService.activeRouteInEditMode());
	}
}

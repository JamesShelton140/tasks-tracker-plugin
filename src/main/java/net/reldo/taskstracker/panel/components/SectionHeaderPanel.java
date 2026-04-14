package net.reldo.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.RouteEditActions;
import net.reldo.taskstracker.data.route.RouteSection;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class SectionHeaderPanel extends JPanel
{
	private static final Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR.darker();
	private static final Color HOVER_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color PROGRESS_COLOR = new Color(180, 180, 180);
	private static final Color PROGRESS_COMPLETE_COLOR = new Color(100, 200, 100);
	private static final Color DELETE_HOVER_COLOR = Color.RED;

	private static final String ARROW_EXPANDED = "\u25BC";
	private static final String ARROW_COLLAPSED = "\u25B6";
	private static final String DELETE_ICON = "\uD83D\uDDD1";
	private static final String EDIT_ICON = "\u270E";

	private final TasksTrackerPlugin plugin;
	@Getter
	private final String sectionId;
	@Getter
	private String sectionName;
	private String description;

	@Getter
	private boolean collapsed = false;

	private final JPanel container;
	private final JLabel titleLabel;
	private final JLabel progressLabel;
	private final JButton deleteButton;
	private final JButton editButton;
	private final JComponent listPanel;

	@Setter
	private Consumer<Boolean> collapseCallback;
	@Setter
	private Consumer<String> collapseOthersCallback;

	public SectionHeaderPanel(TasksTrackerPlugin plugin, String sectionId, String sectionName, String description, JComponent listPanel)
	{
		this.plugin = plugin;
		this.sectionId = sectionId;
		this.sectionName = sectionName;
		this.description = description;
		this.listPanel = listPanel;

		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(new EmptyBorder(0, 0, 4, 0));

		container = new JPanel(new BorderLayout());
		container.setBackground(BACKGROUND_COLOR);
		container.setBorder(new EmptyBorder(6, 10, 6, 10));
		container.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Title with arrow and optional description
		titleLabel = new JLabel();
		titleLabel.setForeground(TEXT_COLOR);
		titleLabel.setFont(FontManager.getRunescapeFont());
		updateTitleText();

		// Container for east layout section
		JPanel eastContainer = new JPanel();
		eastContainer.setLayout(new BoxLayout(eastContainer, BoxLayout.X_AXIS));
		eastContainer.setOpaque(false);

		// Progress label (right side)
		progressLabel = new JLabel();
		progressLabel.setForeground(PROGRESS_COLOR);
		progressLabel.setFont(FontManager.getRunescapeSmallFont());

		// Edit button
		editButton = new JButton(EDIT_ICON);
		editButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		editButton.addActionListener(e  -> editSection("Name", sectionName, false, RouteSection::setName));
		SwingUtil.removeButtonDecorations(editButton);
		editButton.setForeground(TEXT_COLOR);
		editButton.setToolTipText("Edit Name");
		editButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				editButton.setForeground(ColorScheme.BRAND_ORANGE);
				container.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				editButton.setForeground(TEXT_COLOR);
				container.setBackground(BACKGROUND_COLOR);
			}
		});

		// Delete button
		deleteButton = new JButton(DELETE_ICON);
		deleteButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		deleteButton.addActionListener(e  -> {
			RouteEditActions.removeSectionAction(plugin, plugin.getTaskService().getActiveRoute(), sectionId).actionPerformed(e);
		});
		SwingUtil.removeButtonDecorations(deleteButton);
		deleteButton.setForeground(TEXT_COLOR);
		editButton.setToolTipText("Delete Section");
		deleteButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				deleteButton.setForeground(DELETE_HOVER_COLOR);
				container.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteButton.setForeground(TEXT_COLOR);
				container.setBackground(BACKGROUND_COLOR);
			}
		});

		eastContainer.add(progressLabel);
		eastContainer.add(editButton);
		eastContainer.add(deleteButton);

		boolean editingARoute = plugin.getTaskService().activeRouteInEditMode();
		progressLabel.setVisible(!editingARoute);
		deleteButton.setVisible(editingARoute);

		container.add(titleLabel, BorderLayout.CENTER);
		container.add(eastContainer, BorderLayout.EAST);

		container.setComponentPopupMenu(createPopupMenu());

		add(container, BorderLayout.CENTER);

		// forward mouse drag events to parent panel for drag and drop reordering
		ConditionalMouseDragEventForwarder mouseDragEventForwarder = new ConditionalMouseDragEventForwarder(listPanel, () -> plugin.getTaskService().activeRouteInEditMode());
		container.addMouseListener(mouseDragEventForwarder);
		container.addMouseMotionListener(mouseDragEventForwarder);

		// Click to toggle collapse
		container.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					toggleCollapse();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				container.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				container.setBackground(BACKGROUND_COLOR);
			}
		});
	}


	public JPopupMenu createPopupMenu()
	{
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem collapseOthersItem = new JMenuItem("Collapse All Except");
		collapseOthersItem.addActionListener(e -> {
			if (collapseCallback != null)
			{
				collapseOthersCallback.accept(sectionId);
			}
		});
		popupMenu.add(collapseOthersItem);

		JMenuItem routeEditHeader = new JMenuItem("Edit route");
		routeEditHeader.setEnabled(false);
		popupMenu.add(routeEditHeader);

		JMenuItem editNameItem = new JMenuItem("Edit Name");
		editNameItem.addActionListener(e -> editSection("Name", sectionName, false,
			(section, name) ->
			{
				sectionName = name;
				section.setName(name);
			}));
		popupMenu.add(editNameItem);

		JMenuItem editDescriptionItem = new JMenuItem("Edit Description");
		editDescriptionItem.addActionListener(e -> editSection("Description", description, true,
			(section, desc) ->
			{
				description = desc;
				section.setDescription(desc);
			}));
		popupMenu.add(editDescriptionItem);

		JMenuItem removeTaskFromRoute = new JMenuItem("Remove");
		removeTaskFromRoute.addActionListener(e -> {
			RouteEditActions.removeSectionAction(plugin, plugin.getTaskService().getActiveRoute(), sectionId).actionPerformed(e);
		});
		popupMenu.add(removeTaskFromRoute);

		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent)
			{
				if (plugin.getTaskService().activeRouteInEditMode())
				{
					routeEditHeader.setVisible(true);
					editNameItem.setVisible(true);
					editDescriptionItem.setVisible(true);
					removeTaskFromRoute.setVisible(true);
				}
				else
				{
					routeEditHeader.setVisible(false);
					editNameItem.setVisible(false);
					editDescriptionItem.setVisible(false);
					removeTaskFromRoute.setVisible(false);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent)
			{
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent popupMenuEvent)
			{
			}
		});

		return popupMenu;
	}

	public void setProgress(int completed, int total)
	{
		progressLabel.setText(completed + "/" + total);

		if (completed >= total && total > 0)
		{
			progressLabel.setForeground(PROGRESS_COMPLETE_COLOR);
		}
		else
		{
			progressLabel.setForeground(PROGRESS_COLOR);
		}
	}

	private void toggleCollapse()
	{
		setCollapsed(!collapsed);
	}

	private void updateTitleText()
	{
		String arrow = collapsed ? ARROW_COLLAPSED : ARROW_EXPANDED;
		StringBuilder html = new StringBuilder();
		html.append("<html>").append(arrow).append(" ").append(sectionName);

		if (description != null && !description.isEmpty())
		{
			html.append(" <span style='color: rgb(120,120,120); font-style: italic;'>- ").append(description)
				.append("</span>");
		}

		html.append("</html>");
		titleLabel.setText(html.toString());
	}

	public void setCollapsed(boolean collapsed)
	{
		this.collapsed = collapsed;
		updateTitleText();
		if (collapseCallback != null)
		{
			collapseCallback.accept(collapsed);
		}
	}

	/**
	 * Sets collapse state without triggering callback.
	 */
	public void setCollapsedSilent(boolean collapsed)
	{
		this.collapsed = collapsed;
		updateTitleText();
	}

	private void editSection(String property, String value, boolean allowEmpty, BiConsumer<RouteSection, String> action)
	{
		JOptionPane optionPane = new JOptionPane(property + ":", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optionPane.setInitialSelectionValue(value);
		optionPane.setWantsInput(true);
		JDialog inputDialog = optionPane.createDialog(this, "Edit Section " + property);
		inputDialog.setAlwaysOnTop(true);
		inputDialog.setVisible(true);
		Object inputValue = optionPane.getInputValue();
		if (inputValue != JOptionPane.UNINITIALIZED_VALUE)
		{
			String inputString = inputValue.toString();
			CustomRoute activeRoute = plugin.getTaskService().getActiveRoute();
			RouteSection section = activeRoute.get(sectionId);
			if (section != null
				&& (allowEmpty || !inputString.isEmpty()))
			{
				log.info("Setting section {} - {} to {}", sectionName, property, inputString);
				action.accept(section, inputString);
				updateTitleText();
				plugin.getTrackerGlobalConfigStore().addRoute(plugin.getTaskService().getCurrentTaskType().getTaskJsonName(), activeRoute);
				plugin.refreshAllPanels();
			}
		}
	}

	public void refresh()
	{
		boolean editingActiveRoute = plugin.getTaskService().activeRouteInEditMode();
		progressLabel.setVisible(!editingActiveRoute);
		deleteButton.setVisible(editingActiveRoute);
		editButton.setVisible(editingActiveRoute);
	}
}

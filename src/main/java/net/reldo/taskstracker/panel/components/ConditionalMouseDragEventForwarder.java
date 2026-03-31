package net.reldo.taskstracker.panel.components;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;

public class ConditionalMouseDragEventForwarder extends MouseAdapter
{
	private final Component target;
	private final BooleanSupplier condition;

	public ConditionalMouseDragEventForwarder(Component target, BooleanSupplier condition)
	{
		this.target = target;
		this.condition = condition;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		processEvent(e);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		processEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		processEvent(e);
	}

	private void processEvent(MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e) && condition.getAsBoolean())
		{
			MouseEvent eventForTarget = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, target);
			target.dispatchEvent(eventForTarget);
		}
	}
}

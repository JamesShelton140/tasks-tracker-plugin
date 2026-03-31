package net.reldo.taskstracker.panel.components;

import javax.swing.JPanel;

public abstract class DraggablePanel extends JPanel
{
	public abstract void dragFinished(int endingPosition);
}

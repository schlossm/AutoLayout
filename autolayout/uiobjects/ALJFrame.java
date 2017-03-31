package uikit.autolayout.uiobjects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Created by michaelschloss on 2/1/17.
 *
 * Custom subclass of JFrame.  Subclass ALJFrame to get automatic AutoLayout inheritance.
 */
public class ALJFrame extends JFrame implements ComponentListener
{
	public ALJFrame(String title)
	{
		super(title);
		addComponentListener(this);
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		if (!isVisible()) return;
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
		if (!isVisible()) return;
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) { }

	private void process()
	{
		for (Component component : getContentPane().getComponents())
		{
			component.setBounds(0, component.getBounds().y, getContentPane().getWidth(), getContentPane().getHeight() - component.getBounds().y);
			if (component instanceof ALJPanel)
			{
				((ALJPanel)component).layoutSubviews();
			}
			else if (component instanceof JPanel)
			{
				((ComponentListener)component).componentResized(null);
			}
		}
	}
}
